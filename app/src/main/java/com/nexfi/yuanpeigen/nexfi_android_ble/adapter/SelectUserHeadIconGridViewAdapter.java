package com.nexfi.yuanpeigen.nexfi_android_ble.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;

/**
 * Created by Mark on 2016/4/15.
 */
public class SelectUserHeadIconGridViewAdapter extends BaseAdapter {

    private Context context;

    public SelectUserHeadIconGridViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return UserInfo.userHeadIcon.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(Gravity.CENTER);
        ImageView iv = new ImageView(context);
        iv.setImageResource(UserInfo.userHeadIcon[position]);
        linearLayout.addView(iv);
        return linearLayout;
    }
}
