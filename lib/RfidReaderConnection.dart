part of flutter_rfid_serial;

class RfidReaderConnection {
  final EventChannel _readChannel;
  StreamController<String> _readStreamController;
  StreamSubscription<String> _readStreamSubscription;

  final EventChannel _stopChannel;
  StreamController<bool> _stopStreamController;
  StreamSubscription<bool> _stopStreamSubscription;

  Stream<String> input;

  Stream<bool> stop;

  final int _id;

  RfidReaderConnection._consumeConnection(int id)
      : this._id = id,
        this._readChannel =
            EventChannel('${FlutterRfidSerial.namespace}/read/'),
        this._stopChannel =
        EventChannel('${FlutterRfidSerial.namespace}/stop/') {
    _readStreamController = StreamController<String>();

    _readStreamSubscription =
        _readChannel.receiveBroadcastStream().cast<String>().listen(
              _readStreamController.add,
              onError: _readStreamController.addError,
              onDone: this.close,
            );
    input = _readStreamController.stream;

    _stopStreamController = StreamController<bool>();
    _stopStreamSubscription =
        _stopChannel.receiveBroadcastStream().cast<bool>().listen(
          _stopStreamController.add,
          onError: _stopStreamController.addError,
          onDone: this.closeStop,
        );
    stop = _stopStreamController.stream;

  }

  /// Closes connection (rather immediately), in result should also disconnect.
  Future<void> close() {
    return Future.wait([
      _readStreamSubscription.cancel(),
      (!_readStreamController.isClosed)
          ? _readStreamController.close()
          : Future.value(/* Empty future */)
    ], eagerError: true);
  }

  /// Closes connection (rather immediately), in result should also disconnect.
  Future<void> closeStop() {
    return Future.wait([
      _stopStreamSubscription.cancel(),
      (!_stopStreamController.isClosed)
          ? _stopStreamController.close()
          : Future.value(/* Empty future */)
    ], eagerError: true);
  }
}
