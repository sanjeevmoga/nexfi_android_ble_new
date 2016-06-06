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
//---------------------------------------------------------------------------------
//    private PhotoViewAttacher mAttacher;
//    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
//    ViewPagerFixed viewpager;
//    int firstPosition=0;
//    String userId;
//    private List<SingleChatMessage> mAllDataArrays = new ArrayList<SingleChatMessage>();
//    private List<SingleChatMessage> mImageDataArrays = new ArrayList<SingleChatMessage>();
//
//    List<ImageView> imageViews = new ArrayList<ImageView>();
//
//    private int window_width, window_height;// 控件宽度
//    private int state_height;// 状态栏的高度
//
//    private ImageView[] mImageViews=null;
//    Bitmap[] bitmaps=null;
//    private int width;
//    private int height;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.image_activity);
//
//        Log.e("TAG", "-----------------onCreate-----------------点击传过来的firstPosition");
//        /** 获取可見区域高度 **/
//        WindowManager manager = getWindowManager();
//        window_width = manager.getDefaultDisplay().getWidth();
//        window_height = manager.getDefaultDisplay().getHeight();
//
//        DisplayMetrics metric = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metric);
//
//        width = metric.widthPixels;
//        height = metric.heightPixels;
////        userId= UserInfo.initUserId(userId, BleApplication.getContext());
//        userId=SharedPreferencesUtils.getString(BleApplication.getContext(),"CHAT_ID", UUID.randomUUID().toString());
//        mAllDataArrays = bleDBDao.findMsgByChatId(userId);
//        Log.e("TAG",userId+ "---------userId--------onCreat----mAllDataArrays-------"+mAllDataArrays.size());
//        for (int i = 0; i <mAllDataArrays.size() ; i++) {
//            int type=mAllDataArrays.get(i).messageBodyType;
//            if(type == MessageBodyType.eMessageBodyType_Image){
//                mImageDataArrays.add(mAllDataArrays.get(i));
//            }
//        }
//        Log.e("TAG", "-----------------onCreat----mImageDataArrays-------"+mImageDataArrays.size());
//        Intent intent=getIntent();
//        int page=intent.getIntExtra("page", 0);
//        for (int i = 0; i <mImageDataArrays.size() ; i++) {
//            if(mImageDataArrays.get(i)==mAllDataArrays.get(page)){
//                firstPosition=i;
//                Log.e("TAG", firstPosition + "-----点击传过来的firstPosition");
//                break;
//            }
//        }
//        viewpager = (ViewPagerFixed) findViewById(R.id.viewpager);
//        initAllImages();
//
//        viewpager.setAdapter(new MyPageAdapter());
//        viewpager.setCurrentItem(firstPosition);
//    }
//
//    private void init() {
//
//        /** 获取可見区域高度 **/
//        WindowManager manager = getWindowManager();
//        window_width = manager.getDefaultDisplay().getWidth();
//        window_height = manager.getDefaultDisplay().getHeight();
//    }
//
//    private void initAllImages() {
//        bitmaps=new Bitmap[mImageDataArrays.size()];
//        mImageViews=new ImageView[bitmaps.length];
//        for (int i = 0; i<mImageDataArrays.size() ; i++) {
//            FileMessage fileMessage = mImageDataArrays.get(i).fileMessage;
//            byte[] bys_send = Base64.decode(fileMessage.fileData, Base64.DEFAULT);
////            Bitmap bitmap = FileTransferUtils.getPicFromBytes(bys_send);
//            Bitmap bitmap=FileTransferUtils.decodeSampledBitmapFromResource(bys_send, width, height);
//            Log.e("TAG", bitmap + "-----==========================================-点击传过来的bitmap");
//            bitmaps[i]=bitmap;
////            PhotoView imageView=new PhotoView(getApplicationContext());
////            imageView.setImageBitmap(bitmap);
////            PhotoViewAttacher mAttacher=new PhotoViewAttacher(imageView);
////            imageViews.add(imageView);
//        }
//
//    }
//
//    private class MyPageAdapter extends PagerAdapter {
//
//
//
//        // 决定viewpager的长度
//        @Override
//        public int getCount() {
//            // TODO Auto-generated method stub
//            return mImageViews.length;//Integer.MAX_VALUE
////            return imageViews.size();
//        }
//
//        // 决定是否使用缓存,true的话就使用缓存
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            // TODO Auto-generated method stub
//            return view == object;
//        }
//
//        /**
//         * position 将要初始化的索引
//         */
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//
//            ZoomImageView imageView=new ZoomImageView(BleApplication.getContext());
//            imageView.setImageBitmap(bitmaps[position]);
//            Log.e("TAG", bitmaps[position].getHeight() + "--图片尺寸--" + bitmaps[position].getWidth());
//            Log.e("TAG", imageView.getHeight() + "--imageView尺寸--" + imageView.getWidth());
//            container.addView(imageView);
//            mImageViews[position]=imageView;
//            return imageView;
//
//
////            container.addView(imageViews.get(position));
////            return imageViews.get(position);
//
//        }
//
//        /**
//         * position 将要被删除的索引
//         */
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView(mImageViews[position]);
//        }
//
//    }

}
