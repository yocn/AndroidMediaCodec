package com.yocn.meida.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.yocn.media.R;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName BaseActivity
 */
public class BaseActivity extends Activity {
    FrameLayout mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.trans));
    }

    protected void initView(View root) {
    }

    protected void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
