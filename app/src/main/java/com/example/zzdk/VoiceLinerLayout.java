package com.example.zzdk;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class VoiceLinerLayout extends LinearLayout {
    // 触屏监听
    float lastY;
    int oldOffsetY;

    public VoiceLinerLayout(Context context) {
        super(context);
    }

    public VoiceLinerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //private  WindowManager windowManager;
//    private  WindowManager.LayoutParams winParams;

    /*public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    public void setWinParams(WindowManager.LayoutParams winParams) {
        this.winParams = winParams;
    }
*/


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        float y = -event.getY();
        oldOffsetY = updatePositionListener.getLastY(); // 偏移量
        if (action == MotionEvent.ACTION_DOWN) {
            Log.i("zzz", "event down");
            lastY = y;
        } else if (action == MotionEvent.ACTION_MOVE) {
            Log.i("zzz", "event move");
            oldOffsetY += (int) (y - lastY) / 3; // 减小偏移量,防止过度抖动
            //  winParams.y += (int) (y - lastY); // 减小偏移量,防止过度抖动
            // windowManager.updateViewLayout(playView, winParams);
            updatePositionListener.updateY(oldOffsetY);
        } else {
            Log.i("zzz", "event " + action);
        }
        return super.dispatchTouchEvent(event);
    }

    UpdatePositionListener updatePositionListener;

    public void setUpdatePositionListener(UpdatePositionListener updatePositionListener) {
        this.updatePositionListener = updatePositionListener;
    }

    public interface UpdatePositionListener {
        int getLastY();

        void updateY(int y);
    }
}
