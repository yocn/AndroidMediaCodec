package com.yocn.meida.view.activity.ffmpeg;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yocn.libnative.FFMpegSimpleAudioPlayer;
import com.yocn.libnative.NativeProgress;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.util.TimeUtil;
import com.yocn.meida.view.activity.BaseActivity;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class SimpleFFMpegPlayAudioActivity extends BaseActivity {
    public static String DESC = "音频播放/转换";
    private Button playBtn;
    private Button convertBtn;
    private Button convertBtn2;
    private ProgressBar progressBar;
    private TextView progressTv;
    private TextView currTv;
    private TextView totalTv;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_simple_button;
    }

    protected void initView(View root) {
        playBtn = root.findViewById(R.id.btn_play);
        convertBtn = root.findViewById(R.id.btn_convert);
        convertBtn2 = root.findViewById(R.id.btn_convert2);
        progressBar = root.findViewById(R.id.pb_test);
        progressTv = root.findViewById(R.id.tv_progress);
        currTv = root.findViewById(R.id.tv_curr);
        totalTv = root.findViewById(R.id.tv_total);
    }

    protected void initData() {
        playBtn.setOnClickListener(onClickListener);
        convertBtn.setOnClickListener(onClickListener);
        convertBtn2.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = v -> {
        String mp4FilePath = Constant.getTestMp4FilePath2();
        String mp3FilePath = Constant.getTestMp3FilePath();
        String mp3FilePath2 = Constant.getTestMp3FilePath2();
        String targetPcmFilePath = Constant.getTestFilePath("output.pcm");
        String targetMp3FilePath = Constant.getTestFilePath("output.mp3");
        FFMpegSimpleAudioPlayer ffMpegSimpleAudioPlayer = new FFMpegSimpleAudioPlayer();
        ffMpegSimpleAudioPlayer.setGetProgressCallback(new NativeProgress.GetProgressCallback() {
            @Override
            public void progress(long curr, long total, int percent) {

                runOnUiThread(() -> {
                    progressBar.setProgress(percent);
                    progressTv.setText(String.valueOf(percent));
                    currTv.setText(TimeUtil.getTimeString(curr / 1000));
                    totalTv.setText(TimeUtil.getTimeString(total / 1000));
                });
            }
        });
        if (v.getId() == R.id.btn_play) {
            ffMpegSimpleAudioPlayer.play(mp4FilePath);
        } else if (v.getId() == R.id.btn_convert) {
            ffMpegSimpleAudioPlayer.convert(mp3FilePath, targetPcmFilePath, targetMp3FilePath);
        } else if (v.getId() == R.id.btn_convert2) {
            ffMpegSimpleAudioPlayer.convert(mp4FilePath, targetPcmFilePath, targetMp3FilePath);
        }
    };
}
