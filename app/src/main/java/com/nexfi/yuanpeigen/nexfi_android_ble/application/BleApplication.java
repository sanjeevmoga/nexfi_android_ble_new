package com.nexfi.yuanpeigen.nexfi_android_ble.application;

import android.app.Application;
import android.content.Context;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.uncaught.CrashHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by gengbaolong on 2016/4/14.
 */
public class BleApplication extends Application{
    private static Context mContext;
    private static String uuid= UUID.randomUUID().toString();
    private static CrashHandler crashHandler;
    private static List<Throwable> exceptionLists=new ArrayList<Throwable>();
    private static List<String> logLists=new ArrayList<String>();


    public static int[] userHeadIcon = {R.mipmap.img_head_02, R.mipmap.img_head_03, R.mipmap.img_head_04, R.mipmap.img_head_05, R.mipmap.img_head_06, R.mipmap.img_head_07, R.mipmap.img_head_08, R.mipmap.img_head_09, R.mipmap.img_head_10, R.mipmap.img_head_11, R.mipmap.img_head_12, R.mipmap.img_head_13,R.mipmap.img_head_14,R.mipmap.img_head_15,R.mipmap.img_head_01};
    public static String[] iconNames={"img_head_02","img_head_03","img_head_04","img_head_05","img_head_06","img_head_07","img_head_08","img_head_09","img_head_10","img_head_11","img_head_12","img_head_13","img_head_14","img_head_15","img_head_01"};
    public static HashMap<String,Integer> iconMap=new HashMap<String,Integer>();

    @Override
    public void onCreate() {
        super.onCreate();

        mContext=getApplicationContext();
        crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());


        for (int i = 0; i < iconNames.length; i++) {
            iconMap.put(iconNames[i],userHeadIcon[i]);
        }

    }

    public static Context getContext(){
        return mContext;
    }

    public static String getUUID(){
        return uuid;
    }


    public static CrashHandler getCrashHandler(){
        return crashHandler;
    }

    public static List<Throwable> getExceptionLists(){
        return exceptionLists;
    }


    public static List<String> getLogLists(){
        return logLists;
    }

}
