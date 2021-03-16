import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_rfid_serial/flutter_rfid_serial.dart';
import 'package:flutter_rfid_serial/models.dart';
import 'package:oktoast/oktoast.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await RfidReader.platformVersion;
      showToast(platformVersion);
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
  }

  Future<void> getState() async {
    try {
      var state = await RfidReader.state;
      showToast(state.toString());
    } on PlatformException {
    }
  }

  Future<void> connect() async {
    RfidReader.connect().then((_connection) {
      showToast("true");
      _connection.input.listen((data){
        showToast(data);
      }).onDone((){
        print('done');
      });

      _connection.stop.listen((data){
        showToast("stop:$data");
      }).onDone((){
        print('done');
      });

    }).catchError((error) {
      print('Cannot connect, exception occured');
      print(error);
    });
  }

  Future<void> disconnect() async {
    try {
      var state = await RfidReader.disconnect();
      showToast(state.toString());
    } on PlatformException {
    }
  }

  Future<void> setPower() async {
    try {
      List<Power> powers = [];
      Power power = Power(1,1);
      powers.add(power);
      var state = await RfidReader.setPower(powers);
      showToast(state.toString());
    } on PlatformException {
    }
  }

  Future<void> startRead(int type, bool single) async {
    try {
      var state = await RfidReader.startRead(type, single);
      showToast(state.toString());
    } on PlatformException {
    }
  }

  Future<void> stopRead() async {
    try {
      var state = await RfidReader.stopRead();
      showToast(state.toString());
    } on PlatformException {
    }
  }

  @override
  Widget build(BuildContext context) {
    return OKToast(
      child: MaterialApp(
        home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body: Column(
            children: [
              FlatButton(
                child: Text('获取版本号'),
                color: Colors.blue[200],
                onPressed: (){
                  initPlatformState();
                },
              ),
              FlatButton(
                child: Text('获取连接状态'),
                color: Colors.blue[200],
                onPressed: (){
                  getState();
                },
              ),
              FlatButton(
                child: Text('连接'),
                onPressed: (){
                  connect();
                },
                color: Colors.blue[200],
              ),
              FlatButton(
                child: Text('断开连接'),
                onPressed: (){
                  disconnect();
                },
                color: Colors.blue[200],
              ),
              FlatButton(
                child: Text('设置功率'),
                onPressed: (){
                  setPower();
                },
                color: Colors.blue[200],
              ),
              FlatButton(
                child: Text('开始读取'),
                onPressed: (){
                  startRead(0, true);
                },
                color: Colors.blue[200],
              ),
              FlatButton(
                child: Text('停止读取'),
                onPressed: (){
                  stopRead();
                },
                color: Colors.blue[200],
              ),
            ],
          )
        ),
      )
    );
  }
}
