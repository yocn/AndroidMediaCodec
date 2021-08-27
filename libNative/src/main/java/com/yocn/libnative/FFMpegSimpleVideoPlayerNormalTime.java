package com.yocn.libnative;

public class FFMpegSimpleVideoPlayerNormalTime extends NativeProgress {
    public native void play(String url, Object surface);
}
