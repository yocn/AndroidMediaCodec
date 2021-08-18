package com.yocn.meida.view.activity.ffmpeg;

import android.os.Bundle;
import android.view.View;

import com.yocn.media.R;
import com.yocn.meida.base.DataProvider;
import com.yocn.meida.view.activity.BaseActivity;
import com.yocn.meida.view.widget.TopViewRecyclerView;

/**
 * @author yocn
 */
public class FFMpegActivity extends BaseActivity {
    private TopViewRecyclerView topViewRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    protected void initView(View root) {
        topViewRecyclerView = root.findViewById(R.id.tvrv_main);
    }

    protected void initData() {
        topViewRecyclerView.setActivity(this, DataProvider.Type.FFMPEG);
    }

}
