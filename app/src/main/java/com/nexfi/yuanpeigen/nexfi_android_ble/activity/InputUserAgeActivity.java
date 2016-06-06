package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.UserInfo;
import com.nexfi.yuanpeigen.nexfi_android_ble.wheelview.NumericWheelAdapter;
import com.nexfi.yuanpeigen.nexfi_android_ble.wheelview.OnWheelScrollListener;
import com.nexfi.yuanpeigen.nexfi_android_ble.wheelview.WheelView;
import com.nexfi.yuanpeigen.nexfi_android_ble.wheelview.WheelViewAdapter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Mark on 2016/4/15.
 */
public class InputUserAgeActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout layout_back;
    private TextView tv_save, tv_selectUserAge;

    private LayoutInflater inflater = null;
    private View view = null;
    private WheelView year;
    private WheelView month;
    private WheelView day;

    private int mYear = 1996;
    private int mMonth = 0;
    private int mDay = 1;

    private LinearLayout ll;

    private int userAge, newUserAge;

    private final String USER_AGE = "userAge";

    private boolean isSelected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_age);

        initView();
        setClickListener();

    }

    private void setClickListener() {
        tv_save.setOnClickListener(this);
        layout_back.setOnClickListener(this);
    }

    private void initView() {
        layout_back = (RelativeLayout) findViewById(R.id.layout_back);
        tv_save = (TextView) findViewById(R.id.tv_save);
        tv_selectUserAge = (TextView) findViewById(R.id.tv_selectUserAge);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        ll = (LinearLayout) findViewById(R.id.ll);
        ll.addView(getDataPick());
        userAge = UserInfo.initUserAge(userAge, this);
        if (userAge != 0) {
            tv_selectUserAge.setText(userAge + "岁");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_back:
                Intent intent1 = new Intent(this, MainActivity.class);
                intent1.putExtra(USER_AGE, userAge);
                setResult(3,intent1);
                finish();
                break;
            case R.id.tv_save:
                if (isSelected ) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(USER_AGE, newUserAge);
                    setResult(3, intent);
                    finish();
                    UserInfo.saveUserAge(this, newUserAge);
                    Log.e("newUserAge", newUserAge + "===============tv_save=======================");
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请选择您的生日", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private View getDataPick() {
        Calendar c = Calendar.getInstance();
        int norYear = c.get(Calendar.YEAR);

        int curYear = mYear;
        int curMonth = mMonth + 1;
        int curDate = mDay;

        view = inflater.inflate(R.layout.wheel_date_picker, null);

        year = (WheelView) view.findViewById(R.id.year);
        NumericWheelAdapter numericWheelAdapter1 = new NumericWheelAdapter(this, 1950, norYear);
        numericWheelAdapter1.setLabel("年");
        WheelViewAdapter wheelViewAdapter1 = numericWheelAdapter1;
        year.setViewAdapter(wheelViewAdapter1);
        year.setCyclic(true);//是否可循环滑动
        year.addScrollingListener(scrollListener);

        month = (WheelView) view.findViewById(R.id.month);
        NumericWheelAdapter numericWheelAdapter2 = new NumericWheelAdapter(this, 1, 12, "%02d");
        numericWheelAdapter2.setLabel("月");
        month.setViewAdapter(numericWheelAdapter2);
        month.setCyclic(true);
        month.addScrollingListener(scrollListener);
        day = (WheelView) view.findViewById(R.id.day);
        initDay(curYear, curMonth);
        day.setCyclic(true);

        year.setVisibleItems(7);//设置显示行数
        month.setVisibleItems(7);
        day.setVisibleItems(7);

        year.setCurrentItem(curYear - 1950);
        month.setCurrentItem(curMonth - 1);
        day.setCurrentItem(curDate - 1);

        return view;
    }

    OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }

        @Override
        public void onScrollingFinished(WheelView wheel) {
            int n_year = year.getCurrentItem() + 1950;//年
            int n_month = month.getCurrentItem() + 1;//月

            isSelected = true;

            initDay(n_year, n_month);

            String birthday = new StringBuilder().append((year.getCurrentItem() + 1950)).append("-").append((month.getCurrentItem() + 1) < 10 ? "0" + (month.getCurrentItem() + 1) : (month.getCurrentItem() + 1)).append("-").append(((day.getCurrentItem() + 1) < 10) ? "0" + (day.getCurrentItem() + 1) : (day.getCurrentItem() + 1)).toString();
            tv_selectUserAge.setText(calculateDatePoor(birthday) + "岁");
            newUserAge = Integer.parseInt(calculateDatePoor(birthday));
        }
    };

    /**
     * @param year
     * @param month
     * @return
     */
    private int getDay(int year, int month) {
        int day = 30;
        boolean flag = false;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }

    private void initDay(int arg1, int arg2) {
        NumericWheelAdapter numericWheelAdapter = new NumericWheelAdapter(this, 1, getDay(arg1, arg2), "%02d");
        numericWheelAdapter.setLabel("日");
        day.setViewAdapter(numericWheelAdapter);
    }


    /**
     * 根据日期计算年龄
     *
     * @param birthday
     * @return
     */
    public static final String calculateDatePoor(String birthday) {
        try {
            if (TextUtils.isEmpty(birthday))
                return "0";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date birthdayDate = sdf.parse(birthday);
            String currTimeStr = sdf.format(new Date());
            Date currDate = sdf.parse(currTimeStr);
            if (birthdayDate.getTime() > currDate.getTime()) {
                return "0";
            }
            long age = (currDate.getTime() - birthdayDate.getTime())
                    / (24 * 60 * 60 * 1000) + 1;
            String year = new DecimalFormat("0.00").format(age / 365f);
            if (TextUtils.isEmpty(year))
                return "0";
            return String.valueOf(new Double(year).intValue());
        } catch (ParseException e) {
            BleApplication.getExceptionLists().add(e);
            BleApplication.getCrashHandler().saveCrashInfo2File(e);
            e.printStackTrace();
        }
        return "0";
    }

}
