package com.pkmnapps.android_notification_listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.JSONMessageCodec;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.FlutterNativeView;

/**
 * AndroidNotificationListenerPlugin
 */
public class AndroidNotificationListenerPlugin implements MethodCallHandler, PluginRegistry.ViewDestroyListener {

    private Context context;


    private AndroidNotificationListenerPlugin(Context context) {
        this.context = context;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "android_notification_listener", JSONMethodCodec.INSTANCE);
        final MethodChannel backgroundChannel = new MethodChannel(registrar.messenger(), "android_notification_listener_background", JSONMethodCodec.INSTANCE);

        AndroidNotificationListenerPlugin plugin = new AndroidNotificationListenerPlugin(registrar.context());

        channel.setMethodCallHandler(plugin);
        backgroundChannel.setMethodCallHandler(plugin);

        registrar.addViewDestroyListener(plugin);
        NotificationListener.setsBackgroundChannel(backgroundChannel);
    }

    @SuppressLint("NewApi")
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        try {
            JSONArray args = (JSONArray) call.arguments;
            switch (call.method) {
                case "getPlatformVersion":
                    result.success("Android " + android.os.Build.VERSION.RELEASE);
                    break;
                case "isPermissionGranted":
                    if (Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners").contains(context.getPackageName())) {
                        result.success(true);
                    } else {
                        result.success(false);
                    }
                    break;
                case "askPermission":
                    context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    result.success(null);
                    break;
                case "NotificationListener.initializeService":
                    NotificationListener.setCallbackDispatcher(context, args.getLong(0));
                    NotificationListener.setNotificationCallback(context, args.getLong(1));
                    context.startService(new Intent(context, NotificationListener.class));
                    result.success(true);
                    break;
                case "NotificationListener.initialized":
                    NotificationListener.onInitialized();
                    break;
                default:
                    result.notImplemented();
            }
        } catch (JSONException e) {
        }
    }

    @Override
    public boolean onViewDestroy(FlutterNativeView flutterNativeView) {
        return NotificationListener.setBackgroundFlutterView(flutterNativeView);
    }
}
