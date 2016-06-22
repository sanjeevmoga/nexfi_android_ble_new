package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

import java.io.Serializable;

/**
 * Created by gengbaolong on 2016/5/23.
 */
public class VoiceMessage implements Serializable{
    private static  final long serialVersionUID = 10L;
    public String durational;//语音时间
    public String fileData;//语音数据
    public String isRead;//是否已读
    public String filePath;
}
