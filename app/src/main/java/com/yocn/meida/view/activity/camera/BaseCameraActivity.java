package com.yocn.meida.view.activity.camera;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yocn.media.R;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.view.activity.BaseActivity;

/**
 * @Author yocn
 * @Date 2019/8/4 9:46 AM
 * @ClassName PreviewDataActivity
 */
public abstract class BaseCameraActivity extends BaseActivity implements View.OnClickListener {
    ImageView mBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView(View root) {
        LogUtil.d("initView");
        mBack = root.findViewById(R.id.iv_back);
        mBack.setOnClickListener(this);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                LogUtil.d("finish");
                finish();
                break;
            default:
        }
    }
}
