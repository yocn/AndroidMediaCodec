package com.yocn.meida.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import com.yocn.libnative.YUVTransUtil;
import com.yocn.meida.base.Constant;
import com.yocn.meida.util.BaseMessageLoop;
import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

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

    public static final int ROTATE_0 = 0;
    public static final int ROTATE_90 = 90;
    public static final int ROTATE_180 = 180;
    public static final int ROTATE_270 = 270;

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
    private boolean looping = true;
    private int rotate = ROTATE_90;

    private boolean isThreadRunning = false;
    private boolean isYuvPlaying = false;
    private Thread mPlayThread;

    private long mTotalProcessTimes = 0;

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
            long timeBegin = System.currentTimeMillis();
            if (isYuvPlaying) {
                LogUtil.d("isYuvPlaying:" + isYuvPlaying);
                if (mRandomAccessFile == null) {
                    return;
                }
                byte[] data = new byte[chunkSize];
                try {
                    mRandomAccessFile.seek(mCurrentFrame * chunkSize);
                    mRandomAccessFile.read(data, 0, chunkSize);

                    byte[] argbBytes = new byte[mWidth * mHeight * 4];
                    Bitmap bitmap;
                    YUVTransUtil.getInstance().I420ToArgb(data, chunkSize, argbBytes,
                            mWidth * 4, 0, 0, mWidth, mHeight, mWidth, mHeight, 0, 0);
                    if (rotate == ROTATE_0) {
                        bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbBytes));
                    } else {
                        bitmap = Bitmap.createBitmap(mHeight, mWidth, Bitmap.Config.ARGB_8888);
                        byte[] argbRotateBytes = new byte[mWidth * mHeight * 4];
                        YUVTransUtil.getInstance().ARGBRotate(argbBytes, mWidth * 4, argbRotateBytes, mHeight * 4, mWidth, mHeight, 90);
                        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbRotateBytes));
                    }

//                    byte[] nv21bytes = BitmapUtil.I420Tonv21(data, mWidth, mHeight);
//                    Bitmap bitmap = BitmapUtil.getBitmapImageFromYUV(nv21bytes, mWidth, mHeight);

//                    String path = Constant.getCacheYuvDir() + "/" + mCurrentFrame + ".yuv";
//                    FileUtils.writeToFile(data, path, false);
//                    String bitmapPath = Constant.getCacheYuvDir() + "/" + mCurrentFrame + ".png";
//                    BitmapUtil.saveBitmap(bitmapPath, bitmap);

                    if (mOnGetBitmapInterface != null) {
                        mOnGetBitmapInterface.getBitmap(bitmap);
                    }
                    if (++mCurrentFrame >= mTotalFrames) {
                        //如果需要循环播放就不停止
                        isYuvPlaying = looping;
                        mCurrentFrame = 0;
                        mTotalProcessTimes = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            long timeEnd = System.currentTimeMillis();
            int peocessTime = (int) (timeEnd - timeBegin);
            mTotalProcessTimes += peocessTime;
            int averageTime = mCurrentFrame == 0 ? 0 : (int) (mTotalProcessTimes / mCurrentFrame);

            int sleep = 1000 / fps;
            LogUtil.d(" 平均处理时间->" + averageTime + "当前处理时间：" + peocessTime);
            //如果处理时间超过了实际fps时间，就不sleep直接处理下一帧。
            sleep = peocessTime > sleep ? 0 : (sleep - peocessTime);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public boolean isRunning() {
        return isYuvPlaying;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public void setLooping(boolean looping){
        this.looping = looping;
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
        isYuvPlaying = false;
        mCurrentFrame = 0;
    }
}
