package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

/**
 * Created by gengbaolong on 2016/4/17.
 */
public class MessageType {

    public static final int eMessageType_SingleChat = 1;
    public static final int eMessageType_AllUserChat = 2;
    public static final int eMessageType_GroupChat = 3;
    public static final int eMessageType_SendUserInfo = 4;//反馈消息
    public static final int eMessageType_requestUserInfo = 5;//请求消息
    public static final int eMessageType_UpdateUserInfo = 6;


    public static final int MODIFY_USER_INFO=0;
    public static final int REQUEST_USER_INFO=1;//请求消息
    public static final int RESPONSE_USER_INFO=2;//反馈消息
    public static final int OFFINE_USER_INFO=3;//下线消息
    public static final int SINGLE_CHAT_MESSAGE_TYPE=4;//单聊
    public static final int SEND_TEXT_ONLY_MESSAGE_TYPE=7;//文本消息
    public static final int RECEIVE_TEXT_ONLY_MESSAGE_TYPE=8;//接收文本消息
    public static final int SINGLE_SEND_FOLDER_MESSAGE_TYPE = 9;
    public static final int SINGLE_RECV_FOLDER_MESSAGE_TYPE = 10;
    public static final int SINGLE_SEND_IMAGE_MESSAGE_TYPE = 11;
    public static final int SINGLE_RECV_IMAGE_MESSAGE_TYPE= 12;
    public static final int GROUP_SEND_TEXT_ONLY_MESSAGE_TYPE=13;//群聊发送
    public static final int GROUP_RECEIVE_TEXT_ONLY_MESSAGE_TYPE=14;//群聊接收
    public static final int GROUP_SEND_FOLDER_MESSAGE_TYPE = 15;
    public static final int GROUP_RECEIVE_FOLDER_MESSAGE_TYPE = 16;
    public static final int GROUP_SEND_IMAGE_MESSAGE_TYPE = 17;
    public static final int GROUP_RECEIVE_IMAGE_MESSAGE_TYPE = 18;

}
