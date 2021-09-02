package com.yocn.meida.base;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    public static Application getAppContext() {
        return mInstance;
    }

}
