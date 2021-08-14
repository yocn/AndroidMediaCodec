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
        new TestFFmpeg().init(mp4FilePath);
    }
}
