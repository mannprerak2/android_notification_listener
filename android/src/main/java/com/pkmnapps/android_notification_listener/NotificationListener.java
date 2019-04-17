package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.view.FlutterCallbackInformation;
import io.flutter.view.FlutterMain;
import io.flutter.view.FlutterNativeView;
import io.flutter.view.FlutterRunArguments;

@SuppressLint({"NewApi", "Registered"})
public class NotificationListener extends NotificationListenerService {
    public static final String TAG = "NotificationListener";
    private static final String CALLBACK_HANDLE_KEY = "callback_handle";
    private static final String NOTIFICCACTION_CALLBACK_HANDLE_KEY = "notification_callback_handle";
    private static final String PERSISTENT_ALARMS_SET_KEY = "persistent_alarm_ids";
    private static final String SHARED_PREFERENCES_KEY = "com.pkmnapps.notificationlistener";
    private static final int JOB_ID = 1984; // Random job ID.
    private static final Object sPersistentAlarmsLock = new Object();
    private static final AtomicBoolean sStarted = new AtomicBoolean(false);
    private static final List<JSONArray> sNotificationQueue = Collections.synchronizedList(new LinkedList<JSONArray>());

    private static FlutterNativeView sBackgroundFlutterView;
    private static MethodChannel sBackgroundChannel;
    private static PluginRegistry.PluginRegistrantCallback sPluginRegistrantCallback;

    private Context context;

    public static long notificationCallbackHandle;
    public static long callbackDispatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    // Called once the Dart isolate (sBackgroundFlutterView) has finished
    // initializing. Processes all alarm events that came in while the isolate
    // was starting.
    public static void onInitialized() {
        synchronized (sNotificationQueue) {
            // Handle all the alarm events received before the Dart isolate was fully
            // initialized and clear the queue.
            for (JSONArray item : sNotificationQueue) {
                invokeCallbackDispatcher(item);
            }
            sNotificationQueue.clear();
            sStarted.set(true);
        }
    }

    // This is where we handle alarm events before sending them to our callback
    // dispatcher in Dart.
    private static void invokeCallbackDispatcher(JSONArray array) {
        if (sBackgroundChannel == null) {
            Log.i(TAG, "channel was null, did you call initialise?");
            return;
        }

        sBackgroundChannel.invokeMethod("", array);
    }

    public static void startIsolate(Context context, long callbackHandle) {
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
        JSONArray obj = new JSONArray();

        String title = "null", text = "null", subText = "null";

        if (sbn.getNotification().extras.get(Notification.EXTRA_TITLE) != null) {
            title = sbn.getNotification().extras.get(Notification.EXTRA_TITLE).toString();
        }
        if (sbn.getNotification().extras.get(Notification.EXTRA_TEXT) != null) {
            text = sbn.getNotification().extras.get(Notification.EXTRA_TEXT).toString();
        }
        if (sbn.getNotification().extras.get(Notification.EXTRA_SUB_TEXT) != null) {
            subText = sbn.getNotification().extras.get(Notification.EXTRA_SUB_TEXT).toString();
        }

        obj.put(notificationCallbackHandle);
        obj.put(sbn.getPackageName());
        obj.put(title); //because they can be spannable strings
        obj.put(text);
        obj.put(subText);


        if (!sStarted.get()) {//isolate not started
            sNotificationQueue.add(obj);
        }
        synchronized (sStarted) {
            if (!sStarted.get()) {//isolate not started
                startIsolate(context, callbackDispatcher);
            } else {//isolate is started
                invokeCallbackDispatcher(obj);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    public static void setCallbackDispatcher(Context context, long callbackHandle) {
        NotificationListener.callbackDispatcher = callbackHandle;

//        SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
//        p.edit().putLong(CALLBACK_HANDLE_KEY, callbackHandle).apply();
    }

    public static void setNotificationCallback(Context context, long notificationCallbackHandle) {
        NotificationListener.notificationCallbackHandle = notificationCallbackHandle;

//        SharedPreferences p = context.getSharedPreferences(SHARED_PREFERENCES_KEY, 0);
//        p.edit().putLong(NOTIFICCACTION_CALLBACK_HANDLE_KEY, notificationCallbackHandle).apply();
    }


    public static void setPluginRegistrant(PluginRegistry.PluginRegistrantCallback callback) {
        sPluginRegistrantCallback = callback;
    }

    public static void setsBackgroundChannel(MethodChannel channel) {
        sBackgroundChannel = channel;
    }

    public static boolean setBackgroundFlutterView(FlutterNativeView view) {
        if (sBackgroundFlutterView != null && sBackgroundFlutterView != view) {
            Log.i(TAG, "setBackgroundFlutterView tried to overwrite an existing FlutterNativeView");
            return false;
        }
        sBackgroundFlutterView = view;
        return true;
    }

}
