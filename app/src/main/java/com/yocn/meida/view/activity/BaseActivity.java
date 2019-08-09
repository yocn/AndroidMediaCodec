package com.yocn.meida.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
