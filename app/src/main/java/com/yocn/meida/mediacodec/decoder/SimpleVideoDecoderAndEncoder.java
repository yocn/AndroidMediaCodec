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

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleVideoDecoderAndEncoder {
    private long TIMEOUT_US = 10000;
    private int encodeVideoTrackIndex = -1;
    private MediaCodec encodeCodec;
    private MediaMuxer mMediaMuxer;
    private MediaFormat encodeMediaFormat;
    private MediaFormat decodeMediaFormat;

    public interface PreviewCallback {
        void info(int width, int height, int fps);

        void getBitmap(Bitmap bitmap);

        void progress(int progress);
    }

    public void init(String mp4Path, String yuvPath, String outputH264Path, String outputMp4Path, PreviewCallback previewCallback) {
        new Thread(() -> {
            try {
                initInternal(mp4Path, yuvPath, outputH264Path, outputMp4Path, previewCallback);
            } catch (IOException e) {
                LogUtil.d(MediaCodecUtil.TAG, e.toString());
                e.printStackTrace();
            }
        }).start();
    }

    private void initInternal(String mp4Path, String outputYuvPath, String outputH264Path, String outputMp4Path, PreviewCallback previewCallback) throws IOException {
        if (TextUtils.isEmpty(mp4Path)) {
            return;
        }
        FileUtils.deleteFiles(outputYuvPath, outputH264Path, outputMp4Path);

        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(mp4Path);
        int videoTrackIndex = -1;
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                decodeMediaFormat = mediaFormat;
                videoTrackIndex = i;
            }
            LogUtil.d(MediaCodecUtil.TAG, mime);
        }
        if (decodeMediaFormat == null) {
            return;
        }

        int width = decodeMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = decodeMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
        long totalTime = decodeMediaFormat.getLong(MediaFormat.KEY_DURATION);
        int frameRate = decodeMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
        previewCallback.info(width, height, frameRate);
        LogUtil.d(MediaCodecUtil.TAG, "width::" + width + " height::" + height + " totalTime::" + totalTime + " frameRate:" + frameRate);
        // 只会返回此轨道的信息
        mediaExtractor.selectTrack(videoTrackIndex);

        decodeMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        MediaCodec decodeCodec = MediaCodec.createDecoderByType(decodeMediaFormat.getString(MediaFormat.KEY_MIME));
        decodeCodec.configure(decodeMediaFormat, null, null, 0);
        decodeCodec.start();

        ByteBuffer byteBuffer0 = decodeMediaFormat.getByteBuffer("csd-0");
        ByteBuffer byteBuffer1 = decodeMediaFormat.getByteBuffer("csd-1");
        StringBuilder sb0 = new StringBuilder();
        for (byte b : byteBuffer0.array()) {
            sb0.append(b).append(", ");
        }
        StringBuilder sb1 = new StringBuilder();
        for (byte b : byteBuffer1.array()) {
            sb1.append(b).append(", ");
        }
        LogUtil.d(MediaCodecUtil.TAG, sb0.toString());
        LogUtil.d(MediaCodecUtil.TAG, sb1.toString());

        int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
        String codecName = MediaCodecUtil.getExpectedEncodeCodec(MediaFormat.MIMETYPE_VIDEO_AVC, colorFormat);
        if (TextUtils.isEmpty(codecName)) {
            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
            codecName = MediaCodecUtil.getExpectedEncodeCodec(MediaFormat.MIMETYPE_VIDEO_AVC, colorFormat);
        }
        int encodeFrameRate = 20;
        encodeMediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        encodeMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        encodeMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 2500000);
        encodeMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, encodeFrameRate);
        encodeMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        encodeCodec = MediaCodec.createByCodecName(codecName);
        encodeCodec.configure(encodeMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encodeCodec.start();

        mMediaMuxer = new MediaMuxer(outputMp4Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();

        boolean isVideoEOS = false;

        while (!Thread.interrupted()) {
            //将资源传递到解码器
            if (!isVideoEOS) {
                // dequeue:出列，拿到一个输入缓冲区的index，因为有好几个缓冲区来缓冲数据，所以需要先请求拿到一个InputBuffer的index，-1表示暂时没有可用的
                int inputBufferIndex = decodeCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    // 使用返回的inputBuffer的index得到一个ByteBuffer，可以放数据了
                    ByteBuffer inputBuffer = decodeCodec.getInputBuffer(inputBufferIndex);
                    // 使用extractor往MediaCodec的InputBuffer里面写入数据，-1表示已全部读取完
                    int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                    if (sampleSize < 0) {
                        decodeCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isVideoEOS = true;
                    } else {
                        // 填充好的数据写入第inputBufferIndex个InputBuffer，分贝设置size和sampleTime，这里sampleTime不一定是顺序来的，所以需要缓冲区来调节顺序。
                        decodeCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                        // 在MediaExtractor执行完一次readSampleData方法后，需要调用advance()去跳到下一个sample，然后再次读取数据
                        mediaExtractor.advance();
                        isVideoEOS = false;
                    }
                }
            }

            // 获取outputBuffer的index，
            int outputBufferIndex = decodeCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US);
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
                    Image image = decodeCodec.getOutputImage(outputBufferIndex);
                    byte[] i420bytes = CameraUtil.getDataFromImage(image, CameraUtil.COLOR_FormatI420);
                    FileUtils.writeToFile(i420bytes, outputYuvPath, true);

                    boolean end = ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0);
                    encodeData(i420bytes, outputH264Path, currTime, end);

                    byte[] nv21bytes = BitmapUtil.I420Tonv21(i420bytes, width, height);
                    Bitmap bitmap = BitmapUtil.getBitmapImageFromYUV(nv21bytes, width, height);

                    previewCallback.getBitmap(bitmap);
                    int progress = (int) (currTime * 100 / totalTime);
                    previewCallback.progress(progress);

                    LogUtil.v(MediaCodecUtil.TAG, "progress::" + progress);

                    // 将该ByteBuffer释放掉，以供缓冲区的循环使用。
                    decodeCodec.releaseOutputBuffer(outputBufferIndex, true);
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
        decodeCodec.stop();
        decodeCodec.release();
    }

    private void encodeData(byte[] yuvBytes, String outputH264Path, long presentationTimeUs, boolean isVideoEOS) {
        MediaCodec.BufferInfo encodeOutputBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo muxerOutputBufferInfo = new MediaCodec.BufferInfo();
        int inputBufferIndex = encodeCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = encodeCodec.getInputBuffer(inputBufferIndex);
            inputBuffer.put(yuvBytes);
            encodeCodec.queueInputBuffer(inputBufferIndex, 0, yuvBytes.length, presentationTimeUs, 0);
        }

        if ((encodeOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            LogUtil.v(MediaCodecUtil.TAG, " encode  buffer stream end");
        }

        int outputBufferIndex = encodeCodec.dequeueOutputBuffer(encodeOutputBufferInfo, -1);
        switch (outputBufferIndex) {
            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                MediaFormat newFormat = encodeCodec.getOutputFormat();
//                byte[] header_sps = {0, 0, 0, 1, 39, 100, 0, 31, -84, 86, -64, -120, 30, 105, -88, 8, 8, 8, 16};
//                byte[] header_pps = {0, 0, 0, 1, 40, -18, 60, -80};
//                encodeMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
//                encodeMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
//                encodeVideoTrackIndex = mMediaMuxer.addTrack(encodeMediaFormat);
                encodeVideoTrackIndex = mMediaMuxer.addTrack(newFormat);
                mMediaMuxer.start();
                break;
            case MediaCodec.INFO_TRY_AGAIN_LATER:
                break;
            default:
                ByteBuffer outputBuffer = encodeCodec.getOutputBuffer(outputBufferIndex);
                byte[] outData = new byte[encodeOutputBufferInfo.size];
                outputBuffer.get(outData);
                FileUtils.writeToFile(outData, outputH264Path, true);

                MediaFormat outputFormat = encodeCodec.getOutputFormat(outputBufferIndex);
                LogUtil.d("encodeMediaFormat::" + encodeMediaFormat.toString());
                LogUtil.d("outputFormat::" + outputFormat.toString());

                muxerOutputBufferInfo.offset = 0;
                muxerOutputBufferInfo.size = encodeOutputBufferInfo.size;
                muxerOutputBufferInfo.flags = isVideoEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : MediaCodec.BUFFER_FLAG_KEY_FRAME;
                muxerOutputBufferInfo.presentationTimeUs = presentationTimeUs;
                LogUtil.d("presentationTimeUs::" + presentationTimeUs + " size::" + encodeOutputBufferInfo.size + "  isVideoEOS:" + isVideoEOS);
                mMediaMuxer.writeSampleData(encodeVideoTrackIndex, outputBuffer, encodeOutputBufferInfo);

                encodeCodec.releaseOutputBuffer(outputBufferIndex, false);
                break;
        }

    }

}
