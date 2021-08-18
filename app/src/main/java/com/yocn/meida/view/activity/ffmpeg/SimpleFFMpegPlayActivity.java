package com.yocn.meida.view.activity.ffmpeg;

import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.yocn.libnative.FFMpegSimplePlayer;
import com.yocn.libnative.TestFFmpeg;
import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.view.activity.BaseActivity;

import androidx.annotation.NonNull;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewPureActivity
 */
public class SimpleFFMpegPlayActivity extends BaseActivity {
    public static String DESC = "最简单的FFMpeg播放视频";

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    protected void initView(View root) {
    }

    protected void initData() {

    }

}
