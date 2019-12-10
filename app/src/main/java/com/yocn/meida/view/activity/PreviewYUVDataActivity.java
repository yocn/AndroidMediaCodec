package com.yocn.meida.view.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.camera.BaseCameraProvider;
import com.yocn.meida.camera.Camera2ProviderPreviewWithYUV;
import com.yocn.meida.camera.Camera2ProviderWithData;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * Yuv数据获取
 */
public class PreviewYUVDataActivity extends BaseCameraActivity {
    TextureView mPreviewView;
    ImageView mShowIv;
    Button mStartBtn;
    Button mEndBtn;
    Camera2ProviderPreviewWithYUV mCamera2Provider;
    public static String DESC = "Camera2 两路预览：YUV输出格式为ImageFormat.YUV_420_888，工具类转化为NV21，再使用YuvImage生成Bitmap实现预览";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_preview_data, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        mStartBtn = root.findViewById(R.id.btn_start);
        mEndBtn = root.findViewById(R.id.btn_end);
        CameraUtil.transTextureView(mPreviewView);
        mShowIv = root.findViewById(R.id.iv_show);
        mStartBtn.setOnClickListener(this);
        mEndBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2ProviderPreviewWithYUV(this);
        mCamera2Provider.initTexture(mPreviewView);
        mCamera2Provider.setmOnGetBitmapInterface(onGetBitmapInterface);
        String path = Constant.getTestYuvFilePath();
        FileUtils.deleteFile(path);
        LogUtil.d(path);
        mCamera2Provider.setSaveYUVPath(this, path);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                mCamera2Provider.shouldWriteYuvFile(true);
                break;
            case R.id.btn_end:
                mCamera2Provider.shouldWriteYuvFile(false);
                break;
            default:
        }
    }
}
