package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.SelectUserHeadIconGridViewAdapter;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.SharedPreferencesUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

/**
 * Created by Mark on 2016/4/14.
 */
public class SelectUserHeadIconActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_save;
    private RelativeLayout layout_back;
    private GridView gridView;
    private boolean isSelected = false;

    private final String USER_AVATAR = "userAvatar";

    private String userAvatar, newUserAvatar;

    private SelectUserHeadIconGridViewAdapter mGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_userheadicon);

        initView();
    }

    private void initView() {
        tv_save = (TextView) findViewById(R.id.tv_save);
        layout_back = (RelativeLayout) findViewById(R.id.layout_back);
        gridView = (GridView) findViewById(R.id.gridView);
        layout_back.setOnClickListener(this);
        tv_save.setOnClickListener(this);
        mGridViewAdapter = new SelectUserHeadIconGridViewAdapter(this);
        gridView.setAdapter(mGridViewAdapter);
        gridViewSetOnclickLisener();
        userAvatar = UserInfo.initUserAvatar(userAvatar, this);
    }

    private void gridViewSetOnclickLisener() {
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                newUserAvatar = BleApplication.iconNames[position];
                isSelected = true;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.putExtra(USER_AVATAR, userAvatar);//gengbaolong
                setResult(1, intent1);
                finish();
                break;
            case R.id.tv_save:
//                if (isSelected) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra(USER_AVATAR, newUserAvatar);
                        setResult(1, intent);
                        finish();
                        UserInfo.saveUserHeadIcon(this, newUserAvatar);//存储的是头像的名字
                        SharedPreferencesUtils.saveString(getApplicationContext(),"USER_AVATAR",newUserAvatar);
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(this, "您还未选择头像哦", Toast.LENGTH_SHORT).show();
//                }
                break;
        }
    }
}
