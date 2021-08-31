package com.yocn.meida.view.activity.mediacodec;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.mediacodec.decoder.SimpleVideoDecoder;
import com.yocn.meida.view.activity.BaseActivity;
import com.yocn.meida.view.activity.YUVPlayerActivity;

public class SimpleMediaCodecDecodeActivity extends BaseActivity {
    public static String DESC = "视频解码并通过Image获取数据";

    private ImageView previewIv;
    private Handler mainHandler;
    private ProgressBar playPb;
    private Button playBtn;
    private final String mp4Path = Constant.getTestMp4FilePath();
    private final String yuvPath = Constant.getOutTestYuvFilePath();
    private int width, height, fps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_media_codec_decoder;
    }

    @Override
    protected void initView(View root) {
        previewIv = findViewById(R.id.iv_preview);
        playPb = findViewById(R.id.pb_play);
        playBtn = findViewById(R.id.btn_play);
        playBtn.setOnClickListener(v -> YUVPlayerActivity.playYuv(SimpleMediaCodecDecodeActivity.this, yuvPath, width, height, fps, true));
    }

    @Override
    public void initData() {
        mainHandler = new Handler(Looper.getMainLooper());
        SimpleVideoDecoder simpleVideoDecoder = new SimpleVideoDecoder();
        simpleVideoDecoder.init(mp4Path, yuvPath, new SimpleVideoDecoder.PreviewCallback() {
            @Override
            public void info(int width, int height, int fps) {
                SimpleMediaCodecDecodeActivity.this.width = width;
                SimpleMediaCodecDecodeActivity.this.height = height;
                SimpleMediaCodecDecodeActivity.this.fps = fps;
            }

            @Override
            public void getBitmap(Bitmap bitmap) {
                mainHandler.post(() -> previewIv.setImageBitmap(bitmap));
            }

            @Override
            public void progress(int progress) {
                mainHandler.post(() -> {
                    playPb.setProgress(progress);
                    if (progress == 100) {
                        playBtn.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

}