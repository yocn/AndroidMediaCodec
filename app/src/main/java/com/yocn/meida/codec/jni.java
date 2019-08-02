package com.yocn.meida.codec;

/**
 * @Author yocn
 * @Date 2019/8/2 11:03 AM
 * @ClassName jni
 */
public class jni {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
