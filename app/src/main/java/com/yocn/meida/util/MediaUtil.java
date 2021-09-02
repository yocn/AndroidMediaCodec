package com.yocn.meida.util;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.yocn.meida.base.BaseApplication;
import com.yocn.meida.bean.WH;

import java.io.File;

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

    public static void playVideo(Activity activity, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String path = Environment.getExternalStorageDirectory().getPath()+ "/1.mp4";//该路径可以自定义
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "video/*");
        activity.startActivity(intent);
    }
}
