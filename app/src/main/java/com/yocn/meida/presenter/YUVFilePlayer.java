package com.yocn.meida.presenter;

import android.content.Context;
import android.os.Message;

import com.yocn.meida.util.BaseMessageLoop;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

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

    public YUVFilePlayer(Context context) {
        messageLoop = new BaseMessageLoop(context, "YUVFilePlayer") {
            @Override
            protected boolean recvHandleMessage(Message msg) {
                switch (msg.what) {
                    case STATUS_PLAY:
                        mCurrentStatus = STATUS_PLAY;
                        break;
                    case STATUS_PAUSE:
                        mCurrentStatus = STATUS_PAUSE;
                        break;
                    case STATUS_STOP:
                        mCurrentStatus = STATUS_IDLE;
                        break;
                    default:
                }
                return false;
            }
        };
        messageLoop.Run();
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

    public boolean isRunning() {
        return mCurrentStatus == STATUS_PLAY;
    }

    public void start() {
        messageLoop.sendEmptyMessage(STATUS_PLAY);
    }

    public void pause() {
        messageLoop.sendEmptyMessage(STATUS_PAUSE);
    }

    public void stop() {
        messageLoop.sendEmptyMessage(STATUS_STOP);
    }
}
