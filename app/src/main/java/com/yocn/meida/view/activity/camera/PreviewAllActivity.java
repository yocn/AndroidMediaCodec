package com.yocn.meida.view.activity.camera;

import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;

import com.yocn.media.R;
import com.yocn.meida.camera.BaseCommonCameraProvider;
import com.yocn.meida.camera.Camera2AllSizeProvider;
import com.yocn.meida.view.widget.AspectTextureView;

import java.util.List;

public class PreviewAllActivity extends BaseCameraActivity {
    public static String DESC = "预览Camera提供的所有尺寸";
    private AspectTextureView mPreviewView;
    private AspectTextureView mPreviewView2;
    private Camera2AllSizeProvider mCamera2Provider;
    private List<Size> outputSizes;
    private int index;

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
        mPreviewView2 = root.findViewById(R.id.tv_camera_2);
        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (++index >= outputSizes.size()) {
                    index = 0;
                }
                Size size = outputSizes.get(index);
                btn.setText(size.toString());
                mCamera2Provider.startPreviewSession(size);
            }
        });
    }

    @Override
    protected void initData() {
        mCamera2Provider = new Camera2AllSizeProvider(this);
        mCamera2Provider.setGetCameraInfoListener(new BaseCommonCameraProvider.GetCameraInfoListener() {
            @Override
            public void getInfos(List<Size> outputSizes) {
                PreviewAllActivity.this.outputSizes = outputSizes;
            }
        });
        mCamera2Provider.initTexture(mPreviewView, mPreviewView2);
    }

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }
}
