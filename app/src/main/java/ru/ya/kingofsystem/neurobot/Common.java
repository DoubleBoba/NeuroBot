package ru.ya.kingofsystem.neurobot;

import android.bluetooth.BluetoothDevice;

/**
 * Created by tamtaradam on 28.08.15.
 */
public class Common {
    private Common(){}
    private static BluetoothDevice mindWave, robot;

    public static void setMindWave(BluetoothDevice mindWave) {
        Common.mindWave = mindWave;
    }

    public static void setRobot(BluetoothDevice robot) {
        Common.robot = robot;
    }

    public static BluetoothDevice getMindWave() {
        return mindWave;
    }

    public static BluetoothDevice getRobot() {
        return robot;
    }
}
