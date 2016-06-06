package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

import java.io.Serializable;

/**
 * Created by gengbaolong on 2016/4/13.
 */
public class BaseMessage implements Serializable{
    private static  final long serialVersionUID = 1L;
    public int messageType;//消息类型
    public int messageBodyType;//消息体类型
    public String timeStamp;//时间
    public UserMessage userMessage;//消息实体
    public String msgId;//消息id
}
