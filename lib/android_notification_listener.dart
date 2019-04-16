import 'dart:async';

import 'package:flutter/services.dart';

class AndroidNotificationListener {
  static const MethodChannel _channel =
      const MethodChannel('android_notification_listener');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
