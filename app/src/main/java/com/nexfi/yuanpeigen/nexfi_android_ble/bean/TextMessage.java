package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

import java.io.Serializable;

/**
 * Created by gengbaolong on 2016/4/13.
 */
public class TextMessage implements Serializable{
    private static  final long serialVersionUID = 9L;
    public String fileData;//文本消息
    public String isRead;
}
