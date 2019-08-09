package com.yocn.meida.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;

import com.yocn.libyuv.YUVTransUtil;
import com.yocn.media.R;
import com.yocn.meida.JumpBean;
import com.yocn.meida.util.LogUtil;
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
//        getWindow().setBackgroundDrawableResource(R.color.write);
        View rootView = getLayoutInflater().inflate(R.layout.activity_main, null);
        setContentView(rootView);
        initView(rootView);
        initData();
    }

    private void initView(View root) {
        mRecyclerView = root.findViewById(R.id.rv_main);
    }

    private void initData() {
        String ss = new YUVTransUtil().stringFromJNI();
        LogUtil.d("ss->" + ss);
        List<JumpBean> list = new ArrayList<>();
//        list.add(new JumpBean("", PurePreviewActivity.class));
        list.add(new JumpBean("TextureView预览", PurePreviewActivity.class));
        list.add(new JumpBean("预览并获取数据", PreviewDataActivity.class));
        list.add(new JumpBean("Yuv数据获取", PreviewYUVDataActivity.class));
        list.add(new JumpBean("Native转换Yuv", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("FormatTransportActivity", FormatTransportActivity.class));
        list.add(new JumpBean("2", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("3", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("4", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("5", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        list.add(new JumpBean("6", PreviewNativeYUVActivity.class));
        MainAdapter mMainAdapter = new MainAdapter(list);
        mMainAdapter.setmContext(this);
        int spanCount;
        if (list.size() < 6) {
            spanCount = 2;
        } else if (list.size() < 24) {
            spanCount = 3;
        } else {
            spanCount = 4;
        }
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                                @Override
                                                public int getSpanSize(int position) {
                                                    return position == 0 ? gridLayoutManager.getSpanCount() : 1;
                                                }
                                            }
        );

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mMainAdapter);
    }

}
