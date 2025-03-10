part of flutter_rfid_serial;

class RfidReader {
  static Future<dynamic>? get platformVersion =>
      FlutterRfidSerial._methodChannel.invokeMethod('getPlatformVersion');

  /// 获取连接状态
  static Future<ConnectState> get state async => RfidState.of(
      await FlutterRfidSerial._methodChannel.invokeMethod('getState'));

  /// 打开连接
  static Future<RfidReaderConnection> connect() async {
    return RfidReaderConnection._consumeConnection(await FlutterRfidSerial
        ._methodChannel
        .invokeMethod('connect'));
  }

  /// 断开连接
  static Future<bool> disconnect() async {
    return await FlutterRfidSerial._methodChannel.invokeMethod('disconnect');
  }

  /// 设置功率
  static Future<bool> setPower(List<Power> array) async {
    Map<String, dynamic> args = <String, dynamic>{};
    args.putIfAbsent("json", () => jsonEncode(array));
    return await FlutterRfidSerial._methodChannel
        .invokeMethod('setPower', args);
  }

  /// 停止读取
  static Future<bool> stopRead() async {
    return await FlutterRfidSerial._methodChannel.invokeMethod('stopRead');
  }

  /// 开始读取
  static Future<bool> startRead(int type, bool single) async {
    return await FlutterRfidSerial._methodChannel
        .invokeMethod('startRead', {"type": type, "single": single});
  }
}
