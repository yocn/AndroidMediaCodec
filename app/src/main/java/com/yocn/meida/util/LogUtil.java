package com.yocn.meida.util;

import android.util.Log;

/**
 * @Author yocn
 * @Date 2019/8/2 11:05 AM
 * @ClassName LogUtil
 */
public class LogUtil {
    private static final String TAG = LogUtil.class.getSimpleName();

    public static void d(String msg) {
        Log.d(TAG, msg);
    }
}
