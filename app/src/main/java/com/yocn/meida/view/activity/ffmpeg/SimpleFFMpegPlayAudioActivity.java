package com.yocn.meida.view.activity.ffmpeg;

import android.view.View;
import android.widget.Button;

import com.yocn.libnative.FFMpegSimpleAudioPlayer;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.view.activity.BaseActivity;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class SimpleFFMpegPlayAudioActivity extends BaseActivity {
    public static String DESC = "最简单的FFMpeg播放音频";
    private Button playBtn;
    private Button convertBtn;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_simple_button;
    }

    protected void initView(View root) {
        playBtn = root.findViewById(R.id.btn_play);
        convertBtn = root.findViewById(R.id.btn_convert);
    }

    protected void initData() {
        playBtn.setOnClickListener(onClickListener);
        convertBtn.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = v -> {
        String mp3FilePath = Constant.getTestMp3FilePath();
        String targetMp3FilePath = Constant.getTestFilePath("test.pcm");
        if (v.getId() == R.id.btn_play) {
            new Thread(() -> new FFMpegSimpleAudioPlayer().play(mp3FilePath)).start();
        } else if (v.getId() == R.id.btn_convert) {
            new Thread(() -> new FFMpegSimpleAudioPlayer().convert(mp3FilePath, targetMp3FilePath)).start();
        }
    };
}
