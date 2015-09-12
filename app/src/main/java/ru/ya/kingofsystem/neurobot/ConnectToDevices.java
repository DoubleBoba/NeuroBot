package ru.ya.kingofsystem.neurobot;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by tamtaradam on 27.08.15.
 */
public class ConnectToDevices extends Activity implements FoundedDevice.OnDeviceChoosenListener {
    private int REQUEST_ENABLE_BT = 23424;
    private BluetoothAdapter btAdapter;
    private TextView searchStatus;
    private Boolean onlymindWaves = false;
    private CheckBox checkBox;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        searchStatus = (TextView) findViewById(R.id.searchStatus);

        IntentFilter discoverFilter = new IntentFilter();
        discoverFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoverFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoverFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btMonitor, discoverFilter);

        Button btnDiscover = (Button) findViewById(R.id.btnDiscover);
        btnDiscover.setOnClickListener(new DiscoverListener());
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
    private boolean mindWaveSelected = false;
    @Override
    public void onDeviceChoosen(String mac, String name) {
        if (!mindWaveSelected) {
            Common.setMindWave(btAdapter.getRemoteDevice(mac));
            mindWaveSelected = true;
            Toast.makeText(getApplicationContext(), getString(R.string.get_next_device),
                    Toast.LENGTH_LONG).show();
            Common.setRobot(btAdapter.getRemoteDevice(mac));
            btAdapter.cancelDiscovery();
            Intent intent = new Intent(ConnectToDevices.this, ButtonControl.class);
            startActivity(intent);
        }

    }

    private class DiscoverListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!btAdapter.isEnabled()) {
                enableBt();
            }else{
                btAdapter.startDiscovery();
                searchStatus.setText(getString(R.string.status_searching));
            }
        }
    }


    private ArrayList<FoundedDevice> foundedDevicesAll = new ArrayList<FoundedDevice>();
    //Мониторит bluetooth адаптер на предмет появления разнообразных симптомов
    private BroadcastReceiver btMonitor = new BroadcastReceiver() {
            // Данил, у меня не было времени делить это на методы.
            // Не ленись отрефакторить это, я буду благодарен!
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String mac = device.getAddress();
                    String name = device.getName();
                    FoundedDevice devPanel = FoundedDevice.newInstance(mac, name);
//                    devPanel.onAttach((Context)ConnectToDevices.this);
                    addDevicePanel(devPanel);
                }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    searchStatus.setText(getString(R.string.status_stop_searching));
                }

            }
        };


    private void resetDevicesPanel() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        for (FoundedDevice dev: foundedDevicesAll) {
            fragmentTransaction.remove(dev);
        }
        recreateDevPanes(foundedDevicesAll, fragmentTransaction);
        fragmentTransaction.commit();
    }

    private void recreateDevPanes(ArrayList<FoundedDevice> list,
                                  FragmentTransaction transaction) {
        //// FIXME: 28.08.15 Слишком много багов из-за этой штуки
        // Она должна во время переключения режимов удалять/добалять
        // найденные устройства на панель
        ArrayList<FoundedDevice> tempList = new ArrayList<>();
        for (FoundedDevice devPanel : list) {
            FoundedDevice newDevPanel = FoundedDevice.newInstance(devPanel.getMac(),
                    devPanel.getName());
            if (devPanel.getName().toLowerCase().contains("mindwave")
                    &&
                    onlymindWaves)
                transaction.add(R.id.devicesContainer, newDevPanel);
            if (!onlymindWaves)
                transaction.add(R.id.devicesContainer, newDevPanel);
            tempList.add(newDevPanel);
        }
        list.removeAll(list);
        list.addAll(tempList);
    }


    private void addDevicePanel(FoundedDevice devPanel) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.devicesContainer, devPanel);
        fragmentTransaction.commit();

    }

    private void enableBt() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }
}
