package com.yocn.meida.base;

import com.yocn.meida.JumpBean;
import com.yocn.meida.view.activity.FormatTrans264Activity;
import com.yocn.meida.view.activity.FormatTransportActivity;
import com.yocn.meida.view.activity.MediaCodecActivity;
import com.yocn.meida.view.activity.PreviewDataActivity;
import com.yocn.meida.view.activity.PreviewGPUImageActivity;
import com.yocn.meida.view.activity.SimpleOpenGLESActivity;
import com.yocn.meida.view.activity.YUVPlayerActivity;
import com.yocn.meida.view.activity.camera.Camera1PreviewActivity;
import com.yocn.meida.view.activity.camera.CameraActivity;
import com.yocn.meida.view.activity.camera.PreviewNativeYUVActivity;
import com.yocn.meida.view.activity.camera.PreviewPureActivity;
import com.yocn.meida.view.activity.camera.PreviewWithOpenGLESActivity;
import com.yocn.meida.view.activity.camera.PreviewYUVDataActivity;
import com.yocn.meida.view.activity.camera.PreviewYUVDataActivity2;
import com.yocn.meida.view.activity.ffmpeg.FFMpegActivity;
import com.yocn.meida.view.activity.ffmpeg.SimpleFFMpegPlayActivity;
import com.yocn.meida.view.activity.ffmpeg.SimpleFFMpegPlayAudioActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;

/**
 * @Author yocn
 * @Date 2019-11-07 22:14
 * @ClassName DataProvider
 */
public class DataProvider {
    @IntDef({
            Type.MAIN,
            Type.CAMERA,
            Type.FFMPEG,
            Type.OTHER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        int MAIN = 1;
        int CAMERA = 2;
        int FFMPEG = 3;
        int OTHER = 4;
    }

    private static final List<JumpBean> mainDataList = new ArrayList<>();
    private static final List<JumpBean> cameraDataList = new ArrayList<>();
    private static final List<JumpBean> ffmpegDataList = new ArrayList<>();
    private static final List<JumpBean> otherDataList = new ArrayList<>();

    static {
        mainDataList.add(new JumpBean("Camera", CameraActivity.class));
        mainDataList.add(new JumpBean("FFMpeg", FFMpegActivity.class));
        mainDataList.add(new JumpBean("播放YUV文件", YUVPlayerActivity.class));
        mainDataList.add(new JumpBean("OpenGLES", SimpleOpenGLESActivity.class));
        mainDataList.add(new JumpBean("Preview OpenGLES", PreviewWithOpenGLESActivity.class));
        mainDataList.add(new JumpBean("x264转换", FormatTrans264Activity.class));
        mainDataList.add(new JumpBean("MediaCodec", MediaCodecActivity.class));
//--------------------------------------------------------------------------------------------------
        cameraDataList.add(new JumpBean("Camera1预览", Camera1PreviewActivity.class));
        cameraDataList.add(new JumpBean("TextureView预览", PreviewPureActivity.class));
        cameraDataList.add(new JumpBean("预览并获取数据", PreviewDataActivity.class));
        cameraDataList.add(new JumpBean("Yuv数据获取", PreviewYUVDataActivity.class));
        cameraDataList.add(new JumpBean("Yuv数据获取 方式2", PreviewYUVDataActivity2.class));
        cameraDataList.add(new JumpBean("Native转换Yuv", PreviewNativeYUVActivity.class));
        cameraDataList.add(new JumpBean("ARGB转I420-libyuv", FormatTransportActivity.class));
        cameraDataList.add(new JumpBean("GPUImage预览", PreviewGPUImageActivity.class));
//--------------------------------------------------------------------------------------------------
        ffmpegDataList.add(new JumpBean("FFMpeg播放", SimpleFFMpegPlayActivity.class));
        ffmpegDataList.add(new JumpBean("FFMpeg播放Audio", SimpleFFMpegPlayAudioActivity.class));
    }

    public static List<JumpBean> getDataList(@Type int type) {
        List<JumpBean> list;
        switch (type) {
            case Type.MAIN:
                list = mainDataList;
                break;
            case Type.CAMERA:
                list = cameraDataList;
                break;
            case Type.FFMPEG:
                list = ffmpegDataList;
                break;
            case Type.OTHER:
                list = otherDataList;
                break;
            default:
                list = mainDataList;
        }
        return list;
    }
}
