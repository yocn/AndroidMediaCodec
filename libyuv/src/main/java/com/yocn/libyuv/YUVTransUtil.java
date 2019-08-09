package com.yocn.libyuv;

/**
 * @Author yocn
 * @Date 2019/8/9 3:40 PM
 * @ClassName YUVTransUtil
 */
public class YUVTransUtil {
    static {
        System.loadLibrary("YUVTrans");
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
