package com.yocn.meida.view.activity.mediacodec;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.mediacodec.decoder.SimpleVideoDecoder;
import com.yocn.meida.view.activity.BaseActivity;

public class SimpleMediaCodecDecodeActivity extends BaseActivity {

    private ImageView previewIv;

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
    }

    @Override
    public void initData() {
        SimpleVideoDecoder simpleVideoDecoder = new SimpleVideoDecoder();
        String mp4Path = Constant.getTestMp4FilePath();
        simpleVideoDecoder.init(mp4Path, new SimpleVideoDecoder.PreviewCallback() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                previewIv.post(new Runnable() {
                    @Override
                    public void run() {
                        previewIv.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

}