package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gengbaolong on 2016/8/26.
 */
public class MapActivity extends AppCompatActivity implements AMap.OnCameraChangeListener, AMap.OnMarkerClickListener {


    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    private String userId;
    //从数据库中获取用户数据
    private List<UserMessage> userMessageList;

    private MapView mMapView;

    private AMap mAMap;

    HashMap<Marker,UserMessage> hashMap = new HashMap<Marker,UserMessage>();

    private final String USER_AGE = "userAge";
    private final String USER_AVATAR = "userAvatar";
    private final String USER_GENDER = "userGender";
    private final String USER_NICK = "userNick";
    private final String USER_NODE_ID = "nodeId";
    private final String USER_ID = "userId";



    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    double latitude = amapLocation.getLatitude();//获取纬度
                    double longitude = amapLocation.getLongitude();//获取经度
                    amapLocation.getAccuracy();//获取精度信息
                    String address = amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                    Log.e("定位结果 ", userMessageList.size()+"  经度:" + longitude + " 纬度:" + latitude + " 地址:" + address);//经度:121.598927 纬度:31.20867 地址:上海市浦东新区金科路靠近上海浦东软件园15号楼
                    int icon = R.drawable.icon_openmap_mark;

                    // 设置当前地图显示为当前位置
                    mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),15));
                    setMarker(latitude, longitude, address, icon);
                    Log.e("MapActivity ", "  -------------设置自己的位置---------  ");
                    double lat_v1 = 0;
                    double lon_v1 = 0 ;
                    if(userMessageList.size()>0){
                        Log.e("MapActivity ","  --------------这里得到的位置信息---------  "+userMessageList.size());
                        for (int i = 0; i < userMessageList.size(); i++) {
                            if(i%2==0){
                                Log.e("MapActivity ","  --------------得到的偶数 i = ---------"+i);
                                //偶数
                                lat_v1= Double.parseDouble(userMessageList.get(i).lattitude)+0.000047*(i+1);
                                lon_v1 = Double.parseDouble(userMessageList.get(i).longitude);
                                Log.e("MapActivity ","  --------------得到的偶数 i = ---------"+i);
                            }else{
                                Log.e("MapActivity ","  --------------得到的奇数 i = ---------"+i);
                                lat_v1= Double.parseDouble(userMessageList.get(i).lattitude);
                                lon_v1 = Double.parseDouble(userMessageList.get(i).longitude)+0.000047*(i+1);
                            }
                            int chatIcon = BleApplication.iconMap.get(userMessageList.get(i).userAvatar);
                            Log.e("MapActivity ","  --------------这里得到的用户头像---------  "+chatIcon);
                            Marker marker = setMarker(lat_v1, lon_v1,"",chatIcon);
                            hashMap.put(marker,userMessageList.get(i));
                        }
                    }

                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };


    public static double add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }


    /** * 调节地图到正好放置查询范围的所有点 * @param centerLatLng 中心点 * @param range 查询范围（米） */
    private void adjustCamera(LatLng centerLatLng,int range) {
        //http://www.eoeandroid.com/blog-1107295-47621.html
        //当前缩放级别下的比例尺
        //"每像素代表" + scale + "米"
        float scale = mAMap.getScalePerPixel();
        //代表range（米）的像素数量
        int pixel = Math.round(range / scale);
        //小范围，小缩放级别（比例尺较大），有精度损失
        Projection projection = mAMap.getProjection();
        //将地图的中心点，转换为屏幕上的点
        Point center = projection.toScreenLocation(centerLatLng);
        //获取距离中心点为pixel像素的左、右两点（屏幕上的点
        Point right = new Point(center.x + pixel, center.y);
        Point left = new Point(center.x - pixel, center.y);

        //将屏幕上的点转换为地图上的点
        LatLng rightLatlng = projection.fromScreenLocation(right);
        LatLng LeftLatlng = projection.fromScreenLocation(left);

        LatLngBounds bounds = LatLngBounds.builder().include(rightLatlng).include(LeftLatlng).build();
        //bounds.contains();

        mAMap.getMapScreenMarkers();

        //调整可视范围
        mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds.builder().include(rightLatlng).include(LeftLatlng).build(), 10));
    }





    @NonNull
    private Marker setMarker(double latitude, double longitude,String address,int icon) {
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),15));

        adjustCamera(new LatLng(latitude,longitude),20);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude));
        markerOptions.visible(true);
        if(icon!=0) {
            Bitmap srcBit = BitmapFactory.decodeResource(getResources(), icon);
            //获取这个图片的宽和高
            int width = srcBit.getWidth();
            int height = srcBit.getHeight();

            //定义预转换成的图片的宽度和高度
            int newWidth = 100;
            int newHeight = 100;

            //计算缩放率，新尺寸除原始尺寸
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;

            // 创建操作图片用的matrix对象
            Matrix matrix = new Matrix();

            // 缩放图片动作
            matrix.postScale(scaleWidth, scaleHeight);
            // 创建新的图片
            Bitmap destBitmap = Bitmap.createBitmap(srcBit, 0, 0,
                    width, height, matrix, true);

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(destBitmap);
            markerOptions.icon(bitmapDescriptor);
        }
        return mAMap.addMarker(markerOptions);
    }


    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);


        //定位

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());

        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        if(mAMap == null) {
            mAMap = mMapView.getMap();
        }

        mAMap.setOnCameraChangeListener(this);
        mAMap.setOnMarkerClickListener(this);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);

        mLocationOption.setOnceLocation(true);

        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();


        userId = UserInfo.initUserId(userId, BleApplication.getContext());
        //从数据库中获取用户数据
        userMessageList = bleDBDao.findAllUsers(userId);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();

    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

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
}
