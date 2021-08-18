package com.yocn.meida.view.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderWithData;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.view.activity.camera.BaseCameraActivity;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewDataActivity
 * 预览并获取数据
 */
public class PreviewDataActivity extends BaseCameraActivity {
    TextureView mPreviewView;
    ImageView mShowIv;
    Camera2ProviderWithData mCamera2Provider;
    public static String DESC = "Camera2 两路预览：一路TextureView预览，一路获取JPEG输出Bitmap\n" +
            "Camera2 两路预览：一路TextureView预览，一路获取JPEG输出Bitmap\n" +
            "Camera2 两路预览：一路TextureView预览，一路获取JPEG输出Bitmap";

    @Override
    protected int getContentViewId() {
        return R.layout.activity_preview_data;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        CameraUtil.transTextureView(mPreviewView);
        mShowIv = root.findViewById(R.id.iv_show);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2ProviderWithData(this);
        mCamera2Provider.initTexture(mPreviewView);
        mCamera2Provider.setmOnGetBitmapInterface(onGetBitmapInterface);
    }

    private Camera2ProviderWithData.OnGetBitmapInterface onGetBitmapInterface = new Camera2ProviderWithData.OnGetBitmapInterface() {
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
