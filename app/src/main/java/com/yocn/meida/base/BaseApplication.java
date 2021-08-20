package com.yocn.meida.base;

import android.app.Application;

import com.yocn.meida.util.FileUtils;

/**
 * @Author yocn
 * @Date 2019-11-07 17:25
 * @ClassName BaseApplication
 */
public class BaseApplication extends Application {
    private static BaseApplication mInstance;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
        FileUtils.copyAssets();
    }

    public static Application getAppContext() {
        return mInstance;
    }

}
