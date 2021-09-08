package com.yocn.meida.util;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.util.Size;
import android.view.TextureView;
import android.view.ViewGroup;

import com.yocn.meida.camera.BaseCameraProvider;
import com.yocn.meida.codec.YUVData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author yocn
 * @Date 2019/8/2 11:26 AM
 * @ClassName CameraUtil
 */
public class CameraUtil {
    private static final String TAG = "";
    private static boolean VERBOSE = false;

    //选择sizeMap中大于并且最接近width和height的size
    public static Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    public static void transTextureView(ViewGroup parentViewGroup, TextureView mPreviewView) {
        int parentHeight = parentViewGroup.getHeight();
        int parentWidth = parentViewGroup.getWidth();
        int tarHeight = mPreviewView.getHeight();
        int tarWidth = mPreviewView.getWidth();
        LogUtil.d("yocn", "parentHeight::" + parentHeight + " parentWidth::" + parentWidth + " tarHeight::" + tarHeight + " tarWidth::" + tarWidth);
        if (parentWidth * 1.0f / parentHeight > tarWidth * 1.0f / tarHeight) {
            // parent的宽高比 比 预览的宽高比大, 也就是parent比较宽，预览比较细长，需要移动x轴
            int deltaX = (int) (parentWidth - parentHeight * 1.0f * tarWidth / tarHeight);
            LogUtil.d("yocn", "deltaX::" + deltaX);
        } else {
            int deltaY = (int) (parentHeight - parentWidth * 1.0f * tarHeight / tarWidth);
            LogUtil.d("yocn", "deltaY::" + deltaY);
        }
    }

    public static void transTextureView(TextureView mPreviewView) {
        int minus = BaseCameraProvider.TextureViewSize.getWidth() - BaseCameraProvider.ScreenSize.getWidth();
        mPreviewView.setTranslationX(-minus / 2);
    }

    public static final int COLOR_FormatI420 = 1;
    public static final int COLOR_FormatNV21 = 2;

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
            default:
        }
        return false;
    }

    public static YUVData getYUV(Image image) {
        YUVData data = new YUVData();
        int w = image.getWidth(), h = image.getHeight();
        int i420Size = w * h * 3 / 2;
        Image.Plane[] planes = image.getPlanes();
        //
        byte[] i420bytes = new byte[i420Size];
        byte[] ySrcBytes = new byte[w * h];
        byte[] uSrcBytes = new byte[w * h / 4];
        byte[] vSrcBytes = new byte[w * h / 4];

        //y分量
        planes[0].getBuffer().get(ySrcBytes);
        System.arraycopy(ySrcBytes, 0, i420bytes, 0, w * h);
        //uv分量
        int pixelStride = planes[1].getPixelStride();
        if (pixelStride == 1) {
            //YYYYYYYYUUVV
            planes[1].getBuffer().get(uSrcBytes);
            planes[2].getBuffer().get(vSrcBytes);
        } else {
            //YYYYYYYYUVUV
            byte[] uvBytes = new byte[w * h / 2];
        }

        LogUtil.d("-----------------wh->" + w + "/" + h);
//        System.arraycopy(uSrcBytes, 0, i420bytes1, w * h, w * h / 4);
//        System.arraycopy(vSrcBytes, 0, i420bytes1, w * h * 5 / 4, w * h / 4);
//        BitmapUtil.dumpFile("mnt/sdcard/2.yuv", i420bytes1);


        return data;
    }

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        if (VERBOSE) {
            LogUtil.d("get data from " + planes.length + " planes");
        }
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
                default:
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            if (VERBOSE) {
                LogUtil.v(TAG, "pixelStride " + pixelStride);
                LogUtil.v(TAG, "rowStride " + rowStride);
                LogUtil.v(TAG, "width " + width);
                LogUtil.v(TAG, "height " + height);
                LogUtil.v(TAG, "buffer size " + buffer.remaining());
            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            if (VERBOSE) {
                LogUtil.v(TAG, "Finished reading data from plane " + i);
            }
        }
        return data;
    }
}
