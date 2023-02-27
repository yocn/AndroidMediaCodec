package com.yocn.meida.view.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderPreviewWithGPUImage;
import com.yocn.meida.util.DisplayUtil;
import com.yocn.meida.util.GPUImageFilterTools;
import com.yocn.meida.view.activity.camera.BaseCameraActivity;
import com.yocn.meida.view.adapter.GPUImageFilterAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.util.Rotation;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * GPUImage预览
 */
public class PreviewGPUImageActivity extends BaseCameraActivity {
    GPUImageView mPreviewView;
    ImageView mShowIV;
    SeekBar mSeekBbar;
    Button mSelectFilterBtn;
    GPUImageFilterAdapter mGPUImageFilterAdapter;
    RecyclerView mRecyclerView;
    Camera2ProviderPreviewWithGPUImage mCamera2Provider;
    public static String DESC = "使用GPUImage框架实现预览和滤镜的实现";

    @Override
    protected int getContentViewId() {
        return R.layout.activity_pure_gpuimage;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        mShowIV = root.findViewById(R.id.iv_show);
        mSeekBbar = findViewById(R.id.sk);
        mRecyclerView = root.findViewById(R.id.rv_gpuimage);
        mSelectFilterBtn = root.findViewById(R.id.btn_select);
        mSelectFilterBtn.setOnClickListener(this);
        mSeekBbar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    @Override
    protected void initData() {
        endY = -DisplayUtil.dip2px(this, 300);
        mCamera2Provider = new Camera2ProviderPreviewWithGPUImage(this);
        mCamera2Provider.setmOnGetBitmapInterface((bitmap, nv21, w, h) -> {
            mShowIV.post(() -> {
                mShowIV.setImageBitmap(bitmap);
                mPreviewView.updatePreviewFrame(nv21, w, h);
            });
        });
        mCamera2Provider.initCamera();
        mPreviewView.setRotation(Rotation.ROTATION_90);
        mPreviewView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY);
        GPUImageFilterTools.FilterList list = GPUImageFilterTools.INSTANCE.getFilters();
        mGPUImageFilterAdapter = new GPUImageFilterAdapter(list.getNames());
        mGPUImageFilterAdapter.setmContext(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mGPUImageFilterAdapter);
        mGPUImageFilterAdapter.setSelectListener(position -> {
            GPUImageFilter filter = GPUImageFilterTools.INSTANCE.createFilterForType(PreviewGPUImageActivity.this, list.getFilters().get(position));
            switchFilter(filter);
        });
        initAnim();
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
        if (v.getId() == R.id.btn_select) {
            exeAnim();
        }
    }

    boolean isShow = false;
    ObjectAnimator translationYDown;
    ObjectAnimator translationYUp;
    int startY = 0, endY = 0;
    int duration = 200;

    private void initAnim() {
        translationYUp = ObjectAnimator.ofFloat(mRecyclerView, "translationY", endY, startY);
        translationYUp.setDuration(duration);
        translationYDown = ObjectAnimator.ofFloat(mRecyclerView, "translationY", startY, endY);
        translationYDown.setDuration(duration);
    }

    private void exeAnim() {
        if (isShow) {
            mSelectFilterBtn.setText("打开Filter");
            translationYUp.start();
        } else {
            mSelectFilterBtn.setText("关闭Filter");
            translationYDown.start();
        }
        isShow = !isShow;
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
