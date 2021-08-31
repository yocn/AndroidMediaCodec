package com.yocn.meida.view.activity.mediacodec;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.mediacodec.decoder.SimpleDecodeVideoPlayer;
import com.yocn.meida.view.activity.BaseActivity;

public class SimpleMediaCodecDecodeVActivity extends BaseActivity {
    public static String DESC = "视频解码播放";

    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_media_codec;
    }

    @Override
    protected void initView(View root) {
        mSurfaceView = findViewById(R.id.sv_camera);
    }

    @Override
    public void initData() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                String mp4Path = Constant.getTestMp4FilePath();
                new SimpleDecodeVideoPlayer().init(mp4Path, mSurfaceView.getHolder().getSurface());
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

}