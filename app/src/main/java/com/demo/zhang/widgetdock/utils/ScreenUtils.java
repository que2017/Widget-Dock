package com.demo.zhang.widgetdock.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 用于获取设备屏幕相关参数
 */
public class ScreenUtils {
    public static final String TAG = ScreenUtils.class.getSimpleName();

    private WindowManager windowManager;
    private DisplayMetrics metrics;

    public ScreenUtils(Context context){
        this.windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        this.metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
    }

    public int getScreenWidth(){
        return metrics.widthPixels;
    }

    public int getScreenHeight(){
        return metrics.heightPixels;
    }
}
