package com.yocn.libnative;

import android.util.Log;

public class FFMpegSimpleAudioPlayer extends NativeProgress {
    public void progress(int precent) {
        Log.d("yocnyocn", "precent::" + precent);
    }

    public native void play(String url);

    public native void convert(String src, String out);
}
