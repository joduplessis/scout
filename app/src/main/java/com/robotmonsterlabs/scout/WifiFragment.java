package com.robotmonsterlabs.scout;

import android.app.Activity;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WifiFragment extends Fragment {

    View view;
    WifiManager wifi;
    Handler handler;
    ArrayList<HashMap<String,String>> data;
    ListView wifiList;
    int scanFrequency = 3000;

    public WifiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get the view
        view = inflater.inflate(R.layout.fragment_wifi, container, false);

        // Get our listview
        wifiList = (ListView) view.findViewById(R.id.wifi_list);

        // Get our wifi manager
        wifi = (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);

        if (wifi.isWifiEnabled() == false) {
            Toast.makeText(getActivity(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }

        wifi.startScan();
        handler = new Handler() ;

        // Thread that polls for updates
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // List to store everything
                List<ScanResult> returnedNetworks = wifi.getScanResults();
                // Initialize our array list as a new one
                data = new ArrayList<HashMap<String,String>>();
                // Add the wifi networks
                for (int w=0; w<returnedNetworks.size(); w++) {
                    // Get the single network
                    ScanResult singleNetwork = returnedNetworks.get(w);
                    // Create a hashmap for the data
                    HashMap<String,String> hm = new HashMap<String,String>();
                    hm.put("title", singleNetwork.SSID);
                    hm.put("subtitle", singleNetwork.capabilities);
                    hm.put("level", singleNetwork.level + "");
                    // Add it to our ArrayList
                    data.add(hm);
                }
                // Get our listview
                wifiList.setAdapter(new AdaptorWifi(getActivity(), data));

                // Run the handler again
                handler.postDelayed(this, scanFrequency);
            }
        }, scanFrequency);



        // Inflate the layout for this fragment
        return view;
    }

}
