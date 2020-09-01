package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.view.TextureView;

import com.yocn.meida.camera.BaseCameraProvider;
import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019/8/13 6:48 PM
 * @ClassName MyTextureView
 */
public class MyTextureView extends TextureView {
    Size size = BaseCameraProvider.TextureViewSize;

    public MyTextureView(Context context) {
        super(context);
    }

    public MyTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setShowSize(Size size) {
        this.size = size;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.makeMeasureSpec(size.getHeight(), MeasureSpec.EXACTLY);
        int width = MeasureSpec.makeMeasureSpec(size.getWidth(), MeasureSpec.EXACTLY);
        setMeasuredDimension(width, height);
        int realH = getMeasuredHeight();
        int realW = getMeasuredWidth();
        LogUtil.d("wh->" + realW + "/" + realH);
    }
}
