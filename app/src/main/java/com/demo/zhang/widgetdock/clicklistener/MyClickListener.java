package com.demo.zhang.widgetdock.clicklistener;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class MyClickListener implements View.OnTouchListener {
    private static final int SINGLE_CLICK = 1;
    private static final int DOUBLE_CLICK = 2;
    private static int timeout = 400;
    private int clickCount = 0;
    private Handler handler;
    private MyClickCallBack myClickCallBack;
    public interface MyClickCallBack {
        void singleClick();
        void doubleClick();
    }

    public MyClickListener(MyClickCallBack myClickCallBack) {
        this.myClickCallBack = myClickCallBack;
        handler = new Handler();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP) {
            clickCount++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (SINGLE_CLICK == clickCount) {
                        myClickCallBack.singleClick();
                    } else if (DOUBLE_CLICK == clickCount) {
                        myClickCallBack.doubleClick();
                    }
                    handler.removeCallbacksAndMessages(null);
                    clickCount = 0;
                }
            }, timeout);
        }
        return false;
    }
}
