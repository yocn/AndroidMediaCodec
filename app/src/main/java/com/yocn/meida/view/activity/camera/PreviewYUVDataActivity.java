package com.yocn.meida.view.activity.camera;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.camera.Camera2ProviderPreviewWithYUV;
import com.yocn.meida.util.CameraUtil;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.util.MediaUtil;

import androidx.annotation.NonNull;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * Yuv数据获取
 */
public class PreviewYUVDataActivity extends BaseCameraActivity {
    public static String DESC = "Camera2 两路预览：YUV输出格式为ImageFormat.YUV_420_888，工具类转化为NV21，再使用YuvImage生成Bitmap实现预览";
    private TextureView mPreviewView;
    private ImageView mShowIv;
    private Button mStartBtn;
    private Button mEndBtn;
    private Button mOpenBtn;
    private TextView mShowTv;
    private Camera2ProviderPreviewWithYUV mCamera2Provider;
    private String outMp4Path = Constant.getTestFilePath("cameraOut.mp4");

    @Override
    protected int getContentViewId() {
        return R.layout.activity_preview_data;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mPreviewView = root.findViewById(R.id.tv_camera);
        mStartBtn = root.findViewById(R.id.btn_start);
        mEndBtn = root.findViewById(R.id.btn_end);
        mOpenBtn = root.findViewById(R.id.btn_open);
        CameraUtil.transTextureView(mPreviewView);
        mShowIv = root.findViewById(R.id.iv_show);
        mShowTv = root.findViewById(R.id.tv_show);
        mStartBtn.setOnClickListener(this);
        mEndBtn.setOnClickListener(this);
        mOpenBtn.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        mShowTv.setText(getString(R.string.save_mp4, outMp4Path));
        mCamera2Provider = new Camera2ProviderPreviewWithYUV(this, outMp4Path);
        mCamera2Provider.initTexture(mPreviewView);
        mCamera2Provider.setmOnGetBitmapInterface(onGetBitmapInterface);
        String path = Constant.getTestYuvFilePath();
        FileUtils.deleteFile(path);
        LogUtil.d(path);
    }

    private final Camera2ProviderPreviewWithYUV.OnGetBitmapInterface onGetBitmapInterface = new Camera2ProviderPreviewWithYUV.OnGetBitmapInterface() {
        @Override
        public void getABitmap(final Bitmap bitmap) {
            mShowIv.post(() -> mShowIv.setImageBitmap(bitmap));
        }
    };

    @Override
    protected void onDestroy() {
        mCamera2Provider.closeCamera();
        super.onDestroy();
    }

    private boolean animing = false;
    private int point = 0;
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (animing) {
                mainHandler.post(animRunnable);
                mainHandler.sendEmptyMessageDelayed(0, 500);
            }
        }
    };

    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (++point > 6) {
                point = 0;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < point; i++) {
                sb.append(".");
            }
            mShowTv.setText("Anim" + sb.toString());
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                mCamera2Provider.startRecord();
                animing = true;
                animRunnable.run();
                mainHandler.sendEmptyMessageDelayed(0, 500);
                break;
            case R.id.btn_end:
                mCamera2Provider.stopRecord();
                mOpenBtn.setVisibility(View.VISIBLE);
                animing = false;
                mainHandler.removeMessages(0);
                mShowTv.setText(getString(R.string.save_mp4, outMp4Path));
                break;
            case R.id.btn_open:
                MediaUtil.playVideo(this, outMp4Path);
                break;
            default:
        }
    }
}
