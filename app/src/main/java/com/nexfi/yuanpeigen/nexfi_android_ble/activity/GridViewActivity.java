package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.adapter.MyAdapter;
import com.nexfi.yuanpeigen.nexfi_android_ble.bean.ImageFloder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gengbaolong on 2016/5/1.
 */
public class GridViewActivity extends AppCompatActivity {

    private GridView mGirdView;
    private TextView mChooseDir;
    private TextView mImageCount;
    private RelativeLayout mBottomLy;
    private ImageFloder floder;
    private List<String> mImgs;
    private File mImgDir;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        Intent intent=getIntent();
        floder = (ImageFloder) intent.getSerializableExtra("FLODER");
        initView();
        initAdapter();
        initListener();
    }

    private void initListener() {
        mGirdView.setFocusable(true);
        mGirdView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path=mImgs.get(position);
                String picturePath=mImgDir.getAbsolutePath()+"/"+path;
                Intent intent=new Intent(getApplicationContext(), SelectBigImageActivity.class);
				intent.putExtra("picturePath",picturePath);
                GridViewActivity.this.startActivityForResult(intent, 2);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            if(requestCode==2){
                String result = data.getStringExtra("RESULT");
                GridViewActivity.this.setResult(1,data);
                GridViewActivity.this.finish();
            }

        }

    }

    private void initAdapter() {
        mImgDir = new File(floder.getDir());
        mImgs = Arrays.asList(mImgDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(".jpg") || filename.endsWith(".png")
                        || filename.endsWith(".jpeg"))
                    return true;
                return false;
            }
        }));
        MyAdapter mAdapter = new MyAdapter(getApplicationContext(), mImgs,
                R.layout.grid_item, mImgDir.getAbsolutePath());
        mGirdView.setAdapter(mAdapter);
        mImageCount.setText(floder.getCount() + "张");
        mChooseDir.setText(floder.getName());
    }

    private void initView() {
        mGirdView = (GridView) findViewById(R.id.id_gridView);
        mChooseDir = (TextView) findViewById(R.id.id_choose_dir);
        mImageCount = (TextView) findViewById(R.id.id_total_count);

        mBottomLy = (RelativeLayout) findViewById(R.id.id_bottom_ly);
    }
}
