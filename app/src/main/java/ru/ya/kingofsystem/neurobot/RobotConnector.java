package ru.ya.kingofsystem.neurobot;


import android.util.Log;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Валька уебал все блютузники и теперь коннектимся через сеть
 */
public class RobotConnector implements Runnable{
    private String host;
    private int port;
    private Socket socket;
    private DataOutput out;
    private DataInput in;
    public RobotConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public RobotConnector() {
        this("192.168.0.177", 22288); // Я в депрессии и не буду делать автоматический поиск :(
    }

    @Override
    public void run() {
        // FIXME: 06.09.15  Я пока не придумал, что арда нaм присылает, хотя я вру
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            //System.exit(-1); // Я в очень большой депрессии и мне лень обрабатывать ошибки
        }
    }

    public void sendData(String str) {
        try {
            out.writeChars(str+'\n');
            Log.d("RobotConnector", str);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
