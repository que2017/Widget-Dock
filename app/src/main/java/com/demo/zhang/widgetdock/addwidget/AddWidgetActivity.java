package com.demo.zhang.widgetdock.addwidget;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.demo.zhang.widgetdock.R;

public class AddWidgetActivity extends Activity {
    private static final String TAG = AddWidgetActivity.class.getClass().getSimpleName();

    private static final int REQUEST_PICK_APPWIDGET = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 2;
    private static final int APP_WIDGET_HOST_ID = 0x100; // 用于标识
    private static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
    private static final String SEARCH_WIDGET = "search_widget";

    private AppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
//    private FrameLayout frameLayout;
    private LinearLayout widgetWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.widget_container);

        mAppWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        mAppWidgetHost = new AppWidgetHost(getApplicationContext(), APP_WIDGET_HOST_ID);

        mAppWidgetHost.startListening();
        widgetWrapper = (LinearLayout) findViewById(R.id.widget_wrapper);
        widgetWrapper.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showWidgetPickDialog();
                return true;
            }
        });

//        frameLayout = new FrameLayout(this);
//        frameLayout.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                showWidgetPickDialog();
//                return true;
//            }
//        });
//        setContentView(frameLayout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_APPWIDGET:
                    addAppWidget(data);
                    break;
                case REQUEST_CREATE_APPWIDGET:
                    completeAddAppWidget(data);
                    break;
            }
        } else if (requestCode == REQUEST_PICK_APPWIDGET
                && resultCode == RESULT_CANCELED
                && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if(appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    /**
     * 选中了某个widget之后，根据是否有配置来决定直接添加还是弹出配置activity
     * @param data
     */
    private void addAppWidget(Intent data) {
        int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        String customWidget = data.getStringExtra(EXTRA_CUSTOM_WIDGET);
        Log.i(TAG, "data:" + customWidget);
        if (SEARCH_WIDGET.equals(customWidget)) {
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
        } else {
            AppWidgetProviderInfo appWidgetProviderInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            Log.i(TAG, "configure:" + appWidgetProviderInfo.configure);
            if (appWidgetProviderInfo.configure != null){
                // 有配置，弹出配置
                Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
                intent.setComponent(appWidgetProviderInfo.configure);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
            } else {
                // 没有配置，直接添加
                completeAddAppWidget(data);
            }
        }
    }

    /**
     * 添加widget
     * @param data
     */
    private void completeAddAppWidget(Intent data) {
        Bundle bundle = data.getExtras();
        int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        if(-1 == appWidgetId) {
            return;
        }
        Log.i(TAG, "dumping extras content = " + bundle.toString());
        Log.i(TAG, "appWidgetId = " + appWidgetId);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        View hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        FrameLayout frameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        frameLayout.setLayoutParams(lp);
        frameLayout.addView(hostView);
        widgetWrapper.addView(frameLayout);
    }

    /**
     * 用于选取系统中的widget
     */
    private void showWidgetPickDialog() {
        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        Intent widgetPick = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        widgetPick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        startActivityForResult(widgetPick, REQUEST_PICK_APPWIDGET);
    }
}
