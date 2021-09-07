package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019/8/13 6:48 PM
 * @ClassName MyTextureView
 */
public class AspectTextureView extends TextureView {

    private int ratioW = 0;
    private int ratioH = 0;

    public AspectTextureView(Context context) {
        super(context);
    }

    public AspectTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置宽高比
     *
     * @param width
     * @param height
     */
    public void setAspect(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("width or height can not be negative.");
        }
        post(() -> {
            ratioW = width;
            ratioH = height;
            //请求重新布局
            requestLayout();
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (0 == ratioW || 0 == ratioH) {
            //未设定宽高比，使用预览窗口默认宽高
            setMeasuredDimension(width, height);
        } else {
            int tarW, tarH;
            //设定宽高比，调整预览窗口大小（调整后窗口大小不超过默认值）
            if (width < height * ratioW / ratioH) {
                tarW = width;
                tarH = width * ratioH / ratioW;
            } else {
                tarW = height * ratioW / ratioH;
                tarH = height;
            }
            LogUtil.d("AspectTextureView", "原来的:" + width + "|" + height
                    + "  raw:" + ratioW + "|" + ratioH + "  后：" + tarH + ":" + tarH);
            setMeasuredDimension(tarW, tarH);
        }

    }
}
