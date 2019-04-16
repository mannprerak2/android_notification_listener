package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterRunArguments;

import static android.content.ContentValues.TAG;

@SuppressLint({"NewApi", "Registered"})
public class NotificationListener extends NotificationListenerService {
    private static final String CALLBACK_HANDLE_KEY = "callback_handle";
    private static final String SHARED_PREFERENCES_KEY = "com.pkmnapps.notification_listener";
    private static AtomicBoolean sStarted = new AtomicBoolean(false);
    private static final List<Intent> sNotificationQueue = Collections.synchronizedList(new LinkedList<Intent>());
    private String mAppBundlePath;

    private static FlutterNativeView sBackgroundFlutterView;
    private static MethodChannel sBackgroundChannel;
    private static PluginRegistry.PluginRegistrantCallback sPluginRegistrantCallback;

    public static void setBackgroundChannel(MethodChannel channel) {
        NotificationListener.sBackgroundChannel = channel;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        FlutterMain.ensureInitializationComplete(context, null);
        mAppBundlePath = FlutterMain.findAppBundlePath(context);
        if (!sStarted.get()) {
            SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
            long callbackHandle = p.getLong(CALLBACK_HANDLE_KEY, 0);
            startAlarmService(context, callbackHandle);
        }
    }

    public static void onInitialized() {
        sStarted.set(true);
        synchronized (sNotificationQueue) {
            // Handle all the alarm events received before the Dart isolate was fully
            // initialized and clear the queue.
            Iterator<Intent> i = sNotificationQueue.iterator();
            while (i.hasNext()) {
                invokeCallbackDispatcher(i.next());
            }
            sNotificationQueue.clear();
        }
    }

    private static void invokeCallbackDispatcher(Intent intent) {
        // Grab the handle for the callback associated with this alarm. Pay close
        // attention to the type of the callback handle as storing this value in a
        // variable of the wrong size will cause the callback lookup to fail.
        long callbackHandle = intent.getLongExtra("callbackHandle", 0);
        if (sBackgroundChannel == null) {
            Log.e(
                    TAG,
                    "setBackgroundChannel was not called before alarms were scheduled." + " Bailing out.");
            return;
        }
        // Handle the alarm event in Dart. Note that for this plugin, we don't
        // care about the method name as we simply lookup and invoke the callback
        // provided.
        sBackgroundChannel.invokeMethod("", new Object[]{callbackHandle});
    }

    public static void startAlarmService(Context context, long callbackHandle) {
        FlutterMain.ensureInitializationComplete(context, null);
        String mAppBundlePath = FlutterMain.findAppBundlePath(context);
        FlutterCallbackInformation cb =
                FlutterCallbackInformation.lookupCallbackInformation(callbackHandle);
        if (cb == null) {
            Log.e(TAG, "Fatal: failed to find callback");
            return;
        }

        // Note that we're passing `true` as the second argument to our
        // FlutterNativeView constructor. This specifies the FlutterNativeView
        // as a background view and does not create a drawing surface.
        sBackgroundFlutterView = new FlutterNativeView(context, true);
        if (mAppBundlePath != null && !sStarted.get()) {
            if (sPluginRegistrantCallback == null) {
                return;
            }
            Log.i(TAG, "Starting NotificationListenerService...");
            FlutterRunArguments args = new FlutterRunArguments();
            args.bundlePath = mAppBundlePath;
            args.entrypoint = cb.callbackName;
            args.libraryPath = cb.callbackLibraryPath;
            sBackgroundFlutterView.runFromBundle(args);
            sPluginRegistrantCallback.registerWith(sBackgroundFlutterView.getPluginRegistry());
        }
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        synchronized (sNotificationQueue) {

        }

        if (sBackgroundChannel != null) {
            List<String> obj = new ArrayList<>();

            obj.add(sbn.getPackageName());
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_TEXT));
            obj.add(sbn.getNotification().extras.getString(Notification.EXTRA_SUB_TEXT));

            sBackgroundChannel.invokeMethod("onNotificationPosted", obj);
        }
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    public static void setCallbackDispatcher(Context context, long callbackHandle) {
        SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
        p.edit().putLong(CALLBACK_HANDLE_KEY, callbackHandle).apply();
    }


}
