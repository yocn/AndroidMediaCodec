package com.yocn.meida.util;

import android.graphics.Bitmap;

import com.yocn.libnative.YUVTransUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * @Author yocn
 * @Date 2019-11-09 18:31
 * @ClassName YUVUtils
 */
public class YUVUtils {
    public static Bitmap getFirstFrame(String yuvPath, int width, int height, int rotate) {
        LogUtil.d("rotate:" + rotate);
        int chunkSize = width * height * 3 / 2;
        byte[] data = new byte[chunkSize];
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(yuvPath, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.d("e->" + e.getMessage());
            return null;
        }
        try {
            randomAccessFile.read(data, 0, chunkSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] argbBytes = new byte[width * height * 4];
        byte[] argbRotateBytes = new byte[width * height * 4];
        Bitmap bitmap;
        YUVTransUtil.getInstance().I420ToArgb(data, chunkSize, argbBytes,
                width * 4, 0, 0, width, height, width, height, 0, 0);

        if (rotate == 0) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbBytes));
        } else {
            if (rotate == 180) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                YUVTransUtil.getInstance().ARGBRotate(argbBytes, width * 4, argbRotateBytes, width * 4, width, height, rotate);
            } else {
                bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);
                YUVTransUtil.getInstance().ARGBRotate(argbBytes, width * 4, argbRotateBytes, height * 4, width, height, rotate);
            }
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbRotateBytes));
        }

        return bitmap;
    }
}
