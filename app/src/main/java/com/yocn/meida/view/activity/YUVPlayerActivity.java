package com.yocn.meida.view.activity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.presenter.YUVFilePlayer;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName YUVPlayerActivity
 * 播放yuv文件
 */
public class YUVPlayerActivity extends BaseActivity implements View.OnClickListener {
    ImageView mShowIV;
    ImageView mPlayIV;
    ImageView mStopIV;
    EditText mWidthET;
    EditText mHeigtET;
    YUVFilePlayer mYUVFilePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_yuv_play, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mShowIV = root.findViewById(R.id.iv_show);
        mPlayIV = root.findViewById(R.id.iv_play);
        mStopIV = root.findViewById(R.id.iv_stop);
        mWidthET = root.findViewById(R.id.et_w);
        mHeigtET = root.findViewById(R.id.et_h);
        mPlayIV.setOnClickListener(this);
        mStopIV.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        String yuvFilePath = Constant.getTestYuvFilePath();
        mYUVFilePlayer = new YUVFilePlayer(this).setFilePath(yuvFilePath).setWH(640, 480);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                if (mYUVFilePlayer.isRunning()) {
                    mYUVFilePlayer.start();
                    mPlayIV.setImageResource(R.drawable.mediacontroller_pause);
                } else {
                    mYUVFilePlayer.pause();
                    mPlayIV.setImageResource(R.drawable.mediacontroller_play);
                }
                break;
            case R.id.iv_stop:
                mYUVFilePlayer.stop();
                mPlayIV.setImageResource(R.drawable.mediacontroller_play);
                break;
            default:
        }
    }
}
