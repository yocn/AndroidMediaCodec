package com.yocn.meida.presenter;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Message;

import com.yocn.meida.util.BaseMessageLoop;
import com.yocn.meida.util.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Mp4Writer {
    private String mSavePath;
    private final BaseMessageLoop mThread;
    private static final int TYPE_BEGIN = 0;
    private static final int TYPE_WRITE = 1;
    private static final int TYPE_END = 2;
    private MediaMuxer mMediaMuxer;
    private int encodeVideoTrackIndex;
    private MediaCodec mediaCodec;
    private volatile boolean isRunning = false;

    public Mp4Writer(Context context, int width, int height, String savePath) {
        this.mSavePath = savePath;

        mThread = new BaseMessageLoop(context, "write") {
            @Override
            protected boolean recvHandleMessage(Message msg) {
                LogUtil.d("recvHandleMessage:" + msg.what);
                switch (msg.what) {
                    case TYPE_BEGIN:
                        try {
                            init(width, height, savePath);
                            isRunning = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            stopAndRelease();
                            Quit();
                        }
                        break;
                    case TYPE_WRITE:
                        if (isRunning) {
                            byte[] frameData = (byte[]) msg.obj;
                        }
                        break;
                    case TYPE_END:
                        if (isRunning) {
                            stopAndRelease();
                            isRunning = false;
                            Quit();
                        }
                        break;
                    default:
                }
                return false;
            }
        };
        mThread.Run();
    }

    private void init(int width, int height, String savePath) throws IOException {
        int framerate = 30;
        int bitrate = width * height * 5;
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
//        byte[] header_sps = {0, 0, 0, 1, 39, 100, 0, 31, -84, 86, -64, -120, 30, 105, -88, 8, 8, 8, 16};
//        byte[] header_pps = {0, 0, 0, 1, 40, -18, 60, -80};
//        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//        mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);//设置颜色格式对应NV21
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);//帧传输速率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);//设置fps，一般20 或者30
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);//关键帧的延时

        mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        encodeVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
        mMediaMuxer.start();

        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
    }

    private void stopAndRelease() {
        mMediaMuxer.stop();
        mMediaMuxer.release();
        mediaCodec.stop();
        mediaCodec.release();
    }

    private void write() {
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
//        if (inputBufferIndex >= 0) {
//            // 使用返回的inputBuffer的index得到一个ByteBuffer，可以放数据了
//            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
//            // 使用extractor往MediaCodec的InputBuffer里面写入数据，-1表示已全部读取完
//            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
//            if (sampleSize < 0) {
//                mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
//                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//            } else {
//                write(encodeBufferInfo, sampleSize, encodeVideoTrackIndex, inputBuffer, mMediaMuxer, frameRate, false);
//                // 填充好的数据写入第inputBufferIndex个InputBuffer，分贝设置size和sampleTime，这里sampleTime不一定是顺序来的，所以需要缓冲区来调节顺序。
//                mediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
//                // 在MediaExtractor执行完一次readSampleData方法后，需要调用advance()去跳到下一个sample，然后再次读取数据
//                mediaExtractor.advance();
//            }
//        }
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
