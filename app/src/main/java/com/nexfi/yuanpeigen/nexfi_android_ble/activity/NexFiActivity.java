package com.nexfi.yuanpeigen.nexfi_android_ble.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nexfi.yuanpeigen.nexfi_android_ble.R;
import com.nexfi.yuanpeigen.nexfi_android_ble.util.ShellUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mark on 2016/5/26.
 */
public class NexFiActivity extends AppCompatActivity implements View.OnClickListener {
    private List<String> commnandList = new ArrayList<String>();
    private Button button, btn_continue;
    private String release;
    private String model;
    private String sdk;
    private boolean isRoot, isWifiOpen;
    private Thread thread;
    private Handler handler, mHandler;
    private AlertDialog mAlertDialog, waimingDialog;
    private RelativeLayout layout_back_nexfi;
    private boolean isFailed = true;
    private WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nexfi);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        initView();
        if (wifiManager.isWifiEnabled()) {
            isWifiOpen = true;
        }
        sdk = android.os.Build.VERSION.SDK;
        model = android.os.Build.MODEL;
        release = android.os.Build.VERSION.RELEASE;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Toast.makeText(NexFiActivity.this, "恭喜您成功打开NexFi网络", Toast.LENGTH_LONG).show();
                    mAlertDialog.dismiss();
                } else {
                    if (isFailed) {
                        initWarmingDialog();
                    }
                }
            }
        };

    }


    private void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        layout_back_nexfi = (RelativeLayout) findViewById(R.id.layout_back_nexfi);
        layout_back_nexfi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        button.setOnClickListener(this);
    }

    private void initDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_loading, null);
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        mAlertDialog.show();
        mAlertDialog.getWindow().setContentView(v);
        mAlertDialog.setCancelable(false);
    }

    private void initWarmingDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.dialog_warning, null);
        btn_continue = (Button) v.findViewById(R.id.btn_continue);
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waimingDialog.dismiss();
            }
        });
        waimingDialog = new AlertDialog.Builder(this).create();
        waimingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00000000));
        waimingDialog.show();
        waimingDialog.getWindow().setContentView(v);
        waimingDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                if (isFailed) {
                    if (model.equals("HM 2LTE-CMCC")) {
                        closeWifi();
                        createFile(R.raw.batmand_hm, "batmand_hm");
                        createFile(R.raw.iw_hm, "iw_hm");
                        createFile(R.raw.iptables_hm, "iptables_hm");
                        createFile(R.raw.iwconfig_hm, "iwconfig_hm");
                        createFile(R.raw.libnl_3_hm, "libnl_3_hm.so");
                        createFile(R.raw.libnl_genl_3_hm, "libnl_genl_3_hm.so");
                        initDialog();
                        mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAlertDialog.dismiss();
                                Toast.makeText(NexFiActivity.this, "恭喜您成功打开NexFi网络", Toast.LENGTH_LONG).show();
                            }
                        }, 25000);
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printLog();
                                isRoot = upgradeRootPermission(getPackageCodePath());
                                Log.e("isRoot", isRoot + "");
                                commnandList.add("su");
                                commnandList.add("sleep 1");
                                commnandList.add("mount -o rw,remount /");
                                commnandList.add("sleep 1");
                                commnandList.add("mkdir -p /data/run/");
                                commnandList.add("sleep 1");
                                commnandList.add("mount -o rw,remount /dev/block/bootdevice/by-name/system /system");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/batmand_hm /system/bin/");
                                commnandList.add("sleep 1");


                                commnandList.add("cp /sdcard/iptables_hm /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iw_hm /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/libnl_3_hm.so /system/lib/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/libnl_genl_3_hm.so /system/lib/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iwconfig_hm /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iptables_hm /system/bin/iptables");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iptables");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iw_hm /system/bin/iw");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iw");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/lib/libnl_3_hm.so /system/lib/libnl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/lib/libnl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/lib/libnl_genl_3_hm.so /system/lib/libnl-genl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/lib/libnl-genl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iwconfig_hm /system/bin/iwconfig");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iwconfig");
                                commnandList.add("sleep 1");

                                commnandList.add("rmmod wlan");
                                commnandList.add("sleep 1");
                                commnandList.add("insmod /system/lib/modules/wlan.ko");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 down");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 up");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 set type ibss");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 ibss join imesh 2437 NOHT fixed-freq 00:11:22:33:44:55");
                                commnandList.add("sleep 1");
                                commnandList.add("iw reg set GY");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 set txpower fixed 30mBm");
                                commnandList.add("sleep 1");
                                commnandList.add("iw phy phy0 set distance 114750");
                                commnandList.add("sleep 1");
                                commnandList.add("iw phy phy0 set coverage 255");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 inet 192.168.2.139 netmask 255.255.255.0");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 up");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/batmand_hm");
                                commnandList.add("sleep 1");
                                commnandList.add("/system/bin/batmand_hm -a 192.168.2.139/32 wlan0");
                                ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                                ShellUtils.execCommand(commnandList, true);
                                String errorMsg = result.errorMsg;
                                String successMsg = result.successMsg;
                                int resultNum = result.result;
                                Log.e("errorMsg", errorMsg);
                                Log.e("successMsg", successMsg);
                                Log.e("resultNum", resultNum + "");
                                if (resultNum == 1 || successMsg != null) {
                                    Message message1 = handler.obtainMessage();
                                    message1.obj = successMsg;
                                    message1.what = 1;
                                    handler.sendMessage(message1);
                                    isFailed = false;
                                } else {
                                    Message message2 = handler.obtainMessage();
                                    message2.obj = errorMsg;
                                    message2.what = 2;
                                    handler.sendMessage(message2);
                                }

                            }
                        });
                        thread.start();
                    } else if (model.equals("MI 4LTE")) {
                        closeWifi();
                        impl();
                    } else if (model.equals("Nexus 5X")) {
                        closeWifi();
                        initDialog();
                        createFile(R.raw.batmand_nexus5x, "batmand_nexus5x");
                        createFile(R.raw.iptables_nexus5x, "iptables_nexus5x");
                        createFile(R.raw.iw_nexus5x, "iw_nexus5x");
                        createFile(R.raw.iwconfig_nexus5x, "iwconfig_nexus5x");
                        createFile(R.raw.libnl_3_nexus5x, "libnl_3_nexus5x.so");
                        createFile(R.raw.libnl_genl_3_nexus5x, "libnl_genl_3_nexus5x.so");
                        mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAlertDialog.dismiss();
                                Toast.makeText(NexFiActivity.this, "恭喜您成功打开NexFi网络", Toast.LENGTH_LONG).show();
                            }
                        }, 25000);
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printLog();
                                isRoot = upgradeRootPermission(getPackageCodePath());
                                Log.e("isRoot", isRoot + "");
                                commnandList.add("su");
                                commnandList.add("sleep 1");
                                commnandList.add("mount -o rw,remount /");
                                commnandList.add("sleep 1");
                                commnandList.add("mkdir -p /data/run/");
                                commnandList.add("sleep 1");
                                commnandList.add("mount -o rw,remount /dev/block/platform/soc.0/f9824900.sdhci/by-name/system /system");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/batmand_nexus5x /system/bin/");
                                commnandList.add("sleep 1");

                                commnandList.add("cp /sdcard/iptables_nexus5x /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iw_nexus5x /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/libnl_3_nexus5x.so /system/lib64/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/libnl_genl_3_nexus5x.so /system/lib64/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iwconfig_nexus5x /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iptables_nexus5x /system/bin/iptables");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iptables");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iw_nexus5x /system/bin/iw");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iw");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/lib64/libnl_3_nexus5x.so /system/lib64/libnl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/lib64/libnl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/lib64/libnl_genl_3_nexus5x.so /system/lib64/libnl-genl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/lib64/libnl-genl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iwconfig_nexus5x /system/bin/iwconfig");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iwconfig");
                                commnandList.add("sleep 1");

                                commnandList.add("ifconfig wlan0 down");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 up");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 set power_save off");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 set type ibss");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 ibss join imesh 2437 HT20 fixed-freq 00:11:22:33:44:55");
                                commnandList.add("sleep 1");
                                commnandList.add("iw reg set GY");
                                commnandList.add("sleep 1");
                                commnandList.add("iw dev wlan0 set txpower fixed 30mBm");
                                commnandList.add("sleep 1");
                                commnandList.add("iw phy phy0 set distance 114750");
                                commnandList.add("sleep 1");
                                commnandList.add("iw phy phy0 set coverage 255");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 inet 192.168.2.138 netmask 255.255.255.0");
                                commnandList.add("sleep 1");
                                commnandList.add("ifconfig wlan0 up");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/batmand_nexus5x");
                                commnandList.add("sleep 1");
                                commnandList.add("/system/bin/batmand_nexus5x -a 192.168.2.138/32 wlan0");
                                Log.e("开始执行命令", " ----------------");
                                ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                                Log.e("执行完命令", " ----------------");
                                String errorMsg = result.errorMsg;
                                String successMsg = result.successMsg;
                                int resultNum = result.result;
                                Log.e("errorMsg", errorMsg);
                                Log.e("successMsg", successMsg);
                                Log.e("resultNum", resultNum + "");
                                if (resultNum == 1 || successMsg != null) {
                                    Message message1 = handler.obtainMessage();
                                    message1.obj = successMsg;
                                    message1.what = 1;
                                    handler.sendMessage(message1);
                                    isFailed = false;
                                } else {
                                    Message message2 = handler.obtainMessage();
                                    message2.obj = errorMsg;
                                    message2.what = 2;
                                    handler.sendMessage(message2);
                                }

                            }
                        });
                        thread.start();
                    } else if (model.equals("ZTE C880A")) {
                        closeWifi();
                        initDialog();
                        createFile(R.raw.iptables_zte, "iptables_zte");
                        createFile(R.raw.iw_zte, "iw_zte");
                        createFile(R.raw.iwconfig_zte, "iwconfig_zte");
                        createFile(R.raw.libnl_3_zte, "libnl_3_zte.so");
                        createFile(R.raw.libnl_genl_3_zte, "libnl_genl_3_zte.so");
                        mHandler = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAlertDialog.dismiss();
                                Toast.makeText(NexFiActivity.this, "恭喜您成功打开NexFi网络", Toast.LENGTH_LONG).show();
                            }
                        }, 20000);
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printLog();
                                isRoot = upgradeRootPermission(getPackageCodePath());
                                Log.e("isRoot", isRoot + "");
                                commnandList.add("su");
                                commnandList.add("sleep 1");
                                commnandList.add("mount -o remount /system");
                                commnandList.add("sleep 1");
                                commnandList.add("mount -o rw,remount /dev/block/platform/mtk-msdc.0/by-name/system /system");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iptables_zte /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iw_zte /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/libnl_3_zte.so /system/lib64/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/libnl_genl_3_zte.so /system/lib64/");
                                commnandList.add("sleep 1");
                                commnandList.add("cp /sdcard/iwconfig_zte /system/bin/");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iptables_zte /system/bin/iptables");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iptables");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iw_zte /system/bin/iw");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iw");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/lib64/libnl_3_zte.so /system/lib64/libnl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/lib/libnl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/lib64/libnl_genl_3_zte.so /system/lib64/libnl-genl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/lib64/libnl-genl-3.so");
                                commnandList.add("sleep 1");
                                commnandList.add("mv /system/bin/iwconfig_zte /system/bin/iwconfig");
                                commnandList.add("sleep 1");
                                commnandList.add("chmod 777 /system/bin/iwconfig");
                                commnandList.add("sleep 1");
                                ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                                String errorMsg = result.errorMsg;
                                String successMsg = result.successMsg;
                                int resultNum = result.result;
                                Log.e("errorMsg", errorMsg);
                                Log.e("successMsg", successMsg);
                                Log.e("resultNum", resultNum + "");
                                if (resultNum == 1 || successMsg != null) {
                                    Message message1 = handler.obtainMessage();
                                    message1.obj = successMsg;
                                    message1.what = 1;
                                    isFailed = false;
                                    handler.sendMessage(message1);
                                } else {
                                    Message message2 = handler.obtainMessage();
                                    message2.obj = errorMsg;
                                    message2.what = 2;
                                    handler.sendMessage(message2);
                                }
                            }
                        });
                        thread.start();
                    } else {
                        impl();
                    }
                    break;
                } else {
                    Toast.makeText(NexFiActivity.this, "您已成功打开NexFi网络", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void impl() {
        initDialog();
        createFile(R.raw.batmand_xiaomi4, "batmand_xiaomi4");
        createFile(R.raw.iptables_xiaomi4, "iptables_xiaomi4");
        createFile(R.raw.iw_xiaomi4, "iw_xiaomi4");
        createFile(R.raw.iwconfig_xiaomi4, "iwconfig_xiaomi4");
        createFile(R.raw.libnl_3_xiaomi4, "libnl_3_xiaomi4.so");
        createFile(R.raw.libnl_genl_3_xiaomi4, "libnl_genl_3_xiaomi4.so");
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAlertDialog.dismiss();
                Toast.makeText(NexFiActivity.this, "恭喜您成功打开NexFi网络", Toast.LENGTH_LONG).show();
            }
        }, 25000);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                printLog();
                isRoot = upgradeRootPermission(getPackageCodePath());
                Log.e("isRoot", isRoot + "");
                commnandList.add("su");
                commnandList.add("sleep 1");
                commnandList.add("mount -o rw,remount /");
                commnandList.add("sleep 1");
                commnandList.add("mkdir -p /data/run/");
                commnandList.add("sleep 1");
                commnandList.add("mount -o rw,remount /dev/block/bootdevice/by-name/system /system");
                commnandList.add("sleep 1");
                commnandList.add("cp /sdcard/batmand_xiaomi4 /system/bin/");
                commnandList.add("sleep 1");

                commnandList.add("cp /sdcard/iptables_xiaomi4 /system/bin/");
                commnandList.add("sleep 1");
                commnandList.add("cp /sdcard/iw_xiaomi4 /system/bin/");
                commnandList.add("sleep 1");
                commnandList.add("cp /sdcard/libnl_3_xiaomi4.so /system/lib/");
                commnandList.add("sleep 1");
                commnandList.add("cp /sdcard/libnl_genl_3_xiaomi4.so /system/lib/");
                commnandList.add("sleep 1");
                commnandList.add("cp /sdcard/iwconfig_xiaomi4 /system/bin/");
                commnandList.add("sleep 1");
                commnandList.add("mv /system/bin/iptables_xiaomi4 /system/bin/iptables");
                commnandList.add("sleep 1");
                commnandList.add("chmod 777 /system/bin/iptables");
                commnandList.add("sleep 1");
                commnandList.add("mv /system/bin/iw_xiaomi4 /system/bin/iw");
                commnandList.add("sleep 1");
                commnandList.add("chmod 777 /system/bin/iw");
                commnandList.add("sleep 1");
                commnandList.add("mv /system/lib/libnl_3_xiaomi4.so /system/lib/libnl-3.so");
                commnandList.add("sleep 1");
                commnandList.add("chmod 777 /system/lib/libnl-3.so");
                commnandList.add("sleep 1");
                commnandList.add("mv /system/lib/libnl_genl_3_xiaomi4.so /system/lib/libnl-genl-3.so");
                commnandList.add("sleep 1");
                commnandList.add("chmod 777 /system/lib/libnl-genl-3.so");
                commnandList.add("sleep 1");
                commnandList.add("mv /system/bin/iwconfig_xiaomi4 /system/bin/iwconfig");
                commnandList.add("sleep 1");
                commnandList.add("chmod 777 /system/bin/iwconfig");
                commnandList.add("sleep 1");


                commnandList.add("rmmod wlan");
                commnandList.add("sleep 1");
                commnandList.add("insmod /system/lib/modules/wlan.ko");
                commnandList.add("sleep 1");
                commnandList.add("ifconfig wlan0 down");
                commnandList.add("sleep 1");
                commnandList.add("ifconfig wlan0 up");
                commnandList.add("sleep 1");
                commnandList.add("iw dev wlan0 set type ibss");
                commnandList.add("sleep 1");
                commnandList.add("iw dev wlan0 ibss join imesh 2437 HT20 fixed-freq 00:11:22:33:44:55");
                commnandList.add("sleep 1");
                commnandList.add("iw reg set GY");
                commnandList.add("sleep 1");
                commnandList.add("iw dev wlan0 set txpower fixed 30mBm");
                commnandList.add("sleep 1");
                commnandList.add("iw phy phy1 set distance 114750");
                commnandList.add("sleep 1");
                commnandList.add("iw phy phy1 set coverage 255");
                commnandList.add("sleep 1");
                commnandList.add("ifconfig wlan0 inet 192.168.2.140 netmask 255.255.255.0");
                commnandList.add("sleep 1");
                commnandList.add("ifconfig wlan0 up");
                commnandList.add("sleep 1");
                commnandList.add("chmod 777 /system/bin/batmand_xiaomi4");
                commnandList.add("sleep 1");
                commnandList.add("/system/bin/batmand_xiaomi4 -a 192.168.2.140/32 wlan0");
                Log.e("开始执行命令", " ----------------");
                ShellUtils.CommandResult result = ShellUtils.execCommand(commnandList, true);
                ShellUtils.execCommand(commnandList, true);
                Log.e("执行完命令", " ----------------");
                String errorMsg = result.errorMsg;
                String successMsg = result.successMsg;
                int resultNum = result.result;
                Log.e("errorMsg", errorMsg);
                Log.e("successMsg", successMsg);
                Log.e("resultNum", resultNum + "");
                if (resultNum == 1 || successMsg != null) {
                    Message message1 = handler.obtainMessage();
                    message1.obj = successMsg;
                    message1.what = 1;
                    handler.sendMessage(message1);
                    isFailed = false;
                } else {
                    Message message2 = handler.obtainMessage();
                    message2.obj = errorMsg;
                    message2.what = 2;
                    handler.sendMessage(message2);
                }

            }
        });
        thread.start();
    }

    private void printLog() {
        Log.e("SDK", sdk);
        Log.e("model", model);
        Log.e("release", release);
    }

    /**
     * 应用程序运行命令获取 Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void createFile(int id, String name) {
        String filePath = android.os.Environment
                .getExternalStorageDirectory().getAbsolutePath() + "/" + name;// 文件路径
        try {
            File dir = new File(android.os.Environment
                    .getExternalStorageDirectory().getAbsolutePath());// 目录路径
            if (!dir.exists()) {// 如果不存在，则创建路径名
                System.out.println("要存储的目录不存在");
                if (dir.mkdirs()) {// 创建该路径名，返回true则表示创建成功
                    System.out.println("已经创建文件存储目录");
                } else {
                    System.out.println("创建目录失败");
                }
            }
            // 目录存在，则将apk中raw中的需要的文档复制到该目录下
            File file = new File(filePath);
            if (!file.exists()) {// 文件不存在
                System.out.println("要打开的文件不存在");
                InputStream ins = getResources().openRawResource(
                        id);// 通过raw得到数据资源
                System.out.println("开始读入");
                FileOutputStream fos = new FileOutputStream(file);
                System.out.println("开始写出");
                byte[] buffer = new byte[8192];
                int count = 0;// 循环写出
                while ((count = ins.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                System.out.println("已经创建该文件");
                fos.close();// 关闭流
                ins.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
