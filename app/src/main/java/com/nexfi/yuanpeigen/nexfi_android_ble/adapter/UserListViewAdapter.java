package com.nexfi.yuanpeigen.nexfi_android_ble.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.ChatActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.UserInformationActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;

import java.util.List;

/**
 * Created by Mark on 2016/4/27.
 */
public class UserListViewAdapter extends BaseAdapter {


    private final String USER_AGE = "userAge";
    private final String USER_AVATAR = "userAvatar";
    private final String USER_GENDER = "userGender";
    private final String USER_NICK = "userNick";
    private final String USER_NODE_ID = "nodeId";
    private final String USER_ID = "userId";

    private Context mContext;
    private LayoutInflater mInflater;
    private List<UserMessage> userMessageList;
    private final String USER_SEX = "1";

    private boolean isNewUser = false;

    public UserListViewAdapter(Context context, List<UserMessage> userMessageList, boolean isNewUser) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.userMessageList = userMessageList;
        this.isNewUser = isNewUser;
    }


    @Override
    public int getCount() {
        return userMessageList.size();
    }


    @Override
    public Object getItem(int position) {
        return userMessageList.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UserMessage entity = userMessageList.get(position);
        if (entity != null) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_userlist, null);
                holder.btn_chat = (Button) convertView.findViewById(R.id.btn_chat);
                holder.iv_sex = (ImageView) convertView.findViewById(R.id.iv_sex);
                holder.iv_userhead_icon = (ImageView) convertView.findViewById(R.id.iv_userhead_icon);
                holder.tv_username = (TextView) convertView.findViewById(R.id.tv_username);
                holder.layout_userList = (RelativeLayout) convertView.findViewById(R.id.layout_userList);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String na = entity.userAvatar;
            if(na!=null){
                int imageRes = BleApplication.iconMap.get(na);
                holder.iv_userhead_icon.setImageResource(imageRes);
            }

            holder.tv_username.setText(entity.userNick);

            if (isNewUser && position == userMessageList.size() - 1) {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.hyperspace_out);
                holder.tv_username.setAnimation(animation);
            }

            if (entity.userGender.equals(USER_SEX)) {
                holder.iv_sex.setImageResource(R.mipmap.img_male);
            } else {
                holder.iv_sex.setImageResource(R.mipmap.img_female);
            }
            holder.btn_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ChatActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(USER_AGE, entity.userAge);
                    intent.putExtra(USER_AVATAR, entity.userAvatar);
                    intent.putExtra(USER_GENDER, entity.userGender);
                    intent.putExtra(USER_NICK, entity.userNick);
                    intent.putExtra(USER_NODE_ID, entity.nodeId);
                    intent.putExtra(USER_ID, entity.userId);
                    mContext.startActivity(intent);
                }
            });

            holder.layout_userList.setOnClickListener(new UserListOnClickListener(position));
        }
        return convertView;
    }

    static class ViewHolder {
        public ImageView iv_userhead_icon, iv_sex;
        public Button btn_chat;
        public TextView tv_username;
        public RelativeLayout layout_userList;
    }


    //单击事件实现
    class UserListOnClickListener implements View.OnClickListener {
        public int position;

        public UserListOnClickListener(int p) {
            position = p;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setClass(mContext, UserInformationActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("userList", userMessageList.get(position));
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

}
