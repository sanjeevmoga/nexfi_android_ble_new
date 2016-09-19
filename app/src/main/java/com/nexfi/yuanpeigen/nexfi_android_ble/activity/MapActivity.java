package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ZoomControls;

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

import java.util.HashMap;
import java.util.List;

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
    private boolean isOpenHeatMap;


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
    private boolean isDestroy = false;
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
                if (!isOpenHeatMap) {
                    isOpenHeatMap = true;
                    mBaiduMap.setBaiduHeatMapEnabled(true);//开启热力图
                } else {
                    isOpenHeatMap = false;
                    mBaiduMap.setBaiduHeatMapEnabled(false);//关闭热力图
                }
            }
        });
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

        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }

        //地图放大比例
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);

    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume (),实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        mLocationClient.stop();//停止定位后，本地定位服务并不会被销毁
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
}
