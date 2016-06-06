package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.GroupChatAdapater;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.FileMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.GroupChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageBodyType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.TextMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.ReceiveGroupMsgListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.model.Node;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.TUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.TimeUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import io.underdark.transport.Link;

/**
 * Created by Mark on 2016/4/13.
 */
public class GroupChatActivity extends AppCompatActivity implements View.OnClickListener, ReceiveGroupMsgListener {

    private RelativeLayout layout_backGroup;
    private ImageView iv_addGroup, iv_camera, iv_position, iv_pic, iv_showUserInfo;
    private EditText et_chatGroup;
    private Button btn_sendMsgGroup;
    private boolean visibility_Flag = false;
    private LinearLayout layout_view;
    private ListView lv_chatGroup;
    private String userSelfId;//用户自身
    private Node node;
    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    private GroupChatAdapater groupChatAdapater;
    private List<GroupChatMessage> mDataArrays = new ArrayList<GroupChatMessage>();
    public static final int REQUEST_CODE_LOCAL_IMAGE = 1;//图片
    public static final int REQUEST_CODE_SELECT_FILE = 2;//文件
    public static final int SELECT_A_PICTURE = 3;//4.4以下
    public static final int SELECET_A_PICTURE_AFTER_KIKAT = 4;//4.4以上
    public static final int GET_IMAGE_VIA_CAMERA=5;

    private String localTempImgDir;
    private String localTempImgFileName;


    /**
     * 每页加载20条数据
     */
    private int pageSize = 20;
    /**
     * 默认从第0条开始加载数据
     */
    private int startIndex = 0;
    private int mCount;
    private int firstItem=-1;


    private boolean sendOnce=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat);
        node = MainActivity.getNode();
        userSelfId = UserInfo.initUserId(userSelfId, BleApplication.getContext());
        initView();
        initAdapter();
        setClicklistener();
        //注册监听，则数据库数据更新时会通知聊天界面
        getContentResolver().registerContentObserver(
                Uri.parse("content://www.nexfi_ble_user_group.com"), true,
                new Myobserve(new Handler()));
    }


    private class Myobserve extends ContentObserver {
        public Myobserve(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {

            initAdapter();

            super.onChange(selfChange);
        }
    }


    private void initAdapter() {
        mCount=bleDBDao.getGroupCount();
        if (mDataArrays != null && mDataArrays.size() == 0) {
            // 初始化20条数据
            mDataArrays = bleDBDao.findPartGroupMsg(pageSize, startIndex);
            Collections.reverse(mDataArrays);
        }
        if(mDataArrays!=null && mDataArrays.size()>0) {
            groupChatAdapater = new GroupChatAdapater(GroupChatActivity.this, mDataArrays, userSelfId);
            lv_chatGroup.setAdapter(groupChatAdapater);
            lv_chatGroup.setSelection(mDataArrays.size() - 1);//直接定位到最底部
        }
    }

    private void setClicklistener() {
        layout_backGroup.setOnClickListener(this);
        iv_addGroup.setOnClickListener(this);
        btn_sendMsgGroup.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        iv_position.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        iv_showUserInfo.setOnClickListener(this);
        if(node!=null) {
            node.setReceiveGroupMsgListener(this);
        }
    }

    private void initView() {
        layout_backGroup = (RelativeLayout) findViewById(R.id.layout_backGroup);
        iv_addGroup = (ImageView) findViewById(R.id.iv_addGroup);
        et_chatGroup = (EditText) findViewById(R.id.et_chatGroup);
        btn_sendMsgGroup = (Button) findViewById(R.id.btn_sendMsgGroup);
        iv_pic = (ImageView) findViewById(R.id.iv_pic);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        iv_position = (ImageView) findViewById(R.id.iv_position);
        layout_view = (LinearLayout) findViewById(R.id.layout_viewGroup);
        lv_chatGroup = (ListView) findViewById(R.id.lv_chatGroup);
        iv_showUserInfo = (ImageView) findViewById(R.id.iv_showUserInfo);

        //TODO 2016/5/29
        lv_chatGroup.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 闲置状态(停止的时候调用)
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if(firstItem==0){
                            startIndex =mDataArrays.size() ;
                            if (startIndex >= mCount) {
                                return;
                            }
                            // 追加后面20条数据
                            List<GroupChatMessage> mLists=bleDBDao.findPartGroupMsg(pageSize, startIndex);
                            Collections.reverse(mLists);
                            mDataArrays.addAll(0, mLists);
                            groupChatAdapater = new GroupChatAdapater(GroupChatActivity.this, mDataArrays, userSelfId);
                            lv_chatGroup.setAdapter(groupChatAdapater);
                            lv_chatGroup.setSelection(mLists.size());
                        }

                        break;
                    // SCROLL_STATE_TOUCH_SCROLL触摸到屏幕的时候调用
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    // 惯性滑动
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItem=firstVisibleItem;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_backGroup:
                finish();
                break;
            case R.id.iv_addGroup:
                if (visibility_Flag) {
                    layout_view.setVisibility(View.GONE);
                    visibility_Flag = false;
                } else {
                    layout_view.setVisibility(View.VISIBLE);
                    visibility_Flag = true;
                }
                break;
            case R.id.btn_sendMsgGroup://发送消息
                sendOnce=true;
                sendGroupMsg();
                et_chatGroup.setText(null);
                break;
            case R.id.iv_pic://发图片
                Intent intentMain=new Intent(GroupChatActivity.this,MainImageActivity.class);
                startActivityForResult(intentMain, 0);
                break;
            case R.id.iv_camera:
                if (Build.VERSION.SDK_INT >= 23) {

                    if(!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                        requestStoragePermission();
                    }


                    if (!(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                        requestCameraPermission();
                    }
                }else{
                    cameraToSend();
                }
                break;
            case R.id.iv_position:
                showToast();
                break;
            case R.id.iv_showUserInfo:
                startActivity(new Intent(this,GroupChatUserActivity.class));
                break;
        }
    }



    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE=2;


    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestStoragePermission(){
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE){
            int grantResult = grantResults[0];
            if(grantResult == PackageManager.PERMISSION_GRANTED){//Permission Granted

            }else{// Permission Denied
                Toast.makeText(this, "内存卡Permission Denied", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if (requestCode == REQUEST_PERMISSION_CAMERA_CODE) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            if(granted){
                cameraToSend();
            }

        }
    }

    private void cameraToSend() {
        //先验证手机是否有sdcard
        String status= Environment.getExternalStorageState();
        if(status.equals(Environment.MEDIA_MOUNTED))
        {
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                localTempImgDir = "Nexfi_ble";
                localTempImgFileName = "camera_"+ timeStamp + ".jpg";
                File dir=new File(Environment.getExternalStorageDirectory() + "/"+ localTempImgDir);
                if(!dir.exists())dir.mkdirs();

                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f=new File(dir, localTempImgFileName);//localTempImgDir和localTempImageFileName是自己定义的名字
                Uri u=Uri.fromFile(f);
                intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                startActivityForResult(intent, GET_IMAGE_VIA_CAMERA);
            } catch (ActivityNotFoundException e) {
                // TODO Auto-generated catch block
                Toast.makeText(GroupChatActivity.this, "没有找到储存目录", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(GroupChatActivity.this, "没有储存卡",Toast.LENGTH_LONG).show();
        }
    }

    private void sendGroupMsg() {
        String contString = et_chatGroup.getText().toString();
        if (contString.length() > 0) {
            GroupChatMessage groupChatMessage=new GroupChatMessage();
            groupChatMessage.messageType=MessageType.eMessageType_AllUserChat;
            groupChatMessage.messageBodyType= MessageBodyType.eMessageBodyType_Text;
            groupChatMessage.msgId=UUID.randomUUID().toString();
            groupChatMessage.timeStamp=TimeUtils.getNowTime();

            UserMessage user = bleDBDao.findUserByUserId(userSelfId);
            groupChatMessage.userMessage=user;
            TextMessage textMessage=new TextMessage();
            textMessage.fileData=contString;
            groupChatMessage.textMessage=textMessage;

            Gson gson = new Gson();
            String json= gson.toJson(groupChatMessage);
            byte[] send_text_data=json.getBytes();
            if(node!=null) {
                node.broadcastFrame(send_text_data);
                bleDBDao.addGroupTextMsg2(groupChatMessage);
                setAdapter(groupChatMessage);
            }
        }
    }



    /**
     * 根据图片路径发送图片
     *
     * @param filePath
     */
    private void sendImageMsg(String filePath) {
        File fileToSend = FileTransferUtils.scal(filePath);
        byte[] send_file_size=(""+fileToSend.length()).getBytes();
        String fileSize= Base64.encodeToString(send_file_size, Base64.DEFAULT);//文件长度
        byte[] bys_send_data = null;
        try {
            bys_send_data = FileTransferUtils.getBytesFromFile(fileToSend);
        } catch (Exception e) {
            BleApplication.getExceptionLists().add(e);
            BleApplication.getCrashHandler().saveCrashInfo2File(e);
            e.printStackTrace();
        }
        if (null == bys_send_data) {
            return;
        }
        String tFileData = Base64.encodeToString(bys_send_data, Base64.DEFAULT);
        String fileName = fileToSend.getName();//文件名
        if(node==null) {
            return;
        }
        ArrayList<Link> links = node.getLinks();
        if (links != null && links.size()>0) {
            GroupChatMessage groupChatMessage = new GroupChatMessage();
            groupChatMessage.messageType = MessageType.eMessageType_AllUserChat;
            groupChatMessage.messageBodyType=MessageBodyType.eMessageBodyType_Image;
            groupChatMessage.timeStamp = TimeUtils.getNowTime();
            groupChatMessage.msgId=UUID.randomUUID().toString();

            UserMessage user = bleDBDao.findUserByUserId(userSelfId);
            groupChatMessage.userMessage=user;

            FileMessage fileMessage = new FileMessage();
            fileMessage.fileSize = fileSize;
            fileMessage.fileData=tFileData;
            fileMessage.fileName = fileName;
            fileMessage.filePath = filePath;
            groupChatMessage.fileMessage=fileMessage;

            Gson gson=new Gson();
            String json=gson.toJson(groupChatMessage);
            byte[] send_file_data=json.getBytes();
            node.broadcastFrame(send_file_data);
            bleDBDao.addGroupTextMsg2(groupChatMessage);//geng
            setAdapter(groupChatMessage);
        }
    }



    private void showToast() {
        Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onReceiveGroupMsg(Object obj) {
        GroupChatMessage groupChatMessage= (GroupChatMessage) obj;
        if(node==null){
            return;
        }
        if(groupChatMessage.messageBodyType==MessageBodyType.eMessageBodyType_Text){
            TextMessage textMessage= groupChatMessage.textMessage;
            setAdapter(groupChatMessage);//设置适配器
            //转发消息
            if(node.getLinks().size()>1) {
                Gson gson=new Gson();
                String json=gson.toJson(groupChatMessage);
                final byte[] send_file_data = json.getBytes();
                if(groupChatMessage.userMessage==null){
                    return;
                }
                final Link link2 = node.getLink(groupChatMessage.userMessage.nodeId);
                if (node.getLinks().size() > 0) {
                    for (Link link1 : node.getLinks()) {
                        if (link2 != link1) {
                            link1.sendFrame(send_file_data);
                        }
                    }
                }
            }
        }else if(groupChatMessage.messageBodyType==MessageBodyType.eMessageBodyType_Image){
            FileMessage fileMessage=groupChatMessage.fileMessage;
            setAdapter(groupChatMessage);//设置适配器
            //转发消息
            if(node.getLinks().size()>1) {
                Gson gson=new Gson();
                String json=gson.toJson(groupChatMessage);
                final byte[] send_file_data = json.getBytes();
                final Link link2 = node.getLink(groupChatMessage.userMessage.nodeId);
                if (node.getLinks().size() > 0) {
                    for (Link link1 : node.getLinks()) {
                        if (link2 != link1) {
                            link1.sendFrame(send_file_data);
                        }
                    }
                }
            }
        }

    }


    private void setAdapter(GroupChatMessage groupChatMessage) {
        mDataArrays.add(groupChatMessage);
        if(null==groupChatAdapater){
            groupChatAdapater=new GroupChatAdapater(GroupChatActivity.this, mDataArrays,userSelfId);
            lv_chatGroup.setAdapter(groupChatAdapater);
        }
        groupChatAdapater.notifyDataSetChanged();
        if (mDataArrays.size() > 0) {
            lv_chatGroup.setSelection(mDataArrays.size() - 1);// 最后一行
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String selectPath = null;
        if (requestCode == SELECT_A_PICTURE) {//4.4以下
            if (resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                selectPath = FileUtils.getPath(this, selectedImage);
                sendImageMsg(selectPath);
            }
        } else if (requestCode == SELECET_A_PICTURE_AFTER_KIKAT) {//4.4以上
            if (resultCode == RESULT_OK && null != data) {
                selectPath = TUtils.getPath(this, data.getData());
                if (null != selectPath) {
                    sendImageMsg(selectPath);
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_FILE) {
            if (data != null) {
                Uri uri = data.getData();
                if (null != uri) {
                    String select_file_path = FileUtils.getPath(this, uri);
                    if (select_file_path != null) {
//                        sendFileMsg(select_file_path);
                    }
                }
            }
        }else if (requestCode == 0) {
            if (null != data) {
                String seP=data.getStringExtra("RESULT");
                sendImageMsg(seP);
            }
        }else if (requestCode == GET_IMAGE_VIA_CAMERA) {
            File f=new File(Environment.getExternalStorageDirectory()
                    +"/"+localTempImgDir+"/"+localTempImgFileName);
            File newF=FileTransferUtils.scal(f.getAbsolutePath());
            try {
                Uri u =
                        Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
                                newF.getAbsolutePath(), null, null));
                //u就是拍摄获得的原始图片的uri
                String select_camera_path = FileUtils.getPath(GroupChatActivity.this, u);
                sendImageMsg(select_camera_path);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
