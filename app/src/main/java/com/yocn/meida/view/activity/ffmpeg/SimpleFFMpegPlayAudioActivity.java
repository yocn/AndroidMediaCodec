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
    public static String DESC = "最简单的FFMpeg播放视频";
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
        clickBtn.setOnClickListener(v -> new FFMpegSimpleAudioPlayer().playAudio(mp4FilePath));
    }

}
