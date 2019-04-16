import 'dart:async';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:meta/meta.dart';

class AndroidNotificationListener {
  static const String CHANNEL = 'android_notification_listener';
  static const String BACKGROUND_CHANNEL =
      'android_notification_listener_background';

  void Function(NotificationItem) onNotificationPosted;

  AndroidNotificationListener({
    @required this.onNotificationPosted,
  });

  static const MethodChannel _channel = const MethodChannel(CHANNEL);

  static Future<bool> initialize() async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    await _channel
        .invokeMethod('startListener', <dynamic>[callback.toRawHandle()]);
  }

  static Future<bool> registerNotificationService(void Function() callback) {
    final args = <dynamic>[
      PluginUtilities.getCallbackHandle(callback).toRawHandle()
    ];
    _channel.invokeMethod('GeofencingPlugin.registerGeofence', args);
  }

  static void callbackDispatcher() {
    // 1. Initialize MethodChannel used to communicate with the platform portion of the plugin.
    const MethodChannel _backgroundChannel = MethodChannel(BACKGROUND_CHANNEL);

    // 2. Setup internal state needed for MethodChannels.
    WidgetsFlutterBinding.ensureInitialized();

    // 3. Listen for background events from the platform portion of the plugin.
    _backgroundChannel.setMethodCallHandler((MethodCall call) async {
      final args = call.arguments;

      // 3.1. Retrieve callback instance for handle.
      final Function callback = PluginUtilities.getCallbackFromHandle(
          CallbackHandle.fromRawHandle(args[0]));
      assert(callback != null);

      // 3.2. Preprocess arguments.
      final packageName = args[1].cast<String>();

      // 3.3. Invoke callback.
      callback(packageName);
    });

    // 4. Alert plugin that the callback handler is ready for events.
    _backgroundChannel.invokeMethod('GeofencingService.initialized');
  }

//  Future<dynamic> _handleMethod(MethodCall call) async {
//    switch (call.method) {
//      case "onNotificationPosted":
//        onNotificationPosted(
//          NotificationItem(call.arguments[0], call.arguments[1],
//              call.arguments[2], call.arguments[3]),
//        );
//        break;
//    }
//  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  Future<bool> isPermissionGranted() async =>
      await _channel.invokeMethod('isPermissionGranted');

  Future<void> askPermission() async =>
      await _channel.invokeMethod('askPermission');

//  Future<bool> startListener() async {
//    await _channel.invokeMethod('startListener');
//  }
}

class NotificationItem {
  String packageName, title, text, subText;

  NotificationItem(this.packageName, this.title, this.text, this.subText);
}
