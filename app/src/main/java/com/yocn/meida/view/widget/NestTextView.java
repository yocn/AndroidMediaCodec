package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019-12-10 11:56
 * @ClassName NestTextView
 */
public class NestTextView extends TextView {


    public NestTextView(Context context) {
        super(context);
    }

    public NestTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        LogUtil.d("dispatchTouchEvent:" + event.toString());
        return true;
//        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtil.d("onTouchEvent:" + event.toString());
        return true;
//        return super.onTouchEvent(event);
    }
}
