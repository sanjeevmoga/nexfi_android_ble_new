package com.nexfi.yuanpeigen.nexfi_android_ble.model;

import android.app.Activity;
import android.os.Environment;
import android.util.Base64;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationConfiguration;
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
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.ReceiveUserOfflineListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.TimeUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import org.json.JSONObject;

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

public class Node implements TransportListener {
    private boolean running;
    private Activity activity;
    private long nodeId;
    private Transport transport;

    private ArrayList<Link> links = new ArrayList<>();
    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    ReceiveTextMsgListener mReceiveTextMsgListener = null;
    ReceiveGroupMsgListener mReceiveGroupMsgListener = null;
    ReceiveUserOfflineListener mReceiveUserOfflineListener = null;
    private Gson gson;

    public void setReceiveTextMsgListener(ReceiveTextMsgListener receiveTextMsgListener) {
        this.mReceiveTextMsgListener = receiveTextMsgListener;
    }

    public void setReceiveGroupMsgListener(ReceiveGroupMsgListener receiveGroupMsgListener) {
        this.mReceiveGroupMsgListener = receiveGroupMsgListener;
    }

    public void setReceiveUserOfflineListener(ReceiveUserOfflineListener receiveUserOfflineListener) {
        this.mReceiveUserOfflineListener = receiveUserOfflineListener;
    }

    private String userSelfId;


    UserMessage userSelf;

    /**
     * 定位
     *
     */

    private double longitude = 0;
    private double latitude = 0;


    //定位
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private boolean isFirstIn = true;//用户是否是第一次进入
    private MyLocationConfiguration.LocationMode mLocationMode;//定位模式

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            latitude= bdLocation.getLatitude();
            longitude=bdLocation.getLongitude();
            String address = bdLocation.getAddrStr();
//            Log.e("定位结果 ", " 经度:" + longitude + " 纬度:" + latitude + " 地址:" + address);//经度:121.605259 纬度:31.21449 地址:中国上海市浦东新区郭守敬路498号-14号楼-1楼
        }
    }



//    //声明AMapLocationClient类对象
//    public AMapLocationClient mLocationClient = null;
//
//    //声明AMapLocationClientOption对象
//    public AMapLocationClientOption mLocationOption = null;
//
//    //声明定位回调监听器
//    public AMapLocationListener mLocationListener = new AMapLocationListener(){
//
//        @Override
//        public void onLocationChanged(AMapLocation amapLocation) {
//            if (amapLocation != null) {
//                if (amapLocation.getErrorCode() == 0) {
//                    //可在其中解析amapLocation获取相应内容。
//                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//                    latitude = amapLocation.getLatitude();
//                    longitude = amapLocation.getLongitude();
//                    amapLocation.getAccuracy();//获取精度信息
//                    String address = amapLocation.getAddress();
//                    Log.e("定位结果 ", " 经度:" + longitude + " 纬度:" + latitude + " 地址:" + address);
//                }else {
//                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
//                    Log.e("AmapError", "location Error, ErrCode:"
//                            + amapLocation.getErrorCode() + ", errInfo:"
//                            + amapLocation.getErrorInfo());
//                }
//            }
//        }
//    };


    public Node(Activity activity) {
        this.activity = activity;

        do {
            nodeId = new Random().nextLong();
        } while (nodeId == 0);

        if (nodeId < 0)
            nodeId = -nodeId;

        configureLogging();

        EnumSet<TransportKind> kinds = EnumSet.of(TransportKind.BLUETOOTH, TransportKind.WIFI);

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

    }

    public void start() {
        Debug.debugLog("TAG", "-1--Node------start------");
        if (running)
            return;

        running = true;

        initLocation();
        transport.start();//已改动

    }


    //
    private void initLocation() {

//        //初始化定位
//        mLocationClient = new AMapLocationClient(this.activity);
//
//        //设置定位回调监听
//        mLocationClient.setLocationListener(mLocationListener);
//
//        //初始化AMapLocationClientOption对象
//        mLocationOption = new AMapLocationClientOption();
//
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
//
//        mLocationOption.setOnceLocation(true);
//
//        //设置是否返回地址信息（默认返回地址信息）
//        mLocationOption.setNeedAddress(true);
//
//        //给定位客户端对象设置定位参数
//        mLocationClient.setLocationOption(mLocationOption);
//        //启动定位
//        mLocationClient.startLocation();



        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;//默认是普通模式
        mLocationClient = new LocationClient(activity);

        myLocationListener = new MyLocationListener();

        mLocationClient.registerLocationListener(myLocationListener);

        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");//坐标类型,"bd09ll"能与百度地图很好的融合
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(0);//仅定位一次

        mLocationClient.setLocOption(option);

        //开启定位
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }

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
        userSelf = bleDBDao.findUserByUserId(userSelfId);
        if (null != links && links.size() > 0) {//搜索到设备后就发送请求，并把自己的信息携带过去
            BaseMessage baseMessage = new BaseMessage();
            baseMessage.messageType = MessageType.eMessageType_requestUserInfo;
            baseMessage.timeStamp = TimeUtils.getNowTime();
            UserMessage userMessage = bleDBDao.findUserByUserId(userSelfId);
            if (null != userMessage) {

                if(latitude!=0 && longitude!=0) {
                    //定位信息
                    userMessage.lattitude = latitude + "";
                    userMessage.longitude = longitude + "";
                }

                userMessage.nodeId = link.getNodeId() + "";
                baseMessage.userMessage = userMessage;
                bleDBDao.updateUserInfoByUserId(userMessage, userSelfId);
                gson = new Gson();
                String json = gson.toJson(baseMessage);
                byte[] data = json.getBytes();
                broadcastFrame(data);
            }
        }
    }

    //断开连接
    @Override
    public void transportLinkDisconnected(Transport transport, Link link) {
        Debug.debugLog("TAG", "----发送离线消息--------------" + links.size());

        bleDBDao.deleteUserByNodeId(link.getNodeId() + "", userSelfId);

        links.remove(link);//移除link

        if (null != mReceiveUserOfflineListener) {
            mReceiveUserOfflineListener.onReceiveUserMsg();
        }
    }

    //接收数据，自动调用
    @Override
    public void transportLinkDidReceiveFrame(Transport transport, Link link, byte[] frameData) {
        //接收到数据后将用户数据发送给对方
        String json = new String(frameData);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);//outOfMemoryError
        } catch (Exception e) {//
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            return;
        }
        if (jsonObject == null) {
            return;
        }
        int messageType = (int) jsonObject.opt("messageType");

        if (MessageType.eMessageType_requestUserInfo == messageType) {
            BaseMessage baseMessage = gson.fromJson(json, BaseMessage.class);
            if (baseMessage == null) {
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

            if(null != userMg && latitude != 0 && longitude != 0){
                userMg.lattitude = latitude + "";
                userMg.longitude = longitude + "";//自己的位置信息
            }

            baseMsg.userMessage = userMg;
            String responseJson = gson.toJson(baseMsg);
            byte[] dataM = responseJson.getBytes();
            link.sendFrame(dataM);

        } else if (MessageType.eMessageType_SendUserInfo == messageType) {

            BaseMessage baseMessage = gson.fromJson(json, BaseMessage.class);

            //接收对方反馈的用户信息
            UserMessage userMsg = baseMessage.userMessage;
            userMsg.nodeId = link.getNodeId() + "";
            baseMessage.userMessage = userMsg;
            //然后将接收到的用户信息保存到数据库
            if (userMsg.userNick != null) {
                if (!bleDBDao.findSameUserByUserId(userMsg.userId)) {
                    bleDBDao.add(baseMessage);
                }
            }

        } else if (MessageType.eMessageType_UpdateUserInfo == messageType) {//用户信息修改请求

            BaseMessage baseMessage = gson.fromJson(json, BaseMessage.class);

            //接收到用户信息修改消息后，根据userId将对应的用户信息修改
            UserMessage userMessage = baseMessage.userMessage;
            userMessage.nodeId = link.getNodeId() + "";
            Debug.debugLog("TAG", userMessage.nodeId + "----接收修改后的NOdeId---" + link.getNodeId());
            bleDBDao.updateUserInfoByUserId(userMessage, userMessage.userId);//根据userId更新数据库中对应的用户信息
            bleDBDao.updateP2PMsgByUserId(userMessage, userMessage.userId);//单聊数据
            bleDBDao.updateGroupMsgByUserId(userMessage, userMessage.userId);//群聊数据

        } else if (MessageType.eMessageType_SingleChat == messageType) {//单聊

            SingleChatMessage singleChatMessage = gson.fromJson(json, SingleChatMessage.class);

            //如果是图片消息，需要创建文件
            if (singleChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Image) {
                String imageData = singleChatMessage.fileMessage.fileData;
                byte[] by_receive_data = Base64.decode(imageData, Base64.DEFAULT);
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String imageFileName = "IMAGE_" + timeStamp + ".jpg";
                File fileDir = null;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    //存在sd卡
                    fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NexFi_ble/image");
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();
                    }
                }
                String rece_file_path = fileDir + "/" + imageFileName;
                File fileRece = FileTransferUtils.getFileFromBytes(by_receive_data, rece_file_path);
                singleChatMessage.fileMessage.filePath = fileRece.getAbsolutePath();

            }//如果是语音消息，需要创建临时文件，因为播放语音需要路径
            else if (singleChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Voice) {
                String fileData = singleChatMessage.voiceMessage.fileData;
                byte[] by_receive_data = Base64.decode(fileData, Base64.DEFAULT);
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                String videoFileName = "VIDEO_" + timeStamp + ".mp3";
                File fileDir = null;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    //存在sd卡
                    fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NexFi_ble/voice");
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();
                    }
                }
                String rece_file_path = fileDir + "/" + videoFileName;
                File fileRece = FileTransferUtils.getFileFromBytes(by_receive_data, rece_file_path);
                singleChatMessage.voiceMessage.filePath = fileRece.getAbsolutePath();
            }
            singleChatMessage.receiver = singleChatMessage.userMessage.userId;
            bleDBDao.addP2PTextMsg(singleChatMessage);
            if (null != mReceiveTextMsgListener) {
                mReceiveTextMsgListener.onReceiveTextMsg(singleChatMessage);
            }

        } else if (MessageType.eMessageType_AllUserChat == messageType) {//群聊

            GroupChatMessage groupChatMessage = gson.fromJson(json, GroupChatMessage.class);

            if (!(bleDBDao.findSameGroupByUuid(groupChatMessage.msgId))) {
                if (groupChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Voice) {//群聊语音
                    String fileData = groupChatMessage.voiceMessage.fileData;
                    byte[] by_receive_data = Base64.decode(fileData, Base64.DEFAULT);
                    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    String videoFileName = "VIDEO_" + timeStamp + ".mp3";
                    File fileDir = null;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        //存在sd卡
                        fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NexFi_ble/voice");
                        if (!fileDir.exists()) {
                            fileDir.mkdirs();
                        }
                    }
                    String rece_file_path = fileDir + "/" + videoFileName;
                    File fileRece = FileTransferUtils.getFileFromBytes(by_receive_data, rece_file_path);
                    groupChatMessage.voiceMessage.filePath = fileRece.getAbsolutePath();

                } else if (groupChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Image) {//群聊图片
                    String imageData = groupChatMessage.fileMessage.fileData;
                    byte[] by_receive_data = Base64.decode(imageData, Base64.DEFAULT);
                    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    String imageFileName = "IMAGE_" + timeStamp + ".jpg";
                    File fileDir = null;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        //存在sd卡
                        fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/NexFi_ble/image");
                        if (!fileDir.exists()) {
                            fileDir.mkdirs();
                        }
                    }
                    String rece_file_path = fileDir + "/" + imageFileName;
                    File fileRece = FileTransferUtils.getFileFromBytes(by_receive_data, rece_file_path);
                    groupChatMessage.fileMessage.filePath = fileRece.getAbsolutePath();
                    bleDBDao.addGroupImageMsg(groupChatMessage.fileMessage);//保存图片数据
                }
                UserMessage userMessage = groupChatMessage.userMessage;
                userMessage.nodeId = link.getNodeId() + "";
                bleDBDao.addGroupTextMsg2(groupChatMessage);
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
        if(nodeId == null){
            return null;
        }
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            if (nodeId.equals(link.getNodeId() + "")) {
                return link;
            }
        }
        return null;
    }

} // Node
