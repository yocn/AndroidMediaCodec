package com.yocn.meida.mediacodec.decoder;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.view.Surface;

import com.yocn.meida.mediacodec.MediaCodecUtil;
import com.yocn.meida.util.LogUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SimpleDecodeVideoPlayer {
    private long TIMEOUT_US = 10000;

    public void init(String mp4Path, Surface surface) {
        new Thread(() -> {
            try {
                initInternal(mp4Path, surface);
            } catch (IOException e) {
                LogUtil.d(MediaCodecUtil.TAG, e.toString());
                e.printStackTrace();
            }
        }).start();
    }

    private void initInternal(String mp4Path, Surface surface) throws IOException {
        if (TextUtils.isEmpty(mp4Path)) {
            return;
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
        long time = videoMediaFormat.getLong(MediaFormat.KEY_DURATION);
        LogUtil.d(MediaCodecUtil.TAG, "width::" + width + " height::" + height + " time::" + time);
        // 只会返回此轨道的信息
        mediaExtractor.selectTrack(videoTrackIndex);

//        videoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        MediaCodec videoCodec = MediaCodec.createDecoderByType(videoMediaFormat.getString(MediaFormat.KEY_MIME));
        videoCodec.configure(videoMediaFormat, surface, null, 0);
        videoCodec.start();

        LogUtil.d(MediaCodecUtil.TAG, "getOutputFormat::" + videoCodec.getOutputFormat().toString());

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();

        boolean isVideoEOS = false;

        long startMs = System.currentTimeMillis();
        while (!Thread.interrupted()) {
//            if (!isPlaying) {
//                continue;
//            }
            //将资源传递到解码器
            if (!isVideoEOS) {
                // dequeue:出列，拿到一个输入缓冲区的index，因为有好几个缓冲区来缓冲数据，所以需要先请求拿到一个InputBuffer的index，-1表示暂时没有可用的
                int inputBufferIndex = videoCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    // 使用返回的inputBuffer的index得到一个ByteBuffer，可以放数据了
                    ByteBuffer inputBuffer = videoCodec.getInputBuffer(inputBufferIndex);
                    // 使用extractor往MediaCodec的InputBuffer里面写入数据，-1表示已全部读取完
                    int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                    LogUtil.d(MediaCodecUtil.TAG, "inputBufferIndex::" + inputBufferIndex + " sampleSize::" + sampleSize + " mediaExtractor.getSampleTime()::" + mediaExtractor.getSampleTime());
                    if (sampleSize < 0) {
                        videoCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        isVideoEOS = true;
                    } else {
                        // 填充好的数据写入第inputBufferIndex个InputBuffer，分贝设置size和sampleTime，这里sampleTime不一定是顺序来的，所以需要缓冲区来调节顺序。
                        videoCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize,
                                mediaExtractor.getSampleTime(), 0);
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
                    //直接渲染到Surface时使用不到outputBuffer
                    //ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    //延时操作
                    //如果缓冲区里的可展示时间>当前视频播放的进度，就休眠一下
                    sleepRender(videoBufferInfo, startMs);
                    // 将该ByteBuffer释放掉，以供缓冲区的循环使用。
                    videoCodec.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }

            if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                LogUtil.v(MediaCodecUtil.TAG, "buffer stream end");
                break;
            }
        }//end while
    }

    private void sleepRender(MediaCodec.BufferInfo audioBufferInfo, long startMs) {
        while (audioBufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

}
