package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by Mark on 2016/6/27.
 */
public class VerifyActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_register;
    private ImageView iv_back;
    private TextView tv_text_phone, tv_send;
    private EditText et_code;
    private static final String TAG = VerifyActivity.class.getSimpleName();
    private boolean isFirst = true;

    private String phoneNunmber;
    private String code;
    private String intentName = "phoneNunmber";
    private String textPhone;

    private TimeCount time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        initView();
        initSDK();
        time = new TimeCount(60000, 1000);
        time.start();
        initIntentData();
    }

    private void initIntentData() {
        Intent intent = getIntent();
        phoneNunmber = intent.getStringExtra(intentName);
        textPhone = "+86  " + phoneNunmber.substring(0, 3) + " " + phoneNunmber.substring(3, 7) + " " + phoneNunmber.substring(7, 11);
        tv_text_phone.setText(textPhone);
    }


    private void initView() {
        et_code = (EditText) findViewById(R.id.et_code);
        btn_register = (Button) findViewById(R.id.btn_register);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_text_phone = (TextView) findViewById(R.id.tv_text_phone);
        tv_send = (TextView) findViewById(R.id.tv_send);

        btn_register.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        tv_send.setOnClickListener(this);

    }


    private void initSDK() {
        EventHandler eh = new EventHandler() {
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_GET_VOICE_VERIFICATION_CODE) {
                    if (!isFirst) {
                        Toast.makeText(getApplicationContext(), "发送验证码成功", Toast.LENGTH_SHORT).show();
                    }
                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "验证成功", Toast.LENGTH_SHORT).show();
                    UserInfo.saveUserPhoneNumber(VerifyActivity.this, phoneNunmber);
                    if (!UserInfo.initConfigurationInformation(VerifyActivity.this)) {
                        startActivity(new Intent(VerifyActivity.this, MainActivity.class));
                    } else {
                        startActivity(new Intent(VerifyActivity.this, LoginActivity.class));
                    }
                    finish();
                }
            } else if (result == SMSSDK.RESULT_ERROR) {
                try {
                    Throwable throwable = (Throwable) data;
                    throwable.printStackTrace();
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");//错误描述
                    int status = object.optInt("status");//错误代码
                    if (status > 0 && !TextUtils.isEmpty(des)) {
                        Toast.makeText(VerifyActivity.this, des, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "读取服务器返回信息失败");
                }
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                if (!TextUtils.isEmpty(et_code.getText())) {
                    code = et_code.getText().toString();
                    SMSSDK.submitVerificationCode("86", phoneNunmber, code);
                } else {
                    Toast.makeText(getApplicationContext(), "请输入您的验证码", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_back:
                startActivity(new Intent(VerifyActivity.this, RegisterActivity.class));
                finish();
                break;
            case R.id.tv_send:
                time.start();
                SMSSDK.getVoiceVerifyCode("86", phoneNunmber);
                isFirst = false;
                break;
        }
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            tv_send.setText("发送验证码");
            tv_send.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            tv_send.setClickable(false);//防止重复点击
            tv_send.setText("重新发送（" + millisUntilFinished / 1000 + "）");
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(VerifyActivity.this, RegisterActivity.class));
            finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();
    }
}
