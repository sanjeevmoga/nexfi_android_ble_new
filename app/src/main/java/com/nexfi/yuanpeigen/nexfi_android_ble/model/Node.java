package com.nexfi.yuanpeigen.nexfi_android_ble.model;

import android.app.Activity;
import android.os.Environment;
import android.util.Base64;

import com.google.gson.Gson;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.BaseMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.GroupChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageBodyType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.SingleChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.ReceiveGroupMsgListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.ReceiveTextMsgListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.operation.TextMsgOperation;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.TimeUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import org.json.JSONObject;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Random;

import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;
import io.underdark.util.nslogger.NSLogger;
import io.underdark.util.nslogger.NSLoggerAdapter;

public class Node implements TransportListener {
    private boolean running;
    private Activity activity;
    private long nodeId;
    private Transport transport;

    private ArrayList<Link> links = new ArrayList<>();
    private int framesCount = 0;
    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    TextMsgOperation textMsgOperation = new TextMsgOperation();
    ReceiveTextMsgListener mReceiveTextMsgListener = null;
    ReceiveGroupMsgListener mReceiveGroupMsgListener=null;
    private Gson gson;

    public void setReceiveTextMsgListener(ReceiveTextMsgListener receiveTextMsgListener) {
        this.mReceiveTextMsgListener = receiveTextMsgListener;
    }

    public void setReceiveGroupMsgListener(ReceiveGroupMsgListener receiveGroupMsgListener) {
        this.mReceiveGroupMsgListener = receiveGroupMsgListener;
    }

    private String userSelfId;

    public Node(Activity activity) {
        this.activity = activity;

        do {
            nodeId = new Random().nextLong();
        } while (nodeId == 0);

        if (nodeId < 0)
            nodeId = -nodeId;

        configureLogging();

        EnumSet<TransportKind> kinds = EnumSet.of(TransportKind.BLUETOOTH, TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.BLUETOOTH);

        this.transport = Underdark.configureTransport(
                234235,
                nodeId,
                this,
                null,
                activity.getApplicationContext(),
                kinds
        );
    }

    private void configureLogging() {
        NSLoggerAdapter adapter = (NSLoggerAdapter)
                StaticLoggerBinder.getSingleton().getLoggerFactory().getLogger(Node.class.getName());
        adapter.logger = new NSLogger(activity.getApplicationContext());
        adapter.logger.connect("192.168.5.203", 50000);

        Underdark.configureLogging(true);
    }

    public void start() {
        Debug.debugLog("TAG","-1--Node------------------------------------start------");
        if (running)
            return;

        running = true;
        transport.start();
    }

    public void stop() {
        if (!running)
            return;

        running = false;
        transport.stop();
    }

    public ArrayList<Link> getLinks() {
        return links;
    }


    //发送数据
    public void broadcastFrame(byte[] frameData) {
        if (links.size() == 0) {
            return;
        }
        for (Link link : links) {
            link.sendFrame(frameData);

        }
    }

    //region TransportListener
    @Override
    public void transportNeedsActivity(Transport transport, ActivityCallback callback) {
        callback.accept(activity);
    }

    //连接
    @Override
    public void transportLinkConnected(Transport transport, Link link) {
        links.add(link);
        Debug.debugLog("TAG", links.size() + "------连接数");
        userSelfId = UserInfo.initUserId(userSelfId, BleApplication.getContext());
        if (null != links && links.size() > 0) {//搜索到设备后就发送请求，并把自己的信息携带过去
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.messageType = MessageType.eMessageType_requestUserInfo;
            baseMessage.timeStamp = TimeUtils.getNowTime();
            UserMessage userMessage = bleDBDao.findUserByUserId(userSelfId);
            if(null!=userMessage) {
                userMessage.nodeId = link.getNodeId()+"";
                baseMessage.userMessage = userMessage;
                bleDBDao.updateUserInfoByUserId(userMessage, userSelfId);
                gson = new Gson();
                String json= gson.toJson(baseMessage);
                byte[] data=json.getBytes();
                broadcastFrame(data);
            }
        }
    }

    //断开连接
    @Override
    public void transportLinkDisconnected(Transport transport, Link link) {
        Debug.debugLog("TAG", "----发送离线消息--------------" + links.size());
        bleDBDao.deleteUserByNodeId(link.getNodeId()+"",userSelfId);
        UserMessage uuu=bleDBDao.findUserByUserId(userSelfId);
        links.remove(link);//移除link
    }

    //接收数据，自动调用
    @Override
    public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData) {
        //接收到数据后将用户数据发送给对方
        String json = new String(frameData);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (Exception e) {//
            e.printStackTrace();
        }
        if (jsonObject == null) {
            return;
        }
        int messageType = (int) jsonObject.opt("messageType");

        if (MessageType.eMessageType_requestUserInfo == messageType) {
            BaseMessage baseMessage = gson.fromJson(json, BaseMessage.class);
            if(baseMessage==null){
                return;
            }
            //对方发过来的请求消息中包含有对方的信息,此时可以将对方的数据保存到本地数据库
            UserMessage userMsg = baseMessage.userMessage;
            userMsg.nodeId = link.getNodeId() + "";
            baseMessage.userMessage = userMsg;
            if (!bleDBDao.findSameUserByUserId(userMsg.userId)) {
                bleDBDao.add(baseMessage);
            }
            //收到请求之后，将自己的信息封装发给对方
            BaseMessage baseMsg = new BaseMessage();
            baseMsg.messageType = MessageType.eMessageType_SendUserInfo;//反馈消息
            baseMsg.timeStamp = TimeUtils.getNowTime();
            UserMessage userMg = bleDBDao.findUserByUserId(userSelfId);
            baseMsg.userMessage = userMg;
            String responseJson = gson.toJson(baseMsg);
            byte[] dataM = responseJson.getBytes();
            link.sendFrame(dataM);
        } else if (MessageType.eMessageType_SendUserInfo == messageType) {
            BaseMessage baseMessage = gson.fromJson(json, BaseMessage.class);
            //接收对方反馈的用户信息
            UserMessage userMsg = baseMessage.userMessage;
            userMsg.nodeId = link.getNodeId() + "";//这是很重要的一步，将所连接的link跟连接的用户绑定，这样通过nodeId就可以找到对应的link,这样就可以给指定的人发消息了
            baseMessage.userMessage = userMsg;
            //然后将接收到的用户信息保存到数据库
            if (userMsg.userNick != null) {
                if (!bleDBDao.findSameUserByUserId(userMsg.userId)) {
                    bleDBDao.add(baseMessage);//华为-保存到数据库---2130903076====d14143dd-22dd-4c86-a9f8-a3698330ce6d
                }
            }
        }
        else if(MessageType.eMessageType_UpdateUserInfo==messageType){//用户信息修改请求
            BaseMessage baseMessage = gson.fromJson(json, BaseMessage.class);
            //接收到用户信息修改消息后，根据userId将对应的用户信息修改
            UserMessage userMessage=baseMessage.userMessage;
            userMessage.nodeId=link.getNodeId()+"";
            Debug.debugLog("TAG", userMessage.nodeId + "----接收修改后的NOdeId---" + link.getNodeId());
            bleDBDao.updateUserInfoByUserId(userMessage,userMessage.userId);//根据userId更新数据库中对应的用户信息
            bleDBDao.updateP2PMsgByUserId(userMessage,userMessage.userId);//单聊数据
            bleDBDao.updateGroupMsgByUserId(userMessage,userMessage.userId);//群聊数据
        }
        else if (MessageType.eMessageType_SingleChat == messageType) {//单聊
            SingleChatMessage singleChatMessage = gson.fromJson(json, SingleChatMessage.class);
            //如果是语音消息，需要创建临时文件，因为播放语音需要路径
            if(singleChatMessage.messageBodyType== MessageBodyType.eMessageBodyType_Voice){
                Debug.debugLog("receivevoice","-----======收到语音------------------");
                String fileData=singleChatMessage.voiceMessage.fileData;
                byte[] by_receive_data= Base64.decode(fileData, Base64.DEFAULT);
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String videoFileName = "VIDEO_"+ timeStamp + "_";
                File fileDir = null;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    //存在sd卡
                    fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NexFi_ble/image");
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();
                    }
                }
                String rece_file_path = fileDir + "/" + videoFileName;
                File fileRece=FileTransferUtils.getFileFromBytes(by_receive_data, rece_file_path);
                singleChatMessage.voiceMessage.filePath=fileRece.getAbsolutePath();
                Debug.debugLog("receivevoice","----收到语音路径------------------"+fileRece.getAbsolutePath());
            }
            singleChatMessage.receiver = singleChatMessage.userMessage.userId;
//            bleDBDao.addP2PTextMsg(singleChatMessage);//geng
            if (null != mReceiveTextMsgListener) {
                mReceiveTextMsgListener.onReceiveTextMsg(singleChatMessage);
            }
        } else if (MessageType.eMessageType_AllUserChat == messageType) {
            GroupChatMessage groupChatMessage = gson.fromJson(json, GroupChatMessage.class);
            if (!(bleDBDao.findSameGroupByUuid(groupChatMessage.msgId))) {
                //如果数据库没有此msgId，则将此条消息转发,并显示
                UserMessage userMessage=groupChatMessage.userMessage;
                userMessage.nodeId=link.getNodeId()+"";
                bleDBDao.addGroupTextMsg2(groupChatMessage);
                UserMessage user = bleDBDao.findUserByUserId(userSelfId);
                if (null != mReceiveGroupMsgListener) {
                    mReceiveGroupMsgListener.onReceiveGroupMsg(groupChatMessage);
                }
            }
        }
    }


    /**
     * 根据nodeId获取link
     *
     * @param nodeId
     * @return
     */
    public Link getLink(String nodeId) {
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            if (nodeId.equals(link.getNodeId()+"")) {
                return link;
            }
        }
        return null;
    }
} // Node
