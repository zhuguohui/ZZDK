package com.example.zzdk;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by 仁军 on 2017/4/18.
 */

public class NeNotificationService extends AccessibilityService {


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        //判断辅助服务触发的事件是否是通知栏改变事件
        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

            //获取Parcelable对象
            Parcelable data = event.getParcelableData();

            //判断是否是Notification对象
            if (data instanceof Notification) {

                Notification notification = (Notification) data;

                Intent intent = new Intent();
                intent.putExtra("NotifyData", notification);
                intent.putExtra("packageName", event.getPackageName());

                intent.setAction(".NeNotificationService");

                //进行处理解析通知栏内容的函数
                MainActivity.notifyReceive((String) event.getPackageName(), notification);

            } else {

            }

        }
    }


    /**
     * Service被启动的时候会调用这个API
     */
    @Override
    protected void onServiceConnected() {

        //设置关心的事件类型
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;//两个相同事件的超时时间间隔
        setServiceInfo(info);
    }


    @Override
    public void onInterrupt() {

    }
}