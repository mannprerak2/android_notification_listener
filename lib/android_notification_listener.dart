import 'dart:async';

import 'package:flutter/services.dart';

class AndroidNotificationListener {
  static const MethodChannel _channel =
      const MethodChannel('android_notification_listener');

  static Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case "onNotificationPosted":
        print("=================");
        print(call.arguments);
        print("=================");
    }
  }


  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> isPermissionGranted() async =>
      await _channel.invokeMethod('isPermissionGranted');

  static Future<void> askPermission() async =>
      await _channel.invokeMethod('askPermission');

  static Future<bool> startListener() async {
    _channel.setMethodCallHandler(_handleMethod);
    await _channel.invokeMethod('startListener');
  }
}
