
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterCyberVision {
  static const MethodChannel _channel =
      const MethodChannel('flutter_cyber_vision');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
