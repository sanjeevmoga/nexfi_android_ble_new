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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class NsdServer
{
	public interface Listener
	{
		void onServerAccepting(InetAddress address, int port);
		void onServerError(InetAddress address);

		void linkConnected(NsdLink link);
		void linkDisconnected(NsdLink link);
		void linkDidReceiveFrame(NsdLink link, byte[] frameData);
	}

	private volatile boolean accepting;

	private long nodeId;
	private Listener listener;
	Executor queue;

	private InetAddress serverAddress;
	ServerSocket serverSocket;
	//TODO
	int port;

	private List<NsdLink> linksConnecting = new ArrayList<>();

	public NsdServer(long nodeId, Listener listener, Executor queue)
	{
		this.nodeId = nodeId;
		this.listener = listener;
		this.queue = queue;
	}

	public long getNodeId()
	{
		return nodeId;
	}

	public void startAccepting(final InetAddress address)
	{
		if(accepting)
			return;

		synchronized (this)
		{
			accepting = true;
		}

		serverAddress = address;
		//TODO
//		port = 22345;//192.168.1.172服务端监听的端口是：23456
		//192.168.1.170服务端监听的端口是：12345
		try
		{
			serverSocket = new ServerSocket(0, 50, address);
//			serverSocket = new ServerSocket(port);
		}
		catch (IOException ex)
		{

			queue.execute(new Runnable()
			{
				@Override
				public void run()
				{
					listener.onServerError(serverAddress);
				}
			});

			return;
		}

		serverAddress = ((InetSocketAddress) serverSocket.getLocalSocketAddress()).getAddress();
		final InetAddress localAddress = serverAddress;
		final int port = serverSocket.getLocalPort();//得到随机端口(构造中的port为0)
		queue.execute(new Runnable()
		{
			@Override
			public void run()
			{
				listener.onServerAccepting(localAddress, port);
			}
		});

		Thread acceptThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				accept();
			}
		});
		acceptThread.setName("NsdServer Accept");
		acceptThread.setDaemon(true);
		acceptThread.start();
	} // startAccepting

	public void stopAccepting()
	{
		if(!accepting)
			return;

		synchronized (this)
		{
			accepting = false;
		}

		try
		{
			if(serverSocket!=null){
				serverSocket.close();
			}
		}
		catch (IOException ex)
		{
		}
	} // stopAccepting

	private void accept()
	{
		// Accept thread.
		while(true)
		{
			synchronized (this)
			{
				if(!accepting)
					break;
			}

			Socket socket;

			try
			{
				socket = serverSocket.accept();
			} catch (IOException ex)
			{
				continue;
			}
			final NsdLink link = new NsdLink(this, socket);
//			Log.e("NsdServer","  sssssssss  "+socket.getRemoteSocketAddress()+" Server link: "+link);
			queue.execute(new Runnable()
			{
				@Override
				public void run()
				{
					linkConnecting(link);
					link.connect();
				}
			});
		} // while
	} // accept

	public void connect(long nodeId, InetAddress address, int port)
	{
		NsdLink link = new NsdLink(this, nodeId, address, port);
		linkConnecting(link);
		link.connect();
	}

	//region Links
	private void linkConnecting(NsdLink link)
	{
		// Queue.
		linksConnecting.add(link);
	}

	void linkConnected(NsdLink link)
	{
		// Queue.
		linksConnecting.remove(link);
		listener.linkConnected(link);
	}

	void linkDisconnected(NsdLink link, boolean wasConnected)
	{
		// Queue.
		linksConnecting.remove(link);

		if(wasConnected)
		{
			listener.linkDisconnected(link);
		}
	}

	void linkDidReceiveFrame(NsdLink link, byte[] frameData)
	{
		// Queue.
		listener.linkDidReceiveFrame(link, frameData);
	}
	//endregion

} // NsdServer
