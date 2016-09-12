package com.nexfi.yuanpeigen.nexfi_android_ble.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.nexfi.yuanpeigen.nexfi_android_ble.application.BleApplication;

/**
 * Created by gengbaolong on 2016/3/11.
 */
public class FileUtils {
    public static String getPath(Context context, Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                BleApplication.getExceptionLists().add(e);
                BleApplication.getCrashHandler().saveCrashInfo2File(e);
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

}
