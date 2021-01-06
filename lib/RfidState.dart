enum ConnectState { connected, disconnected }

class RfidState {
  static ConnectState of(int value) {
    return ConnectState.values[value];
  }
}
