package com.yocn.meida.mediacodec;

import android.app.Activity;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.yocn.meida.base.Constant;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TextMediaExtractor {
    private static final String TAG = "TextMediaExtractor";

    public static void separate(Activity activity) {
        String mp4FilePath = Constant.getTestMp4FilePath();
//        if (!FileUtils.fileExists(mp4FilePath)) {
//            FileUtils.copyAssetsFile2Phone(activity, "test.mp4", mp4FilePath);
//        }
        File mFile = new File(mp4FilePath);
        if (!mFile.exists()) {
            Log.e(TAG, "mp4文件不存在");
            return;
        }
        MediaExtractor extractor = new MediaExtractor();//实例一个MediaExtractor
        try {
            extractor.setDataSource(mFile.getAbsolutePath());//设置添加MP4文件路径
        } catch (IOException e) {
            e.printStackTrace();
        }
        int trackCount = extractor.getTrackCount();//获得通道数量
        int videoTrackIndex = 0;//视频轨道索引
        MediaFormat videoMediaFormat = null;//视频格式
        int audioTrackIndex = 0;//音频轨道索引
        MediaFormat audioMediaFormat = null;

        /**
         * 查找需要的视频轨道与音频轨道index
         */
        for (int i = 0; i < trackCount; i++) { //遍历所以轨道
            MediaFormat itemMediaFormat = extractor.getTrackFormat(i);
            String itemMime = itemMediaFormat.getString(MediaFormat.KEY_MIME);
            if (itemMime.startsWith("video")) { //获取视频轨道位置
                videoTrackIndex = i;
                videoMediaFormat = itemMediaFormat;
                continue;
            }
            if (itemMime.startsWith("audio")) { //获取音频轨道位置
                audioTrackIndex = i;
                audioMediaFormat = itemMediaFormat;
                continue;
            }
        }

        File videoFile = new File(Constant.getTestFilePath("test.h264"));
        File audioFile = new File(Constant.getTestFilePath("test.aac"));
        if (videoFile.exists()) {
            videoFile.delete();
        }
        if (audioFile.exists()) {
            audioFile.delete();
        }

        try {
            FileOutputStream videoOutputStream = new FileOutputStream(videoFile);
            FileOutputStream audioOutputStream = new FileOutputStream(audioFile);

            /**
             * 分离视频
             */
            int maxVideoBufferCount = videoMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);//获取视频的输出缓存的最大大小
            ByteBuffer videoByteBuffer = ByteBuffer.allocate(maxVideoBufferCount);
            extractor.selectTrack(videoTrackIndex);//选择到视频轨道
            int len = 0;
            while ((len = extractor.readSampleData(videoByteBuffer, 0)) != -1) {
                byte[] bytes = new byte[len];
                videoByteBuffer.get(bytes);//获取字节
                videoOutputStream.write(bytes);//写入字节
                videoByteBuffer.clear();
                extractor.advance();//预先加载后面的数据
            }
            videoOutputStream.flush();
            videoOutputStream.close();
            extractor.unselectTrack(videoTrackIndex);//取消选择视频轨道

            /**
             * 分离音频
             */
            int maxAudioBufferCount = audioMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);//获取音频的输出缓存的最大大小
            ByteBuffer audioByteBuffer = ByteBuffer.allocate(maxAudioBufferCount);
            extractor.selectTrack(audioTrackIndex);//选择音频轨道
            len = 0;
            while ((len = extractor.readSampleData(audioByteBuffer, 0)) != -1) {
                byte[] bytes = new byte[len];
                audioByteBuffer.get(bytes);


                /**
                 * 添加adts头
                 */
                byte[] adtsData = new byte[len + 7];
                addADTStoPacket(adtsData, len + 7);
                System.arraycopy(bytes, 0, adtsData, 7, len);

                audioOutputStream.write(bytes);
                audioByteBuffer.clear();
                extractor.advance();
            }

            audioOutputStream.flush();
            audioOutputStream.close();


        } catch (FileNotFoundException e) {
            Log.e(TAG, "separate: 错误原因=" + e);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        extractor.release();//释放资源
    }

    private static void addADTStoPacket(byte[] packet, int packetLen) {
        /*
        标识使用AAC级别 当前选择的是LC
        一共有1: AAC Main 2:AAC LC (Low Complexity) 3:AAC SSR (Scalable Sample Rate) 4:AAC LTP (Long Term Prediction)
        */
        int profile = 2;
        int frequencyIndex = 0x04; //设置采样率
        int channelConfiguration = 2; //设置频道,其实就是声道

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (frequencyIndex << 2) + (channelConfiguration >> 2));
        packet[3] = (byte) (((channelConfiguration & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public static void combine() {
        String tarMp4FilePath = Constant.getTestFilePath("combine.mp4");
        if (FileUtils.fileExists(tarMp4FilePath)) {
            FileUtils.deleteFile(tarMp4FilePath);
        }
        try {
            MediaMuxer mediaMuxer = new MediaMuxer(tarMp4FilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaExtractor videoExtractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(Constant.getTestFilePath("test.h264"));
            audioExtractor.setDataSource(Constant.getTestFilePath("test.aac"));
            int videoCount = videoExtractor.getTrackCount();
            int audioCount = audioExtractor.getTrackCount();
            for (int i = 0; i < videoCount; i++) {
                MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
                LogUtil.d("yocn", "video:" + mediaFormat.toString());
            }
            for (int i = 0; i < audioCount; i++) {
                MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
                LogUtil.d("yocn", "audio:" + mediaFormat.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}