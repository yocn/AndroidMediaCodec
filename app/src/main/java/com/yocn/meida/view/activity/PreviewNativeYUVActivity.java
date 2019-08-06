package com.yocn.meida.view.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderPreviewWithYUV;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewNativeYUVActivity
 */
public class PreviewNativeYUVActivity extends BaseCameraActivity {
    TextureView mPreviewView;
    ImageView mShowIv;
    Camera2ProviderPreviewWithYUV mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.activity_preview_yuv, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        mShowIv = root.findViewById(R.id.iv_show);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2ProviderPreviewWithYUV(this);
        mCamera2Provider.initTexture(mPreviewView);
        mCamera2Provider.setmOnGetBitmapInterface(onGetBitmapInterface);
    }

    private Camera2ProviderPreviewWithYUV.OnGetBitmapInterface onGetBitmapInterface = new Camera2ProviderPreviewWithYUV.OnGetBitmapInterface() {
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
