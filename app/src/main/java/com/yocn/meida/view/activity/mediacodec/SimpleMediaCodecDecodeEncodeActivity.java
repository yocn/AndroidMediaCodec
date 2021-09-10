package com.yocn.meida.view.activity.mediacodec;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.mediacodec.decoder.SimpleVideoDecoderAndEncoder;
import com.yocn.meida.util.DisplayUtil;
import com.yocn.meida.util.MediaUtil;
import com.yocn.meida.view.activity.BaseActivity;
import com.yocn.meida.view.activity.YUVPlayerActivity;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SimpleMediaCodecDecodeEncodeActivity extends BaseActivity {
    public static String DESC = "视频解码成yuv数据后编码成h264并生成mp4";

    private ImageView previewIv;
    private Handler mainHandler;
    private ProgressBar playPb;
    private Button playYuvBtn, playMp4Btn, playH264Btn;
    private final String mp4InPath = Constant.getTestMp4FilePath();
    private final String yuvOutPath = Constant.getOutTestYuvFilePath();
    private final String mp4OutPath = Constant.getTestFilePath("encode.mp4");
    private final String h264OutPath = Constant.getTestFilePath("encode.h264");
    private int width, height, fps;
    private TextView yuvPathTv;
    private TextView mp4PathTv;
    private TextView h264PathTv;

    public static void start(Context context) {
        Intent intent = new Intent(context, SimpleMediaCodecDecodeEncodeActivity.class);
        context.startActivity(intent);
    }

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
        TextView tv = findViewById(R.id.tv_hint);
        tv.setText(DESC);
        previewIv = findViewById(R.id.iv_preview);
        playPb = findViewById(R.id.pb_play);
        playYuvBtn = findViewById(R.id.btn_play_yuv);
        playMp4Btn = findViewById(R.id.btn_play_mp4);
        playH264Btn = findViewById(R.id.btn_play_h264);
        yuvPathTv = findViewById(R.id.tv_path_yuv);
        mp4PathTv = findViewById(R.id.tv_path_mp4);
        h264PathTv = findViewById(R.id.tv_path_h264);
        yuvPathTv.setText(yuvOutPath);
        mp4PathTv.setText(mp4OutPath);
        h264PathTv.setText(h264OutPath);
        int navigationHeight = DisplayUtil.getNavigationBarHeight(this);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) yuvPathTv.getLayoutParams();
        layoutParams.bottomMargin = navigationHeight;
        yuvPathTv.setLayoutParams(layoutParams);

        playYuvBtn.setOnClickListener(onClickListener);
        playMp4Btn.setOnClickListener(onClickListener);
        playH264Btn.setOnClickListener(onClickListener);
    }

    @Override
    public void initData() {
        mainHandler = new Handler(Looper.getMainLooper());
        SimpleVideoDecoderAndEncoder simpleVideoDecoderAndEncoder = new SimpleVideoDecoderAndEncoder();
        simpleVideoDecoderAndEncoder.init(mp4InPath, yuvOutPath, h264OutPath, mp4OutPath, new SimpleVideoDecoderAndEncoder.PreviewCallback() {
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
                        playH264Btn.setVisibility(View.VISIBLE);
                        yuvPathTv.setVisibility(View.VISIBLE);
                        mp4PathTv.setVisibility(View.VISIBLE);
                        h264PathTv.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_play_yuv) {
                YUVPlayerActivity.playYuv(SimpleMediaCodecDecodeEncodeActivity.this, yuvOutPath, width, height, fps, true);
            } else if (v.getId() == R.id.btn_play_mp4) {
                MediaUtil.playVideo(SimpleMediaCodecDecodeEncodeActivity.this, mp4OutPath);
            } else if (v.getId() == R.id.btn_play_h264) {
                MediaUtil.playVideo(SimpleMediaCodecDecodeEncodeActivity.this, h264OutPath);
            }
        }
    };
}