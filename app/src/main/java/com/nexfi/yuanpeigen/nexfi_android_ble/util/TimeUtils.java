package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gengbaolong on 2016/4/18.
 */
public class TimeUtils {
    /**
     * 获得发送时间
     */
    public static String getNowTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//TODO
        Date date=new Date();
        String dateTime=format.format(date);
        return dateTime;
    }
}
