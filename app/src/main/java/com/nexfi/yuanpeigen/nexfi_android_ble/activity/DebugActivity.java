package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;

import java.util.List;

/**
 * Created by gengbaolong on 2016/5/2.
 */
public class DebugActivity extends AppCompatActivity {

    private List<String> logLists;
    private ListView list_debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        logLists = BleApplication.getLogLists();
        list_debug = (ListView)findViewById(R.id.list_debug);
        list_debug.setAdapter(new LogAdapter());
    }




    public class LogAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return logLists.size();
        }


        @Override
        public Object getItem(int position) {
            return logLists.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String log=logLists.get(position);
            TextView textView;
            textView=new TextView(getApplicationContext());
            textView.setBackgroundColor(Color.BLACK);
            textView.setText(position + ": " + log);
            return textView;
        }
    }

}
