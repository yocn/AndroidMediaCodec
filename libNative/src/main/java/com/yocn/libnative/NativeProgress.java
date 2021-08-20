package com.yocn.libnative;

public abstract class NativeProgress {
    private GetProgressCallback getProgressCallback;

    public void setGetProgressCallback(GetProgressCallback getProgressCallback) {
        this.getProgressCallback = getProgressCallback;
    }

    protected void progress(long curr, long total, int percent) {
        if (getProgressCallback != null) {
            getProgressCallback.progress(curr, total, percent);
        }
    }

    public interface GetProgressCallback {
        void progress(long curr, long total, int percent);
    }
}
