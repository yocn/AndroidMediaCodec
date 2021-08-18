package com.yocn.meida.view.activity;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.mediacodec.AACToPCM;
import com.yocn.meida.mediacodec.MediaCodecUtil;
import com.yocn.meida.mediacodec.TextMediaExtractor;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.LogUtil;

import java.io.IOException;

public class MediaCodecActivity extends BaseActivity {

    private SurfaceView mSurfaceView;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    String mimeType = MediaFormat.MIMETYPE_VIDEO_AVC;

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
//        MediaCodecUtil.testMediaExtractor(this);
//        TextMediaExtractor.separate(this);
        TextMediaExtractor.combine();
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
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