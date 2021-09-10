package com.yocn.meida.view.activity.camera;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderPreviewWithYUV2;
import com.yocn.meida.util.CameraUtil;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * Yuv数据获取 方式2
 */
public class PreviewYUVDataActivity2 extends BaseCameraActivity {
    TextureView mPreviewView;
    ImageView mShowIv;
    Camera2ProviderPreviewWithYUV2 mCamera2Provider;
    public static String DESC = "YUV输出格式I420，自己方法实现转化为NV21，再使用YuvImage生成Bitmap实现预览";

    @Override
    protected int getContentViewId() {
        return R.layout.activity_preview_data;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        mShowIv = root.findViewById(R.id.iv_show);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2ProviderPreviewWithYUV2(this);
        mCamera2Provider.initTexture(mPreviewView);
        mCamera2Provider.setmOnGetBitmapInterface(onGetBitmapInterface);
    }

    private Camera2ProviderPreviewWithYUV2.OnGetBitmapInterface onGetBitmapInterface = new Camera2ProviderPreviewWithYUV2.OnGetBitmapInterface() {
        @Override
        public void getABitmap(final Bitmap bitmap) {
            mShowIv.post(new Runnable() {
                @Override
                public void run() {
                    mShowIv.setImageBitmap(bitmap);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }
}
