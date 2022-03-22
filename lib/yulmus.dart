
import 'dart:async';

import 'package:flutter/services.dart';

class Yulmus {
  static const MethodChannel _channel = MethodChannel('yulmus');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
