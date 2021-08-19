package com.yocn.libnative;

public class FFMpegSimpleAudioPlayer {
    public native void playAudio(String url);

    public native void convert(String src, String out);
}
