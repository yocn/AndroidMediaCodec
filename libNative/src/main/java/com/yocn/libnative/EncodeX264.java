package com.yocn.libnative;

/**
 * @Author yocn
 * @Date 2019-11-06 16:10
 * @ClassName EncodeX264
 */
public class EncodeX264 {
    static {
        System.loadLibrary("Native");
    }

    static EncodeX264 mX264Translater;

    public static EncodeX264 getInstance() {
        if (mX264Translater == null) {
            mX264Translater = new EncodeX264();
        }
        return mX264Translater;
    }

    public native void initX264Encoder(int width, int height, int fps, int bite);

}
