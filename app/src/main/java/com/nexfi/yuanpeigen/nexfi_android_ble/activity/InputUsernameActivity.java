package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

/**
 * Created by Mark on 2016/4/15.
 */
public class InputUsernameActivity extends AppCompatActivity implements View.OnClickListener {


    private RelativeLayout layout_back;
    private TextView tv_save;
    private EditText et_inputUsername;

    private String userNick, newUserNick;

    private final String USER_NICK = "userNick";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_username);

        initView();
        setClickListener();

    }

    private void setClickListener() {
        tv_save.setOnClickListener(this);
        layout_back.setOnClickListener(this);
    }

    private void initView() {
        layout_back = (RelativeLayout) findViewById(R.id.layout_back);
        tv_save = (TextView) findViewById(R.id.tv_save);
        et_inputUsername = (EditText) findViewById(R.id.et_inputUsername);
        userNick = UserInfo.initUserNick(userNick, this);
        if (!userNick.equals("未填写")) {
            et_inputUsername.setText(userNick);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.putExtra(USER_NICK, userNick);
                setResult(2, intent1);
                finish();
                Log.e("inputusername", userNick + "===============layout_back=======================");
                break;
            case R.id.tv_save:
                if (!TextUtils.isEmpty(et_inputUsername.getText())) {
                    String name = et_inputUsername.getText().toString();
//                    if (!name.equals(userNick)) {
                        userNick = name;
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra(USER_NICK, userNick);
                        Log.e("inputusername", userNick + "===============tv_save=======================");
                        setResult(2, intent);
                        finish();
                        UserInfo.saveUsername(this, userNick);
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    Toast.makeText(this, "您还未输入昵称哦", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

