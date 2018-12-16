package com.demo.zhang.widgetdock.addwidget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WidgetDatabase {
    private static final String TAG = WidgetDatabase.class.getSimpleName();

    private static final String DB_NAME = "Widget.db";
    private static final int DB_VERSION = 1;
    private static final String TB_NAME = "widget_id_table";

    private static final String KEY_ID = "_id";
    public static final String WIDGET_ID = "widget_id";

    private final Context mContext;
    private WidgetDBHelper widgetDBHelper;
    private SQLiteDatabase mDatabase;

    private class WidgetDBHelper extends SQLiteOpenHelper {
        public WidgetDBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE "
                    + TB_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + WIDGET_ID + " INTEGER NOT NULL" + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TB_NAME);
            onCreate(db);
        }
    }

    public WidgetDatabase(Context context) {
        mContext = context;
    }

    public WidgetDatabase open() throws SQLException {
        widgetDBHelper = new WidgetDBHelper(mContext);
        mDatabase = widgetDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        widgetDBHelper.close();
    }

    public Cursor queryAll(){
        Cursor cursor = mDatabase.query(TB_NAME, null, null, null, null, null, null);
        return cursor;
    }

    public void insert(int appWidgetId){
        ContentValues values = new ContentValues();
        values.put(WIDGET_ID, appWidgetId);
        long row_id = mDatabase.insert(TB_NAME, null, values);
        if (-1 == row_id) {
            Log.d(TAG, "insert failed: " + appWidgetId);
        }
    }

    public void delete(int appWidgetId) {
        mDatabase.delete(TB_NAME, WIDGET_ID + " = ?", new String[]{String.valueOf(appWidgetId)});
    }
}
