package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

/**
 * Created by Mark on 2016/6/6.
 */
public class GuideActivity extends AppCompatActivity implements Runnable {
    private Handler handler;
    private boolean isFirstIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView imageView = new ImageView(this);
        try {
            imageView.setImageResource(R.mipmap.icon_loading);//outOfMemoryError
        }catch (OutOfMemoryError error){
            imageView.setImageResource(R.mipmap.img_add);
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setContentView(imageView);
        isFirstIn = UserInfo.initConfigurationInformation(isFirstIn, this);
        handler = new Handler();
        handler.postDelayed(this, 1000);
    }

    @Override
    public void run() {
        if (!isFirstIn) {
            startActivity(new Intent(GuideActivity.this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(GuideActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }
}
