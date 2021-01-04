
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterRfidSerial {
  static const MethodChannel _channel =
      const MethodChannel('flutter_rfid_serial');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
