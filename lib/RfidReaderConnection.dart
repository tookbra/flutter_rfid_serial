part of flutter_rfid_serial;

class RfidReaderConnection {
  final EventChannel _readChannel;
  StreamController<String> _readStreamController;
  StreamSubscription<String> _readStreamSubscription;

  Stream<String> input;

  final int _id;

  RfidReaderConnection._consumeConnection(int id)
      : this._id = id,
        this._readChannel =
            EventChannel('${FlutterRfidSerial.namespace}/read/') {
    _readStreamController = StreamController<String>();

    _readStreamSubscription =
        _readChannel.receiveBroadcastStream().cast<String>().listen(
              _readStreamController.add,
              onError: _readStreamController.addError,
              onDone: this.close,
            );

    input = _readStreamController.stream;
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
}
