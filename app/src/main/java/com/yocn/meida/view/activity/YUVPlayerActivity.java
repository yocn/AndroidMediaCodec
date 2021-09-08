package com.yocn.meida.view.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yocn.media.R;
import com.yocn.meida.base.Constant;
import com.yocn.meida.presenter.yuv.YUVFilePlayer;
import com.yocn.meida.util.DisplayUtil;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.util.StringUtils;
import com.yocn.meida.view.widget.MTextWatcher;
import com.yocn.meida.view.widget.PopupWindowGenerater;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName YUVPlayerActivity
 * 播放yuv文件
 */
public class YUVPlayerActivity extends BaseActivity implements View.OnClickListener {
    public static String DESC = "I420播放器，可以配置参数，自己选定文件";
    private static String WIDTH = "WIDTH";
    private static String HEIGHT = "HEIGHT";
    private static String PATH = "PATH";
    private static String FPS = "FPS";
    private static String AUTOPLAY = "AUTOPLAY";

    ImageView mArrayIV;
    ImageView mShowIV;
    ImageView mPlayIV;
    ImageView mLoopIV;
    ImageView mStopIV;
    EditText mWidthET;
    EditText mHeigtET;
    EditText mFPSET;
    TextView mRotateTV;
    TextView mFormatTV;
    YUVFilePlayer mYUVFilePlayer;
    private LinearLayout mPanelLL;
    private RelativeLayout mPanelRL;
    PopupWindowGenerater mRotateOpoupWindow;
    PopupWindowGenerater mFormatOpoupWindow;

    boolean isShow = false;
    boolean isLoop = true;
    ObjectAnimator translationYDown;
    ObjectAnimator translationYUp;
    int startY = 0, endY = 0;
    int duration = 200;
    private String yuvFilePath = Constant.getTestYuvFilePath();
    private int width = 640;
    private int height = 480;
    private int fps = 30;
    private boolean autoPlay = false;
    private TextView pathTv;

    public static void playYuv(Context context, String path, int width, int height, int fps, boolean autoPlay) {
        Intent intent = new Intent(context, YUVPlayerActivity.class);
        intent.putExtra(PATH, path);
        intent.putExtra(WIDTH, width);
        intent.putExtra(HEIGHT, height);
        intent.putExtra(FPS, fps);
        intent.putExtra(AUTOPLAY, autoPlay);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exeAnim();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_yuv_play;
    }

    @Override
    protected void initView(View root) {
        pathTv = root.findViewById(R.id.tv_path);
        mPanelRL = root.findViewById(R.id.rl_panel);
        mPanelLL = root.findViewById(R.id.ll_panel);
        mArrayIV = root.findViewById(R.id.iv_array);
        mShowIV = root.findViewById(R.id.iv_show);
        mLoopIV = root.findViewById(R.id.iv_looping);
        mPlayIV = root.findViewById(R.id.iv_play);
        mStopIV = root.findViewById(R.id.iv_stop);
        mWidthET = root.findViewById(R.id.et_w);
        mHeigtET = root.findViewById(R.id.et_h);
        mFPSET = root.findViewById(R.id.et_fps);
        mRotateTV = root.findViewById(R.id.tv_rotate);
        mFormatTV = root.findViewById(R.id.tv_format);
        mPlayIV.setOnClickListener(this);
        mStopIV.setOnClickListener(this);
        mPanelLL.setOnClickListener(this);
        mLoopIV.setOnClickListener(this);
        mRotateTV.setOnClickListener(this);
        mFormatTV.setOnClickListener(this);
        mFPSET.addTextChangedListener(mFpsTextWatcher);
        mWidthET.addTextChangedListener(mWidthTextWatcher);
        mHeigtET.addTextChangedListener(mHeightTextWatcher);
    }

    @Override
    protected void initData() {
        endY = -DisplayUtil.dip2px(this, 120);
        initAnim();
        mRotateOpoupWindow = new PopupWindowGenerater<Integer>().init(this).setItems(YUVFilePlayer.mRotateTextList)
                .setAnchorView(mRotateTV).setOnItemClickListener(mRotateItemClickListener).setOnDismissListener(mRotateDismissListener);
        mFormatOpoupWindow = new PopupWindowGenerater<String>().init(this).setItems(YUVFilePlayer.mFormatTextList)
                .setAnchorView(mFormatTV).setOnItemClickListener(mFormatItemClickListener).setOnDismissListener(mFormatDismissListener);
        if (!TextUtils.isEmpty(getIntent().getStringExtra(PATH))) {
            yuvFilePath = getIntent().getStringExtra(PATH);
            width = getIntent().getIntExtra(WIDTH, 0);
            height = getIntent().getIntExtra(HEIGHT, 0);
            fps = getIntent().getIntExtra(FPS, 0);
            autoPlay = getIntent().getBooleanExtra(AUTOPLAY, false);
            mWidthET.setText(String.valueOf(width));
            mHeigtET.setText(String.valueOf(height));
            mFPSET.setText(String.valueOf(fps));
        }
        pathTv.setText(yuvFilePath);

        mYUVFilePlayer = new YUVFilePlayer(this);
        mYUVFilePlayer.setYuvCallback(new YUVFilePlayer.OnYuvPlayCallbackInterface() {
            @Override
            public void getBitmap(Bitmap bitmap) {
                if (bitmap == null) {
                    return;
                }
                mShowIV.post(() -> mShowIV.setImageBitmap(bitmap));
            }

            @Override
            public void playStatus(int status) {
                switch (status) {
                    case YUVFilePlayer.STATUS_PLAY:
                        mPlayIV.setImageResource(R.drawable.mediacontroller_pause);
                        break;
                    case YUVFilePlayer.STATUS_PAUSE:
                    case YUVFilePlayer.STATUS_STOP:
                    case YUVFilePlayer.STATUS_ERROR:
                        mPlayIV.setImageResource(R.drawable.mediacontroller_play);
                        break;
                    default:
                }
            }
        });
        if (autoPlay) {
            exeAnim();
            play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mYUVFilePlayer.stop();
        mYUVFilePlayer.release();
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

    private void play() {
        if (mYUVFilePlayer.isRunning()) {
            mYUVFilePlayer.pause();
            mPlayIV.setImageResource(R.drawable.mediacontroller_play);
        } else {
            mYUVFilePlayer.setFilePath(yuvFilePath).setWH(width, height).setFPS(fps).start();
            mPlayIV.setImageResource(R.drawable.mediacontroller_pause);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                play();
                break;
            case R.id.iv_stop:
                mYUVFilePlayer.stop();
                mPlayIV.setImageResource(R.drawable.mediacontroller_play);
                break;
            case R.id.ll_panel:
                exeAnim();
                break;
            case R.id.iv_looping:
                isLoop = !isLoop;
                mLoopIV.setImageResource(isLoop ? R.drawable.icon_select_pre : R.drawable.icon_select_n);
                mYUVFilePlayer.setLooping(isLoop);
                break;
            case R.id.tv_rotate:
                mRotateOpoupWindow.show();
                setTVDrawable(mRotateTV, true);
                break;
            case R.id.tv_format:
                mFormatOpoupWindow.show();
                setTVDrawable(mFormatTV, true);
                break;
            default:
        }
    }

    MTextWatcher mFpsTextWatcher = new MTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String num = s.toString();
            if (!StringUtils.isEmpty(num)) {
                fps = Integer.parseInt(num);
            }
        }
    };

    MTextWatcher mWidthTextWatcher = new MTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String num = s.toString();
            if (!StringUtils.isEmpty(num)) {
                width = Integer.parseInt(num);
            }
        }
    };

    MTextWatcher mHeightTextWatcher = new MTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String num = s.toString();
            if (!StringUtils.isEmpty(num)) {
                height = Integer.parseInt(num);
            }
        }
    };

    AdapterView.OnItemClickListener mRotateItemClickListener = (parent, view, position, id) -> {
        int rotate = YUVFilePlayer.mRotateTextList.get(position);
        LogUtil.d("rotate->" + rotate);
        mYUVFilePlayer.setRotate(rotate);
        mRotateOpoupWindow.dismiss();
        mRotateTV.setText("旋转:" + rotate);
    };

    AdapterView.OnItemClickListener mFormatItemClickListener = (parent, view, position, id) -> {
        String format = YUVFilePlayer.mFormatTextList.get(position);
        LogUtil.d("format->" + format);
        mFormatOpoupWindow.dismiss();
        mFormatTV.setText(format);
    };

    PopupWindow.OnDismissListener mRotateDismissListener = () -> {
        setTVDrawable(mRotateTV, false);
    };

    PopupWindow.OnDismissListener mFormatDismissListener = () -> {
        setTVDrawable(mFormatTV, false);
    };

    private void setTVDrawable(TextView tv, boolean open) {
        Drawable drawable = open ? getResources().getDrawable(R.drawable.arrow_up) : getResources().getDrawable(R.drawable.arrow_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tv.setCompoundDrawables(null, null, drawable, null);
    }

}
