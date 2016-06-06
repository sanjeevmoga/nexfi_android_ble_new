package com.nexfi.yuanpeigen.nexfi_android_ble.fragment;

import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.GroupChatActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.MainActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.activity.NexFiActivity;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.UserListViewAdapter;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.UserMessage;
import com.nexfi.yuanpeigen.nexfi_android_ble.dao.BleDBDao;
import com.nexfi.yuanpeigen.nexfi_android_ble.model.Node;
import com.nexfi.yuanpeigen.nexfi_android_ble.smoothprogressbar.SmoothProgressBar;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/4/12.
 */
public class FragmentNearby extends Fragment implements View.OnClickListener {

    private PopupWindow mPopupWindow = null, mPopupWindow_share = null;
    private ImageView iv_add;
    private View View_pop, view_share, v_parent;
    private LinearLayout addChatRoom, share, nexfi;
    private ListView lv_userlist;
    private SmoothProgressBar myProgressbar;
    private List<UserMessage> userMessageList = new ArrayList<UserMessage>();
    private UserListViewAdapter userListViewAdapter;
    private TextView tv_near;

    private Handler handler;
    private boolean isNewUser = false;
    private String userId;


    BleDBDao bleDBDao = new BleDBDao(BleApplication.getContext());
    Node node = MainActivity.getNode();//geng

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initView(inflater, container);
        setClickListener();
        initData();
        initMyprogressbar();
        getActivity().getContentResolver().registerContentObserver(
                Uri.parse("content://www.nexfi_ble_user.com"), true,
                new Myobserve(new Handler()));
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (userMessageList.size() == 0)
                    if (isResumed()) {
                        showScanDialog();
                    }
            }
        }, 45 * 1000);
        return v_parent;
    }


    private void initMyprogressbar() {
        if (userMessageList.size() != 0) {
            myProgressbar.progressiveStop();
            myProgressbar.setVisibility(View.GONE);
        }
    }

    private void showScanDialog() {
        FragmentDialogConnected dialog = new FragmentDialogConnected();
        dialog.show(getFragmentManager(), "Dialog");
    }

    private class Myobserve extends ContentObserver {
        public Myobserve(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            isNewUser = true;
            initData();
            myProgressbar.progressiveStop();
            myProgressbar.setVisibility(View.GONE);
            super.onChange(selfChange);
        }
    }


    private void initData() {
        userId = UserInfo.initUserId(userId, BleApplication.getContext());
        //从数据库中获取用户数据
        userMessageList = bleDBDao.findAllUsers(userId);
        userListViewAdapter = new UserListViewAdapter(BleApplication.getContext(), userMessageList, isNewUser);
        lv_userlist.setAdapter(userListViewAdapter);
        if (userListViewAdapter != null) {
            userListViewAdapter.notifyDataSetChanged();
        }
        int userCount=userMessageList.size();
        tv_near.setText("附近的人("+userCount+")");
    }


    private void setClickListener() {
        share.setOnClickListener(this);
        addChatRoom.setOnClickListener(this);
        iv_add.setOnClickListener(this);
        nexfi.setOnClickListener(this);
    }

    private void initView(LayoutInflater inflater, ViewGroup container) {
        v_parent = inflater.inflate(R.layout.fragment_nearby, container, false);
        View_pop = inflater.inflate(R.layout.pop_menu_add, null);
        view_share = inflater.inflate(R.layout.layout_share, null);
        lv_userlist = (ListView) v_parent.findViewById(R.id.lv_userlist);
        addChatRoom = (LinearLayout) View_pop.findViewById(R.id.add_chatRoom);
        share = (LinearLayout) View_pop.findViewById(R.id.share);
        iv_add = (ImageView) v_parent.findViewById(R.id.iv_add);
        nexfi = (LinearLayout) View_pop.findViewById(R.id.nexfi);
        myProgressbar = (SmoothProgressBar) v_parent.findViewById(R.id.myProgressbar);
        tv_near= (TextView) v_parent.findViewById(R.id.tv_near);
    }


    private void initPop() {
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(View_pop, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow.showAsDropDown(iv_add, 0, 0);
    }


    private void initPopShare() {
        if (mPopupWindow_share == null) {
            mPopupWindow_share = new PopupWindow(view_share, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow_share.setBackgroundDrawable(new ColorDrawable(0x00000000));
        }
        mPopupWindow_share.showAtLocation(v_parent, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share:
                mPopupWindow.dismiss();
                initPopShare();
                break;

            case R.id.add_chatRoom:
                Intent intent = new Intent(BleApplication.getContext(), GroupChatActivity.class);
                startActivity(intent);
                mPopupWindow.dismiss();
                break;

            case R.id.iv_add:
                initPop();
                break;

            case R.id.nexfi:
                Intent intent1 = new Intent(BleApplication.getContext(), NexFiActivity.class);
                startActivity(intent1);
                mPopupWindow.dismiss();
                break;
        }
    }
}
