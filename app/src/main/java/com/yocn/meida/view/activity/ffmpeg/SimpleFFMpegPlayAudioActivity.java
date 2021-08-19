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
    private Button clickBtn;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_simple_button;
    }

    protected void initView(View root) {
        clickBtn = root.findViewById(R.id.btn_click);
    }

    protected void initData() {
        String mp4FilePath = Constant.getTestMp4FilePath();
        String mp3FilePath = "/sdcard/MP3/ring.mp3";
        String targetMp3FilePath = "/sdcard/MP3/ring.pcm";
        clickBtn.setOnClickListener(v -> {
//            new Thread(() -> new FFMpegSimpleAudioPlayer().playAudio(mp4FilePath)).start();
            new Thread(() -> new FFMpegSimpleAudioPlayer().convert(mp3FilePath, targetMp3FilePath)).start();
        });
    }

}
