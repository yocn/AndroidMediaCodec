package com.yocn.meida.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

import com.yocn.meida.camera.BaseCameraProvider;
import com.yocn.meida.util.LogUtil;

/**
 * @Author yocn
 * @Date 2019/8/13 6:48 PM
 * @ClassName MyTextureView
 */
public class MyTextureView extends TextureView {

    public MyTextureView(Context context) {
        super(context);
    }

    public MyTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.makeMeasureSpec(BaseCameraProvider.TextureViewSize.getHeight(), MeasureSpec.EXACTLY);
        int width = MeasureSpec.makeMeasureSpec(BaseCameraProvider.TextureViewSize.getWidth(), MeasureSpec.EXACTLY);
//        int height = MeasureSpec.makeMeasureSpec(400, MeasureSpec.EXACTLY);
//        int width = MeasureSpec.makeMeasureSpec(1400, MeasureSpec.EXACTLY);
        setMeasuredDimension(width, height);
        int realH = getMeasuredHeight();
        int realW = getMeasuredWidth();
        LogUtil.d("wh->" + realW + "/" + realH);
    }
}
