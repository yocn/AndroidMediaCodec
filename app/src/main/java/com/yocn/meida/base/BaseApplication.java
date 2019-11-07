package com.yocn.meida.base;

import android.app.Application;

/**
 * @Author yocn
 * @Date 2019-11-07 17:25
 * @ClassName BaseApplication
 */
public class BaseApplication extends Application {
    private static BaseApplication mInstance;

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
    }

    public static Application getAppContext() {
        return mInstance;
    }
}
