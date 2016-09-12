/*
 * Copyright (c) 2016 Vladimir L. Shabanov <virlof@gmail.com>
 *
 * Licensed under the Underdark License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://underdark.io/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package impl.underdark.transport.nsd;

import android.util.Log;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import impl.underdark.logging.Logger;
import impl.underdark.protobuf.Frames;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.underdark.Config;
import io.underdark.transport.Link;
import io.underdark.util.dispatch.SerialExecutorService;

public class NsdLink implements Link
{
	public enum State
	{
		CONNECTING,
		CONNECTED,
		DISCONNECTED
	}

	private boolean client;
	private NsdServer server;
	private Socket socket;
	private long nodeId;

	private InetAddress host;
	private int port;

	// Changed only from input thread.
	private State state = State.CONNECTING;

	private InputStream inputStream;
	private volatile OutputStream outputStream;

	private ScheduledThreadPoolExecutor pool;
	private ExecutorService outputExecutor;
	private Queue<Frames.Frame> outputQueue = new LinkedList<>();//Queue就是一个集合

	private boolean shouldCloseWhenOutputIsEmpty = false;

	NsdLink(NsdServer server, Socket socket)
	{
		// Any thread
		super();
		this.client = false;
		this.server = server;
		this.socket = socket;
		this.host = socket.getInetAddress();
		this.port = socket.getPort();
		configureOutput();
	}

	NsdLink(NsdServer server, long nodeId, InetAddress host, int port)
	{
		// Any thread
		super();
		this.client = true;
		this.server = server;
		this.nodeId = nodeId;
		this.host = host;
		this.port = port;
		configureOutput();
	}

	private void configureOutput()
	{
		pool = new ScheduledThreadPoolExecutor(1, new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread thread = new Thread(r);
				thread.setName("NsdLink " + this.hashCode() + " Output");
				thread.setDaemon(true);
				return thread;
			}
		});

		outputExecutor = new SerialExecutorService(pool);
	}

	@Override
	public String toString() {
		return (client ? "c" : "s")
				+ "link"
				+ " nodeId " + nodeId
				+ " " + host.toString()
				+ ":" + port;
	}

	void connect()
	{
		// Queue
		Thread inputThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				connectImpl();
			}
		});
		inputThread.setName("NsdLink " + this.hashCode() + " Input");
		inputThread.setDaemon(true);
		inputThread.start();
	} // connect

	//region Link
	@Override
	public long getNodeId()
	{
		return nodeId;
	}

	@Override
	public int getPriority()
	{
		return 10;
	}

	@Override
	public void disconnect()
	{
		outputExecutor.execute(new Runnable() {
			@Override
			public void run() {
				shouldCloseWhenOutputIsEmpty = true;
				writeNextFrame();
			}
		});
	}

	@Override
	public void sendFrame(final byte[] frameData)
	{
		// Listener thread.
		if(state != State.CONNECTED)
			return;

		Frames.Frame.Builder builder = Frames.Frame.newBuilder();
		builder.setKind(Frames.Frame.Kind.PAYLOAD);

		Frames.PayloadFrame.Builder payload = Frames.PayloadFrame.newBuilder();
		payload.setPayload(ByteString.copyFrom(frameData));
		builder.setPayload(payload);

		final Frames.Frame frame = builder.build();

		sendLinkFrame(frame);
	}

	void sendLinkFrame(final Frames.Frame frame)
	{
		// Listener thread.
		if(state != State.CONNECTED)
			return;

		enqueueFrame(frame);
	}
	//endregion

	private void notifyDisconnect()
	{
		// Input thread
		try
		{
			if(socket != null)
				socket.close();
		}
		catch (IOException e)
		{
		}

		final boolean wasConnected = (this.state == State.CONNECTED);
		this.state = State.DISCONNECTED;

		//outputExecutor.close();
		outputExecutor.shutdown();

		pool.shutdown();

		server.queue.execute(new Runnable()
		{
			@Override
			public void run()
			{
				server.linkDisconnected(NsdLink.this, wasConnected);
			}
		});
	}

	private void enqueueFrame(final Frames.Frame frame)
	{
		// Any thread.
		outputExecutor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				outputQueue.add(frame);
				writeNextFrame();
			}
		});
	}

	private void writeNextFrame()
	{
		// Output thread.
		if (state == State.DISCONNECTED)
		{
			outputQueue.clear();
			return;
		}

		byte[] frameBytes;

		{
			Frames.Frame frame = outputQueue.poll();
			if (frame == null)
			{
				if (shouldCloseWhenOutputIsEmpty)
				{
					try
					{
						//TODO
						outputStream.close();
						socket.close();
					}
					catch (IOException e)
					{
					}
				}

				return;
			}

			frameBytes = frame.toByteArray();
		}

		if(!writeFrameBytes(frameBytes))
		{
			outputQueue.clear();
			return;
		}

		outputExecutor.execute(new Runnable()
		{
			@Override
			public void run()
			{
				writeNextFrame();
			}
		});
	}

	private boolean writeFrameBytes(byte[] frameBytes)
	{
		// Output thread.
		ByteBuffer header = ByteBuffer.allocate(4);//这个数字4不能随意改动
		header.order(ByteOrder.BIG_ENDIAN);
		header.putInt(frameBytes.length);
		try
		{
			outputStream.write(header.array());
			outputStream.write(frameBytes);//java.net.SocketException: sendto failed: EPIPE (Broken pipe)
			outputStream.flush();
		}
		catch (IOException ex)
		{
			//TODO
			try
			{
				outputStream.close();
				socket.close();
			}
			catch (IOException e)
			{
			}

			return false;
		}

		return true;
	} // writeFrame

	private void connectImpl()
	{
		// Input thread.
		if (client)
		{
			try
			{
				this.socket = new Socket(host, port);
//				byte[] hostBys = BnjUtil.ipTo4Byte("192.168.1.172");
//				for (int i = 0; i < hostBys.length; i++) {
//					Log.e("NsdLink   ",hostBys[i]+"  --------hostBys----------  ");
//				}
//				byte[] portBys = BnjUtil.HexString2Bytes(Integer.toHexString(12345));
//				if(hostBys == null | portBys == null){
//					return;
//				}
//				Log.e("NsdLink   ",portBys[0]+"  --------portBys----------  "+portBys[1]);

				//TODO proxy
//				this.socket = new Socket("127.0.0.1", 9999);//跟代理连接
//				Log.e("create", "socket create successfully");
//
//				byte[] proxyDatas = {0x05, 0x01, 0x00};//发送的握手字节序列
//				OutputStream proxyOS = this.socket.getOutputStream();
//				proxyOS.write(proxyDatas);
//				Log.e("write", "send 1 successfully");
//
//				byte[] proxyReceive = new byte[2]; //服务端返回的字节
//				InputStream proxyIS = this.socket.getInputStream();
//				int proxyCount = proxyIS.read(proxyReceive);
//				if (proxyCount < 0) {
//					return;
//				}
//				Log.e("create ", "The server response !");
//
//				if (proxyReceive[0] == 0x05 && proxyReceive[1] == 0x00) {
//					Log.e("connect", "Connected to socks5 proxy server");
//				}
//				//192.168.1.170		C0A801AA	-64,-88,1,-86
//				//192.168.1.171		C0A801AB
//				//192.168.1.172		C0A801AC	-64,-88,1,-84
//
//				//12345     3039	48,57
//				//23456     5BA0	91,-96
//
//				//192.168.10.160	C0A80AA0
//				byte[] destDatas = {0x05, 0x01, 0x00, 0x01, -64,-88,1,-86, (byte)0x30, (byte)0x39};//192.168.1.172	 23456
//				proxyOS.write(destDatas);
//				Log.e("NsdLink   ","  proxyOS.write(destDatas);-------------- ");
//				byte[] ipPort = new byte[16];
//				int destLen = proxyIS.read(ipPort);
//
//				Log.e("NsdLink   ",new String(ipPort,0,destLen)+"  proxyOS.write ------destLen-------- "+destLen);
//				for (int i = 0; i <destLen ; i++) {
//					Log.e("NsdLink   ","  proxyOS.write ------destLen-字节---- "+ipPort[i]);
//				}
//				if (destLen < 0) {
//					return;
//				}
				//proxy end

			}
			catch (IOException ex)
			{
				notifyDisconnect();
				return;
			}
		}

		try
		{
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
			socket.setSoTimeout(Config.bnjTimeoutInterval);
		}
		catch (SocketException ex)
		{
			ex.printStackTrace();
		}

		if (!connectStreams())//在这里获得了InputStream 和 OutputStream
		{
			notifyDisconnect();
			return;
		}

		sendHelloFrame();

		pool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (state != State.CONNECTED)
					return;

				sendHeartbeat();
			}
		}, 0, Config.bnjHeartbeatInterval, TimeUnit.MILLISECONDS);

		inputLoop();//已改动

	} // connectImpl

	private boolean connectStreams()
	{
		// Input thread.
		try
		{
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		}
		catch (IOException ex)
		{
			Logger.error("nsd link streams get failed {}", ex);
			return false;
		}

		return true;
	}

	private void sendHelloFrame()
	{
		// Input thread.
		Frames.Frame.Builder builder = Frames.Frame.newBuilder();
		builder.setKind(Frames.Frame.Kind.HELLO);

		Frames.HelloFrame.Builder payload = Frames.HelloFrame.newBuilder();
		payload.setNodeId(server.getNodeId());
		payload.setPeer(
				Frames.Peer.newBuilder()
						.setAddress(ByteString.copyFrom(new byte[0]))
						.setLegacy(false)
						.addAllPorts(new ArrayList<Integer>())
		);

		builder.setHello(payload);

		final Frames.Frame frame = builder.build();
		enqueueFrame(frame);
	} // sendHelloFrame

	private void sendHeartbeat()
	{
		// Any thread
		Frames.Frame.Builder builder = Frames.Frame.newBuilder();
		builder.setKind(Frames.Frame.Kind.HEARTBEAT);

		Frames.HeartbeatFrame.Builder payload = Frames.HeartbeatFrame.newBuilder();

		builder.setHeartbeat(payload);

		final Frames.Frame frame = builder.build();
		enqueueFrame(frame);
	} // sendHeartbeat

	private void inputLoop()
	{
		// Input thread.
		final int bufferSize = 4096;
		ByteBuf inputData = Unpooled.buffer(bufferSize);
		inputData.order(ByteOrder.BIG_ENDIAN);
		try
		{
			int len;
			while (true)
			{
				inputData.ensureWritable(bufferSize, true);
				len = inputStream.read(
						inputData.array(),
						inputData.writerIndex(),
						bufferSize);//java.net.SocketException: recvfrom failed: EBADF (Bad file descriptor)
				if(len <= 0)
					break;

				inputData.writerIndex(inputData.writerIndex() + len);
				if(!formFrames(inputData))
					break;

				inputData.discardReadBytes();
				inputData.capacity(inputData.writerIndex() + bufferSize);
			} // while
		}
		catch (InterruptedIOException ex)
		{
			try
			{
				inputStream.close();
			}
			catch (IOException ioex)
			{Log.e("NsdLink   ", "  inputLoop()#-------------#IOException# "+(ioex.toString()));
			}

			notifyDisconnect();
			return;
		}
		catch (Exception ex)
		{
			try
			{
				inputStream.close();
			}
			catch (IOException ioex)
			{
			}

			notifyDisconnect();
			return;
		}

		Logger.debug("nsd input read end");
		notifyDisconnect();
	} // inputLoop

	private boolean formFrames(ByteBuf inputData)
	{
		final int headerSize = 4;

		while(true)
		{
			if(inputData.readableBytes() < headerSize)
				break;

			inputData.markReaderIndex();
			int	frameSize = inputData.readInt();
			if(frameSize > Config.frameSizeMax)
			{
				return false;
			}

			if( inputData.readableBytes() < frameSize )
			{
				inputData.resetReaderIndex();
				break;
			}

			final Frames.Frame frame;

			{
				final byte[] frameBody = new byte[frameSize];
				inputData.readBytes(frameBody, 0, frameSize);

				try
				{
					frame = Frames.Frame.parseFrom(frameBody);
				}
				catch (Exception ex)
				{
					continue;
				}
			}
			if(this.state == State.CONNECTING)
			{
				if(frame.getKind() != Frames.Frame.Kind.HELLO)
					continue;

				this.nodeId = frame.getHello().getNodeId();
				this.state = State.CONNECTED;

				Logger.debug("nsd connected {}", NsdLink.this.toString());

				server.queue.execute(new Runnable()
				{
					@Override
					public void run()
					{
						server.linkConnected(NsdLink.this);
					}
				});

				continue;
			}

			if(frame.getKind() == Frames.Frame.Kind.PAYLOAD)
			{
				if(!frame.hasPayload() || !frame.getPayload().hasPayload())
					continue;

				final byte[] frameData = frame.getPayload().getPayload().toByteArray();
				if(frameData.length == 0)
					continue;

				server.queue.execute(new Runnable()
				{
					@Override
					public void run()
					{
						server.linkDidReceiveFrame(NsdLink.this, frameData);
					}
				});

				continue;
			}

			if(frame.getKind() == Frames.Frame.Kind.HEARTBEAT)
			{
				continue;
			}

		} // while

		return true;
	} // formFrames


} // NsdLink
