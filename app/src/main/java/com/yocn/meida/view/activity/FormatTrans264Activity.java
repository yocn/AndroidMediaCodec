package com.yocn.meida.view.activity;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yocn.libnative.NativeProgress;
import com.yocn.libnative.X264Encoder;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.util.FileUtils;
import com.yocn.meida.util.MediaUtil;
import com.yocn.meida.view.activity.mediacodec.SimpleMediaCodecDecodeEncodeActivity;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 * x264转换
 */
public class FormatTrans264Activity extends BaseActivity {
    public static String DESC = "yuv编码成x264";
    private Button mClickBtn;
    private Button mPlayBtn;
    private TextView hintTv;
    private ProgressBar progressBar;
    private X264Encoder x264Encoder = new X264Encoder();
    private String yuvPath = Constant.getOutTestYuvFilePath();
    private String h264Path = Constant.getTestFilePath("encode.h264");

    @Override
    protected int getContentViewId() {
        return R.layout.activity_x264_encode;
    }

    @Override
    protected void initView(View root) {
        mClickBtn = root.findViewById(R.id.btn_click);
        mPlayBtn = root.findViewById(R.id.btn_play);
        progressBar = findViewById(R.id.pb_test);
        hintTv = findViewById(R.id.tv_hint);
        mClickBtn.setOnClickListener(v -> trans());
        mPlayBtn.setOnClickListener(v -> {
            MediaUtil.playVideo(this, h264Path);
        });
        String srcString = getString(R.string.save_h264_src, yuvPath);
        String tarString = getString(R.string.save_h264, h264Path);
        hintTv.setText(srcString + "\n" + tarString);
    }

    @Override
    protected void initData() {
        x264Encoder.setGetProgressCallback(new NativeProgress.GetProgressCallback() {
            @Override
            public void progress(long curr, long total, int percent) {
                progressBar.post(() -> {
                    progressBar.setProgress(percent);
                    if (percent == 100) {
                        mPlayBtn.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void trans() {
        int width = 544, height = 960, fps = 30;
        if (!FileUtils.fileExists(yuvPath)) {
            Toast.makeText(this, "设置的YuvPath不存在，请先生成一个", Toast.LENGTH_SHORT).show();
            SimpleMediaCodecDecodeEncodeActivity.start(FormatTrans264Activity.this);
            return;
        }
        x264Encoder.initEncoder(yuvPath, h264Path, width, height, fps);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
