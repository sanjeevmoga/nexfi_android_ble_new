package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.view.ShowScaleImageView;


/**
 * Created by gengbaolong on 2016/5/1.
 */
public class SelectBigImageActivity extends AppCompatActivity {
    private ShowScaleImageView imageView;

    private Button bt_cancle;
    private Button bt_sure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

                WindowManager.LayoutParams.FLAG_FULLSCREEN);  //设置全屏
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //设置没有标题
        setContentView(R.layout.activity_big_image);
        Intent intent=getIntent();
        final String picturePath=intent.getStringExtra("picturePath");

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);


        int width = metric.widthPixels;  // 宽度（PX）
        int height = metric.heightPixels;  // 高度（PX）
        Bitmap bitmap= FileTransferUtils.compressImageFromFile(picturePath, width, height);
        //TODO 2016/5/29
        if(bitmap==null){
            return;
        }
        imageView = (ShowScaleImageView) findViewById(R.id.imgView);
        bt_cancle= (Button) findViewById(R.id.bt_cancle);
        bt_sure= (Button) findViewById(R.id.bt_sure);
        imageView.setImageBitmap(bitmap);


        bt_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data=new Intent(SelectBigImageActivity.this,ChatActivity.class);
                data.putExtra("RESULT", picturePath);
                SelectBigImageActivity.this.setResult(2, data);
//                startActivity(data);
                SelectBigImageActivity.this.finish();
            }
        });


        bt_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
