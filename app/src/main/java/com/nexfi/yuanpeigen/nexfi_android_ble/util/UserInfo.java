package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;

/**
 * Created by Mark on 2016/4/25.
 */
public class UserInfo {

    public static int[] userHeadIcon = {R.mipmap.img_head_02, R.mipmap.img_head_03, R.mipmap.img_head_04, R.mipmap.img_head_05, R.mipmap.img_head_06, R.mipmap.img_head_07, R.mipmap.img_head_08, R.mipmap.img_head_09, R.mipmap.img_head_10, R.mipmap.img_head_11, R.mipmap.img_head_12, R.mipmap.img_head_13,R.mipmap.img_head_14,R.mipmap.img_head_15};


    public static void saveUsername(Context context, String username) {
        SharedPreferences sp = context.getSharedPreferences("username", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userName", username);
        editor.commit();
    }

    public static void saveUsersex(Context context, String usersex) {
        SharedPreferences sp = context.getSharedPreferences("usersex", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("userSex", usersex);
        editor.commit();
    }

    public static void setConfigurationInformation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("first_pref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isFirstIn", false);
        editor.commit();
    }

    public static void setNexFiInformation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("first_nexfi", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isNexFi", true);
        editor.commit();
    }

    public static void saveEnabledInformation(Context context, boolean flag) {
        SharedPreferences preferences = context.getSharedPreferences("EnabledInformation", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("Enabled", flag);
        editor.commit();
    }


    public static void saveUserHeadIcon(Context context, String id) {
        SharedPreferences preferences = context.getSharedPreferences("UserHeadIcon", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userhead", id);
        editor.commit();
    }

    public static void saveUserAge(Context context, int age) {
        SharedPreferences preferences = context.getSharedPreferences("UserAge", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("userage", age);
        editor.commit();
    }

    public static void saveUserId(Context context, String UserId) {
        SharedPreferences preferences = context.getSharedPreferences("UserId", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId", UserId);
        editor.commit();
    }

    public static String initUserAvatar(String userAvatar, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("UserHeadIcon", Context.MODE_PRIVATE);
        userAvatar = preferences.getString("userhead", "img_head_01");
        return userAvatar;
    }

    public static String initUserNick(String userNick, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("username", Context.MODE_PRIVATE);
        userNick = preferences.getString("userName", "未填写");
        return userNick;
    }

    public static String initUserGender(String userGender, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("usersex", Context.MODE_PRIVATE);
        userGender = preferences.getString("userSex", null);
        return userGender;
    }

    public static int initUserAge(int userAge, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("UserAge", Context.MODE_PRIVATE);
        userAge = preferences.getInt("userage", 0);
        return userAge;
    }

    public static boolean initConfigurationInformation(boolean isFirstIn, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("first_pref", Context.MODE_PRIVATE);
        isFirstIn = preferences.getBoolean("isFirstIn", true);
        return isFirstIn;
    }

    public static String initUserId(String UserId, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("UserId", Context.MODE_PRIVATE);
        UserId = preferences.getString("userId", null);
        return UserId;
    }

}
