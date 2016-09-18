package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by gengbaolong on 2016/8/26.
 */
public class MapActivity extends AppCompatActivity{


    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    private String userId;
    //从数据库中获取用户数据
    private List<UserMessage> userMessageList;

    private MapView mMapView;
    private BaiduMap mBaiduMap;


    private HeatMap heatmap;
    private boolean isDestroy;
    private boolean isOpenHeatMap;

//    private AMap mAMap;

    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),
            Color.argb(255 / 3 * 2, 0, 255, 0),
            Color.rgb(125, 191, 0),
            Color.rgb(185, 71, 0),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = { 0.0f,
            0.10f, 0.20f, 0.60f, 1.0f };

//    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(
//            ALT_HEATMAP_GRADIENT_COLORS, ALT_HEATMAP_GRADIENT_START_POINTS);
//
//
//
    HashMap<Marker,UserMessage> hashMap = new HashMap<Marker,UserMessage>();

    private final String USER_AGE = "userAge";
    private final String USER_AVATAR = "userAvatar";
    private final String USER_GENDER = "userGender";
    private final String USER_NICK = "userNick";
    private final String USER_NODE_ID = "nodeId";
    private final String USER_ID = "userId";

    /**
     * 定位
     *
     */

    private double longitude = 0;
    private double latitude = 0;


    //定位
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private boolean isFirstIn = true;//用户是否是第一次进入
    private MyLocationConfiguration.LocationMode mLocationMode;//定位模式

    private BitmapDescriptor mIconLocation;//自定义图标
    private MyLocationConfiguration myConfiguration;

    // 覆盖物相关
    private BitmapDescriptor mMarker;
    private ImageView map_switch;

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            latitude= bdLocation.getLatitude();
            longitude=bdLocation.getLongitude();
            String address = bdLocation.getAddrStr();
            Log.e("定位结果 ", " 经度:" + longitude + " 纬度:" + latitude + " 地址:" + address);//经度:121.605259 纬度:31.21449 地址:中国上海市浦东新区郭守敬路498号-14号楼-1楼

            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
            mBaiduMap.setMyLocationData(locData);

            LatLng lll = new LatLng(latitude,longitude);

            //得到定位结果后设置自定义的图标
            myConfiguration= new MyLocationConfiguration(mLocationMode,true,mIconLocation);
            mBaiduMap.setMyLocationConfigeration(myConfiguration);

            mMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);
            // 图标
            OverlayOptions options = new MarkerOptions().position(lll).icon(mMarker)
                    .zIndex(5);
            Marker marker = (Marker) mBaiduMap.addOverlay(options);

            centerToMyLocation();//移动到我的位置
            double lat_v1 = 0;
            double lon_v1 = 0 ;
                    if(userMessageList.size()>0){
                        for (int i = 0; i < userMessageList.size(); i++) {
                            if(i%2==0){
                                //偶数
                                lat_v1= Double.parseDouble(userMessageList.get(i).lattitude)+0.000047*(i+1);
                                lon_v1 = Double.parseDouble(userMessageList.get(i).longitude);
                            }else{
                                lat_v1= Double.parseDouble(userMessageList.get(i).lattitude);
                                lon_v1 = Double.parseDouble(userMessageList.get(i).longitude)+0.000047*(i+1);
                            }
                            int chatIcon = BleApplication.iconMap.get(userMessageList.get(i).userAvatar);
                            Marker marker3 = setMarker(lat_v1, lon_v1,"",chatIcon);
                            hashMap.put(marker3,userMessageList.get(i));
                        }
                    }

        }
    }



    public Marker setMarker(double latitude, double longitude,String address,int icon) {


        LatLng latLng = new LatLng(latitude, longitude);

        if (icon != 0) {

            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;//只读边,不读内容

            BitmapFactory.decodeResource(getResources(), icon, newOpts);

            newOpts.inJustDecodeBounds = false;
            //获取这个图片的宽和高
            int width = newOpts.outWidth;
            int height = newOpts.outHeight;

            //定义预转换成的图片的宽度和高度
            int newWidth = 100;
            int newHeight = 100;

//            //计算缩放率，新尺寸除原始尺寸
//            float scaleWidth = ((float) newWidth) / width;
//            float scaleHeight = ((float) newHeight) / height;
//
//            newOpts.inPurgeable = true;// 同时设置才会有效
//            newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收
//
//
//            // 创建操作图片用的matrix对象
//            Matrix matrix = new Matrix();
//
//            // 缩放图片动作
//            matrix.postScale(scaleWidth, scaleHeight);
//            // 创建新的图片
//            Bitmap destBitmap = Bitmap.createBitmap(srcBit, 0, 0,
//                    width, height, matrix, true);



//            int inSampleSize = 1;
//            if (height > newHeight || width > newWidth) {
//                // 计算出实际宽高和目标宽高的比率
//                final int heightRatio = Math.round((float) height / (float) newHeight);
//                final int widthRatio = Math.round((float) width / (float) newWidth);
//                // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
//                // 一定都会大于等于目标的宽和高。
//                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//            }

            newOpts.inSampleSize = 3;

            Bitmap srcBit = BitmapFactory.decodeResource(getResources(), icon,newOpts);


            BitmapDescriptor mMarker2 = BitmapDescriptorFactory.fromBitmap(srcBit);

            // 图标
            OverlayOptions options = new MarkerOptions().position(latLng).icon(mMarker2)
                    .zIndex(5);
            Marker marker2 = (Marker) mBaiduMap.addOverlay(options);

            return marker2;

        }
        return null;
    }

    private void centerToMyLocation() {

        LatLng latLng=new LatLng(latitude,longitude);

        MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);

        mBaiduMap.setMapStatus(msu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        initView();

        initLocation();

        userId = UserInfo.initUserId(userId, BleApplication.getContext());
        //从数据库中获取用户数据
        userMessageList = bleDBDao.findAllUsers(userId);

        initListener();
    }

    private void initListener() {

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(hashMap!=null && hashMap.size()>0) {
                    UserMessage userMessage = hashMap.get(marker);
                    if(userMessage!=null){
                        Intent intent = new Intent(MapActivity.this, ChatActivity.class);
                        intent.putExtra(USER_AGE, userMessage.userAge);
                        intent.putExtra(USER_AVATAR, userMessage.userAvatar);
                        intent.putExtra(USER_GENDER, userMessage.userGender);
                        intent.putExtra(USER_NICK, userMessage.userNick);
                        intent.putExtra(USER_NODE_ID, userMessage.nodeId);
                        intent.putExtra(USER_ID, userMessage.userId);
                        startActivity(intent);
                    }
                }
                return false;
            }
        });



        map_switch.setEnabled(true);
        map_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(5));
                if(!isOpenHeatMap){
                    isOpenHeatMap = true;
                    addHeatMap();
                }else{
                    isOpenHeatMap = false;
                    heatmap.removeHeatMap();
                }
            }
        });
    }

    private void addHeatMap() {

        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (!isDestroy) {
                    mBaiduMap.addHeatMap(heatmap);
                }
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<LatLng> data = getLocations();
                //将自己的位置放入所写定点的集合中
                LatLng selfLatLng = new LatLng(latitude,longitude);
                data.add(selfLatLng);
                heatmap = new HeatMap.Builder().data(data).build();
                h.sendEmptyMessage(0);
            }
        }.start();
    }


    private List<LatLng> getLocations() {
        List<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(R.raw.locations);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array;
        try {
            array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                double lat = object.getDouble("lat");
                double lng = object.getDouble("lng");
                list.add(new LatLng(lat, lng));
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }


    private void initLocation() {

        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;//默认是普通模式
        mLocationClient = new LocationClient(getApplicationContext());

        myLocationListener = new MyLocationListener();

        mLocationClient.registerLocationListener(myLocationListener);

        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");//坐标类型,"bd09ll"能与百度地图很好的融合
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(0);//定位一次

        mLocationClient.setLocOption(option);

        //初始化定位图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.icon_openmap_mark);

        //开启定位
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }
    }


    private void initView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map_view);

        map_switch = (ImageView) findViewById(R.id.map_switch);

        mBaiduMap = mMapView.getMap();

        //地图放大比例
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);

    }

    //    //声明AMapLocationClient类对象
//    public AMapLocationClient mLocationClient = null;
//    //声明定位回调监听器
//    public AMapLocationListener mLocationListener = new AMapLocationListener(){
//        @Override
//        public void onLocationChanged(AMapLocation amapLocation) {
//            if (amapLocation != null) {
//                if (amapLocation.getErrorCode() == 0) {
//                    //可在其中解析amapLocation获取相应内容。
//                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
//                    double latitude = amapLocation.getLatitude();//获取纬度
//                    double longitude = amapLocation.getLongitude();//获取经度
//                    amapLocation.getAccuracy();//获取精度信息
//                    String address = amapLocation.getAddress();
//                    Log.e("定位结果 ", userMessageList.size()+"  经度:" + longitude + " 纬度:" + latitude + " 地址:" + address);//经度:121.598927 纬度:31.20867 地址:上海市浦东新区金科路靠近上海浦东软件园15号楼
//                    int icon = R.drawable.icon_openmap_mark;
//
//                    // 设置当前地图显示为当前位置
//                    setMarker(latitude, longitude, address, icon);
//                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
//
//                    double lat_v1 = 0;
//                    double lon_v1 = 0 ;
//                    if(userMessageList.size()>0){
//                        for (int i = 0; i < userMessageList.size(); i++) {
//                            if(i%2==0){
//                                //偶数
//                                lat_v1= Double.parseDouble(userMessageList.get(i).lattitude)+0.000047*(i+1);
//                                lon_v1 = Double.parseDouble(userMessageList.get(i).longitude);
//                            }else{
//                                lat_v1= Double.parseDouble(userMessageList.get(i).lattitude);
//                                lon_v1 = Double.parseDouble(userMessageList.get(i).longitude)+0.000047*(i+1);
//                            }
//                            int chatIcon = BleApplication.iconMap.get(userMessageList.get(i).userAvatar);
//                            Marker marker = setMarker(lat_v1, lon_v1,"",chatIcon);
//                            hashMap.put(marker,userMessageList.get(i));
//                        }
//                    }
//
//                }else {
//                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
//                    Log.e("AmapError", "location Error, ErrCode:"
//                            + amapLocation.getErrorCode() + ", errInfo:"
//                            + amapLocation.getErrorInfo());
//                }
//            }
//        }
//    };
//    private ImageView map_switch;
//
//
//    public static double add(double v1,double v2){
//        BigDecimal b1 = new BigDecimal(Double.toString(v1));
//        BigDecimal b2 = new BigDecimal(Double.toString(v2));
//        return b1.add(b2).doubleValue();
//    }
//
//
//
//
//    @NonNull
//    private Marker setMarker(double latitude, double longitude,String address,int icon) {
//
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(new LatLng(latitude, longitude));
//        markerOptions.visible(true);
//        if(icon!=0) {
//            Bitmap srcBit = BitmapFactory.decodeResource(getResources(), icon);
//            //获取这个图片的宽和高
//            int width = srcBit.getWidth();
//            int height = srcBit.getHeight();
//
//            //定义预转换成的图片的宽度和高度
//            int newWidth = 100;
//            int newHeight = 100;
//
//            //计算缩放率，新尺寸除原始尺寸
//            float scaleWidth = ((float) newWidth) / width;
//            float scaleHeight = ((float) newHeight) / height;
//
//            // 创建操作图片用的matrix对象
//            Matrix matrix = new Matrix();
//
//            // 缩放图片动作
//            matrix.postScale(scaleWidth, scaleHeight);
//            // 创建新的图片
//            Bitmap destBitmap = Bitmap.createBitmap(srcBit, 0, 0,
//                    width, height, matrix, true);
//
//            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(destBitmap);
//            markerOptions.icon(bitmapDescriptor);
//        }
//        return mAMap.addMarker(markerOptions);
//    }
//
//
//    //声明AMapLocationClientOption对象
//    public AMapLocationClientOption mLocationOption = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_map);
//        mMapView = (MapView) findViewById(R.id.map_view);
//
//        mMapView.onCreate(savedInstanceState);
//
//        initView();
//
//        //定位
//
//        //初始化定位
//        mLocationClient = new AMapLocationClient(MapActivity.this);
//
//        //设置定位回调监听
//        mLocationClient.setLocationListener(mLocationListener);
//
//        if(mAMap == null) {
//            mAMap = mMapView.getMap();
//        }
//
//        mAMap.setOnMarkerClickListener(this);
//        //初始化AMapLocationClientOption对象
//        mLocationOption = new AMapLocationClientOption();
//
//        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
//
//        mLocationOption.setOnceLocation(true);
//
//        //设置是否返回地址信息（默认返回地址信息）
//        mLocationOption.setNeedAddress(true);
//
//        //给定位客户端对象设置定位参数
//        mLocationClient.setLocationOption(mLocationOption);
//        //启动定位
//        mLocationClient.startLocation();
//
//        userId = UserInfo.initUserId(userId, BleApplication.getContext());
//        //从数据库中获取用户数据
//        userMessageList = bleDBDao.findAllUsers(userId);
//
//        initListener();
//
//    }
//
//    private void initListener() {
//
//        map_switch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //点击开启或者取消热力图
//                initDataAndHeatMap();
//            }
//        });
//    }
//
//
//    private void initDataAndHeatMap() {
//        LatLng[] latlngs = new LatLng[500];
//        double x = 39.904979;
//        double y = 116.40964;
//
//        for (int i = 0; i < 500; i++) {
//            double x_ = 0;
//            double y_ = 0;
//            x_ = Math.random() * 0.5 - 0.25;
//            y_ = Math.random() * 0.5 - 0.25;
//            latlngs[i] = new LatLng(x + x_, y + y_);
//        }
//        HeatmapTileProvider heatmapTileProvider = new HeatmapTileProvider.Builder()
//                .data(Arrays.asList(latlngs)).gradient(ALT_HEATMAP_GRADIENT)
//
//                .build();
//        mAMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));
//
//    }
//
//
//
//    private void initView() {
//
//        map_switch = (ImageView) findViewById(R.id.map_switch);
//    }
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
//        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
//        mMapView.onDestroy();
//
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //在activity执行onResume时执行mMapView.onResume (),实现地图生命周期管理
//        mMapView.onResume();
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
//        mMapView.onPause();
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
//        mMapView.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public boolean onMarkerClick(Marker marker) {
//
//        if(hashMap!=null && hashMap.size()>0) {
//            UserMessage userMessage = hashMap.get(marker);
//            if(userMessage!=null){
//                Intent intent = new Intent(MapActivity.this, ChatActivity.class);
//                intent.putExtra(USER_AGE, userMessage.userAge);
//                intent.putExtra(USER_AVATAR, userMessage.userAvatar);
//                intent.putExtra(USER_GENDER, userMessage.userGender);
//                intent.putExtra(USER_NICK, userMessage.userNick);
//                intent.putExtra(USER_NODE_ID, userMessage.nodeId);
//                intent.putExtra(USER_ID, userMessage.userId);
//                startActivity(intent);
//            }
//        }
//        return false;
//    }
}
