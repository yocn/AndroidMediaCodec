package com.yocn.libyuv;

/**
 * @Author yocn
 * @Date 2019/8/9 3:40 PM
 * @ClassName YUVTransUtil
 */
public class YUVTransUtil {
    static {
        System.loadLibrary("myYuv");
    }

    static YUVTransUtil mYUVTransUtil;

    public static YUVTransUtil getInstance() {
        if (mYUVTransUtil == null) {
            mYUVTransUtil = new YUVTransUtil();
        }
        return mYUVTransUtil;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native void ARGBToI420(byte[] src_argb, int src_stride_argb,
                                  byte[] dst_y, int dst_stride_y,
                                  byte[] dst_u, int dst_stride_u,
                                  byte[] dst_v, int dst_stride_v,
                                  int width, int height);

    public native void convertToArgb(byte[] src_frame, int src_size,
                                     byte[] dst_argb, int dst_stride_argb,
                                     int crop_x, int crop_y,
                                     int src_width, int src_height,
                                     int crop_width, int crop_height,
                                     int rotation,
                                     int format);

    public native void rotateYUV420(byte[] src_y,
                                    byte[] src_u,
                                    byte[] src_v,
                                    byte[] dst_y,
                                    byte[] dst_u,
                                    byte[] dst_v,
                                    int width, int height, int rotate);

    public native void NV21ToArgb(byte[] src_y, int src_stride_y,
                                  byte[] src_vu, int src_stride_vu,
                                  byte[] dst_argb, int dst_stride_argb,
                                  int width, int height);

}
