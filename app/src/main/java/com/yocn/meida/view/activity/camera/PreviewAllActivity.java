package com.yocn.meida.view.activity.camera;

import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2AllSizeProvider;
import com.yocn.meida.view.widget.AspectTextureView;
import com.yocn.meida.view.widget.FlowLayout;

import java.util.List;

public class PreviewAllActivity extends BaseCameraActivity {
    public static String DESC = "预览Camera提供的所有尺寸";
    private AspectTextureView mPreviewView;
    private AspectTextureView mPreviewView2;
    private Camera2AllSizeProvider mCamera2Provider;
    private List<Size> outputSizes;
    private int index;
    private FlowLayout flowLayout;
    private TextView sizeTv;
    private Button showBtn;

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
        flowLayout = findViewById(R.id.flow);
        sizeTv = findViewById(R.id.tv_size);

        Button changeBtn = findViewById(R.id.btn_change);
        showBtn = findViewById(R.id.btn_show);
        changeBtn.setOnClickListener(clickListener);
        showBtn.setOnClickListener(clickListener);
    }

    @Override
    protected void initData() {
        mPreviewView.setScaleType(AspectTextureView.ScaleType.FIT_CENTER);
        mPreviewView2.setScaleType(AspectTextureView.ScaleType.CENTER_CROP);
        mCamera2Provider = new Camera2AllSizeProvider(this);
        mCamera2Provider.setGetCameraInfoListener(outputSizes -> {
            PreviewAllActivity.this.outputSizes = outputSizes;
            addTvs(flowLayout);
        });
        mCamera2Provider.initTexture(mPreviewView, mPreviewView2);
    }

    boolean isFlowShow = true;
    View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_change) {
                if (++index >= outputSizes.size()) {
                    index = 0;
                }
                Size size = outputSizes.get(index);
                sizeTv.setText(size.toString());
                mCamera2Provider.startPreviewSession(size);
            } else if (v.getId() == R.id.btn_show) {
                isFlowShow = !isFlowShow;
                boolean visible = flowLayout.getVisibility() == View.VISIBLE;
                flowLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
                showBtn.setText(getResources().getString(visible ? R.string.show_all_size : R.string.hide_all_size));
            } else {
                Object tag = v.getTag(R.id.size);
                if (tag != null) {
                    Size size = (Size) tag;
                    sizeTv.setText(size.toString());
                    mCamera2Provider.startPreviewSession(size);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }

    private void addTvs(FlowLayout flowLayout) {
        if (flowLayout == null) {
            return;
        }
        //往容器内添加TextView数据
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 5, 10, 5);
        flowLayout.removeAllViews();
        for (int i = 0; i < outputSizes.size(); i++) {
            TextView tv = new TextView(this);
            tv.setPadding(28, 10, 28, 10);
            tv.setText(outputSizes.get(i).toString());
            tv.setMaxEms(10);
            tv.setTextColor(getResources().getColor(R.color.write));
            tv.setSingleLine();
            tv.setBackgroundResource(R.color.h_half);
            tv.setLayoutParams(layoutParams);
            tv.setTag(R.id.size, outputSizes.get(i));
            tv.setOnClickListener(clickListener);
            flowLayout.addView(tv, layoutParams);
        }
    }
}
