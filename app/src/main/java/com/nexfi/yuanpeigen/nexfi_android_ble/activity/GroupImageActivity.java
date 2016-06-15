package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.FileMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.GroupChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageBodyType;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.SharedPreferencesUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.view.ViewPagerFixed;
import com.nexfi.yuanpeigen.nexfi_android_ble.view.ZoomImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by gengbaolong on 2016/6/15.
 */
public class GroupImageActivity extends AppCompatActivity {
    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    ViewPagerFixed viewpager;
    int firstPosition=0;
    String userId;
    private List<GroupChatMessage> mAllDataArrays = new ArrayList<GroupChatMessage>();
    private List<GroupChatMessage> mImageDataArrays = new ArrayList<GroupChatMessage>();

    private int window_width, window_height;// 控件宽度

    private ImageView[] mImageViews=null;
    Bitmap[] bitmaps=null;
    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_activity);

        /** 获取可見区域高度 **/
        WindowManager manager = getWindowManager();
        window_width = manager.getDefaultDisplay().getWidth();
        window_height = manager.getDefaultDisplay().getHeight();

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        width = metric.widthPixels;
        height = metric.heightPixels;
        userId= SharedPreferencesUtils.getString(BleApplication.getContext(), "CHAT_ID", UUID.randomUUID().toString());
        mAllDataArrays = bleDBDao.findGroupMsg();
        for (int i = 0; i <mAllDataArrays.size() ; i++) {
            int type=mAllDataArrays.get(i).messageBodyType;
            if(type == MessageBodyType.eMessageBodyType_Image){
                mImageDataArrays.add(mAllDataArrays.get(i));
            }
        }
        Intent intent=getIntent();
        int page=intent.getIntExtra("page", 0);
        for (int i = 0; i <mImageDataArrays.size() ; i++) {
            if(mImageDataArrays.get(i)==mAllDataArrays.get(page)){
                firstPosition=i;
                break;
            }
        }
        viewpager = (ViewPagerFixed) findViewById(R.id.viewpager);

        initAllImages();

        viewpager.setAdapter(new MyPageAdapter());
        viewpager.setCurrentItem(firstPosition);
    }

    private void initAllImages() {
        bitmaps=new Bitmap[mImageDataArrays.size()];
        mImageViews=new ImageView[bitmaps.length];
        for (int i = 0; i<mImageDataArrays.size() ; i++) {
            FileMessage fileMessage = mImageDataArrays.get(i).fileMessage;
            byte[] bys_send = Base64.decode(fileMessage.fileData, Base64.DEFAULT);
            Bitmap bitmap= FileTransferUtils.decodeSampledBitmapFromResource(bys_send, width, height);
            bitmaps[i]=bitmap;
        }

    }

    private class MyPageAdapter extends PagerAdapter {

        // 决定viewpager的长度
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mImageViews.length;//Integer.MAX_VALUE
        }

        // 决定是否使用缓存,true的话就使用缓存
        @Override
        public boolean isViewFromObject(View view, Object object) {
            // TODO Auto-generated method stub
            return view == object;
        }

        /**
         * position 将要初始化的索引
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ZoomImageView imageView=new ZoomImageView(BleApplication.getContext());
            imageView.setImageBitmap(bitmaps[position]);
            container.addView(imageView);
            mImageViews[position]=imageView;
            return imageView;
        }

        /**
         * position 将要被删除的索引
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mImageViews[position]);
        }
    }
}
