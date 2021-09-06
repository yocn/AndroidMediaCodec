package com.yocn.meida.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.yocn.meida.camera.BaseCameraProvider;

/**
 * @Author yocn
 * @Date 2019/8/13 4:41 PM
 * @ClassName DisplayUtil
 */
public class DisplayUtil {
    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);

    }

    public static void setStatusBarColor(Activity activity, int colorId) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(activity.getResources().getColor(colorId));
    }

    public static int getNavigationBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static void setAndroidNativeLightStatusBar(Activity activity, boolean dark) {
        View decor = activity.getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public static String getColor(int percent) {
        String prefix = "#";
        String rawColor = "EDEDED";
        String s = get(percent);
        return prefix + s + rawColor;
    }

    public static String get(int num) {
        String hexStr = "";
        float temp = 255 * num * 1.0f / 100f;
        int alpha = Math.round(temp);
        hexStr = Integer.toHexString(alpha);
        if (hexStr.length() < 2) {
            hexStr = "0" + hexStr;
        }
//            System.out.println(i + "%, " + hexStr.toUpperCase());
        return hexStr.toUpperCase();
    }

    public static Size getTextureViewSize(Size previewSize) {
//      640/480=1920/x; -> x=1920*480/640;
        int tWidth = BaseCameraProvider.ScreenSize.getHeight() * previewSize.getWidth() / previewSize.getHeight();
        int tHeight = BaseCameraProvider.ScreenSize.getHeight();
        LogUtil.d("BaseCameraProvider.ScreenSize.getHeight()：" + BaseCameraProvider.ScreenSize.getHeight() + " " + previewSize.getWidth() + "/" + previewSize.getHeight());
        LogUtil.d("计算的宽高：" + tWidth + "/" + tHeight);
        return new Size(tWidth, tHeight);
    }
}