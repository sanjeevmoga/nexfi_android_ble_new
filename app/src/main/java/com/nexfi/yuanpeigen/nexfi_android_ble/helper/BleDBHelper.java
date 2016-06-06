package com.nexfi.yuanpeigen.nexfi_android_ble.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gengbaolong on 2016/4/14.
 */
public class BleDBHelper extends SQLiteOpenHelper {
    public BleDBHelper(Context context) {
        super(context, "blueSky.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //用户
        db.execSQL("create table userData (_id integer primary key autoincrement,messageType Integer(20),messageBodyType Integer(20),timeStamp varchar(20),msgId varchar(20),nodeId varchar(20),userId varchar(20),userNick varchar(20),userAge Integer(20),userGender varchar(20),userAvatar varchar(20),birthday varchar(20))");
        //单聊
        db.execSQL("create table textP2PMessg(_id integer primary key autoincrement,messageType Integer(20),messageBodyType Integer(20),timeStamp varchar(20),msgId varchar(20),nodeId varchar(20),userId varchar(20),userNick varchar(20),userAge Integer(20),userGender varchar(20),userAvatar varchar(20),birthday varchar(20)," +
                "receiver varchar(20),fileData varchar(20),isRead varchar(20),fileName varchar(20),filePath varchar(20),fileSize varchar(20),fileIcon Integer(20),isPb Integer(20),durational Integer(20))");

        // 群聊
        db.execSQL("create table textGroupMesg(_id integer primary key autoincrement,messageType Integer(20),messageBodyType Integer(20),timeStamp varchar(20),msgId varchar(20),nodeId varchar(20),userId varchar(20),userNick varchar(20),userAge Integer(20),userGender varchar(20),userAvatar varchar(20),birthday varchar(20),"+
                "groupId varchar(20),fileData varchar(20),isRead varchar(20),fileName varchar(20),filePath varchar(20),fileSize varchar(20),fileIcon Integer(20),isPb Integer(20),durational Integer(20))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
