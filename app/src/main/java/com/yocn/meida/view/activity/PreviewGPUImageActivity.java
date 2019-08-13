package com.yocn.meida.view.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2Provider;
import com.yocn.meida.camera.Camera2ProviderPreviewWithGPUImage;
import com.yocn.meida.util.CameraUtil;

import androidx.core.view.ViewCompat;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class PreviewGPUImageActivity extends BaseCameraActivity {
    GPUImageView mPreviewView;
    ImageView mShowIV;
    Camera2ProviderPreviewWithGPUImage mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_pure_gpuimage, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        mShowIV = root.findViewById(R.id.iv_show);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2ProviderPreviewWithGPUImage(this);
        mCamera2Provider.setmOnGetBitmapInterface(new Camera2ProviderPreviewWithGPUImage.OnGetBitmapInterface() {
            @Override
            public void getABitmap(Bitmap bitmap, byte[] nv21, int w, int h) {
                mShowIV.setImageBitmap(bitmap);
                mPreviewView.updatePreviewFrame(nv21, w, h);
            }
        });
        mCamera2Provider.initCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCamera2Provider.initCamera();
    }

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }

}
