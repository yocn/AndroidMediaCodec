package com.yocn.libnative;

public class TestFFmpeg {
    public native void init(String url, String out);
    public native void decode2Yuv(String url, String out);
}
