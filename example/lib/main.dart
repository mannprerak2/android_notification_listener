import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
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
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Center(
      child: Column(
        children: <Widget>[
          RaisedButton(
            child: Text("isPermissionGranted"),
            onPressed: () async {
              if (await AndroidNotificationListener.isPermissionGranted()) {
                Scaffold.of(context)
                    .showSnackBar(SnackBar(content: Text("yes")));
              } else {
                Scaffold.of(context)
                    .showSnackBar(SnackBar(content: Text("No")));
              }
            },
          ),
          RaisedButton(
            child: Text("askPermission"),
            onPressed: () async {
              await AndroidNotificationListener.askPermission();
            },
          ),
          RaisedButton(
            child: Text("Start Listener"),
            onPressed: () async {
              await AndroidNotificationListener.initialize((item) {
                Scaffold.of(context)
                    .showSnackBar(SnackBar(content: Text(item.toString())));
              });
            },
          ),
        ],
      ),
    );
  }
}
