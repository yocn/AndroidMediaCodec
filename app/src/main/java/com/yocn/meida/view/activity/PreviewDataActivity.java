package com.yocn.meida.view.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2ProviderWithData;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PurePreviewActivity
 */
public class PreviewDataActivity extends Activity {
    TextureView mPreviewView;
    ImageView mShowIv;
    Camera2ProviderWithData mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_preview_data, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    private void initView(View root) {
        mPreviewView = root.findViewById(R.id.tv_camera);
        mShowIv = root.findViewById(R.id.iv_show);
    }

    private void initData() {
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
}
