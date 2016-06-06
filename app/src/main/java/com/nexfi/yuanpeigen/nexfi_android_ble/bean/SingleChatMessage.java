package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

import java.io.Serializable;

/**
 * Created by gengbaolong on 2016/5/23.
 */
public class SingleChatMessage extends BaseMessage implements Serializable {
    private static  final long serialVersionUID = 5L;
    public String receiver;//会话id
    public TextMessage textMessage;//文本消息
    public FileMessage fileMessage;//文件
    public VoiceMessage voiceMessage;//语音
}
