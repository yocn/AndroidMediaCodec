package com.yocn.meida.view.activity.camera;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2AllSizeProvider;

public class PreviewAllActivity extends BaseCameraActivity {
    public static String DESC = "预览Camera提供的所有尺寸";
    private TextureView mPreviewView;
    private Camera2AllSizeProvider mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_preview_all_size;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2AllSizeProvider(this);
        mCamera2Provider.initTexture(mPreviewView);
    }

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }
}
