package ru.ya.kingofsystem.neurobot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;
/**
 * Класс, который рулит подключением к MindeWave и парсит все данные с неё
 */

public class MindWave{
    private BluetoothAdapter btAdapter;
    private BluetoothDevice device;
    private TGDevice mindwave;
    private boolean isConnected = false;

    private int attention = 0;
    private int meditation = 0;
    private int blinkStrength = 0;
    private int heartRate = 0;

    private int signalLevel;
    private boolean bataryLow;

    private MindWaveListener mindWaveListener;
    
    public MindWave(BluetoothAdapter btAdapter, BluetoothDevice device,
                    MindWaveListener mindWaveListener) {
        this.btAdapter = btAdapter;
        this.device = device;
        this.mindWaveListener = mindWaveListener;
        mindwave = new TGDevice(btAdapter, new NeuroHandler());
    }
    public void connect() {
        mindwave.connect(device);
    }
    public void disconnect() {
        mindwave.close();
        isConnected = false;
    }
    private class NeuroHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            isConnected = false;
                            break;
                        case TGDevice.STATE_CONNECTING:
                            isConnected = false;
                            break;
                        case TGDevice.STATE_CONNECTED:
                            mindWaveListener.onConnect();
                            mindwave.start();
                            isConnected = true;
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            processDisconnected(msg.what);
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            processDisconnected(msg.what);
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            processDisconnected(msg.what);
                            break;
                    }

                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    signalLevel = msg.arg1;
                    mindWaveListener.onBadSignal();
                    break;
                case TGDevice.MSG_RAW_DATA:
                    // Хуита какая-то
                    break;
                case TGDevice.MSG_HEART_RATE:

                    Log.println(Log.DEBUG, "MindWave/heart_rate", ""+msg.arg1);
                    heartRate = msg.arg1;
                    break;
                case TGDevice.MSG_ATTENTION:
                    Log.println(Log.DEBUG, "MindWave/attention", ""+msg.arg1);
                    mindWaveListener.attention(msg.arg1);
                    attention = msg.arg1;
                    break;
                case TGDevice.MSG_MEDITATION:
                    mindWaveListener.meditation(msg.arg1);
                    Log.println(Log.DEBUG, "MindWave/meditation", ""+msg.arg1);
                    meditation = msg.arg1;
                    break;
                case TGDevice.MSG_BLINK:
                    mindWaveListener.blink(msg.arg1);
                    Log.println(Log.DEBUG, "MindWave/blink", ""+msg.arg1);
                    blinkStrength = msg.arg1;
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    bataryLow = true;
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //Тоже херь невнятная
                    break;
                default:
                    break;
            }
        }
    }

    private void processDisconnected(int state) {
        isConnected = false;
        if (mindWaveListener != null)
                mindWaveListener.onDisconnect(state);
    }

    public boolean isConnected() {
        return isConnected;
    }

    /**
     *
     * @return cосредоточенность
     */
    public int getAttention() {
        return attention;
    }

    /**
     *
     * @return расслабление
     */
    public int getMeditation() {
        return meditation;
    }

    /**
     *
     * @param listener - слушатель события разрыва соединения
     */
    public void setOnDisconnectListener(MindWaveListener listener) {
        mindWaveListener = listener;
    }

    /**
     *
     * @return состояние батареи
     * true - скоро разрядится
     */
    public boolean isBataryLow() {
        return bataryLow;
    }

    /**
     *
     * @return частота биения сердца
     */
    public int getHeartRate() {
        return heartRate;
    }

    /**
     *
     * @return уровень сигнала. Значение изменяется только
     * тогда, когда приходит сообщение о плохом сигнале
     */
    public int getSignalLevel() {
        return signalLevel;
    }

    /**
     *
     * @return вроде бы сила моргания, но я чот хз пиздос
     */
    public int getBlinkStrength() {
        return blinkStrength;
    }
}
