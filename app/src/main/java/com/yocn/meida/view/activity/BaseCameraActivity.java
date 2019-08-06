package com.yocn.meida.view.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderWithData;
import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewDataActivity
 */
public class BaseCameraActivity extends Activity implements View.OnClickListener {
    FrameLayout mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    protected void initView(View root) {
        LogUtil.d("initView");
        mBack = root.findViewById(R.id.fl_back);
        mBack.setOnClickListener(this);
    }

    protected void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_back:
                LogUtil.d("finish");
                finish();
                break;
            default:
        }
    }
}
