package com.yocn.meida.view.activity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2Provider;
import com.yocn.meida.camera.Camera2ProviderPreviewWithGPUImage;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.GPUImageFilterTools;

import androidx.core.view.ViewCompat;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.util.Rotation;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class PreviewGPUImageActivity extends BaseCameraActivity {
    GPUImageView mPreviewView;
    ImageView mShowIV;
    SeekBar mSeekBbar;
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
        mSeekBbar = findViewById(R.id.sk);
        root.findViewById(R.id.btn_select).setOnClickListener(this);
        mSeekBbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2ProviderPreviewWithGPUImage(this);
        mCamera2Provider.setmOnGetBitmapInterface((bitmap, nv21, w, h) -> {
            mShowIV.setImageBitmap(bitmap);
            mPreviewView.updatePreviewFrame(nv21, w, h);
        });
        mCamera2Provider.initCamera();
        mPreviewView.setRotation(Rotation.ROTATION_90);
        mPreviewView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY);
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

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_select:
                GPUImageFilterTools.INSTANCE.showDialog(this, new Function1<GPUImageFilter, Unit>() {
                    @Override
                    public Unit invoke(GPUImageFilter gpuImageFilter) {
                        switchFilter(gpuImageFilter);
                        return null;
                    }
                });
                break;
            default:
        }
    }

    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mFilterAdjuster != null) {
                mFilterAdjuster.adjust(mSeekBbar.getProgress());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    GPUImageFilterTools.FilterAdjuster mFilterAdjuster;


    private void switchFilter(GPUImageFilter filter) {
        mPreviewView.setFilter(filter);
        mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(filter);
        mFilterAdjuster.adjust(mSeekBbar.getProgress());
    }
}
