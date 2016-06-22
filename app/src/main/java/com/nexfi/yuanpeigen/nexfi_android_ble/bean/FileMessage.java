package com.nexfi.yuanpeigen.nexfi_android_ble.bean;

import java.io.Serializable;

/**
 * Created by gengbaolong on 2016/4/13.
 */
public class FileMessage implements Serializable{
    private static  final long serialVersionUID = 8L;
    public String fileName;
    public String fileSize;//文件大小
    public String fileData;//文件数据
    public int fileIcon;
    public int isPb;
    public String filePath;
    public String isRead;
}
