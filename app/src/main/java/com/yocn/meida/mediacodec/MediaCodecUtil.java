package com.yocn.meida.mediacodec;

import android.app.Activity;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.yocn.meida.base.Constant;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.IOException;

public class MediaCodecUtil {

    public static boolean supportAvcCodec() {
//        if (Build.VERSION.SDK_INT >= 18) {
        for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                LogUtil.d("yocn", j + ":" + type);
//                    if (type.equalsIgnoreCase(mimeType)) {
//                        return true;
//                    }
            }
        }
//        }
        return false;
    }

    public static void testMediaExtractor(Activity activity) {
        String mp4FilePath = Constant.getTestMp4FilePath();
        if (!FileUtils.fileExists(mp4FilePath)) {
            FileUtils.copyAssetsFile2Phone(activity, "test.mp4", mp4FilePath);
        }
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(mp4FilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.contains("video")) {
                int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                LogUtil.d("yocn", i + "- mime:" + mime + " w/h:" + width + "/" + height);
            }
            long duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
            LogUtil.d("yocn", i + "- mime:" + mime + " duration:" + duration);
            LogUtil.d("yocn", mediaFormat.toString());
        }
    }

    public static void testAACToPCM(Activity activity) {
        String fileName = "shoot.aac";
        String inputFileName = Constant.getMediaCodecDir() + "/shoot.aac";
        String outputFileName = Constant.getMediaCodecDir() + "/shoot.pcm";
        FileUtils.copyAssetsFile2Phone(activity, fileName, inputFileName);
        new AACToPCM().decodeAACToPCM(inputFileName, outputFileName);

    }
}