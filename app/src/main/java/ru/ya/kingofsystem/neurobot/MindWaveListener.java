package ru.ya.kingofsystem.neurobot;

import java.io.IOException;

/**
 * Интерфейс-слушатель события разрыва соединения
 */
public interface MindWaveListener {
    /**
     * Вызывается, когда пропадает соединение
     * @param state - состояние.
     * Все состояния приведенны в классе com.neurosky.thinkgear.TGDevice
     */
    public void onDisconnect(int state);
    public void onConnect();
    public void attention(int strength);
    public void meditation(int strength);
    public void blink(int strength);
    public void onBadSignal();
}
