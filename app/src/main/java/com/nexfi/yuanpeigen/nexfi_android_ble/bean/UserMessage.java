package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

import java.io.Serializable;

/**
 * Created by gengbaolong on 2016/4/13.
 */
public class UserMessage implements Serializable {
    private static  final long serialVersionUID = 4L;
    public String userId;
    public String nodeId;
    public String userNick;
    public int userAge;
    public String userGender;
    public String userAvatar;
    public String birthday;

    @Override
    public String toString() {
        return "userId="+userId+",userNick="+userNick+",nodeId="+nodeId;
    }
}
