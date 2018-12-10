package com.demo.zhang.widgetdock.addwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.demo.zhang.widgetdock.R;

public class AddWidgetActivity extends Activity {
    private static final String TAG = AddWidgetActivity.class.getSimpleName();

    private static final int REQUEST_PICK_APPWIDGET = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 2;
    private static final int APP_WIDGET_HOST_ID = 0x100; // 用于标识
    private static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
    private static final String SEARCH_WIDGET = "search_widget";

    private AppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
//    private FrameLayout frameLayout;
    private LinearLayout widgetWrapper;

    private class LongClickFrameLayout extends FrameLayout {
        private boolean isLongClick = false;
        private int appWidgetId;
        private int timeout = 500;
        private long start;
        private long end = 0;

        public LongClickFrameLayout(@Nullable Context context, int appWidgetId) {
            super(context);
            this.appWidgetId = appWidgetId;
        }

        public LongClickFrameLayout(@NonNull Context context) {
            super(context);
        }

        public LongClickFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public LongClickFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
//            Log.i(TAG, "onInterceptTouchEvent action = " + ev.getAction());
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                isLongClick = false;
                start = System.currentTimeMillis();
            }
            if (action == MotionEvent.ACTION_MOVE) {
                end = System.currentTimeMillis();
                if((end - start) > timeout && !isLongClick) {
                    deleteWidgetDialog(appWidgetId);
                    isLongClick = true;
                    return true;
                }
            }
            return false;
        }
    }

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
        widgetWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initWidget();

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

    private void initWidget() {
        WidgetDatabase db = new WidgetDatabase(this);
        try {
            db.open();
            Cursor cursor = db.queryAll();
            if (cursor.moveToFirst()) {
                do {
                    int appWidgetId = cursor.getInt(cursor.getColumnIndex(WidgetDatabase.WIDGET_ID));
                    completeAddAppWidget(appWidgetId);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null)
            db.close();
        }
    }

    private void insertToDB(int appWidgetId) {
        WidgetDatabase db = new WidgetDatabase(this);
        try {
            db.open();
            db.insert(appWidgetId);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    private void deleteFromDB(int appWidgetId) {
        WidgetDatabase db = new WidgetDatabase(this);
        try {
            db.open();
            db.delete(appWidgetId);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
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
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    completeAddAppWidget(appWidgetId);
                    insertToDB(appWidgetId);
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
                completeAddAppWidget(appWidgetId);
                insertToDB(appWidgetId);
            }
        }
    }

    /**
     * 添加widget
     * @param appWidgetId
     */
    private void completeAddAppWidget(int appWidgetId) {
        Log.i(TAG, "appWidgetId = " + appWidgetId);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

        View hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        LongClickFrameLayout frameLayout = new LongClickFrameLayout(this, appWidgetId);
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

    /**
     * 弹出删除widget的对话框
     */
    private void deleteWidgetDialog(final int appWidgetId) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFromDB(appWidgetId);
                widgetWrapper.removeAllViews();
                initWidget();
            }
        });
        ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.setTitle("删除widget：");
        ab.setMessage("确认要删除该widget吗？");
        ab.show();
    }
}
