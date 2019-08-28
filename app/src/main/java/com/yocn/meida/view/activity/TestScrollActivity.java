package com.yocn.meida.view.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderPreviewWithYUV2;
import com.yocn.meida.util.CameraUtil;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class TestScrollActivity extends Activity {
    TextureView mPreviewView;
    ImageView mShowIv;
    Camera2ProviderPreviewWithYUV2 mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_test_scroll, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    protected void initView(View root) {
    }

    protected void initData() {
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
