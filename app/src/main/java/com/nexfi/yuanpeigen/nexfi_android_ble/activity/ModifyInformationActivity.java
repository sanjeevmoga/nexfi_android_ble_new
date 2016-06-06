package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.BaseMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.model.Node;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

/**
 * Created by Mark on 2016/4/29.
 */
public class ModifyInformationActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_username, tv_userAge;
    private ImageView iv_userhead_icon;
    private RadioButton rb_female, rb_male;
    private Button btn_finish;
    private RadioGroup radioGrop;
    private RelativeLayout layout_username, layout_userAge;

    private final String USER_SEX_MALE = "1";
    private final String USER_SEX_FEMALE = "2";
    private final String USER_AGE = "userAge";
    private final String USER_AVATAR = "userAvatar";
    private final String USER_NICK = "userNick";


    private String userSelfId;
    private String userNick, newUserNick, userGender;
    private int userAge, newUserAge;
    private String newUserAvater;
    private String userAvatar;
    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    private Node node;
    private UserMessage user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_information);
        node = MainActivity.getNode();
        initData();
        initView();
        setClickListener();
        setViewData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initView() {
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_userAge = (TextView) findViewById(R.id.tv_userAge);
        iv_userhead_icon = (ImageView) findViewById(R.id.iv_userhead_icon);
        btn_finish = (Button) findViewById(R.id.btn_finish);
        layout_userAge = (RelativeLayout) findViewById(R.id.layout_userAge);
        layout_username = (RelativeLayout) findViewById(R.id.layout_username);
        radioGrop = (RadioGroup) findViewById(R.id.radioGrop);
        rb_female = (RadioButton) findViewById(R.id.rb_female);
        rb_male = (RadioButton) findViewById(R.id.rb_male);
    }

    private void setViewData() {
        tv_username.setText(userNick);
        if (userAge == 0) {
            tv_userAge.setText("未填写");
        } else {
            tv_userAge.setText(userAge + "");
        }
        if (userGender != null) {
            if (userGender.equals(USER_SEX_MALE)) {
                rb_male.setChecked(true);
            } else {
                rb_female.setChecked(true);
            }
        }
        iv_userhead_icon.setImageResource(BleApplication.iconMap.get(userAvatar));

    }

    private void initData() {
        userSelfId = UserInfo.initUserId(userSelfId, BleApplication.getContext());
        userAge = UserInfo.initUserAge(userAge, BleApplication.getContext());
        userAvatar = UserInfo.initUserAvatar(userAvatar, BleApplication.getContext());
        userGender = UserInfo.initUserGender(userGender, BleApplication.getContext());
        userNick = UserInfo.initUserNick(userNick, BleApplication.getContext());
        user = bleDBDao.findUserByUserId(userSelfId);//geng
    }


    private void setClickListener() {
        iv_userhead_icon.setOnClickListener(this);
        layout_username.setOnClickListener(this);
        layout_userAge.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
        radioSetOnCheckedListener();
    }

    private void radioSetOnCheckedListener() {
        radioGrop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_male:
                        modify_userGender(USER_SEX_MALE);
                        break;
                    case R.id.rb_female:
                        modify_userGender(USER_SEX_FEMALE);
                        break;
                }
            }
        });
    }

    private void modify_userGender(String sex) {
        if (userGender != null) {
            if (!userGender.equals(sex)) {
                userGender = sex;
                UserInfo.saveUsersex(this, userGender);
                userInfoModify();
            }
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finish:
                finish();
                break;
            case R.id.iv_userhead_icon:
                Intent intent1 = new Intent(this, SelectUserHeadIconActivity.class);
                startActivityForResult(intent1, 1);
                break;
            case R.id.layout_username:
                Intent intent2 = new Intent(this, InputUsernameActivity.class);
                startActivityForResult(intent2, 2);
                break;
            case R.id.layout_userAge:
                Intent intent3 = new Intent(this, InputUserAgeActivity.class);
                startActivityForResult(intent3, 3);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            userAvatar = data.getStringExtra(USER_AVATAR);
            if (user.userAvatar.equals(userAvatar)) {
                //没有修改
            } else {
                //修改
                userInfoModify();
            }
            iv_userhead_icon.setImageResource(BleApplication.iconMap.get(userAvatar));
        } else if (resultCode == 2) {
            userNick = data.getStringExtra(USER_NICK);
            if (user.userNick.equals(userNick)) {
                //没有修改
            } else {
                //修改
                userInfoModify();
            }

            tv_username.setText(userNick);
        } else if (resultCode == 3) {
            userAge = data.getIntExtra(USER_AGE, 18);
            if (user.userAge == userAge) {
                //没有修改
            } else {
                //修改
                userInfoModify();
            }
            tv_userAge.setText(userAge + "");
        }

    }



    public void userInfoModify(){
        user.userNick = userNick;
        user.userAge = userAge;
        user.userGender = userGender;
        user.userAvatar = userAvatar;
        //将修改后的数据更新到数据库中
        bleDBDao.updateUserInfoByUserId(user, userSelfId);
        bleDBDao.updateP2PMsgByUserId(user, userSelfId);
        bleDBDao.updateGroupMsgByUserId(user, userSelfId);
        //发送改变通知
        BaseMessage baseMessage = new BaseMessage();
        baseMessage.messageType = MessageType.eMessageType_UpdateUserInfo;
        baseMessage.userMessage = user;
        Gson gson = new Gson();
        String json= gson.toJson(baseMessage);
        byte[] notify_msg_bys=json.getBytes();
        if(node!=null) {
            node.broadcastFrame(notify_msg_bys);
            Debug.debugLog("isModified", user.userNick + "------------用户信息已改变------" + user.userGender);
        }

    }
}
