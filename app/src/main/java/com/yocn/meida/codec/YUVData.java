package com.yocn.meida.codec;

/**
 * 看Image-Planes的PixelStride
 * 1 -> 表示I420
 * 2 -> 表示UV交叉
 * plane[0] + plane[1] 可得NV12
 * plane[0] + plane[2] 可得NV21
 *
 * @Author yocn
 * @Date 2019/8/10 4:06 PM
 * @ClassName YUVData
 */
public class YUVData {
    //YYYYYYYYUUVV
    public static final int I420 = 0;
    //YYYYYYYYUVUV
    public static final int NV12 = 1;
    //YYYYYYYYVUVU
    public static final int NV21 = 2;

    int type = I420;
    int w, h;
    private byte[] y;
    private byte[] u;
    private byte[] v;
    private byte[] yuv;

    public static int getI420() {
        return I420;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public byte[] getY() {
        return y;
    }

    public void setY(byte[] y) {
        this.y = y;
    }

    public byte[] getU() {
        return u;
    }

    public void setU(byte[] u) {
        this.u = u;
    }

    public byte[] getV() {
        return v;
    }

    public void setV(byte[] v) {
        this.v = v;
    }

    public byte[] getYuv() {
        return yuv;
    }

    public void setYuv(byte[] yuv) {
        this.yuv = yuv;
    }
}
