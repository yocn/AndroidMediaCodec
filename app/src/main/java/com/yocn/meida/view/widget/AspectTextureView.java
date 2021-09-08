package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.yocn.meida.util.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * @Author yocn
 * @Date 2019/8/13 6:48 PM
 * @ClassName MyTextureView
 */
public class AspectTextureView extends TextureView {
    @ScaleType
    private int scaleType = ScaleType.FIT_CENTER;
    private int previewW = 0;
    private int previewH = 0;

    @IntDef({
            ScaleType.FIT_XY,
            ScaleType.FIT_CENTER,
            ScaleType.CENTER_CROP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScaleType {
        int FIT_XY = 0;
        int FIT_CENTER = 1;
        int CENTER_CROP = 2;
    }

    public AspectTextureView(Context context) {
        super(context);
    }

    public AspectTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScaleType(@ScaleType int scaleType) {
        this.scaleType = scaleType;
    }

    public void setSize(int width, int height) {
        post(() -> {
            previewW = width;
            previewH = height;
            //请求重新布局
            requestLayout();
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (0 == previewW || 0 == previewH) {
            //未设定宽高比，使用预览窗口默认宽高
            setMeasuredDimension(width, height);
        } else {
            int tarW = 0, tarH = 0;
            if (scaleType == ScaleType.FIT_XY) {
                tarW = width;
                tarH = height;
            } else if (scaleType == ScaleType.FIT_CENTER) {
                //设定宽高比，调整预览窗口大小（调整后窗口大小不超过默认值）
                if (width * 1F / height < previewW * 1F / previewH) {
                    tarW = width;
                    tarH = width * previewH / previewW;
                } else {
                    tarW = height * previewW / previewH;
                    tarH = height;
                }
            } else if (scaleType == ScaleType.CENTER_CROP) {
                if (width * 1F / height < previewW * 1F / previewH) {
                    //屏幕比预览细长，不管缩小还是拉伸，都应该先满足height
                    tarH = height;
                    tarW = previewW * height / previewH;
                } else {
                    //预览比屏幕细长
                    tarW = width;
                    tarH = previewH * width / previewW;
                }
            }
            LogUtil.d("AspectTextureView", "原来的:" + width + "|" + height
                    + "  raw:" + previewW + "|" + previewH + "  后：" + tarW + ":" + tarH);
            setMeasuredDimension(tarW, tarH);
            setTranslationX((width - tarW) * 1F / 2);
            setTranslationY((height - tarH) * 1F / 2);
        }

    }
}
