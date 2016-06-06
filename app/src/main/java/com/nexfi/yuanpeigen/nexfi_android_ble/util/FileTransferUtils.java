package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gengbaolong on 2016/3/31.
 */
public class FileTransferUtils {
    public static final int REQUEST_CODE_SELECT_FILE = 2;
    public static final int REQUEST_CODE_LOCAL_IMAGE = 1;
    public static final int SELECT_A_PICTURE=3;//4.4以下
    public static final int SELECET_A_PICTURE_AFTER_KIKAT=4;//4.4以上




    /**
     * 选择本地文件
     */
    public static void selectFileFromLocal(Activity context) {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        context.startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    /**
     * 从图库获取图片
     */
    public static void selectPicFromLocal(Activity context) {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            context.startActivityForResult(intent, SELECT_A_PICTURE);
        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent = new Intent(Intent.ACTION_PICK);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
            context.startActivityForResult(intent, SELECET_A_PICTURE_AFTER_KIKAT);
        }
//        context.startActivityForResult(intent, REQUEST_CODE_LOCAL_IMAGE);
    }




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


    /**
     * @param
     * @param bytes
     * @param
     * @return Bitmap
     */
    public static Bitmap getPicFromBytesByScreenSize(byte[] bytes,float ww,float hh) {
        if (bytes != null) {
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            newOpts.inJustDecodeBounds = true;//只读边,不读内容
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,newOpts);

            newOpts.inJustDecodeBounds = false;
            int w = newOpts.outWidth;
            int h = newOpts.outHeight;
//            float hh = 100f;//
//            float ww = 100f;//
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


    public static Bitmap decodeSampledBitmapFromResource(byte[] bytes,
                                                         int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap=null;
        try{
            bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        }catch (OutOfMemoryError error){
            //
        }
        return bitmap;
    }


    public static Bitmap zoomBitmap(String path, int width, int height) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        /*
         * 官方文档这样写着“If set to true, the decoder will return null (no bitmap),
         * but the out... fields will still be set, allowing the caller to query
         * the bitmap without having to allocate the memory for its
         * pixels.　”，大意就是说inJustDecodeBounds
         * 为true时，不给出实际的Bitmap对象，但你可以得到这张图片的实际信息，这样你就可以计算缩放 比例了
         */
        options.inJustDecodeBounds = true; // 下面的bitmap暂时为null

        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回bitmap为空,不占用内存的
        int multiple = (int) (options.outHeight / 54); // 计算缩放值
        if (multiple <= 0) // 如果缩放值小于0，则不对图片进行缩放
            multiple = 1;

        options.inSampleSize = multiple;
        options.inJustDecodeBounds = false; // 得到缩放后的Bitmap对象
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
        }catch (OutOfMemoryError error){
            //
        }
        return bitmap;
    }



    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }





    /**
     * 把一个文件转化为字节
     * @param file
     * @return byte[]
     * @throws Exception
     */
    public static byte[] getByte(File file) throws Exception
    {
        byte[] bytes = null;
        if(file!=null)
        {
            InputStream is = new FileInputStream(file);//
            int length = (int) file.length();
            if(length>Integer.MAX_VALUE) //当文件的长度超过了int的最大值
            {
                return null;
            }
            bytes = new byte[length];
            int offset = 0;
            int numRead = 0;
            while(offset<bytes.length&&(numRead=is.read(bytes,offset,bytes.length-offset))>=0)
            {
                offset+=numRead;
            }
            //如果得到的字节长度和file实际的长度不一致就可能出错了
            if(offset<bytes.length)
            {
                return null;
            }
            is.close();
        }
        return bytes;
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
                // log.error("helper:the file is null!");
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
            // log.error("helper:get bytes from file process error!");
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
            // log.error("helper:get file from byte process error!");
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // log.error("helper:get file from byte process error!");
                    BleApplication.getExceptionLists().add(e);
                    BleApplication.getCrashHandler().saveCrashInfo2File(e);
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }


    public static Bitmap compressImage(Bitmap image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            int options = 100;
            while (baos.toByteArray().length / 1024 > 100 && options>10) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                baos.reset();//重置baos即清空baos
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                options -= 10;//每次都减少10
                Log.e("TAG", options + "-------compressImage-----options------------");
            }
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            Bitmap bitmap=null;
            try {
                bitmap = BitmapFactory.decodeStream(isBm, null, opt);//把ByteArrayInputStream数据生成图片
            }catch (OutOfMemoryError error){
                //
            }
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }





    public static File scal(String path){
        File outputFile = new File(path);
        if(null!=createImageFile()) {
            long fileSize = outputFile.length();
            final long fileMaxSize = 200 * 1024;
            if (fileSize >= fileMaxSize) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
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
        // Save a file: path for use with ACTION_VIEW intents
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


    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;
    }
}
