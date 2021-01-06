package com.washer.flutter.rfid.serial.util;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * <p>
 * 获取android下的端口
 */
public class SerialPortFinder {

    private static final String TAG = SerialPortFinder.class.getSimpleName();

    private static final String DRIVERS_PATH = "/proc/tty/drivers";

    private static final String SERIAL_FIELD = "serial";


    private Vector<Driver> mDrivers = null;

    /**
     * 获取 Drivers
     *
     * @return Drivers
     * @throws IOException IOException
     */
    private Vector<Driver> getDrivers() throws IOException {
        if (mDrivers == null) {
            mDrivers = new Vector<Driver>();
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(DRIVERS_PATH));
            String readLine;
            while ((readLine = lineNumberReader.readLine()) != null) {
                String driverName = readLine.substring(0, 0x15).trim();
                String[] fields = readLine.split(" +");
                if ((fields.length >= 5) && (fields[fields.length - 1].equals(SERIAL_FIELD))) {
                    Log.d(TAG, "Found new driver " + driverName + " on " + fields[fields.length - 4]);
                    mDrivers.add(new Driver(driverName, fields[fields.length - 4]));
                }
            }
            lineNumberReader.close();
        }
        return mDrivers;
    }

    /**
     * 获取文件路径
     * 
     * @return Array
     */
    public String[] getAllDevices() {
        Vector<String> devices = new Vector<String>();
        // Parse each driver
        Iterator<Driver> driverIterator;
        try {
            driverIterator = getDrivers().iterator();
            while (driverIterator.hasNext()) {
                Driver driver = driverIterator.next();
                Iterator<File> fileIterator = driver.getDevices().iterator();
                while (fileIterator.hasNext()) {
                    String device = fileIterator.next().getName();
                    String value = String.format("%s (%s)", device, driver.getName());
                    devices.add(value);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices.toArray(new String[devices.size()]);
    }
}
