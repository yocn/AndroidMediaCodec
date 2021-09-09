package com.yocn.meida.view.widget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public interface AspectInterface {

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

    void setScaleType(@ScaleType int scaleType);

    void setSize(int width, int height);
}
