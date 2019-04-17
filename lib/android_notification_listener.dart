import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

void callbackDispatcher() {
  const MethodChannel _backgroundChannel = MethodChannel(
      'android_notification_listener_background', JSONMethodCodec());

  WidgetsFlutterBinding.ensureInitialized();

  _backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final dynamic args = call.arguments;
    final Function callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args[0]));

    if (callback == null) {
      print('Fatal: could not find callback');
      exit(-1);
    }

    callback(NotificationItem(args[1], args[2], args[3], args[4]));
  });

  _backgroundChannel.invokeMethod('NotificationListener.initialized');
}

void notificationCallback(NotificationItem obj) {
  print("${obj.packageName} ${obj.title} ${obj.text} ${obj.subText}");
}

class AndroidNotificationListener {
  static const MethodChannel _channel =
      const MethodChannel('android_notification_listener', JSONMethodCodec());

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> isPermissionGranted() async =>
      await _channel.invokeMethod('isPermissionGranted');

  static Future<void> askPermission() async =>
      await _channel.invokeMethod('askPermission');

  /// Initialize the notification listener service
  static Future<void> initialize(
      Function(dynamic item) mNotificationCallback) async {
    final CallbackHandle handle =
        PluginUtilities.getCallbackHandle(callbackDispatcher);

    final CallbackHandle callback =
        PluginUtilities.getCallbackHandle(notificationCallback);

    if (handle == null) return false;

    final bool r = await _channel.invokeMethod(
        'NotificationListener.initializeService',
        <dynamic>[handle.toRawHandle(), callback.toRawHandle()]);

    return r ?? false;
  }
}

class NotificationItem {
  String packageName, title, text, subText;

  NotificationItem(this.packageName, this.title, this.text, this.subText);
}
