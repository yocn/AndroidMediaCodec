package com.yocn.libnative;

public class FFMpegSimpleAudioVideoPlayer extends NativeProgress {
    @Override
    protected void progress(long curr, long total, int percent) {
        super.progress(curr, total, percent);
    }

    public void play(final String url, final Object surface) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playJni(url, surface);
            }
        }).start();
    }

    public native void playJni(String url, Object surface);
}
