package com.yocn.meida.view.activity;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.presenter.YUVFilePlayer;
import com.yocn.meida.util.DisplayUtil;
import com.yocn.meida.util.LogUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName YUVPlayerActivity
 * 播放yuv文件
 */
public class YUVPlayerActivity extends BaseActivity implements View.OnClickListener {
    ImageView mArrayIV;
    ImageView mShowIV;
    ImageView mPlayIV;
    ImageView mStopIV;
    EditText mWidthET;
    EditText mHeigtET;
    YUVFilePlayer mYUVFilePlayer;
    private LinearLayout mPanelLL;
    private RelativeLayout mPanelRL;

    boolean isShow = false;
    ObjectAnimator translationYDown;
    ObjectAnimator translationYUp;
    int startY = 0, endY = 0;
    int duration = 200;

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
        mPanelRL = root.findViewById(R.id.rl_panel);
        mPanelLL = root.findViewById(R.id.ll_panel);
        mArrayIV = root.findViewById(R.id.iv_array);
        mShowIV = root.findViewById(R.id.iv_show);
        mPlayIV = root.findViewById(R.id.iv_play);
        mStopIV = root.findViewById(R.id.iv_stop);
        mWidthET = root.findViewById(R.id.et_w);
        mHeigtET = root.findViewById(R.id.et_h);
        mPlayIV.setOnClickListener(this);
        mStopIV.setOnClickListener(this);
        mPanelLL.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        endY = -DisplayUtil.dip2px(this, 170);
        initAnim();
        String yuvFilePath = Constant.getTestYuvFilePath();
        mYUVFilePlayer = new YUVFilePlayer(this).setFilePath(yuvFilePath).setWH(640, 480);
        mYUVFilePlayer.setBitmapInterface(bitmap -> mShowIV.post(() -> mShowIV.setImageBitmap(bitmap)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initAnim() {
        translationYUp = ObjectAnimator.ofFloat(mPanelRL, "translationY", endY, startY);
        translationYUp.setDuration(duration);
        translationYDown = ObjectAnimator.ofFloat(mPanelRL, "translationY", startY, endY);
        translationYDown.setDuration(duration);
    }

    private void exeAnim() {
        if (isShow) {
            translationYUp.start();
            mArrayIV.animate().rotation(180);
        } else {
            translationYDown.start();
            mArrayIV.animate().rotation(0);
        }
        isShow = !isShow;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                if (mYUVFilePlayer.isRunning()) {
                    mYUVFilePlayer.pause();
                    mPlayIV.setImageResource(R.drawable.mediacontroller_play);
                } else {
                    mYUVFilePlayer.start();
                    mPlayIV.setImageResource(R.drawable.mediacontroller_pause);
                }
                break;
            case R.id.iv_stop:
                mYUVFilePlayer.stop();
                mPlayIV.setImageResource(R.drawable.mediacontroller_play);
                break;
            case R.id.ll_panel:
                exeAnim();
                break;
            default:
        }
    }
}
