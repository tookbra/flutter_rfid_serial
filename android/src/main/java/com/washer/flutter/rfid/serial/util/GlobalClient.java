package com.washer.flutter.rfid.serial.util;

import com.gg.reader.api.dal.GClient;

/**
 * <p>
 * 连接客户端-单例
 */
public class GlobalClient {

    private GlobalClient() {

    }

    private enum Singleton {
        INSTANCE;

        private final GClient client;

        Singleton() {
            client = new GClient();
        }

        private GClient getInstance() {
            return client;
        }
    }

    public static GClient getClient() {

        return Singleton.INSTANCE.getInstance();
    }
}
