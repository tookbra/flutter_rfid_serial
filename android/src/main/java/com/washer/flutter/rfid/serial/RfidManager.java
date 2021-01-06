package com.washer.flutter.rfid.serial;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerTag6bLog;
import com.gg.reader.api.dal.HandlerTag6bOver;
import com.gg.reader.api.dal.HandlerTagEpcLog;
import com.gg.reader.api.dal.HandlerTagEpcOver;
import com.gg.reader.api.dal.HandlerTagGbLog;
import com.gg.reader.api.dal.HandlerTagGbOver;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.LogBase6bInfo;
import com.gg.reader.api.protocol.gx.LogBase6bOver;
import com.gg.reader.api.protocol.gx.LogBaseEpcInfo;
import com.gg.reader.api.protocol.gx.LogBaseEpcOver;
import com.gg.reader.api.protocol.gx.LogBaseGbInfo;
import com.gg.reader.api.protocol.gx.LogBaseGbOver;
import com.gg.reader.api.protocol.gx.MsgBaseInventory6b;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryGb;
import com.gg.reader.api.protocol.gx.MsgBaseSetPower;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.ParamEpcReadTid;
import com.washer.flutter.rfid.serial.util.GlobalClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel.Result;

public class RfidManager {

    private static final String TAG = "RfidManager";

    // 是否连接
    private boolean isClient = false;

    // 是否在读
    private boolean isReader = false;

    private EventChannel readChannel;

    private EventChannel.EventSink readSink;

    private BinaryMessenger binaryMessenger;

    private String namespace;

    private int lastConnectionId = 0;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public RfidManager(BinaryMessenger binaryMessenger, String namespace) {
        this.binaryMessenger = binaryMessenger;
        this.namespace = namespace;
    }

    private void subHandler(GClient client) {
        // 订阅标签上报事件
        client.onTagEpcLog = (readerName, info) -> {
            if (null != info && 0 == info.getResult()) {
                Log.d(TAG, info.toString());
                if(null != readSink) {
                    mExecutor.execute(() -> readSink.success(info));
                }

            }
        };

        // 标签上报结束事件
        client.onTagEpcOver = (readerName, info) -> {
            if (null != info) {
                Log.d(TAG, info.toString());
            }
        };

        // ISO18000-6B 标签上报事件
        client.onTag6bLog = (readerName, info) -> {

        };

        // ISO18000-6B 标签上报结束事件
        client.onTag6bOver = (readerName, info) -> {
        };

        // 国标标签上报事件
        client.onTagGbLog = (readerName, info) -> {

        };

        // 国标标签上报结束事件
        client.onTagGbOver = (readerName, info) -> {
        };
    }

    private final EventChannel.StreamHandler readStreamHandler = new EventChannel.StreamHandler() {
        @Override
        public void onListen(Object o, EventChannel.EventSink eventSink) {
            readSink = eventSink;
        }
        @Override
        public void onCancel(Object o) {
            // If canceled by local, disconnects - in other case, by remote, does nothing
//            self.disconnect();

            // True dispose
            AsyncTask.execute(() -> {
                readChannel.setStreamHandler(null);

                Log.d(TAG, "Disconnected stream handler");
            });
        }
    };

    /**
     * connect to serialPort
     * @param
     */
    public int connect(String address, String port) {
        String param = "/dev/" +  address + ":" + port;
        Log.d(TAG, "Connecting to " + param);
        if (GlobalClient.getClient().openAndroidSerial(param, 1000)) {
            MsgBaseStop msgBaseStop = new MsgBaseStop();
            GlobalClient.getClient().sendSynMsg(msgBaseStop);
            if (0 == msgBaseStop.getRtCode()) {
                Log.d(TAG, "Stop successful");
                isClient = true;
                ++lastConnectionId;

                subHandler(GlobalClient.getClient());

                readChannel = new EventChannel(binaryMessenger, namespace + "/read/");
                readChannel.setStreamHandler(readStreamHandler);
            } else {
                Log.d(TAG, "Stop error");
            }
        }
        return lastConnectionId;
    }

    /**
     * disconnect
     * @return
     */
    public boolean disConnect() {
        if(isClient) {
            if(GlobalClient.getClient().close()) {
                isClient = false;
            }
        }
        return !isClient;
    }

    /**
     * 获取状态
     * @return
     */
    public int getState() {
        return isClient ? 0 : 1;
    }

    /**
     * 设置天线功率
     * @param json
     * @return
     */
    public boolean setPower(String json) {
        boolean flag = false;
        if(isClient) {
            try {
                MsgBaseSetPower msgBaseSetPower = new MsgBaseSetPower();
                Hashtable<Integer, Integer> hashtable = new Hashtable<>();
                JSONArray data = new JSONArray(json);

                JSONObject options;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject row = data.getJSONObject(i);

                    /* GET DATA OPTIONS FROM ARRAY */
                    options = row.getJSONObject("Options");
                    int key = options.getInt("key");
                    int value = options.getInt("value");
                    hashtable.put(key, value);
                }
                msgBaseSetPower.setDicPower(hashtable);
                GlobalClient.getClient().sendSynMsg(msgBaseSetPower);
                if (0 == msgBaseSetPower.getRtCode()) {
                    flag = true;
                    Log.d(TAG, "Power configuration successful");
                } else {
                    Log.d(TAG, "Power configuration error");
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return flag;
    }

    /**
     * 停止操作
     * @return
     */
    public boolean stopRead() {
        if(isClient) {
            MsgBaseStop msgBaseStop = new MsgBaseStop();
            GlobalClient.getClient().sendSynMsg(msgBaseStop);
            if (0x00 == msgBaseStop.getRtCode()) {
                isReader = false;
                Log.d(TAG, "Stop read successful");
            } else {
                Log.d(TAG, "Stop read error");
            }
        }
        return !isReader;
    }

    /**
     * 开始操作
     * @param type 0 6c 1 6b 2 gb
     * @param single 是否单次读取
     * @return
     */
    public boolean startRead(int type, boolean single) {
        if(isClient) {
            if(!isReader) {
                if (type == 0) {
                    MsgBaseInventoryEpc msgBaseInventoryEpc = new MsgBaseInventoryEpc();
                    msgBaseInventoryEpc.setAntennaEnable(EnumG.AntennaNo_1 | EnumG.AntennaNo_2 | EnumG.AntennaNo_3 | EnumG.AntennaNo_4);
                    if (single) {
                        msgBaseInventoryEpc.setInventoryMode(EnumG.InventoryMode_Single);
                    } else {
                        msgBaseInventoryEpc.setInventoryMode(EnumG.InventoryMode_Inventory);
                    }
                    GlobalClient.getClient().sendSynMsg(msgBaseInventoryEpc);
                    if (0x00 == msgBaseInventoryEpc.getRtCode()) {
                        Log.d(TAG, "start to read");
                        isReader = true;
                    } else {
                        Log.d(TAG, "start to read error");
                        isReader = false;
                    }
                } else if(type == 1) {
                    MsgBaseInventory6b msgBaseInventory6b = new MsgBaseInventory6b();
                    msgBaseInventory6b.setAntennaEnable(EnumG.AntennaNo_1 | EnumG.AntennaNo_2 | EnumG.AntennaNo_3 | EnumG.AntennaNo_4);
                    if (single) {
                        msgBaseInventory6b.setInventoryMode(EnumG.InventoryMode_Single);
                    } else {
                        msgBaseInventory6b.setInventoryMode(EnumG.InventoryMode_Inventory);
                    }
                    GlobalClient.getClient().sendSynMsg(msgBaseInventory6b);
                    if (0x00 == msgBaseInventory6b.getRtCode()) {
                        Log.d(TAG, "start to read");
                        isReader = true;
                    } else {
                        Log.d(TAG, "start to read error");
                        isReader = false;
                    }
                } else if(type == 2) {
                    MsgBaseInventoryGb msgBaseInventoryGb = new MsgBaseInventoryGb();
                    msgBaseInventoryGb.setAntennaEnable(EnumG.AntennaNo_1 | EnumG.AntennaNo_2 | EnumG.AntennaNo_3 | EnumG.AntennaNo_4);
                    if (single) {
                        msgBaseInventoryGb.setInventoryMode(EnumG.InventoryMode_Single);
                    } else {
                        msgBaseInventoryGb.setInventoryMode(EnumG.InventoryMode_Inventory);
                    }

                    ParamEpcReadTid tid = new ParamEpcReadTid();
                    tid.setMode(EnumG.ParamTidMode_Auto);
                    tid.setLen(6);
                    msgBaseInventoryGb.setReadTid(tid);
                    GlobalClient.getClient().sendSynMsg(msgBaseInventoryGb);
                    if (0x00 == msgBaseInventoryGb.getRtCode()) {
                        Log.d(TAG, "start to read");
                        isReader = true;
                    } else {
                        Log.d(TAG, "start to read error");
                        isReader = false;
                    }
                }
            }
        }
        return isReader;
    }

    // destroy
    public void destroy() {
        if (isClient) {
            if (isReader) {
                MsgBaseStop stop = new MsgBaseStop();
                GlobalClient.getClient().sendSynMsg(stop);
            }
        }

        if(!mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
    }
}
