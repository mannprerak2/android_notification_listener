package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (channel != null) {
            List<String> obj = new ArrayList<>();

            obj.add(sbn.getPackageName());
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_SUB_TEXT));

            channel.invokeMethod("onNotificationPosted", obj);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }


}
