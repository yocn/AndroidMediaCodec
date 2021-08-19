package com.yocn.meida.view.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.yocn.media.R;

import androidx.annotation.LayoutRes;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName BaseActivity
 */
public abstract class BaseActivity extends Activity {
    protected String TAG;
    FrameLayout mBack;
    public static String DESC = "";

    {
        TAG = this.getClass().getSimpleName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        View rootView = getLayoutInflater().inflate(getContentViewId(), null);
        super.onCreate(savedInstanceState);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    @LayoutRes
    protected abstract int getContentViewId();

    protected abstract void initView(View root);

    protected void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
