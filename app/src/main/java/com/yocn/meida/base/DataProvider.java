package com.yocn.meida.base;

import com.yocn.meida.JumpBean;
import com.yocn.meida.view.activity.Camera1PreviewActivity;
import com.yocn.meida.view.activity.FormatTrans264Activity;
import com.yocn.meida.view.activity.FormatTransportActivity;
import com.yocn.meida.view.activity.PreviewDataActivity;
import com.yocn.meida.view.activity.PreviewGPUImageActivity;
import com.yocn.meida.view.activity.PreviewNativeYUVActivity;
import com.yocn.meida.view.activity.PreviewPureActivity;
import com.yocn.meida.view.activity.PreviewWithOpenGLESActivity;
import com.yocn.meida.view.activity.PreviewYUVDataActivity;
import com.yocn.meida.view.activity.PreviewYUVDataActivity2;
import com.yocn.meida.view.activity.SimpleOpenGLESActivity;
import com.yocn.meida.view.activity.TestScrollActivity;
import com.yocn.meida.view.activity.YUVPlayerActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author yocn
 * @Date 2019-11-07 22:14
 * @ClassName DataProvider
 */
public class DataProvider {

    public static List<JumpBean> getDataList() {
        List<JumpBean> list = new ArrayList<>();
        list.add(new JumpBean("Camera1预览", Camera1PreviewActivity.class));
        list.add(new JumpBean("TextureView预览", PreviewPureActivity.class));
        list.add(new JumpBean("预览并获取数据", PreviewDataActivity.class));
        list.add(new JumpBean("Yuv数据获取", PreviewYUVDataActivity.class));
        list.add(new JumpBean("Yuv数据获取 方式2", PreviewYUVDataActivity2.class));
        list.add(new JumpBean("Native转换Yuv", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("ARGB转I420-libyuv", FormatTransportActivity.class));
        list.add(new JumpBean("GPUImage预览", PreviewGPUImageActivity.class));
        list.add(new JumpBean("x264转换", FormatTrans264Activity.class));
        list.add(new JumpBean("播放YUV文件", YUVPlayerActivity.class));
        list.add(new JumpBean("OpenGLES", SimpleOpenGLESActivity.class));
        list.add(new JumpBean("Preview OpenGLES", PreviewWithOpenGLESActivity.class));
//        list.add(new JumpBean("TestScrollActivity", TestScrollActivity.class));
        return list;
    }
}
