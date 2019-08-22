package com.yocn.libnative;

/**
 * @Author yocn
 * @Date 2019/8/9 3:40 PM
 * @ClassName YUVTransUtil
 */
public class X264Translater {
    static {
        System.loadLibrary("Native");
    }

    static X264Translater mX264Translater;

    public static X264Translater getInstance() {
        if (mX264Translater == null) {
            mX264Translater = new X264Translater();
        }
        return mX264Translater;
    }


    public native void initX264Encoder(int width, int height, int fps, int bite);

}
