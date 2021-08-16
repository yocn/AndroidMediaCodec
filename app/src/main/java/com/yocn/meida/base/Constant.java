package com.yocn.meida.base;

import com.yocn.meida.util.FileUtils;

/**
 * @Author yocn
 * @Date 2019-11-07 17:23
 * @ClassName Constant
 */
public class Constant {

    private static String PATH_YUV = "/yuv";
    private static String PATH_MEDIA_CODEC = "/media_codec";

    public static String getRootPath() {
        return BaseApplication.getAppContext().getExternalFilesDir(null).getAbsolutePath();
    }

    public static String getMediaCodecDir() {
        String path = getRootPath() + PATH_MEDIA_CODEC;
        FileUtils.checkDir(path);
        return path;
    }

    public static String getCacheYuvDir() {
        String path = getRootPath() + PATH_YUV;
        FileUtils.checkDir(path);
        return path;
    }

    public static String getTestYuvFilePath() {
        String path = getRootPath() + PATH_YUV;
        FileUtils.checkDir(path);
        String yuvFile = path + "/test.yuv";
        return yuvFile;
    }

    public static String getTestMp4FilePath() {
        return getMediaCodecDir() + "/test.mp4";
    }

    public static String getOutTestMp4FilePath() {
        return getMediaCodecDir() + "/test.yuv";
    }

    public static String getTestFilePath(String fileName) {
        return getMediaCodecDir() + "/" + fileName;
    }
}
