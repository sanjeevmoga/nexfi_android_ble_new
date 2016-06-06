package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;

/**
 * Created by Mark on 2016/4/27.
 */
public class UserInformationActivity extends AppCompatActivity {

    private final String USER_SEX = "1";

    private String userNick, userGender;
    private int userAge;
    private String userAvatar;


    private TextView textView, tv_username, tv_userAge;
    private ImageView iv_userhead_icon;
    private Button btn_finish;
    private RadioButton rb_female, rb_male;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        initIntentData();
        initView();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.textView);
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_userAge = (TextView) findViewById(R.id.tv_userAge);
        iv_userhead_icon = (ImageView) findViewById(R.id.iv_userhead_icon);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_male = (RadioButton) findViewById(R.id.rb_male);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        textView.setText(userNick);
        tv_username.setText(userNick);
        tv_userAge.setText(userAge + "");
        iv_userhead_icon.setImageResource(BleApplication.iconMap.get(userAvatar));
        Debug.debugLog("UserInformationActivity",userGender+"=====userGender---------------"+USER_SEX);
        if (userGender.equals(USER_SEX)) {
            rb_male.setChecked(true);
        } else {
            rb_female.setChecked(true);
        }
        rb_female.setEnabled(false);
        rb_male.setEnabled(false);
    }

    private void initIntentData() {
        Intent intent = getIntent();
        UserMessage userMsg = (UserMessage) intent.getSerializableExtra("data_obj");
        UserMessage userMessage = (UserMessage) intent.getSerializableExtra("userList");
        if (userMessage != null) {
            userNick = userMessage.userNick;
            userGender = userMessage.userGender;
            userAge = userMessage.userAge;
            userAvatar = userMessage.userAvatar;
        }

        if (userMsg != null) {
            userNick = userMsg.userNick;
            userGender = userMsg.userGender;
            userAge = userMsg.userAge;
            userAvatar = userMsg.userAvatar;
        }
    }

}

