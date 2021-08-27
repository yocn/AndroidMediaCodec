package com.yocn.meida.view.activity.ffmpeg;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import com.yocn.libnative.FFMpegSimpleVideoPlayer;
import com.yocn.libnative.FFMpegSimpleVideoPlayerNormalTime;
import com.yocn.libnative.NativeProgress;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.view.activity.BaseActivity;

import androidx.annotation.NonNull;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class SimpleFFMpegPlayVideoActivity extends BaseActivity {
    public static String DESC = "最简单的FFMpeg播放视频";
    private SurfaceView surfaceView;
    private FFMpegSimpleVideoPlayer simplePlayer;
    private ProgressBar progressBar;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_simple_play_video;
    }

    protected void initView(View root) {
        surfaceView = root.findViewById(R.id.sv_play);
        progressBar = root.findViewById(R.id.pb_test);
        simplePlayer = new FFMpegSimpleVideoPlayer();
        simplePlayer.setGetProgressCallback(new NativeProgress.GetProgressCallback() {
            @Override
            public void progress(long curr, long total, int percent) {

                runOnUiThread(() -> {
                    progressBar.setProgress(percent);
                });
            }
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                String mp4FilePath = Constant.getTestMp4FilePath2();
                new Thread(() -> simplePlayer.play(mp4FilePath, surfaceView.getHolder().getSurface())).start();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });
    }


    protected void initData() {

    }

}
