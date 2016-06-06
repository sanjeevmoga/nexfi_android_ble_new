package com.nexfi.yuanpeigen.nexfi_android_ble.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.nexfi.yuanpeigen.nexfi_android_ble.bean.BaseMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.FileMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.GroupChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.SingleChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.TextMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.VoiceMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.helper.BleDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gengbaolong on 2016/4/14.
 */
public class BleDBDao {
    private Context context;
    BleDBHelper helper;

    public BleDBDao(Context context) {
        this.context = context;
        helper = new BleDBHelper(context);
    }

    /**
     * 保存用户数据
     *
     * @param
     */
    public void add(BaseMessage baseMessage) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageType", baseMessage.messageType);
        values.put("timeStamp", baseMessage.timeStamp);
        values.put("messageBodyType", baseMessage.messageBodyType);
        values.put("msgId", baseMessage.msgId);
        UserMessage userMessage = baseMessage.userMessage;
        values.put("nodeId", userMessage.nodeId);
        values.put("userId", userMessage.userId);
        values.put("userNick", userMessage.userNick);
        values.put("userAge", userMessage.userAge);
        values.put("userGender", userMessage.userGender);
        values.put("userAvatar", userMessage.userAvatar);
        values.put("birthday", userMessage.birthday);
        db.insert("userData", null, values);
        db.close();
        Log.e("SQLiteDatabase add", userMessage.userNick + "-保存到数据库---" + userMessage.userAvatar + "====" + userMessage.userId);
        //有新用户上线
        context.getContentResolver().notifyChange(
                Uri.parse("content://www.nexfi_ble_user.com"), null);
    }


    /**
     * 根据userId修改数据库中原有用户信息
     *
     * @param userMessage
     * @param userId
     */
    public void updateUserInfoByUserId(UserMessage userMessage, String userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nodeId", userMessage.nodeId);
        values.put("userId", userMessage.userId);
        values.put("userNick", userMessage.userNick);
        values.put("userAge", userMessage.userAge);
        values.put("userGender", userMessage.userGender);
        values.put("userAvatar", userMessage.userAvatar);
        values.put("birthday", userMessage.birthday);
        db.update("userData", values, "userId=?", new String[]{userId});
        db.close();
        context.getContentResolver().notifyChange(
                Uri.parse("content://www.nexfi_ble_user.com"), null);
        Log.e("TAG", userMessage.userNick + "---------------------用户数据改变了------------------" + userMessage.userAvatar);
    }


    /**
     * 查找所有用户(把用户自身排除)
     *
     * @param userId
     * @return
     */
    public List<UserMessage> findAllUsers(String userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("userData", null, null, null, null, null, null);
        List<UserMessage> mDatas = new ArrayList<UserMessage>();
        List<UserMessage> mList = new ArrayList<UserMessage>();
        while (cursor.moveToNext()) {
            UserMessage user = new UserMessage();
            user.nodeId = cursor.getString(cursor.getColumnIndex("nodeId"));
            user.userId = cursor.getString(cursor.getColumnIndex("userId"));
            user.userNick = cursor.getString(cursor.getColumnIndex("userNick"));
            user.userAge = cursor.getInt(cursor.getColumnIndex("userAge"));
            user.userGender = cursor.getString(cursor.getColumnIndex("userGender"));
            user.userAvatar = cursor.getString(cursor.getColumnIndex("userAvatar"));
            user.birthday = cursor.getString(cursor.getColumnIndex("birthday"));
            if (!userId.equals(user.userId)) {
                mDatas.add(user);
            }
        }
        cursor.close();
        db.close();
        return mDatas;
    }

    /**
     * 根据用户id查找用户
     *
     * @param userId
     * @return
     */
    public UserMessage findUserByUserId(String userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("userData", null, "userId=?", new String[]{userId}, null, null, null);
        while (cursor.moveToNext()) {
            UserMessage user = new UserMessage();
            user.nodeId = cursor.getString(cursor.getColumnIndex("nodeId"));
            user.userId = cursor.getString(cursor.getColumnIndex("userId"));
            user.userNick = cursor.getString(cursor.getColumnIndex("userNick"));
            user.userAge = cursor.getInt(cursor.getColumnIndex("userAge"));
            user.userGender = cursor.getString(cursor.getColumnIndex("userGender"));
            user.userAvatar = cursor.getString(cursor.getColumnIndex("userAvatar"));
            user.birthday = cursor.getString(cursor.getColumnIndex("birthday"));
            return user;
        }
        return null;
    }


    /**
     * 根据用户id查找是否有相同数据
     *
     * @param userId
     * @return
     */
    public boolean findSameUserByUserId(String userId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("userData", null, "userId=?", new String[]{userId}, null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }


    /**
     * 根据userId删除用户信息
     */
    public void deleteUserByNodeId(String nodeId, String userSelfId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int row = db.delete("userData", "nodeId = ? and userId!=?",
                new String[]{nodeId + "", userSelfId});
        db.close();
        //有用户下线
        context.getContentResolver().notifyChange(
                Uri.parse("content://www.nexfi_ble_user.com"), null);
    }


    public void addP2PTextMsg(SingleChatMessage singleChatMessage) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageType", singleChatMessage.messageType);
        values.put("messageBodyType", singleChatMessage.messageBodyType);
        values.put("msgId", singleChatMessage.msgId);
        values.put("timeStamp", singleChatMessage.timeStamp);
        values.put("receiver", singleChatMessage.receiver);

        UserMessage userMessage = singleChatMessage.userMessage;
        values.put("userId", userMessage.userId);
        values.put("userNick", userMessage.userNick);
        values.put("userAge", userMessage.userAge);
        values.put("userAvatar", userMessage.userAvatar);
        values.put("userGender", userMessage.userGender);
        values.put("birthday", userMessage.birthday);

        TextMessage textMessage = singleChatMessage.textMessage;
        if (textMessage != null) {
            values.put("fileData", textMessage.fileData);
            values.put("isRead", textMessage.isRead);
        }

        FileMessage fileMessage = singleChatMessage.fileMessage;
        if (fileMessage != null) {
            values.put("fileData", fileMessage.fileData);
            values.put("fileName", fileMessage.fileName);
            values.put("fileSize", fileMessage.fileSize);
            values.put("fileIcon", fileMessage.fileIcon);
            values.put("filePath", fileMessage.filePath);
            values.put("isPb", fileMessage.isPb);
            values.put("isRead", fileMessage.isRead);
        }

        VoiceMessage voiceMessage = singleChatMessage.voiceMessage;
        if (voiceMessage != null) {
            values.put("durational", voiceMessage.durational);
            values.put("fileData", voiceMessage.fileData);
            values.put("isRead", voiceMessage.isRead);
        }
        db.insert("textP2PMessg", null, values);
        db.close();
    }


    public int getCount(String chat_id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from textP2PMessg where receiver=?", new String[]{chat_id});
        cursor.moveToNext();
        int count = cursor.getInt(0);
        return count;
    }


    /**
     * 分页查询单聊数据
     * @param chat_id
     * @param pageSize
     * @param startIndex
     * @return
     */
    public List<SingleChatMessage> findPartMsgByChatId(String chat_id,int pageSize, int startIndex) {
        List<SingleChatMessage> mDatas=new ArrayList<SingleChatMessage>();
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "select * from textP2PMessg where receiver=? order by  _id desc limit ? offset ?",
                    new String[]{chat_id,String.valueOf(pageSize),
                            String.valueOf(startIndex)});
            while (cursor.moveToNext()) {
                SingleChatMessage singleChatMessage = new SingleChatMessage();
                singleChatMessage.messageType = cursor.getInt(cursor.getColumnIndex("messageType"));
                singleChatMessage.messageBodyType = cursor.getInt(cursor.getColumnIndex("messageBodyType"));
                singleChatMessage.receiver = cursor.getString(cursor.getColumnIndex("receiver"));
                singleChatMessage.msgId = cursor.getString(cursor.getColumnIndex("msgId"));
                singleChatMessage.timeStamp = cursor.getString(cursor.getColumnIndex("timeStamp"));

                UserMessage userMessage = new UserMessage();
                userMessage.birthday = cursor.getString(cursor.getColumnIndex("birthday"));
                userMessage.userId = cursor.getString(cursor.getColumnIndex("userId"));
                userMessage.userNick = cursor.getString(cursor.getColumnIndex("userNick"));
                userMessage.userAvatar = cursor.getString(cursor.getColumnIndex("userAvatar"));
                userMessage.userGender = cursor.getString(cursor.getColumnIndex("userGender"));
                userMessage.userAge = cursor.getInt(cursor.getColumnIndex("userAge"));
                singleChatMessage.userMessage = userMessage;

                TextMessage textMessage = new TextMessage();
                textMessage.fileData = cursor.getString(cursor.getColumnIndex("fileData"));
                textMessage.isRead = cursor.getString(cursor.getColumnIndex("isRead"));
                singleChatMessage.textMessage = textMessage;

                FileMessage fileMessage = new FileMessage();
                fileMessage.fileData = cursor.getString(cursor.getColumnIndex("fileData"));
                fileMessage.isRead = cursor.getString(cursor.getColumnIndex("isRead"));
                fileMessage.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
                fileMessage.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
                fileMessage.fileSize = cursor.getString(cursor.getColumnIndex("fileSize"));
                fileMessage.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
                fileMessage.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
                singleChatMessage.fileMessage = fileMessage;

                VoiceMessage voiceMessage = new VoiceMessage();
                voiceMessage.fileData = cursor.getString(cursor.getColumnIndex("fileData"));
                voiceMessage.isRead = cursor.getString(cursor.getColumnIndex("isRead"));
                voiceMessage.durational = cursor.getString(cursor.getColumnIndex("durational"));
                singleChatMessage.voiceMessage = voiceMessage;

                mDatas.add(singleChatMessage);
            }
            cursor.close();
            db.close();
        } catch (OutOfMemoryError error) {
            //
        }

        return mDatas;
    }



    /**
     * 根据userId更新数据库中的单聊数据
     *
     * @param userMessage
     * @param userId
     */
    public void updateP2PMsgByUserId(UserMessage userMessage, String userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nodeId", userMessage.nodeId);
        values.put("userId", userMessage.userId);
        values.put("userNick", userMessage.userNick);
        values.put("userAge", userMessage.userAge);
        values.put("userGender", userMessage.userGender);
        values.put("userAvatar", userMessage.userAvatar);
        db.update("textP2PMessg", values, "userId=?", new String[]{userId});
        db.close();
        context.getContentResolver().notifyChange(
                Uri.parse("content://www.nexfi_ble_user_single.com"), null);
    }

    /**
     * 保存群聊数据
     *
     * @param
     */
    public void addGroupTextMsg2(GroupChatMessage groupChatMessage) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageType", groupChatMessage.messageType);
        values.put("messageBodyType", groupChatMessage.messageBodyType);
        values.put("timeStamp", groupChatMessage.timeStamp);
        values.put("groupId", groupChatMessage.groupId);
        values.put("msgId", groupChatMessage.msgId);

        UserMessage userMessage = groupChatMessage.userMessage;
        values.put("userId", userMessage.userId);
        values.put("userNick", userMessage.userNick);
        values.put("userAge", userMessage.userAge);
        values.put("userAvatar", userMessage.userAvatar);
        values.put("userGender", userMessage.userGender);
        values.put("birthday", userMessage.birthday);

        TextMessage textMessage = groupChatMessage.textMessage;
        if (textMessage != null) {
            values.put("fileData", textMessage.fileData);
            values.put("isRead", textMessage.isRead);
        }

        FileMessage fileMessage = groupChatMessage.fileMessage;
        if (fileMessage != null) {
            values.put("fileData", fileMessage.fileData);
            values.put("fileName", fileMessage.fileName);
            values.put("fileSize", fileMessage.fileSize);
            values.put("fileIcon", fileMessage.fileIcon);
            values.put("filePath", fileMessage.filePath);
            values.put("isPb", fileMessage.isPb);
            values.put("isRead", fileMessage.isRead);
        }

        VoiceMessage voiceMessage = groupChatMessage.voiceMessage;
        if (voiceMessage != null) {
            values.put("durational", voiceMessage.durational);
            values.put("fileData", voiceMessage.fileData);
            values.put("isRead", voiceMessage.isRead);
        }

        db.insert("textGroupMesg", null, values);
        db.close();
    }


    /**
     * 根据用户uuid查找是否有相同聊天数据
     *
     * @param msgId
     * @return
     */
    public boolean findSameGroupByUuid(String msgId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.query("textGroupMesg", null, "msgId=?", new String[]{msgId}, null, null, null);
        if (cursor.moveToNext()) {
            return true;
        }
        return false;
    }


    //TODO

    /**
     * 分页查询群聊数据
     * @param pageSize
     * @param startIndex
     * @return
     */
    public List<GroupChatMessage> findPartGroupMsg(int pageSize, int startIndex) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from textGroupMesg order by  _id desc limit ? offset ?",
                new String[]{String.valueOf(pageSize),
                        String.valueOf(startIndex)});
        List<GroupChatMessage> mDatas = new ArrayList<GroupChatMessage>();
        while (cursor.moveToNext()) {
            GroupChatMessage groupChatMessage = new GroupChatMessage();
            groupChatMessage.messageType = cursor.getInt(cursor.getColumnIndex("messageType"));
            groupChatMessage.messageBodyType = cursor.getInt(cursor.getColumnIndex("messageBodyType"));
            groupChatMessage.timeStamp = cursor.getString(cursor.getColumnIndex("timeStamp"));
            groupChatMessage.groupId = cursor.getString(cursor.getColumnIndex("groupId"));
            groupChatMessage.msgId = cursor.getString(cursor.getColumnIndex("msgId"));

            UserMessage userMessage = new UserMessage();
            userMessage.birthday = cursor.getString(cursor.getColumnIndex("birthday"));
            userMessage.userId = cursor.getString(cursor.getColumnIndex("userId"));
            userMessage.userNick = cursor.getString(cursor.getColumnIndex("userNick"));
            userMessage.userAvatar = cursor.getString(cursor.getColumnIndex("userAvatar"));
            userMessage.userGender = cursor.getString(cursor.getColumnIndex("userGender"));
            userMessage.userAge = cursor.getInt(cursor.getColumnIndex("userAge"));
            groupChatMessage.userMessage = userMessage;

            TextMessage textMessage = new TextMessage();
            textMessage.fileData = cursor.getString(cursor.getColumnIndex("fileData"));
            textMessage.isRead = cursor.getString(cursor.getColumnIndex("isRead"));
            groupChatMessage.textMessage = textMessage;

            FileMessage fileMessage = new FileMessage();
            fileMessage.fileData = cursor.getString(cursor.getColumnIndex("fileData"));
            fileMessage.isRead = cursor.getString(cursor.getColumnIndex("isRead"));
            fileMessage.filePath = cursor.getString(cursor.getColumnIndex("filePath"));
            fileMessage.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
            fileMessage.fileSize = cursor.getString(cursor.getColumnIndex("fileSize"));
            fileMessage.fileIcon = cursor.getInt(cursor.getColumnIndex("fileIcon"));
            fileMessage.isPb = cursor.getInt(cursor.getColumnIndex("isPb"));
            groupChatMessage.fileMessage = fileMessage;

            VoiceMessage voiceMessage = new VoiceMessage();
            voiceMessage.fileData = cursor.getString(cursor.getColumnIndex("fileData"));
            voiceMessage.isRead = cursor.getString(cursor.getColumnIndex("isRead"));
            voiceMessage.durational = cursor.getString(cursor.getColumnIndex("durational"));
            groupChatMessage.voiceMessage = voiceMessage;

            mDatas.add(groupChatMessage);
        }
        cursor.close();
        db.close();
        return mDatas;
    }


    /**
     * 获得群聊记录的总条数
     * @return
     */
    public int getGroupCount() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from textGroupMesg", null);
        cursor.moveToNext();
        int count = cursor.getInt(0);
        return count;
    }



    /**
     * 根据userId更新数据库中的群聊数据
     *
     * @param userMessage
     * @param userId
     */
    public void updateGroupMsgByUserId(UserMessage userMessage, String userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nodeId", userMessage.nodeId);
        values.put("userId", userMessage.userId);
        values.put("userNick", userMessage.userNick);
        values.put("userAge", userMessage.userAge);
        values.put("userGender", userMessage.userGender);
        values.put("userAvatar", userMessage.userAvatar);
        db.update("textGroupMesg", values, "userId=?", new String[]{userId});
        db.close();
        context.getContentResolver().notifyChange(
                Uri.parse("content://www.nexfi_ble_user_group.com"), null);
    }

}
