package com.yocn.libnative;

/**
 * @Author yocn
 * @Date 2019-11-06 16:10
 * @ClassName X264Encoder
 */
public class X264Encoder extends NativeProgress {

    public void initEncoder(final String yuvPath, final String x264Path, final int width, final int height, final int fps) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                init(yuvPath, x264Path, width, height, fps);
            }
        }).start();
    }

    public native void init(String yuvPath, String x264Path, int width, int height, int fps);

}
