package com.yocn.meida.presenter.yuv;

import android.content.Context;
import android.os.Message;

import com.yocn.meida.util.BaseMessageLoop;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019-11-07 15:16
 * @ClassName YUVFileWriter
 */
public class YUVFileWriter {
    private String mSavePath;
    private BaseMessageLoop mThread;
    private static final int TYPE_BEGIN = 0;
    private static final int TYPE_WRITE = 1;
    private static final int TYPE_END = 2;

    public YUVFileWriter(Context context, String savePath) {
        mSavePath = savePath;
        mThread = new BaseMessageLoop(context, "write") {
            @Override
            protected boolean recvHandleMessage(Message msg) {
                LogUtil.d("recvHandleMessage:" + msg.what);
                switch (msg.what) {
                    case TYPE_BEGIN:
                        break;
                    case TYPE_WRITE:
                        byte[] frameData = (byte[]) msg.obj;
                        LogUtil.d("写入：" + frameData.length + "   " + getSampleOfFrame(frameData));
                        FileUtils.writeToFile(frameData, savePath, true);
                        break;
                    case TYPE_END:
                        Quit();
                        break;
                    default:
                }
                return false;
            }
        };
        mThread.Run();
    }

    private String getSampleOfFrame(byte[] frame) {
        StringBuilder sb = new StringBuilder();
        int length = frame.length;
        for (int i = 0; i < 10; ++i) {
            int index = i * length / 11;
            if (index >= length) {
                index--;
            }
            sb.append(frame[index]);
        }
        return sb.toString();
    }

    public void startWrite() {
        mThread.sendEmptyMessage(TYPE_BEGIN);
    }

    public void write(byte[] frame) {
        mThread.sendMessage(TYPE_WRITE, 0, 0, frame);
    }

    public void endWrite() {
        mThread.removeMessages(TYPE_WRITE);
        mThread.sendEmptyMessage(TYPE_END);
    }

}
