package com.nexfi.yuanpeigen.nexfi_android_ble.operation;

import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.LoginListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.LogoutListener;

/**
 * Created by gengbaolong on 2016/4/13.
 */
public class UserMsgOperation {

    LoginListener mLoginListener=null;
    LogoutListener mLogoutListener=null;
    BleDBDao bleDBDao=new BleDBDao(BleApplication.getContext());

    /**
     * 根据用户id获取用户信息
     * @param userId
     * @return
     */
    public UserMessage getUserInfo(String userId){
        UserMessage userMessage=bleDBDao.findUserByUserId(userId);
        if(null!=userMessage){
            return userMessage;
        }
        return null;
    }


    //设置回调接口(监听器)的方法
    private void setLoginListener(LoginListener loginListener) {
        mLoginListener = loginListener;
    }

    private void setLogoutListener(LogoutListener logoutListener) {
        mLogoutListener = logoutListener;
    }
}
