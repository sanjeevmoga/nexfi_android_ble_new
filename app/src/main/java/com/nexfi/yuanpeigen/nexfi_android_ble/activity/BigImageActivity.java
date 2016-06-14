package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.view.ZoomImageView;


/**
 * Created by gengbaolong on 2016/4/22.
 */
public class BigImageActivity extends AppCompatActivity {
    ZoomImageView big_image_view;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);


        int width = metric.widthPixels;  // 宽度（PX）
        int height = metric.heightPixels;  // 高度（PX）
        Intent intent = getIntent();
        if (intent != null) {
            byte[] bis = intent.getByteArrayExtra("bitmap");
            bitmap= FileTransferUtils.decodeSampledBitmapFromResource(bis, width, height);
            if(bitmap==null){
                return;
            }
        }
        initView();
        big_image_view.setImageBitmap(bitmap);
    }

    private void initView() {
        big_image_view = (ZoomImageView) findViewById(R.id.big_image_view);
    }

}
