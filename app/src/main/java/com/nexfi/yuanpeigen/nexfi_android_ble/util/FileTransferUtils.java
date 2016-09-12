package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by gengbaolong on 2016/3/31.
 */
public class FileTransferUtils {

    /**
     * @param
     * @param bytes
     * @param
     * @return Bitmap
     */
    public static Bitmap getPicFromBytes(byte[] bytes) {
        if (bytes != null) {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;//只读边,不读内容
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,newOpts);

            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
            float hh = 100f;//
            float ww = 100f;//
            int be = 1;
            if (w > h && w > ww) {
                be = (int) (newOpts.outWidth / ww);
            } else if (w < h && h > hh) {
                be = (int) (newOpts.outHeight / hh);
            }
            if (be <= 0)
                be = 1;
            newOpts.inSampleSize = be;//设置采样率

            newOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;//该模式是默认的,可不设
            newOpts.inPurgeable = true;// 同时设置才会有效
            newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收
            try {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        newOpts);
            }catch (OutOfMemoryError error){
                //
            }
            return bitmap;
        }
        return null;
    }


    public static Bitmap compressImageFromFile(String srcPath,float ww,float hh) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置采样率

        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;//该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;//。当系统内存不够时候图片自动被回收
        try {
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        }catch (OutOfMemoryError error){
            //
        }
//      return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
        //其实是无效的,大家尽管尝试
        return bitmap;
    }



    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }



    /**
     * 文件转化为字节数组
     *
     * @param file
     * @return
     */
    public static byte[] getBytesFromFile(File file) {
        byte[] ret = null;
        try {
            if (file == null) {
                return null;
            }
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) != -1) {
                out.write(b, 0, n);
            }
            in.close();
            out.close();
            ret = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }



    /**
     * 把字节数组保存为一个文件
     *
     * @param b
     * @param outputFile
     * @return
     */
    public static File getFileFromBytes(byte[] b, String outputFile) {
        File ret = null;
        BufferedOutputStream stream = null;
        try {
            ret = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(ret);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    BleApplication.getExceptionLists().add(e);
                    BleApplication.getCrashHandler().saveCrashInfo2File(e);
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }



    public static File scal(String path){
        File outputFile = new File(path);
        if(null!=createImageFile()) {
            long fileSize = outputFile.length();
            final long fileMaxSize = 200 * 1024;
            if (fileSize >= fileMaxSize) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                BitmapFactory.decodeFile(path, options);
                int height = options.outHeight;
                int width = options.outWidth;

                double scale = Math.sqrt((float) fileSize / fileMaxSize);
                options.outHeight = (int) (height / scale);
                options.outWidth = (int) (width / scale);
                options.inSampleSize = (int) (scale + 0.5);
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither=false;
                options.inPurgeable=true;
                options.inTempStorage=new byte[32 * 1024];
                options.inJustDecodeBounds = false;
                Bitmap bitmap=null;
                try {
                    bitmap = BitmapFactory.decodeFile(path, options);
                }catch (OutOfMemoryError error){//geng
                    //
                }
                if(bitmap!=null) {
                    outputFile = new File(createImageFile().getPath());
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(outputFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                        fos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        BleApplication.getExceptionLists().add(e);
                        BleApplication.getCrashHandler().saveCrashInfo2File(e);
                        e.printStackTrace();
                    }
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    } else {
                        File tempFile = outputFile;
                        outputFile = new File(createImageFile().getPath());
                        copyFileUsingFileChannels(tempFile, outputFile);
                    }
                }
            }
        }
        return outputFile;

    }



    public static Uri createImageFile(){
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "JPEG_"+ timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = null;
        Uri uri=null;
        try {
            image = File.createTempFile(imageFileName,".jpg", storageDir);
            uri=Uri.fromFile(image);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            BleApplication.getExceptionLists().add(e);
            BleApplication.getCrashHandler().saveCrashInfo2File(e);
            e.printStackTrace();
        }
        return uri;
    }


    public static void copyFileUsingFileChannels(File source, File dest){
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            try {
                inputChannel = new FileInputStream(source).getChannel();
                outputChannel = new FileOutputStream(dest).getChannel();
                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                BleApplication.getExceptionLists().add(e);
                BleApplication.getCrashHandler().saveCrashInfo2File(e);
                e.printStackTrace();
            }
        } finally {
            try {
                inputChannel.close();
                outputChannel.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                BleApplication.getExceptionLists().add(e);
                BleApplication.getCrashHandler().saveCrashInfo2File(e);
                e.printStackTrace();
            }
        }
    }

}
