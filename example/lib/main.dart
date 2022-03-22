import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:yulmus/yulmus.dart' as yulalipush;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
    yulalipush.invokeChannelCallBack();
    if (!Platform.isAndroid) {
      yulalipush.configureNotificationPresentationOption();
    }
    yulalipush.register();
    yulalipush.initCloudChannelResult.listen((event) {
      print(
          "----------->init successful ${event.isSuccessful} ${event.errorCode} ${event.errorMessage}");
    });

    yulalipush.onNotification.listen((data) {
      print("----------->notification here ${data.summary}");
      setState(() {
        _platformVersion = data.summary??"";
      });
    });
    yulalipush.onNotificationOpened.listen((data) {
      print("-----------> ${data.summary} 被点了");
      setState(() {
        _platformVersion = "${data.summary} 被点了";
      });
    });

    yulalipush.onNotificationRemoved.listen((data) {
      print("-----------> $data 被删除了");
    });

    yulalipush.onNotificationReceivedInApp.listen((data) {
      print("-----------> ${data.summary} In app");
    });

    yulalipush.onNotificationClickedWithNoAction.listen((data) {
      print("${data.summary} no action");
    });

    yulalipush.onMessageArrived.listen((data) {
      print("received data -> ${data.content}");
      setState(() {
        _platformVersion = data.content??"";
      });
    });
    //注册辅助通道
    //yulalipush.register();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String  platformVersion = 'Failed to get platform version.';
    if (!mounted) return;
    setState(() {
      _platformVersion = platformVersion;
    });
  }

  getDeviceId() async {
    var deviceId = await yulalipush.deviceId;
    print("deviceId:::$deviceId");
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running on: $_platformVersion\n'),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: ()=>{
            getDeviceId()
          },
          child:const Icon(Icons.wb_sunny_sharp),
        ),
      ),
    );
  }
}
