package com.demo.zhang.widgetdock.clicklistener;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyClickListener implements View.OnTouchListener {
    private static final int SINGLE_CLICK = 1;
    private static final int DOUBLE_CLICK = 2;
    private static final int TIMEOUT = 400;
    private long start;
    private long end;
    private int clickCount = 0;
    private Handler handler;
    private MyClickCallBack myClickCallBack;
    public interface MyClickCallBack {
        void singleClick();
        void longClickMove(MotionEvent event);
        void doubleClick();
    }

    public MyClickListener(MyClickCallBack myClickCallBack) {
        this.myClickCallBack = myClickCallBack;
        handler = new Handler();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            start = System.currentTimeMillis();
        }
        if (action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP) {
            end = System.currentTimeMillis();
            if (end - start > TIMEOUT) {
                myClickCallBack.longClickMove(event);
            }
        }
        if(action == MotionEvent.ACTION_UP) {
            end = System.currentTimeMillis();
            clickCount++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (SINGLE_CLICK == clickCount) {
                        if (end - start < TIMEOUT) {
                            myClickCallBack.singleClick();
                        }
                    } else if (DOUBLE_CLICK == clickCount) {
                        myClickCallBack.doubleClick();
                    }
                    handler.removeCallbacksAndMessages(null);
                    clickCount = 0;
                }
            }, TIMEOUT);
        }
        return false;
    }
}
