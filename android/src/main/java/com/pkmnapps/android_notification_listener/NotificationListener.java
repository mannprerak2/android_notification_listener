package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import io.flutter.plugin.common.MethodChannel;

@SuppressLint({"NewApi", "Registered"})
public class NotificationListener extends NotificationListenerService {

    private static MethodChannel channel;

    public static void setBackgroundChannel(MethodChannel channel) {
        NotificationListener.channel = channel;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        channel.invokeMethod("onNotificationPosted", null);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


}
