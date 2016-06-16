package com.nexfi.yuanpeigen.nexfi_android_ble.operation;

import com.nexfi.yuanpeigen.nexfi_android_ble.listener.ReceiveTextMsgListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.SendTextMsgListener;

/**
 * Created by gengbaolong on 2016/4/13.
 */
public class TextMsgOperation {

    SendTextMsgListener mSendTextMsgListener=null;
    ReceiveTextMsgListener mReceiveTextMsgListener=null;


    /**
     * 发送文本消息
     * @param textMsg
     * @param userId
     */
    public void sendTextMessage(String textMsg,String userId){

    }

    /**
     * 接收文本消息
     */
    public void receiveTextMessage(Object obj){
        if(null!=mReceiveTextMsgListener){
            mReceiveTextMsgListener.onReceiveTextMsg(obj);
        }
    }


    //设置回调接口(监听器)的方法
    public void setSendTextMsgListener(SendTextMsgListener sendTextMsgListener) {
        mSendTextMsgListener = sendTextMsgListener;
    }

    public void setReceiveTextMsgListener(ReceiveTextMsgListener receiveTextMsgListener) {
        mReceiveTextMsgListener = receiveTextMsgListener;
    }

}
