package com.yocn.meida.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2Provider;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PurePreviewActivity
 */
public class PurePreviewActivity extends BaseCameraActivity {
    TextureView mPreviewView;
    Camera2Provider mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_pure_preview, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
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
