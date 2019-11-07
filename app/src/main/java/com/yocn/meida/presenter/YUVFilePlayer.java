package com.yocn.meida.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import com.yocn.meida.base.Constant;
import com.yocn.meida.util.BaseMessageLoop;
import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author yocn
 * @Date 2019-11-07 22:38
 * @ClassName YUVFilePlayer
 */
public class YUVFilePlayer {
    public static final int FORMAT_420_888 = 0;

    private final int STATUS_IDLE = 0;
    private final int STATUS_PLAY = 1;
    private final int STATUS_PAUSE = 2;
    private final int STATUS_STOP = 3;

    private int mCurrentStatus = STATUS_IDLE;

    private BaseMessageLoop messageLoop;

    private String mYuvFilePath;
    private int mWidth;
    private int mHeight;
    private long fileLength;
    private int chunkSize;
    private int mTotalFrames;

    private int mCurrentFrame;
    private int format = FORMAT_420_888;
    private int fps = 25;

    private boolean isThreadRunning = false;
    private boolean isYuvPlaying = false;
    private Thread mPlayThread;

    RandomAccessFile mRandomAccessFile;
    private OnGetBitmapInterface mOnGetBitmapInterface;

    public interface OnGetBitmapInterface {
        void getBitmap(Bitmap bitmap);
    }

    public void setBitmapInterface(OnGetBitmapInterface onGetBitmapInterface) {
        mOnGetBitmapInterface = onGetBitmapInterface;
    }

    public YUVFilePlayer(Context context) {
        mPlayThread = new Thread(runnable);
        isThreadRunning = true;
        mPlayThread.start();
    }

    public YUVFilePlayer setFilePath(String path) {
        mYuvFilePath = path;
        fileLength = FileUtils.getFileLength(path);
        if (chunkSize != 0) {
            mTotalFrames = (int) (fileLength / chunkSize);
        }
        LogUtil.d("path:" + path);
        LogUtil.d("fileLength:" + fileLength);
        return this;
    }

    public YUVFilePlayer setWH(int width, int height) {
        mWidth = width;
        mHeight = height;
        if (format == FORMAT_420_888) {
            chunkSize = width * height * 3 / 2;
        }
        if (fileLength != 0) {
            mTotalFrames = (int) (fileLength / chunkSize);
        }
        LogUtil.d("chunkSize:" + chunkSize + " fileLength:" + fileLength + " mTotalFrames:" + mTotalFrames);
        return this;
    }

    public YUVFilePlayer setFPS(int fps) {
        this.fps = fps;
        return this;
    }

    private Runnable runnable = () -> {
        while (isThreadRunning) {
            if (isYuvPlaying) {
                LogUtil.d("isYuvPlaying:" + isYuvPlaying);
                if (mRandomAccessFile == null) {
                    return;
                }
                byte[] data = new byte[chunkSize];
                try {
                    long totalLength = mRandomAccessFile.length();
                    LogUtil.d("mCurrentFrame:" + mCurrentFrame + " mTotalFrames->" + mTotalFrames + " totalLength:" + totalLength);
                    mRandomAccessFile.seek(mCurrentFrame * chunkSize);
                    mRandomAccessFile.read(data, 0, chunkSize);
                    String path = Constant.getCacheYuvDir() + "/" + mCurrentFrame + ".yuv";
                    FileUtils.writeToFile(data, path, false);

                    byte[] nv21bytes = BitmapUtil.I420Tonv21(data, mWidth, mHeight);
                    Bitmap bitmap = BitmapUtil.getBitmapImageFromYUV(nv21bytes, mWidth, mHeight);
                    if (mOnGetBitmapInterface != null) {
                        mOnGetBitmapInterface.getBitmap(bitmap);
                    }
                    if (++mCurrentFrame >= mTotalFrames) {
                        isYuvPlaying = false;
                        mCurrentFrame = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(1000 / fps);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public boolean isRunning() {
        return isYuvPlaying;
    }

    public void start() {
        try {
            mRandomAccessFile = new RandomAccessFile(mYuvFilePath, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.d("e->" + e.getMessage());
        }
        isYuvPlaying = true;
        LogUtil.d("start:" + isYuvPlaying);
    }

    public void pause() {
        LogUtil.d("pause");
        isYuvPlaying = false;
    }

    public void stop() {
    }
}
