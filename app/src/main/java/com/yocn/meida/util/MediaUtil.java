package com.yocn.meida.util;

import android.media.MediaMetadataRetriever;

import com.yocn.meida.bean.WH;

public class MediaUtil {
    public static WH get(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String widthS = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); //宽
        String heightS = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); //高
//        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);//视频的方向角度
//        long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;//视频的长度
        int width = Integer.parseInt(widthS);
        int height = Integer.parseInt(heightS);
        return new WH(width, height);
    }
}
