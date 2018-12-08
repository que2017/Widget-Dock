package com.demo.zhang.widgetdock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.demo.zhang.widgetdock.addwidget.AddWidgetActivity;

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

    private Boolean shoudDestory = false;

    // 状态栏的高度
    int statusBarHeight = -1;

    private MyBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            try {
                Log.i(TAG, "connected with " + iMyAidlInterface.getServiceName());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainService.this, "链接断开，重新启动 RemoteService", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainService.this, RemoteService.class);
            startService(intent);
            bindService(intent, connection, Context.BIND_IMPORTANT);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBinder = new MyBinder();
        return mBinder;
    }

    private class MyBinder extends IMyAidlInterface.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return MainService.class.getName();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "MainService onCreate");
//        foregroundRun();
        createToucher();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        foregroundRun();
        Intent service = new Intent(MainService.this, RemoteService.class);
        startService(service);
        bindService(service, connection, Context.BIND_IMPORTANT);
        return START_STICKY;
    }

    /**
     * 使服务更好地运行在后台，不被销毁，不适用于本app
     */
    private void foregroundRun() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle(getText(R.string.app_name));
        builder.setContentText("Widget Dock is running");
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    private void createToucher() {
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        // 设置type为系统提示型窗口，通常在应用程序的窗口之上
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        // 设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 设置窗口初始停靠位置
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.x = 0;
        params.y = 500;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.
        //如果你想完全对应布局设置，需要先获取到设备的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        // 获取浮动窗口视图所在的布局
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.activity_main, null);
        // 添加toucherlayout
        windowManager.addView(toucherLayout, params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        // 计算当前View的宽高
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // 用于检测状态栏的高度
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG, "The status bar height is : " + statusBarHeight);

        button = (Button) toucherLayout.findViewById(R.id.bTouch);
        button.setOnClickListener(new View.OnClickListener() {
            long[] hints = new long[2];
            @Override
            public void onClick(View v) {
                System.arraycopy(hints, 1, hints, 0, 1);
                hints[hints.length - 1] = SystemClock.uptimeMillis();
                if ((SystemClock.uptimeMillis() - hints[0] >= 700)) {
                    Intent intent = new Intent();
                    intent.setClass(MainService.this, AddWidgetActivity.class);
                    startActivity(intent);
                    Toast.makeText(MainService.this, "连续点两次退出", Toast.LENGTH_SHORT).show();
                } else {
                    shoudDestory = true;
                    stopSelf();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        windowManager.removeView(toucherLayout);
        if (!shoudDestory) {
            startService(new Intent(this, MainService.class));
        }
        super.onDestroy();
    }
}
