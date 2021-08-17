package com.yocn.meida.view.widget;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import com.yocn.meida.util.LogUtil;

import androidx.annotation.Nullable;

/**
 * @Author yocn
 * @Date 2019-12-10 11:56
 * @ClassName NestScrollView
 */
public class NestScrollView extends ScrollView {
    private static final int distance = 2;
    private PointF downPoint = new PointF();
    // 中间MOVE移动是否超过了distance
    private boolean beyond = false;

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
    public boolean dispatchTouchEvent(MotionEvent event) {
        LogUtil.d("dispatchTouchEvent:" + printEvent(event));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downPoint = new PointF(event.getX(), event.getY());
                getParent().requestDisallowInterceptTouchEvent(true);
                LogUtil.d("dispatchTouchEvent:" + event.toString());
                break;
            case MotionEvent.ACTION_MOVE:
                if (!beyond) {
                    PointF movePoint = new PointF(event.getX(), event.getY());
                    // beyond是否曾经超过过distance
                    beyond = check(downPoint, movePoint, false);
                }
                break;
            case MotionEvent.ACTION_UP:
                PointF upPoint = new PointF(event.getX(), event.getY());
                getParent().requestDisallowInterceptTouchEvent(false);
                LogUtil.d("dispatchTouchEvent2:" + printEvent(event));
                LogUtil.d("dispatchTouchEvent2:" + beyond + " 2：" + check(downPoint, upPoint, true));
                if (!beyond || check(downPoint, upPoint, true)) {
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
        return super.dispatchTouchEvent(event);
    }

    /**
     * 检查两点之间的距离
     *
     * @param p1   点1
     * @param p2   点2
     * @param isIn 是在距离内还是外
     * @return isIn = true: 检查两个点的距离是不是小于distance; isIn = false: 检查两个点的距离是不是大于distance
     */
    private boolean check(PointF p1, PointF p2, boolean isIn) {
        double dis = Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p1.y), 2));
        LogUtil.d("dist:" + dis + "   down:[" + p1.x + "," + p1.y + "]   up:[" + p2.x + "," + p2.y + "]");
        return isIn ? dis < distance : dis > distance;
    }

    private String printEvent(MotionEvent event) {
        StringBuilder sb = new StringBuilder();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sb.append("ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                sb.append("ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                sb.append("ACTION_UP");
                break;
        }
        return sb.toString();
    }
}
