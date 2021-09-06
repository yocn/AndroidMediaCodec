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
import com.yocn.meida.mediacodec.decoder.SimpleVideoDecoderAndEncoder;
import com.yocn.meida.util.MediaUtil;
import com.yocn.meida.view.activity.BaseActivity;
import com.yocn.meida.view.activity.YUVPlayerActivity;

public class SimpleMediaCodecDecodeEncodeActivity extends BaseActivity {
    public static String DESC = "视频解码并通过Image获取数据";

    private ImageView previewIv;
    private Handler mainHandler;
    private ProgressBar playPb;
    private Button playYuvBtn, playMp4Btn;
    private final String mp4Path = Constant.getTestMp4FilePath();
    private final String yuvPath = Constant.getOutTestYuvFilePath();
    private final String mp4OutPath = Constant.getTestFilePath("encode.mp4");
    private final String h264OutPath = Constant.getTestFilePath("encode.h264");
    private int width, height, fps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_media_codec_decoder_encoder;
    }

    @Override
    protected void initView(View root) {
        previewIv = findViewById(R.id.iv_preview);
        playPb = findViewById(R.id.pb_play);
        playYuvBtn = findViewById(R.id.btn_play_yuv);
        playMp4Btn = findViewById(R.id.btn_play_mp4);
        playYuvBtn.setOnClickListener(onClickListener);
        playMp4Btn.setOnClickListener(onClickListener);
    }

    @Override
    public void initData() {
        mainHandler = new Handler(Looper.getMainLooper());
        SimpleVideoDecoderAndEncoder simpleVideoDecoderAndEncoder = new SimpleVideoDecoderAndEncoder();
        simpleVideoDecoderAndEncoder.init(mp4Path, yuvPath, h264OutPath, mp4OutPath, new SimpleVideoDecoderAndEncoder.PreviewCallback() {
            @Override
            public void info(int width, int height, int fps) {
                SimpleMediaCodecDecodeEncodeActivity.this.width = width;
                SimpleMediaCodecDecodeEncodeActivity.this.height = height;
                SimpleMediaCodecDecodeEncodeActivity.this.fps = fps;
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
                        playYuvBtn.setVisibility(View.VISIBLE);
                        playMp4Btn.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_play_yuv) {
                YUVPlayerActivity.playYuv(SimpleMediaCodecDecodeEncodeActivity.this, yuvPath, width, height, fps, true);
            } else if (v.getId() == R.id.btn_play_mp4) {
                MediaUtil.playVideo(SimpleMediaCodecDecodeEncodeActivity.this, mp4OutPath);
            }
        }
    };
}