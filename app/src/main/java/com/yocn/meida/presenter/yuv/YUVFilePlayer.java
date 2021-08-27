package com.yocn.meida.presenter.yuv;

import android.content.Context;
import android.graphics.Bitmap;

import com.yocn.libnative.YUVTransUtil;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.util.YUVUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author yocn
 * @Date 2019-11-07 22:38
 * @ClassName YUVFilePlayer
 */
public class YUVFilePlayer {
    public static final int FORMAT_420_888 = 0;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PLAY = 1;
    public static final int STATUS_PAUSE = 2;
    public static final int STATUS_STOP = 3;
    public static final int STATUS_ERROR = 4;

    public static final int ROTATE_0 = 0;
    public static final int ROTATE_90 = 90;
    public static final int ROTATE_180 = 180;
    public static final int ROTATE_270 = 270;

    public static final List<Integer> mRotateTextList = new ArrayList<>();
    public static final List<String> mFormatTextList = new ArrayList<>();

    private String mYuvFilePath;
    private int mWidth;
    private int mHeight;
    private long fileLength;
    private int chunkSize;
    private int mTotalFrames;

    private int mCurrentFrame;
    private int format = FORMAT_420_888;
    private int fps = 40;
    private boolean looping = true;
    private int rotate = ROTATE_0;

    private boolean isThreadRunning = false;
    private boolean isYuvPlaying = false;
    private Thread mPlayThread;

    private long mTotalProcessTimes = 0;

    RandomAccessFile mRandomAccessFile;
    private OnYuvPlayCallbackInterface mOnGetBitmapInterface;

    static {
        mRotateTextList.clear();
        mRotateTextList.add(0);
        mRotateTextList.add(90);
        mRotateTextList.add(180);
        mRotateTextList.add(270);

        mFormatTextList.clear();
        mFormatTextList.add("YUV_I420_888");
        mFormatTextList.add("YUV_NV21");
        mFormatTextList.add("YUV_NV12");
    }

    public interface OnYuvPlayCallbackInterface {
        void getBitmap(Bitmap bitmap);

        void playStatus(int status);
    }

    public void setYuvCallback(OnYuvPlayCallbackInterface onGetBitmapInterface) {
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

        data = new byte[chunkSize];
        argbRotateBytes = new byte[mWidth * mHeight * 4];
        argbBytes = new byte[mWidth * mHeight * 4];
        LogUtil.d("format::" + format + " w/h:" + width + "/" + height + " chunkSize:" + chunkSize + " fileLength:" + fileLength + " mTotalFrames:" + mTotalFrames);
        return this;
    }

    public YUVFilePlayer setFPS(int fps) {
        this.fps = fps;
        LogUtil.d("fps:" + fps);
        return this;
    }

    byte[] data;
    byte[] argbRotateBytes;
    byte[] argbBytes;

    private Bitmap bitmap;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (isThreadRunning) {
                long timeBegin = System.currentTimeMillis();
                if (isYuvPlaying) {
                    LogUtil.d("isYuvPlaying:" + isYuvPlaying);
                    if (mRandomAccessFile == null) {
                        return;
                    }
                    try {
                        mRandomAccessFile.seek(mCurrentFrame * chunkSize);
                        mRandomAccessFile.read(data, 0, chunkSize);

//                    byte[] rotateData = new byte[chunkSize];
//                    YUVTransUtil.getInstance().rotateI420Full(data, rotateData, mWidth, mHeight, 180);

                        YUVTransUtil.getInstance().I420ToArgb(data, chunkSize, argbBytes,
                                mWidth * 4, 0, 0, mWidth, mHeight, mWidth, mHeight, 0, 0);

                        if (rotate == ROTATE_0) {
                            bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbBytes));
                        } else {
                            if (rotate == ROTATE_180) {
                                bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                                YUVTransUtil.getInstance().ARGBRotate(argbBytes, mWidth * 4, argbRotateBytes, mWidth * 4, mWidth, mHeight, rotate);
                            } else {
                                bitmap = Bitmap.createBitmap(mHeight, mWidth, Bitmap.Config.ARGB_8888);
                                YUVTransUtil.getInstance().ARGBRotate(argbBytes, mWidth * 4, argbRotateBytes, mHeight * 4, mWidth, mHeight, rotate);
                            }
                            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbRotateBytes));
                        }

//                        String path = Constant.getCacheYuvDir() + "/" + mCurrentFrame + ".yuv";
//                        FileUtils.writeToFile(data, path, false);
//                        String bitmapPath = Constant.getCacheYuvDir() + "/" + mCurrentFrame + ".png";
//                        BitmapUtil.saveBitmap(bitmapPath, bitmap);

                        if (mOnGetBitmapInterface != null) {
                            mOnGetBitmapInterface.getBitmap(bitmap);
                        }
                        if (++mCurrentFrame >= mTotalFrames) {
                            //如果需要循环播放就不停止
                            isYuvPlaying = looping;
                            mCurrentFrame = 0;
                            mTotalProcessTimes = 0;
                            if (mOnGetBitmapInterface != null) {
                                if (looping) {
                                    mOnGetBitmapInterface.playStatus(STATUS_PLAY);
                                } else {
                                    mOnGetBitmapInterface.playStatus(STATUS_PAUSE);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                long timeEnd = System.currentTimeMillis();
                int peocessTime = (int) (timeEnd - timeBegin);
                mTotalProcessTimes += peocessTime;
                int averageTime = mCurrentFrame == 0 ? 0 : (int) (mTotalProcessTimes / mCurrentFrame);

                if (fps == 0) {
                    fps = 1;
                }
                int sleep = 1000 / fps;
                if (isYuvPlaying) {
                    LogUtil.d(" 平均处理时间->" + averageTime + "当前处理时间：" + peocessTime);
                }
                //如果处理时间超过了实际fps时间，就不sleep直接处理下一帧。
                sleep = peocessTime > sleep ? 0 : (sleep - peocessTime);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean isRunning() {
        return isYuvPlaying;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public void start() {
        try {
            mRandomAccessFile = new RandomAccessFile(mYuvFilePath, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (mOnGetBitmapInterface != null) {
                mOnGetBitmapInterface.playStatus(STATUS_ERROR);
            }
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
        if (mOnGetBitmapInterface != null) {
            Bitmap bitmap = YUVUtils.getFirstFrame(mYuvFilePath, mWidth, mHeight, rotate);
            mOnGetBitmapInterface.getBitmap(bitmap);
            if (mOnGetBitmapInterface != null) {
                mOnGetBitmapInterface.playStatus(STATUS_STOP);
            }
        }
    }

    public void release() {
        isThreadRunning = false;
    }
}
