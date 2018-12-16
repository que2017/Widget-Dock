package com.demo.zhang.widgetdock;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.demo.zhang.widgetdock.addwidget.AddWidgetActivity;
import com.demo.zhang.widgetdock.clicklistener.MyClickListener;
import com.demo.zhang.widgetdock.utils.ScreenUtils;

/**
 * 为了让MainService保活采取了如下措施：（均无效）
 * 1. 在AndroidManifest文件中设置优先级1000，android:priority="1000"
 * 2. 设置MainService为前台进程，详见foregroundRun方法 startForeground
 * 3. 在onDestroy方法中重新启动MainService
 * 4. 设置双进程守护，RemoteService
 */
public class MainService extends Service {
    private static final String TAG = MainService.class.getSimpleName();

    private ConstraintLayout toucherLayout;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    private Button button;

    private Boolean shouldDestroy = false;

    // 状态栏的高度
    int statusBarHeight = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MainService onCreate");
        createToucher();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createToucher() {
        // 获取浮动窗口视图所在的布局
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.activity_main, null);

        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // 设置type为系统提示型窗口，通常在应用程序的窗口之上。O版需要设置TYPE_APPLICATION_OVERLAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        // 设置效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        // 设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 设置窗口初始停靠位置
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.x = 0;
        params.y = (new ScreenUtils(getApplication()).getScreenHeight() - 500) / 2;

        //设置悬浮窗口长宽数据.
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // 添加toucherlayout
        windowManager.addView(toucherLayout, params);

        // 计算当前View的宽高
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // 用于检测状态栏的高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG, "The status bar height is : " + statusBarHeight);

        button = (Button) toucherLayout.findViewById(R.id.bTouch);
        button.setOnTouchListener(new MyClickListener(new MyClickListener.MyClickCallBack() {
            private boolean isFirstMove = true;
            private int initY;
            private int paramsY;
            @Override
            public void singleClick() {
                Intent intent = new Intent();
                intent.setClass(MainService.this, AddWidgetActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void longClickMove(MotionEvent event) {
                if (isFirstMove) {
                    initY = (int) event.getRawY();
                    paramsY = params.y;
                    isFirstMove = false;
                } else {
                    params.y = paramsY + (int) event.getRawY() - initY;
                    windowManager.updateViewLayout(toucherLayout, params);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    isFirstMove = true;
                }
            }

            @Override
            public void doubleClick() {
                shouldDestroy = true;
                stopSelf();
            }
        }));
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(toucherLayout);
        if (!shouldDestroy) {
            startService(new Intent(this, MainService.class));
        }
        super.onDestroy();
    }
}
