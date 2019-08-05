package com.yocn.meida.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import com.yocn.media.R;
import com.yocn.meida.JumpBean;
import com.yocn.meida.view.adapter.MainAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yocn
 */
public class MainActivity extends Activity {
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View rootView = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    private void initView(View root) {
        mRecyclerView = root.findViewById(R.id.rv_main);

    }

    private void initData() {
        List<JumpBean> list = new ArrayList<>();
        list.add(new JumpBean("PurePreviewActivity", PurePreviewActivity.class));
        list.add(new JumpBean("PreviewDataActivity", PreviewDataActivity.class));
        MainAdapter mMainAdapter = new MainAdapter(list);
        mMainAdapter.setmContext(this);
        mRecyclerView.setAdapter(mMainAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

}
