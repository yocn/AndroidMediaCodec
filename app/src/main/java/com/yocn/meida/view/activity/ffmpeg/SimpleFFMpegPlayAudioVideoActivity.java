package com.yocn.meida.view.activity.ffmpeg;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import com.yocn.libnative.FFMpegSimpleAudioVideoPlayer;
import com.yocn.libnative.NativeProgress;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.util.TimeUtil;
import com.yocn.meida.view.activity.BaseActivity;

import androidx.annotation.NonNull;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class SimpleFFMpegPlayAudioVideoActivity extends BaseActivity {
    public static String DESC = "音视频同步";
    private SurfaceView surfaceView;
    private FFMpegSimpleAudioVideoPlayer player;
    private ProgressBar pb_test;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_simple_play_video;
    }

    protected void initView(View root) {
        surfaceView = root.findViewById(R.id.sv_play);
        pb_test = root.findViewById(R.id.pb_test);
        player = new FFMpegSimpleAudioVideoPlayer();
        player.setGetProgressCallback(new NativeProgress.GetProgressCallback() {
            @Override
            public void progress(long curr, long total, int percent) {

                runOnUiThread(() -> {
                    pb_test.setProgress(percent);
                });
            }
        });
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                String mp4FilePath = Constant.getTestMp4FilePath2();
                player.play(mp4FilePath, surfaceView.getHolder().getSurface());
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
