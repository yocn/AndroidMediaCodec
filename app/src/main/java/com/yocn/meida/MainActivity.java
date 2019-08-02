package com.yocn.meida;

import android.app.Activity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import com.yocn.media.R;
import com.yocn.meida.camera.Camera2Provider;

/**
 * @author yocn
 */
public class MainActivity extends Activity {

    TextureView mPreviewView;
    Camera2Provider mCamera2Provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    private void initView(View root) {
        mPreviewView = findViewById(R.id.tv_camera);
    }

    private void initData() {
        mCamera2Provider = new Camera2Provider(this);
        mCamera2Provider.initTexture(mPreviewView);
    }

}
