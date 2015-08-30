package ru.ya.kingofsystem.neurobot;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;



public class FoundedDevice extends Fragment {

    private String name;
    private String mac;


    public interface OnDeviceChoosenListener {
       public void onDeviceChoosen(String mac, String name);
    }
    private OnDeviceChoosenListener chooseListener;
    public static FoundedDevice newInstance(String mac, String name) {

        Bundle args = new Bundle();
        args.putString("mac", mac);
        args.putString("name", name);

        FoundedDevice fragment = new FoundedDevice();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        name = args.getString("name");
        mac = args.getString("mac");
    }

    private void setSettings() {
        View view = getView();
        TextView macView = (TextView) view.findViewById(R.id.device_mac);
        macView.setText(mac);
        TextView namevView = (TextView) view.findViewById(R.id.device_name);
        namevView.setText(name);
        Button connectBtn = (Button) view.findViewById(R.id.button_connect);
        final String tmpmac = mac;
        final String tmpname = name;
        chooseListener = (OnDeviceChoosenListener) getActivity();
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseListener.onDeviceChoosen(tmpmac, tmpname);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.found_device, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        setSettings();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }
}
