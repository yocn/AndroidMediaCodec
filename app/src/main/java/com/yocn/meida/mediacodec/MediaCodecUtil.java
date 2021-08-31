package com.yocn.meida.mediacodec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.yocn.meida.util.LogUtil;

public class MediaCodecUtil {
    public static final String TAG = "MediaCodec";

    public static void echoCodecList() {
        MediaCodecList allMediaCodecLists = new MediaCodecList(-1);
        MediaCodecList regularMediaCodecLists = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        echoMediaLCodecList("all - ", allMediaCodecLists);
        echoMediaLCodecList("regular - ", regularMediaCodecLists);
    }

    private static void echoMediaLCodecList(String tag, MediaCodecList codecList) {
        StringBuilder sb = new StringBuilder(tag);
        for (MediaCodecInfo mediaCodecInfo : codecList.getCodecInfos()) {
            sb.append(mediaCodecInfo.getName()).append(":");
            for (String supportType : mediaCodecInfo.getSupportedTypes()) {
                sb.append("| ").append(supportType);
            }
            sb.append(" ");
        }
        LogUtil.d(TAG, sb.toString());
    }

//    public static MediaCodecInfo getMediaCodecInfo() {
//        MediaCodecInfo result;
//        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_MPEG4, 0, 0);
//        MediaCodecList regularMediaCodecLists = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
//        for (MediaCodecInfo mediaCodecInfo : regularMediaCodecLists.getCodecInfos()) {
//            for (String supportType : mediaCodecInfo.getSupportedTypes()) {
//                MediaCodecInfo.CodecCapabilities codecCapabilities = mediaCodecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_MPEG4);
//            }
//        }
//
//    }

}