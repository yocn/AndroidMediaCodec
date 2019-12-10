package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019/8/6 1:58 PM
 * @ClassName SquareRelativeLayout
 */
public class SquareRelativeLayout extends RelativeLayout {
    public SquareRelativeLayout(Context context) {
        super(context);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //得到默认测量规则下测量到的宽度
        int measuredWidth = getMeasuredWidth();
        //得到默认测量规则下测量到的高度
        int measuredHeight = getMeasuredHeight();
//        LogUtil.d("w/h->" + measuredWidth + "/" + measuredHeight);
//        setMeasuredDimension(measuredWidth, measuredWidth);
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
