import 'package:flutter/material.dart';
import 'package:android_notification_listener/android_notification_listener.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: Scaffold(
            appBar: AppBar(
              title: const Text('Plugin example app'),
            ),
            body: Body()));
  }
}

class Body extends StatefulWidget {
  @override
  _MyBodyState createState() => _MyBodyState();
}

class _MyBodyState extends State<Body> {
  AndroidNotificationListener notificationListener;

  @override
  void initState() {
    notificationListener = AndroidNotificationListener(
      (item) {
        Scaffold.of(context).showSnackBar(SnackBar(
            content: Text(
                "${item.title} ${item.text} ${item.packageName} ${item.subText}")));
      },
    );
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        children: <Widget>[
          RaisedButton(
            child: Text("isPermissionGranted"),
            onPressed: () async {
              if (await notificationListener.isPermissionGranted()) {
                Scaffold.of(context)
                    .showSnackBar(SnackBar(content: Text("yes")));
              } else {
                Scaffold.of(context).showSnackBar(
                    SnackBar(content: Text("No, ask Permission")));
              }
            },
          ),
          RaisedButton(
            child: Text("askPermission"),
            onPressed: () async {
              await notificationListener.askPermission();
            },
          ),
          RaisedButton(
            child: Text("startListener"),
            onPressed: () async {
              await notificationListener.startListener();
              Scaffold.of(context)
                  .showSnackBar(SnackBar(content: Text("starting listener")));
            },
          ),
        ],
      ),
    );
  }
}
