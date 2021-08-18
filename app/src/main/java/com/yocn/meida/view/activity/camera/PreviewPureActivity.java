package com.yocn.meida.view.activity.camera;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2Provider;
import com.yocn.meida.util.CameraUtil;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * TextureView预览
 */
public class PreviewPureActivity extends BaseCameraActivity {
    TextureView mPreviewView;
    Camera2Provider mCamera2Provider;
    public static String DESC = "使用Camera2，最基本API，直接输出到TextureView进行预览";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_pure_preview;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        CameraUtil.transTextureView(mPreviewView);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2Provider(this);
        mCamera2Provider.initTexture(mPreviewView);
    }

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }

}
