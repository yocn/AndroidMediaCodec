package com.yocn.meida.util;

import android.util.Log;

/**
 * @Author yocn
 * @Date 2019/8/2 11:05 AM
 * @ClassName LogUtil
 */
public class LogUtil {
    public static final String TAG = LogUtil.class.getSimpleName();

    public static void d(String pre, Object... msg) {
        Log.d(TAG, getStr(pre, msg));
    }

    public static void v(String pre, Object... msg) {
        Log.v(TAG, getStr(pre, msg));
    }

    public static void i(String pre, Object... msg) {
        Log.i(TAG, getStr(pre, msg));
    }

    public static void w(String pre, Object... msg) {
        Log.w(TAG, getStr(pre, msg));
    }

    public static void e(String pre, Object... msg) {
        Log.e(TAG, getStr(pre, msg));
    }

    private static String getStr(String pre, Object... msg) {
        StringBuilder stringBuilder = new StringBuilder(pre);
        stringBuilder.append(" ");
        for (Object s : msg) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

}
