package com.yocn.meida.view.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019-12-10 11:56
 * @ClassName NestScrollView
 */
public class NestScrollView extends ScrollView {

    private int mWidth, mHeight;
    private int mChildHeight;
    private boolean mHandle;

    public NestScrollView(Context context) {
        super(context);
    }

    public NestScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        LogUtil.d("mWidth:" + mWidth + "   " + mHeight + "    " + this);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child = getChildAt(0);
        if (child != null) {
            mChildHeight = child.getMeasuredHeight();
            LogUtil.d("childHeight:" + mChildHeight);
        }

        if (mChildHeight > mWidth / 2) {
            mHandle = true;
            //不知道为什么layout_gravity = "center"的时候Child会出现偏移，所以当需要处理的时候手动将layout_gravity去掉
            FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.gravity = LayoutParams.UNSPECIFIED_GRAVITY;
            child.setLayoutParams(lp);
        } else {
            mHandle = false;
        }

        super.onLayout(changed, l, t, r, b);
    }

    boolean move = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

            }
        });
        LogUtil.d("dispatchTouchEvent1111:" + event.toString());
        if (mHandle) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    move = false;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    LogUtil.d("dispatchTouchEvent:" + event.toString());
                    break;
                case MotionEvent.ACTION_MOVE:
                    move = true;
                    break;
                case MotionEvent.ACTION_UP:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    LogUtil.d("dispatchTouchEvent:" + event.toString());
                    if (!move) {
                        boolean call = callOnClick();
                        LogUtil.d("callOnClick:" + call);
                        if (!call) {
                            View parent = (View) getParent();
                            parent.callOnClick();
                        }
                    }
                    break;
                default:
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
