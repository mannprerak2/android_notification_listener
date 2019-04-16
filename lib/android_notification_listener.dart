import 'dart:async';

import 'package:flutter/services.dart';

class AndroidNotificationListener {
  static const MethodChannel _channel =
      const MethodChannel('android_notification_listener');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool> isPermissionGranted() async =>
      await _channel.invokeMethod('isPermissionGranted');

  static Future<void> askPermission() async =>
      await _channel.invokeMethod('askPermission');
}
