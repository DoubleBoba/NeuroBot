package ru.ya.kingofsystem.neurobot;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by tamtaradam on 28.08.15.
 */
public class ButtonControl extends Activity {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static int DIRECT=0;
    public static int LEFT=1;
    public static int RIGHT=2;
    private MindWave mindWave;
    private BluetoothAdapter btAdapter;
    private String MyTag = "ButtonControl";
    private SeekBar senseBar;
    private ImageButton leftBtn, rightBtn;
    private ToggleButton reverseBtn;
    private BluetoothDevice robot;
    private boolean nowLeft=false, nowRight=false;
    private Thread robotConnectorThread;
    private MindWaveDataReceiver mindWaveDataReceiver;
    private RobotConnector robotConnector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_control);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        mindWaveDataReceiver = new MindWaveDataReceiver();
        mindWave = new MindWave(btAdapter, Common.getMindWave(), mindWaveDataReceiver);

        senseBar = (SeekBar) findViewById(R.id.sensitive);
        leftBtn = (ImageButton) findViewById(R.id.leftBtn);
        rightBtn = (ImageButton) findViewById(R.id.rightBtn);
        reverseBtn = (ToggleButton) findViewById(R.id.reverse);
        robot = Common.getRobot();
        leftBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mindWaveDataReceiver.turn(LEFT);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    mindWaveDataReceiver.turn(DIRECT);
                return true;
            }
        });

        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    mindWaveDataReceiver.turn(RIGHT);
                else if (event.getAction() == MotionEvent.ACTION_UP)
                    mindWaveDataReceiver.turn(DIRECT);
                return false;
            }
        });

        reverseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mindWaveDataReceiver.reverse();
            }
        });
        robotConnector = new RobotConnector();
        robotConnectorThread = new Thread(robotConnector);
        robotConnectorThread.setDaemon(true); // Чтобы поток сдох, когда дохнет вся программа
        robotConnectorThread.start();
        mindWave.connect();


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private TextView attention, meditation, blink;
    private class MindWaveDataReceiver implements MindWaveListener{
        private RadioButton blinkIndicator;
        public MindWaveDataReceiver() {
            attention = (TextView) findViewById(R.id.attention);
            meditation = (TextView) findViewById(R.id.meditatiom);
            blink = (TextView) findViewById(R.id.blink_strength);
            blinkIndicator = (RadioButton) findViewById(R.id.blink);
            blinktimer = new Timer();
        }

        @Override
        public void onConnect() {
            Toast.makeText(getApplicationContext(), "Mindwave подключен", Toast.LENGTH_SHORT);
        }

        @Override
        public void onDisconnect(int state) {
            //Пока будем считать, что мы находимся в идеальном мире
        }

        private int computePower(int strength) {
            int progress = senseBar.getProgress();
            progress = (progress > 50) ? progress - 50 : progress + 50;
            strength = (int) ((new Float(strength)) / (new Float(progress) / 100));
            strength = (strength > 255) ? 255 : strength;
            return strength;
        }

        @Override
        public void attention(int strength) {
            attention.setText("" + strength);
            strength = computePower(strength);
            robotConnector.sendData("a" + strength); // "a=25\n"

        }

        @Override
        public void meditation(int strength) {
            meditation.setText("" + strength);
            strength = computePower(strength);
            robotConnector.sendData("m" + strength);

        }
        private Timer blinktimer;
        @Override
        public void blink(int strength) {
            robotConnector.sendData("b");
            blink.setText("" + strength);
            blinkIndicator.setChecked(true);
            try {
                //blinktimer = new Timer();
                BlinkWaiter blinkWaiter = new BlinkWaiter();
                blinktimer.schedule(blinkWaiter, 100);
            } catch (IllegalStateException e)
            {Log.d(MyTag, e.toString());
            }
        }

        public void turn(int direction) {
            robotConnector.sendData("d" + direction);
        }

        public void reverse() {
            robotConnector.sendData("r");
        }

        @Override
        public void onBadSignal() {
//            Toast.makeText(getApplicationContext(), getString(R.string.bad_signal),
//                    Toast.LENGTH_LONG).show();

        }


        private class BlinkWaiter extends TimerTask{
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        blinkIndicator.setChecked(false);
                    }
                });
            }
        }
    }
}