package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.yocn.media.R;
import com.yocn.meida.JumpBean;
import com.yocn.meida.util.LogUtil;
import com.yocn.meida.view.adapter.MainAdapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Author yocn
 * @Date 2019/8/28 2:37 PM
 * @ClassName DampingRecyclerView
 */
public class DampingRecyclerView extends LinearLayout {
    private Context mContext;
    RecyclerView mRecyclerView;

    public DampingRecyclerView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public DampingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public DampingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) LayoutInflater.from(mContext).inflate(R.layout.view_recyclerview, null);
        addView(mRecyclerView);
        initData();
    }

    private void initData() {
        List<JumpBean> data = MainAdapter.getDataList();
        MainAdapter mMainAdapter = new MainAdapter(data);
        mMainAdapter.setmContext(mContext);
        int spanCount = 2;
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, spanCount);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        LogUtil.d("UNSPECIFIED->" + MeasureSpec.UNSPECIFIED + " EXACTLY->" + MeasureSpec.EXACTLY + " AT_MOST->" + MeasureSpec.AT_MOST);
        LogUtil.d("mode->" + mode + " size->" + size);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
