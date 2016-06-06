package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.ChatMessageAdapater;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.FileMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageBodyType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.MessageType;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.SingleChatMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.TextMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.VoiceMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.listener.ReceiveTextMsgListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.model.Node;
import com.nexfi.yuanpeigen.nexfi_android_ble.operation.TextMsgOperation;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.Debug;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileTransferUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.FileUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.MediaManager;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.SharedPreferencesUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.TUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.TimeUtils;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;
import com.nexfi.yuanpeigen.nexfi_android_ble.view.AudioRecordButton;

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
 * Created by Mark on 2016/4/14.
 */
public class ChatActivity extends AppCompatActivity implements View.OnClickListener, ReceiveTextMsgListener, Runnable {

    private RelativeLayout layout_backPrivate;
    private ImageView iv_add_Private, iv_camera, iv_position, iv_pic, iv_editPrivate, iv_changePrivate;
    private EditText et_chatPrivate;
    private Button btn_sendMsgPrivate;
    private LinearLayout layout_view;
    private AudioRecordButton recordButton;
    private ListView lv_chatPrivate;
    private TextView textView_private;

    private final String USER_SEX = "1";
    private final String USER_AGE = "userAge";
    private final String USER_AVATAR = "userAvatar";
    private final String USER_GENDER = "userGender";
    private final String USER_NICK = "userNick";
    private final String USER_NODE_ID = "nodeId";
    private final String USER_ID = "userId";

    private String userNick, userGender;
    private int userAge;
    private String userAvatar;

    private boolean visibility_Flag_add = false, visibility_Flag_edit = false;

    private String nodeId;
    private Node node;
    private Link link;
    private String userId;
    private String userSelfId;//用户自身
    TextMsgOperation textMsgOperation;
    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    public static final int REQUEST_CODE_LOCAL_IMAGE = 1;//图片
    public static final int REQUEST_CODE_SELECT_FILE = 2;//文件
    public static final int SELECT_A_PICTURE = 3;//4.4以下
    public static final int SELECET_A_PICTURE_AFTER_KIKAT = 4;//4.4以上
    public static final int GET_IMAGE_VIA_CAMERA = 5;

    private ChatMessageAdapater chatMessageAdapater;
    private List<SingleChatMessage> mDataArrays = new ArrayList<SingleChatMessage>();

    private AlertDialog mAlertDialog;

    private Handler handler;
    private String selectImagePath;
    private Intent intentData;
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
    private int currentPage = 1; //默认在第一页
    private int totalPageCount = 1;
    private int lvIndex;

    private View viewanim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        node = MainActivity.getNode();
        textMsgOperation = new TextMsgOperation();
        userSelfId = UserInfo.initUserId(userSelfId, BleApplication.getContext());
        //c8f01aea-f0be-4620-8b8d-435e65966e74----聊天界面的
        initIntentData();
        initView();
        initAdapter();
        setClicklistener();
        //注册监听，则数据库数据更新时会通知聊天界面
        getContentResolver().registerContentObserver(
                Uri.parse("content://www.nexfi_ble_user_single.com"), true,
                new Myobserve(new Handler()));

    }


    @Override
    protected void onPause() {
        super.onPause();
        MediaManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaManager.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.release();
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

    private void initDialogConnectedStatus() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_connected, null);
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(v);
        mAlertDialog.setCancelable(false);
        handler = new Handler();
        handler.postDelayed(this, 1500);
    }


    private void initAdapter() {
        mCount = bleDBDao.getCount(userId);
        //计算总页数
        totalPageCount = (mCount + pageSize - 1) / pageSize;

        // 判断是否是第一次加载数据
        if (mDataArrays != null && mDataArrays.size() == 0) {
            // 初始化20条数据
            mDataArrays = bleDBDao.findPartMsgByChatId(userId, pageSize, startIndex);
            Collections.reverse(mDataArrays);
        }


        if (mDataArrays != null && mDataArrays.size() > 0) {

            chatMessageAdapater = new ChatMessageAdapater(ChatActivity.this, mDataArrays, userSelfId);
            lv_chatPrivate.setAdapter(chatMessageAdapater);
            lv_chatPrivate.setSelection(mDataArrays.size() - 1);//直接定位到最底部
            if (chatMessageAdapater != null) {
                chatMessageAdapater.notifyDataSetChanged();
            }
        }
    }


    private void setClicklistener() {
        layout_backPrivate.setOnClickListener(this);
        iv_add_Private.setOnClickListener(this);
        btn_sendMsgPrivate.setOnClickListener(this);
        iv_changePrivate.setOnClickListener(this);
        iv_editPrivate.setOnClickListener(this);
        iv_pic.setOnClickListener(this);
        iv_position.setOnClickListener(this);
        iv_camera.setOnClickListener(this);

        if (node != null) {
            link = node.getLink(nodeId);
            node.setReceiveTextMsgListener(this);
        }

        lv_chatPrivate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final boolean isSend = mDataArrays.get(position).userMessage.userId.equals(userSelfId);
                if (mDataArrays.get(position).messageBodyType == MessageBodyType.eMessageBodyType_Voice) {
                    // 播放动画
                    if (viewanim != null) {//让第二个播放的时候第一个停止播放
                        if (isSend) {
                            viewanim.setBackgroundResource(R.drawable.adj_send);
                        } else {
                            viewanim.setBackgroundResource(R.drawable.adj_receive);
                        }
                        viewanim = null;
                    }
                    viewanim = view.findViewById(R.id.id_recorder_anim);
                    if (isSend) {
                        viewanim.setBackgroundResource(R.drawable.play);
                    } else {
                        viewanim.setBackgroundResource(R.drawable.play_receive);
                    }
                    AnimationDrawable drawable = (AnimationDrawable) viewanim
                            .getBackground();
                    drawable.start();

                    // 播放音频
                    MediaManager.playSound(mDataArrays.get(position).voiceMessage.filePath,
                            new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    if (isSend) {
                                        viewanim.setBackgroundResource(R.drawable.adj_send);
                                    } else {
                                        viewanim.setBackgroundResource(R.drawable.adj_receive);
                                    }
                                }
                            });
                }
            }
        });

        et_chatPrivate.addTextChangedListener(new TextWatcher() {
                                                  @Override
                                                  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                  }

                                                  @Override
                                                  public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                      if (TextUtils.isEmpty(et_chatPrivate.getText())) {
                                                          btn_sendMsgPrivate.setVisibility(View.INVISIBLE);
                                                          iv_add_Private.setVisibility(View.VISIBLE);
                                                      } else {
                                                          btn_sendMsgPrivate.setVisibility(View.VISIBLE);
                                                          iv_add_Private.setVisibility(View.INVISIBLE);
                                                      }
                                                  }

                                                  @Override
                                                  public void afterTextChanged(Editable s) {
                                                  }
                                              }

        );
        recordButton.setAudioFinishRecorderListener(new AudioRecordButton.AudioFinishRecorderListener()

                                                    {
                                                        @Override
                                                        public void onFinished(float seconds, String filePath) {

                                                            sendVoiceMsg(seconds, filePath);
                                                        }
                                                    }

        );
    }


    /**
     * 发送语音
     *
     * @param seconds
     * @param filePath
     */
    private void sendVoiceMsg(float seconds, String filePath) {

        SingleChatMessage singleChatMessage = new SingleChatMessage();
        singleChatMessage.messageType = MessageType.eMessageType_SingleChat;
        singleChatMessage.messageBodyType = MessageBodyType.eMessageBodyType_Voice;

        UserMessage user = bleDBDao.findUserByUserId(userSelfId);
        singleChatMessage.userMessage = user;

        VoiceMessage voiceMessage = new VoiceMessage();
        voiceMessage.durational = seconds + "";
        File file = new File(filePath);
        byte[] voice_send = FileTransferUtils.getBytesFromFile(file);
        String voiceData = Base64.encodeToString(voice_send, Base64.DEFAULT);
        voiceMessage.fileData = voiceData;
        voiceMessage.filePath = filePath;
        singleChatMessage.voiceMessage = voiceMessage;
        setAdapter(singleChatMessage);
        Gson gson = new Gson();
        String json = gson.toJson(singleChatMessage);
        byte[] send_text_data = json.getBytes();
        Debug.debugLog("sendvoice", "--------语音已发送-1111111----");
        if (null != link) {
            link.sendFrame(send_text_data);
            Debug.debugLog("sendvoice", "--------语音已发送-----");
//            bleDBDao.addP2PTextMsg(singleChatMessage);//geng
//            setAdapter(singleChatMessage);
        }

    }


    int firstItem = -1;

    private void initView() {
        layout_backPrivate = (RelativeLayout) findViewById(R.id.layout_backPrivate);
        iv_add_Private = (ImageView) findViewById(R.id.iv_add_Private);
        iv_editPrivate = (ImageView) findViewById(R.id.iv_editPrivate);
        iv_changePrivate = (ImageView) findViewById(R.id.iv_changePrivate);
        et_chatPrivate = (EditText) findViewById(R.id.et_chatPrivate);
        btn_sendMsgPrivate = (Button) findViewById(R.id.btn_sendMsgPrivate);
        recordButton = (AudioRecordButton) findViewById(R.id.recordButton);
        iv_pic = (ImageView) findViewById(R.id.iv_pic);
        iv_camera = (ImageView) findViewById(R.id.iv_camera);
        iv_position = (ImageView) findViewById(R.id.iv_position);
        layout_view = (LinearLayout) findViewById(R.id.layout_viewPrivate);
        lv_chatPrivate = (ListView) findViewById(R.id.lv_chatPrivate);
        textView_private = (TextView) findViewById(R.id.textView_private);
        textView_private.setText(userNick);

        //TODO 2016/5/29
        lv_chatPrivate.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 闲置状态(停止的时候调用)
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        if (firstItem == 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {//不再滚动
                            startIndex = mDataArrays.size();
                            if (startIndex >= mCount) {
                                return;
                            }
                            // 追加后面20条数据
                            List<SingleChatMessage> mLists = bleDBDao.findPartMsgByChatId(userId, pageSize, startIndex);
                            Collections.reverse(mLists);
                            mDataArrays.addAll(0, mLists);
                            chatMessageAdapater = new ChatMessageAdapater(ChatActivity.this, mDataArrays, userSelfId);
                            lv_chatPrivate.setAdapter(chatMessageAdapater);
                            lv_chatPrivate.setSelection(mLists.size());
                        }

                        break;
                    // SCROLL_STATE_TOUCH_SCROLL触摸到屏幕的时候调用
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        lvIndex = view.getLastVisiblePosition();
                        break;
                    // 惯性滑动
                    case OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItem = firstVisibleItem;
            }
        });

    }

    private void initIntentData() {
        intentData = getIntent();
        userNick = intentData.getStringExtra(USER_NICK);
        userGender = intentData.getStringExtra(USER_GENDER);
        userAge = intentData.getIntExtra(USER_AGE, 18);
        userAvatar = intentData.getStringExtra(USER_AVATAR);
        nodeId = intentData.getStringExtra(USER_NODE_ID);
        userId = intentData.getStringExtra(USER_ID);
        SharedPreferencesUtils.saveString(BleApplication.getContext(), "CHAT_ID", userId);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_backPrivate:
                finish();
                break;
            case R.id.iv_add_Private:
                closeKeybord(et_chatPrivate, this);
                if (visibility_Flag_add) {
                    layout_view.setVisibility(View.GONE);
                    visibility_Flag_add = false;
                } else {
                    layout_view.setVisibility(View.VISIBLE);
                    visibility_Flag_add = true;
                }
                break;

            case R.id.iv_editPrivate:
                if (visibility_Flag_edit) {
                    openKeybord(et_chatPrivate, this);
                    iv_changePrivate.setVisibility(View.VISIBLE);
                    recordButton.setVisibility(View.INVISIBLE);
                    iv_editPrivate.setVisibility(View.INVISIBLE);
                    et_chatPrivate.setVisibility(View.VISIBLE);
                    visibility_Flag_edit = false;
                }
                break;

            case R.id.iv_changePrivate:
                if (!visibility_Flag_edit) {
                    closeKeybord(et_chatPrivate, this);
                    iv_changePrivate.setVisibility(View.INVISIBLE);
                    recordButton.setVisibility(View.VISIBLE);
                    iv_editPrivate.setVisibility(View.VISIBLE);
                    et_chatPrivate.setVisibility(View.INVISIBLE);
                    visibility_Flag_edit = true;
                }
                break;

            case R.id.btn_sendMsgPrivate:
                link = node.getLink(nodeId);
                if (link != null) {
                    sendTextMsg();
                    et_chatPrivate.setText(null);
                } else {
                    initDialogConnectedStatus();
                }
                break;
            case R.id.iv_pic:
                if (!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    Toast.makeText(this, "没有内存卡", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent intentMain = new Intent(ChatActivity.this, MainImageActivity.class);
                    startActivityForResult(intentMain, 0);
                } catch (Exception e) {
                    Toast.makeText(this, "系统权限不允许", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_camera:
                if (Build.VERSION.SDK_INT >= 23) {

                    if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                        requestStoragePermission();
                    }

                    if (!(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                        requestCameraPermission();
                    }
                } else {
                    cameraToSend();
                }
                break;
            case R.id.iv_position:
                showToast();
                break;
        }
    }

    public void closeKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }


    public void openKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;


    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA_CODE);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            int grantResult = grantResults[0];
            if (grantResult == PackageManager.PERMISSION_GRANTED) {//Permission Granted

            } else {// Permission Denied
                Toast.makeText(this, "内存卡Permission Denied", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA_CODE) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                cameraToSend();
            }

        }
    }


    private void cameraToSend() {
        //先验证手机是否有sdcard
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                localTempImgDir = "Nexfi_ble";
                localTempImgFileName = "camera_" + timeStamp + ".jpg";
                File dir = new File(Environment.getExternalStorageDirectory() + "/" + localTempImgDir);
                if (!dir.exists()) dir.mkdirs();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(dir, localTempImgFileName);//localTempImgDir和localTempImageFileName是自己定义的名字
                Uri u = Uri.fromFile(f);
                intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, u);
                startActivityForResult(intent, GET_IMAGE_VIA_CAMERA);
            } catch (ActivityNotFoundException e) {
                // TODO Auto-generated catch block
                Toast.makeText(ChatActivity.this, "没有找到储存目录", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ChatActivity.this, "没有储存卡", Toast.LENGTH_LONG).show();
        }
    }


    private void showToast() {
        Toast.makeText(this, "即将上线，敬请期待", Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送文本消息
     */
    private void sendTextMsg() {
        String contString = et_chatPrivate.getText().toString();
        if (contString.length() > 0) {
            SingleChatMessage singleChatMessage = new SingleChatMessage();
            singleChatMessage.messageType = MessageType.eMessageType_SingleChat;
            singleChatMessage.messageBodyType = MessageBodyType.eMessageBodyType_Text;
            singleChatMessage.receiver = userId;
            singleChatMessage.msgId = UUID.randomUUID().toString();
            singleChatMessage.timeStamp = TimeUtils.getNowTime();

            UserMessage user = bleDBDao.findUserByUserId(userSelfId);
            singleChatMessage.userMessage = user;
            TextMessage textMessage = new TextMessage();
            textMessage.fileData = contString;
            singleChatMessage.textMessage = textMessage;
            Gson gson = new Gson();
            String json = gson.toJson(singleChatMessage);
            byte[] send_text_data = json.getBytes();

            if (null != link) {
                link.sendFrame(send_text_data);
                bleDBDao.addP2PTextMsg(singleChatMessage);//geng
                setAdapter(singleChatMessage);
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
        byte[] send_file_size = ("" + fileToSend.length()).getBytes();
        String fileSize = Base64.encodeToString(send_file_size, Base64.DEFAULT);//文件长度
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
        if (node == null) {
            return;
        }
        link = node.getLink(nodeId);
        if (link != null) {
            SingleChatMessage singleChatMessage = new SingleChatMessage();
            singleChatMessage.messageType = MessageType.eMessageType_SingleChat;
            singleChatMessage.messageBodyType = MessageBodyType.eMessageBodyType_Image;
            singleChatMessage.timeStamp = TimeUtils.getNowTime();
            singleChatMessage.receiver = userId;
            singleChatMessage.msgId = UUID.randomUUID().toString();

            UserMessage user = bleDBDao.findUserByUserId(userSelfId);
            singleChatMessage.userMessage = user;

            FileMessage fileMessage = new FileMessage();
            fileMessage.fileSize = fileSize;
            fileMessage.fileData = tFileData;
            fileMessage.fileName = fileName;
            fileMessage.filePath = filePath;
            singleChatMessage.fileMessage = fileMessage;

            Gson gson = new Gson();
            String json = gson.toJson(singleChatMessage);
            byte[] send_file_data = json.getBytes();
            link.sendFrame(send_file_data);
            bleDBDao.addP2PTextMsg(singleChatMessage);//geng
            setAdapter(singleChatMessage);
        } else {
            initDialogConnectedStatus();
        }
    }


    /**
     * 根据文件路径发送文件
     *
     * @param
     */
    private void sendFileMsg(String selectFilePath) {
    }


    @Override
    public void onReceiveTextMsg(Object obj) {
        SingleChatMessage singleChatMessage = (SingleChatMessage) obj;
        if (singleChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Text) {
            UserMessage userMessage = singleChatMessage.userMessage;
            if (userMessage.userId.equals(userId)) {//只有两个人都在聊天界面的时候才显示出来：拿到聊天界面的用户的userId，跟接收到的消息的userId比较，看是否一致，一致了才显示消息
                setAdapter(singleChatMessage);//设置适配器
            }
        } else if (singleChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Image) {
            UserMessage userMsg = singleChatMessage.userMessage;
            if (userMsg.userId.equals(userId)) {
                setAdapter(singleChatMessage);//设置适配器
            }
        } else if (singleChatMessage.messageBodyType == MessageBodyType.eMessageBodyType_Voice) {
            UserMessage userMsg = singleChatMessage.userMessage;
            if (userMsg.userId.equals(userId)) {
                setAdapter(singleChatMessage);//设置适配器
            }
        }
    }

    private void setAdapter(SingleChatMessage singleChatMessage) {
        mDataArrays.add(singleChatMessage);
        if (null == chatMessageAdapater) {
            chatMessageAdapater = new ChatMessageAdapater(ChatActivity.this, mDataArrays, userSelfId);
            lv_chatPrivate.setAdapter(chatMessageAdapater);
        }
        chatMessageAdapater.notifyDataSetChanged();
        if (mDataArrays.size() > 0) {
            lv_chatPrivate.setSelection(mDataArrays.size() - 1);// 最后一行
        }
    }


    @Override
    public void run() {
        mAlertDialog.dismiss();
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
                selectPath = TUtils.getPath(ChatActivity.this, data.getData());
                if (null != selectPath) {
                    sendImageMsg(selectPath);
                }
            }
        } else if (requestCode == REQUEST_CODE_SELECT_FILE) {
            if (data != null) {
                Uri uri = data.getData();
                if (null != uri) {
                    String select_file_path = FileUtils.getPath(ChatActivity.this, uri);
                    if (select_file_path != null) {
                        sendFileMsg(select_file_path);
                    }
                }
            }
        } else if (requestCode == 0) {//4a67538d-8ef3-4cfa-ac59-0792da2c1335
            if (null != data) {
                String seP = data.getStringExtra("RESULT");
                sendImageMsg(seP);
            }
        } else if (requestCode == GET_IMAGE_VIA_CAMERA) {

            File f = new File(Environment.getExternalStorageDirectory()
                    + "/" + localTempImgDir + "/" + localTempImgFileName);
            File newF = FileTransferUtils.scal(f.getAbsolutePath());
            try {
                Uri u =
                        Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
                                newF.getAbsolutePath(), null, null));
                //u就是拍摄获得的原始图片的uri
                String select_camera_path = FileUtils.getPath(ChatActivity.this, u);
                sendImageMsg(select_camera_path);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}
