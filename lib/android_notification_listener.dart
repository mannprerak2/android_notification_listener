import 'dart:async';

import 'package:flutter/services.dart';

class AndroidNotificationListener {
  void Function(NotificationItem) onNotificationPosted;

  AndroidNotificationListener(
    this.onNotificationPosted,
  );

  static const MethodChannel _channel =
      const MethodChannel('android_notification_listener');

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "onNotificationPosted":
        onNotificationPosted(
          NotificationItem(call.arguments[0], call.arguments[1],
              call.arguments[2], call.arguments[3]),
        );
        break;
    }
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> isPermissionGranted() async =>
      await _channel.invokeMethod('isPermissionGranted');

  Future<void> askPermission() async =>
      await _channel.invokeMethod('askPermission');

  Future<bool> startListener() async {
    _channel.setMethodCallHandler(_handleMethod);
    return await _channel.invokeMethod('startListener');
  }
}

class NotificationItem {
  String packageName, title, text, subText;

  NotificationItem(this.packageName, this.title, this.text, this.subText);
}
