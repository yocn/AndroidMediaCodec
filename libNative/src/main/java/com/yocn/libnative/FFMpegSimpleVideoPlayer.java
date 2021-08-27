package com.yocn.libnative;

public class FFMpegSimpleVideoPlayer extends NativeProgress{
    public native void play(String src, String tar, Object surface);
}
