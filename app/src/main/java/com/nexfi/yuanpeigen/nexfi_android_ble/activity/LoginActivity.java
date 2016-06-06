package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.BaseMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.SharedPreferencesUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import java.util.Random;
import java.util.UUID;

/**
 * Created by Mark on 2016/4/15.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_username, tv_userAge;
    private ImageView iv_userhead_icon;
    private Button btn_finish;
    private RadioButton rb_female, rb_male;
    private RadioGroup radioGrop;
    private RelativeLayout layout_username, layout_userAge;

    private Handler mHandler;

    private final String USER_AGE = "userAge";
    private final String USER_AVATAR = "userAvatar";
    private final String USER_GENDER = "userGender";
    private final String USER_NICK = "userNick";
    private final String USER_SEX_MALE = "1";
    private final String USER_SEX_FEMALE = "2";

    private String userNick, userGender;
    private int userAge;
    private String userAvatar;

    private boolean isFirstIn = false;
    private boolean isExit;


    BleDBDao bleDBDao=new BleDBDao(BleApplication.getContext());
    public String userIdOfFirstLogin=UUID.randomUUID().toString();//第一次进入登录时会生成一个用户id
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Debug.debugLog("onCreate", userAvatar + "==========userAvatar===============");
        initView();
        initData();
        setViewData();
        setClickListener();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                isExit = false;
            }
        };
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
        radioSetOnCheckedListener();
    }

    private void setClickListener() {
        btn_finish.setOnClickListener(this);
        iv_userhead_icon.setOnClickListener(this);
        layout_username.setOnClickListener(this);
        layout_userAge.setOnClickListener(this);
    }

    private void setViewData() {
        if (!isFirstIn) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            tv_username.setText("未填写");
            tv_userAge.setText("未填写");
            iv_userhead_icon.setImageResource(R.mipmap.img_head_01);
        }

    }

    private void initData() {
        userAge = UserInfo.initUserAge(userAge, this);
        userAvatar = UserInfo.initUserAvatar(userAvatar, this);
        userGender = UserInfo.initUserGender(userGender, this);
        userNick = UserInfo.initUserNick(userNick, this);
        isFirstIn = UserInfo.initConfigurationInformation(isFirstIn, this);
    }

    private void radioSetOnCheckedListener() {
        radioGrop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_male:
                        userGender = USER_SEX_MALE;
                        UserInfo.saveUsersex(LoginActivity.this, userGender);
                        break;
                    case R.id.rb_female:
                        userGender = USER_SEX_FEMALE;
                        UserInfo.saveUsersex(LoginActivity.this, userGender);
                        break;
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finish:
                //如果userAvatar是默认值，说明用户没有选择头像，那就随机选择一个
                if(userAvatar.equals("img_head_01")){
                    //用户不选择，就为其随机选择一个
                    int randomNum=new Random().nextInt(15)+2;//随机生成2~15之间的数字
                    if(randomNum<10){
                        userAvatar="img_head_0"+randomNum;
                    }else{
                        userAvatar="img_head_"+randomNum;
                    }
                    UserInfo.saveUserHeadIcon(this,userAvatar);
                }else{
                    //如果不是默认值，说明用户自己选择了头像
                    if(userAge != 0 && userGender != null && !userNick.equals("未填写")){
                        UserInfo.setConfigurationInformation(this);
                        UserInfo.saveUserId(this, userIdOfFirstLogin);
                        SharedPreferencesUtils.saveString(getApplicationContext(),"userSelfId",userIdOfFirstLogin);
                        saveUserInfo();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }else{
                        Toast.makeText(this, "您还未输入完信息哦", Toast.LENGTH_SHORT).show();
                    }
                }
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

    /**
     * 保存用户信息
     */
    private void saveUserInfo() {
        BaseMessage baseMessage=new BaseMessage();
        baseMessage.messageType = MessageType.eMessageType_requestUserInfo;
        UserMessage userMessage = new UserMessage();
        userMessage.userNick = userNick;
        userMessage.userAvatar = userAvatar;
        userMessage.userAge = userAge;
        userMessage.userGender = userGender;
        userMessage.userId = userIdOfFirstLogin;//每个用户第一次登录的时候生成一个用户id
        baseMessage.userMessage = userMessage;
        bleDBDao.add(baseMessage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            userAvatar = data.getStringExtra(USER_AVATAR);
            iv_userhead_icon.setImageResource(BleApplication.iconMap.get(userAvatar));
        } else if (resultCode == 2) {
            userNick = data.getStringExtra(USER_NICK);
            tv_username.setText(userNick);
        } else if (resultCode == 3) {
            userAge = data.getIntExtra(USER_AGE, 18);
            tv_userAge.setText(userAge + "");
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                isExit = true;
                mHandler.sendEmptyMessageDelayed(0, 1500);
                if (!userAvatar.equals("img_head_01") || userAge != 0 || userGender != null || !userNick.equals("未填写")) {
                    Toast.makeText(this, "您还未输入完信息哦", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "再按一次退出NexFi", Toast.LENGTH_SHORT).show();
                }
                return false;
            } else {
                finish();
            }
        }
        return true;
    }
}
