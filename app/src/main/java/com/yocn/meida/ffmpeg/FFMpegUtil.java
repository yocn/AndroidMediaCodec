package com.yocn.meida.ffmpeg;

import android.app.Activity;

import com.yocn.libnative.TestFFmpeg;
import com.yocn.meida.base.Constant;
import com.yocn.meida.util.FileUtils;

public class FFMpegUtil {
    public void test(Activity activity) {
        String mp4FilePath = Constant.getTestMp4FilePath();
        if (!FileUtils.fileExists(mp4FilePath)) {
            FileUtils.copyAssetsFile2Phone(activity, "test.mp4", mp4FilePath);
        }
        String outYuvFilePath = Constant.getOutTestMp4FilePath();
        new TestFFmpeg().decode2Yuv(mp4FilePath, outYuvFilePath);
//        new TestFFmpeg().decode2Yuv("/sdcard/ac3/ac3-mkv.mkv", "/sdcard/ac3/ac3-mkv2.yuv");
    }
}
