package com.yocn.libnative;

public class FFMpegSimpleAudioPlayer extends NativeProgress {
    @Override
    protected void progress(long curr, long total, int percent) {
        super.progress(curr, total, percent);
    }

    public void play(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playJni(url);
            }
        }).start();
    }

    public void convert(final String src, final String pcmOut, final String mp3Out) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                convertJni(src, pcmOut, mp3Out);
            }
        }).start();
    }

    public native void playJni(String url);

    public native void convertJni(String src, String pcmOut, String mp3Out);
}
