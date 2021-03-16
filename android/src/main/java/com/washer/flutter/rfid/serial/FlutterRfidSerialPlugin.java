package com.washer.flutter.rfid.serial;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.washer.flutter.rfid.serial.util.GlobalClient;

import java.lang.ref.WeakReference;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.view.FlutterNativeView;

/** FlutterRfidSerialPlugin */
public class FlutterRfidSerialPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ViewDestroyListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private RfidManager rfidManager;

  private static final String TAG = "FlutterRfidSerialPlugin";

  private static final String NAMESPACE = "flutter_rfid_serial";

  private WeakReference<Activity> mActivity;

  private BinaryMessenger binaryMessenger;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    binaryMessenger = flutterPluginBinding.getBinaryMessenger();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), NAMESPACE);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "getPlatformVersion":
        result.success("Android " + android.os.Build.VERSION.RELEASE);
        break;
      case "connect":
        int id = rfidManager.connect();
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

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    mActivity = new WeakReference<>(activityPluginBinding.getActivity());
    init();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    dispose();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
    mActivity = new WeakReference<>(activityPluginBinding.getActivity());
    init();
  }

  @Override
  public void onDetachedFromActivity() {
    dispose();
  }

  private void init() {
    channel.setMethodCallHandler(this);
    rfidManager = new RfidManager(binaryMessenger, NAMESPACE, mActivity.get());
  }

  private void dispose() {
    if(rfidManager != null) {
      rfidManager.destroy();
    }
  }
}
