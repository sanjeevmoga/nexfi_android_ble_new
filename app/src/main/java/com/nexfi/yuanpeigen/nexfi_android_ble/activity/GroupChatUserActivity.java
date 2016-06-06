package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.UserListViewAdapter;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/5/3.
 */
public class GroupChatUserActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView groupUserList;
    private UserListViewAdapter userListViewAdapter;
    private RelativeLayout layout_back_group_user;
    private TextView textViewGroupuser;

    private List<UserMessage> userMessageList = new ArrayList<UserMessage>();
    private String userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_user);
        initView();
        initData();
    }

    private void initView() {
        groupUserList = (ListView) findViewById(R.id.groupUserList);
        layout_back_group_user= (RelativeLayout) findViewById(R.id.layout_back_group_user);
        textViewGroupuser= (TextView) findViewById(R.id.textViewGroupuser);
        layout_back_group_user.setOnClickListener(this);
    }

    private void initData() {
        userId = UserInfo.initUserId(userId, BleApplication.getContext());
        BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
        userMessageList = bleDBDao.findAllUsers(userId);
        userListViewAdapter = new UserListViewAdapter(BleApplication.getContext(), userMessageList, false);
        int length=userMessageList.size();
        textViewGroupuser.setText("群聊信息（"+length+"）");
        groupUserList.setAdapter(userListViewAdapter);
        if (userListViewAdapter != null) {
            userListViewAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layout_back_group_user:
                finish();
                break;
        }
    }
}
