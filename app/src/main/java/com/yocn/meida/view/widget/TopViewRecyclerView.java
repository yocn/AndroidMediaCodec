package com.yocn.meida.view.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.yocn.media.R;
import com.yocn.meida.JumpBean;
import com.yocn.meida.base.DataProvider;
import com.yocn.meida.camera.BaseCameraProvider;
import com.yocn.meida.util.DisplayUtil;
import com.yocn.meida.view.adapter.MainAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TopViewRecyclerView extends FrameLayout {
    @DataProvider.Type
    private int type = DataProvider.Type.MAIN;
    private RecyclerView mRecyclerView;
    private RelativeLayout mTopRL;
    private int currentY;
    private final Context context;

    public TopViewRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public TopViewRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopViewRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.view_top_recyclerview, this, true);
        initView(getRootView());
    }

    private void initView(View root) {
        mRecyclerView = root.findViewById(R.id.rv_main);
        mTopRL = root.findViewById(R.id.rl_top);
        mTopRL.post(() -> {
            int height = getMeasuredHeight();
            int width = getMeasuredWidth();
            BaseCameraProvider.ScreenSize = new Size(width, height);
            BaseCameraProvider.TextureViewSize = DisplayUtil.getTextureViewSize(BaseCameraProvider.previewSize);
        });
    }

    public void setActivity(Activity activity, @DataProvider.Type int type) {
        this.type = type;
        initData(activity);
    }

    private void initData(Activity activity) {
        List<JumpBean> data = DataProvider.getDataList(type);
        MainAdapter mMainAdapter = new MainAdapter(data);
        mMainAdapter.setmContext(context);
        int spanCount = 2;
        if (data.size() < 6) {
            spanCount = 2;
        } else if (data.size() < 24) {
            spanCount = 3;
        } else {
            spanCount = 4;
        }
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(context, spanCount);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                                                @Override
                                                public int getSpanSize(int position) {
                                                    return position == 0 ? gridLayoutManager.getSpanCount() : 1;
                                                }
                                            }
        );

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mMainAdapter);
        mRecyclerView.setItemViewCacheSize(2);

        final int min = DisplayUtil.dip2px(context, 100);
        final int max = DisplayUtil.dip2px(context, 140);
//        gridLayoutManager.scrollToPositionWithOffset(0, -200);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                currentY += dy;
                if (currentY < min) {
                    mTopRL.setVisibility(View.GONE);
                    DisplayUtil.setAndroidNativeLightStatusBar(activity, false);
                } else {
                    mTopRL.setVisibility(View.VISIBLE);
                    DisplayUtil.setAndroidNativeLightStatusBar(activity, true);
                    if (currentY < max) {
                        int percent = (currentY - min) * 100 / (max - min);
                        String color = DisplayUtil.getColor(percent);
                        mTopRL.setBackgroundColor(Color.parseColor(color));
                    } else {
                        mTopRL.setBackgroundResource(R.color.gray);
                    }

                }
            }
        });
    }

}
