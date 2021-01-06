package com.washer.flutter.rfid.serial;

import android.util.Log;

import androidx.annotation.NonNull;

import com.washer.flutter.rfid.serial.util.GlobalClient;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.FlutterNativeView;

/** FlutterRfidSerialPlugin */
public class FlutterRfidSerialPlugin implements FlutterPlugin, MethodCallHandler, PluginRegistry.ViewDestroyListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private RfidManager rfidManager;

  private static final String TAG = "FlutterRfidSerialPlugin";

  private static final String NAMESPACE = "flutter_rfid_serial";

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), NAMESPACE);
    channel.setMethodCallHandler(this);
    rfidManager = new RfidManager(flutterPluginBinding.getBinaryMessenger(), NAMESPACE);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case "connect":
        if (!call.hasArgument("address")) {
          result.error("invalid_argument", "argument 'address' not found", null);
          break;
        }
        if (!call.hasArgument("port")) {
          result.error("invalid_argument", "argument 'port' not found", null);
          break;
        }
        String address = call.argument("address");
        String port = call.argument("port");
        int id = rfidManager.connect(address, port);
        if(id == 0) {
          result.error("connect_error", "", null);
        } else {
          result.success(id);
        }
        break;
      case "disconnect":
        result.success(rfidManager.disConnect());
        break;
      case "getState":
        result.success(rfidManager.getState());
        break;
      case "setPower":
        if (!call.hasArgument("json")) {
          result.error("invalid_argument", "argument 'json' not found", null);
          break;
        }
        String json = call.argument("json");
        Log.d(TAG, json);
        result.success(rfidManager.setPower(json));
        break;
      case "startRead":
        if (!call.hasArgument("type")) {
          result.error("invalid_argument", "argument 'type' not found", null);
          break;
        }
        if (!call.hasArgument("single")) {
          result.error("invalid_argument", "argument 'single' not found", null);
          break;
        }
        int type = call.argument("type");
        boolean single = call.argument("single");
        result.success(rfidManager.startRead(type, single));
        break;
      case "stopRead":
        result.success(rfidManager.stopRead());
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  @Override
  public boolean onViewDestroy(FlutterNativeView flutterNativeView) {
    rfidManager.destroy();
    return false;
  }
}
