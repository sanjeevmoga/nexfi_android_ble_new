package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.BaseMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.fragment.FragmentMine;
import com.nexfi.yuanpeigen.nexfi_android_ble.fragment.FragmentNearby;
import com.nexfi.yuanpeigen.nexfi_android_ble.model.Node;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.ObjectBytesUtils;


public class MainActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener {

    private FragmentManager mFragmentManager;
    private RadioGroup myTabRg;
    private RadioButton rb_nearby, rb_mine;
    private FragmentMine fragmentMine;
    private FragmentNearby fragmentNearby;
    private Handler mHandler;


    private boolean isExit;
    private static Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //进入附近的人界面时开始搜索
        startScan();
        initView();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                isExit = false;
            }
        };
        MediaPlayer mPlayer=new MediaPlayer();
//        FileDescriptor
//        MediaDataSource
    }


    public static Node getNode() {
        return node;
    }

    private void startScan() {
        if(null==node){
            node=new Node(MainActivity.this);
        }
        node.start();
    }

    private void initView() {
        myTabRg = (RadioGroup) findViewById(R.id.tab_menu);
        rb_nearby = (RadioButton) findViewById(R.id.rb_nearby);
        rb_mine = (RadioButton) findViewById(R.id.rb_mine);
        myTabRg.setOnCheckedChangeListener(this);
        initFragment();
    }

    private void initFragment() {
        initNearByFragment();
        rb_nearby.setChecked(true);
    }


    public void initNearByFragment() {
        mFragmentManager = getSupportFragmentManager();
        fragmentNearby = new FragmentNearby();
        fragmentMine = new FragmentMine();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.container, fragmentMine)
                .add(R.id.container, fragmentNearby)
                .hide(fragmentMine).commitAllowingStateLoss();
    }

    private void initMineFragment() {
        mFragmentManager = getSupportFragmentManager();
        fragmentNearby = new FragmentNearby();
        fragmentMine = new FragmentMine();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.add(R.id.container, fragmentMine)
                .add(R.id.container, fragmentNearby)
                .hide(fragmentNearby).commitAllowingStateLoss();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_nearby:
                mFragmentManager.beginTransaction()
                        .show(fragmentNearby).hide(fragmentMine).commitAllowingStateLoss();
                break;
            case R.id.rb_mine:
                mFragmentManager.beginTransaction()
                        .show(fragmentMine).hide(fragmentNearby).commitAllowingStateLoss();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isExit) {
                isExit = true;
                mHandler.sendEmptyMessageDelayed(0, 1500);
                Toast.makeText(this, "再按一次退出NexFi", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                finish();
            }
        }
        return true;
    }



    public void notifyMineMsgHasChanged(BaseMessage baseMessage){
        byte[] notify_msg_bys=ObjectBytesUtils.ObjectToByte(baseMessage);
        node.broadcastFrame(notify_msg_bys);
    }
}

