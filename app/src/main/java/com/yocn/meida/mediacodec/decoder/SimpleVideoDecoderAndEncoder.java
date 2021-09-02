package com.yocn.meida.mediacodec.decoder;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.TextUtils;

import com.yocn.meida.mediacodec.MediaCodecUtil;
import com.yocn.meida.util.BitmapUtil;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleVideoDecoderAndEncoder {
    private long TIMEOUT_US = 10000;

    public interface PreviewCallback {
        void info(int width, int height, int fps);

        void getBitmap(Bitmap bitmap);

        void progress(int progress);
    }

    public void init(String mp4Path, String yuvPath, String outputMp4Path, PreviewCallback previewCallback) {
        new Thread(() -> {
            try {
                initInternal(mp4Path, yuvPath, outputMp4Path, previewCallback);
            } catch (IOException e) {
                LogUtil.d(MediaCodecUtil.TAG, e.toString());
                e.printStackTrace();
            }
        }).start();
    }

    private void initInternal(String mp4Path, String yuvPath, String outputMp4Path, PreviewCallback previewCallback) throws IOException {
        if (TextUtils.isEmpty(mp4Path)) {
            return;
        }
        File file = new File(yuvPath);
        if (file.exists()) {
            file.delete();
        }

        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(mp4Path);
        MediaFormat videoMediaFormat = null;
        int videoTrackIndex = -1;
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                videoMediaFormat = mediaFormat;
                videoTrackIndex = i;
            }
            LogUtil.d(MediaCodecUtil.TAG, mime);
        }
        if (videoMediaFormat == null) {
            return;
        }

        int width = videoMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = videoMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        long totalTime = videoMediaFormat.getLong(MediaFormat.KEY_DURATION);
        int frameRate = videoMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
        previewCallback.info(width, height, frameRate);
        LogUtil.d(MediaCodecUtil.TAG, "width::" + width + " height::" + height + " totalTime::" + totalTime + " frameRate:" + frameRate);
        // 只会返回此轨道的信息
        mediaExtractor.selectTrack(videoTrackIndex);

//        videoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        MediaCodec videoCodec = MediaCodec.createDecoderByType(videoMediaFormat.getString(MediaFormat.KEY_MIME));
        videoCodec.configure(videoMediaFormat, null, null, 0);
        videoCodec.start();

        LogUtil.d(MediaCodecUtil.TAG, "getOutputFormat::" + videoCodec.getOutputFormat().toString());

        MediaMuxer mMediaMuxer = new MediaMuxer(outputMp4Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
//        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        MediaFormat mediaFormat = videoMediaFormat;
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        mediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, 30);
        int encodeVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
        mMediaMuxer.start();

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo encodeBufferInfo = new MediaCodec.BufferInfo();

        boolean isVideoEOS = false;

        while (!Thread.interrupted()) {
            //将资源传递到解码器
            if (!isVideoEOS) {
                // dequeue:出列，拿到一个输入缓冲区的index，因为有好几个缓冲区来缓冲数据，所以需要先请求拿到一个InputBuffer的index，-1表示暂时没有可用的
                int inputBufferIndex = videoCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    // 使用返回的inputBuffer的index得到一个ByteBuffer，可以放数据了
                    ByteBuffer inputBuffer = videoCodec.getInputBuffer(inputBufferIndex);
                    // 使用extractor往MediaCodec的InputBuffer里面写入数据，-1表示已全部读取完
                    int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        videoCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isVideoEOS = true;
                    } else {
                        write(encodeBufferInfo, sampleSize, encodeVideoTrackIndex, inputBuffer, mMediaMuxer, frameRate, false);
                        // 填充好的数据写入第inputBufferIndex个InputBuffer，分贝设置size和sampleTime，这里sampleTime不一定是顺序来的，所以需要缓冲区来调节顺序。
                        videoCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                        // 在MediaExtractor执行完一次readSampleData方法后，需要调用advance()去跳到下一个sample，然后再次读取数据
                        mediaExtractor.advance();
                        isVideoEOS = false;
                    }
                }
            }

            // 获取outputBuffer的index，
            int outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    LogUtil.v(MediaCodecUtil.TAG, outputBufferIndex + " format changed");
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    LogUtil.v(MediaCodecUtil.TAG, outputBufferIndex + " 解码当前帧超时");
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    //outputBuffers = videoCodec.getOutputBuffers();
                    LogUtil.v(MediaCodecUtil.TAG, outputBufferIndex + " output buffers changed");
                    break;
                default:
                    long currTime = videoBufferInfo.presentationTimeUs;
                    Image image = videoCodec.getOutputImage(outputBufferIndex);
                    byte[] i420bytes = CameraUtil.getDataFromImage(image, CameraUtil.COLOR_FormatI420);
                    FileUtils.writeToFile(i420bytes, yuvPath, true);

                    byte[] nv21bytes = BitmapUtil.I420Tonv21(i420bytes, width, height);
//                  BitmapUtil.dumpFile("mnt/sdcard/1.yuv", i420bytes);
                    Bitmap bitmap = BitmapUtil.getBitmapImageFromYUV(nv21bytes, width, height);

                    previewCallback.getBitmap(bitmap);
                    previewCallback.progress((int) (currTime * 100 / totalTime));
                    // 将该ByteBuffer释放掉，以供缓冲区的循环使用。
                    videoCodec.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }

            if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                LogUtil.v(MediaCodecUtil.TAG, "buffer stream end");
                break;
            }
        }//end while

        mMediaMuxer.stop();
        mMediaMuxer.release();
        mediaExtractor.release();
        videoCodec.stop();
        videoCodec.release();
    }

    private void write(MediaCodec.BufferInfo encodeBufferInfo, int sampleSize, int encodeVideoTrackIndex,
                       ByteBuffer inputBuffer, MediaMuxer mMediaMuxer, int frameRate, boolean isEnd) {
        encodeBufferInfo.offset = 0;
        encodeBufferInfo.size = sampleSize;
        encodeBufferInfo.flags = isEnd ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : MediaCodec.BUFFER_FLAG_KEY_FRAME;
        encodeBufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;
        mMediaMuxer.writeSampleData(encodeVideoTrackIndex, inputBuffer, encodeBufferInfo);
    }
}
