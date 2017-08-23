package com.robotmonsterlabs.scout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BluetoothFragment extends Fragment {

    View view;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter adapter;
    private int REQUEST_ENABLE_BT = 5000 ;
    private boolean mScanning;
    private Handler mHandler, handler;
    private static final long SCAN_PERIOD = 5000;
    private ArrayList<HashMap<String, String>> deviceList ;
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    ListView bluetoothList;

    public BluetoothFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate our view
        view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        // Get our listview
        bluetoothList = (ListView) view.findViewById(R.id.bluetooth_list);

        // Setup our bluetooth manager
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().
                getApplicationContext().
                getSystemService(getActivity().getApplicationContext().BLUETOOTH_SERVICE);

        // Get our BT device adaptor
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // If the BT adaptor is not null (exists & is enabled)
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // This one we use inside scanLeDevice
        mHandler = new Handler();

        // Our threads to handle device polling
        // so we start scanning for devices
        // The boolean is for starting/stopping scanning
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                scanLeDevice(true);
                handler.postDelayed(this, SCAN_PERIOD*2);
            }
        }, SCAN_PERIOD*2);

        // Instantiate the default adaptor - standard stuff
        adapter = BluetoothAdapter.getDefaultAdapter();

        // Now we create the filters - these are the actions
        // That our receiver can respond to
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        // We setup the receiver (on our Activity as it's an activity method)
        // We also add the filters to that those trigger the callback response
        getActivity().registerReceiver(mReceiver, filter);

        // Start out BlueTooth adaptor to start scanning
        // This will queue the receivers to respond appropriately
        adapter.startDiscovery();

        // Instantiate our device list
        deviceList = new ArrayList<HashMap<String, String>>();

        // Notifying the user to hold one
        Toast.makeText(getActivity(), "2 seconds, just quickly finding devices...", Toast.LENGTH_LONG).show();




        // Inflate the layout for this fragment
        return view;
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                aggregateDeviceList();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // bluetooth device found & we add the device to the list
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Create our details
                HashMap<String, String> deviceDetails = new HashMap<String, String>();
                deviceDetails.put("name", device.getName());
                deviceDetails.put("level", String.valueOf(rssi));
                deviceDetails.put("ibeacon", "no");

                // Use the new functio so we can filter
                addDeviceToList(deviceDetails);
            }
        }
    };

    // Schedule the start and then stop of the scans
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    aggregateDeviceList() ;
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            adapter.startDiscovery(); // TODO << Throwing an error
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            aggregateDeviceList() ;
        }
    }

    // Function for if devices are found
    // startDiscovery via the receiver is for non LE devices
    // startLeScan is for LE devices (iBeacon, etc.)
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int startByte = 2;
                    boolean patternFound = false;
                    String uuid = "" ;
                    String deviceName = device.getName();

                    while (startByte <= 5) {
                        if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                            patternFound = true;
                            break;
                        }
                        startByte++;
                    }

                    if (patternFound) {

                        //Convert to hex String
                        byte[] uuidBytes = new byte[16];
                        System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                        String hexString = bytesToHex(uuidBytes);

                        //Here is your UUID
                        uuid =  hexString.substring(0,8) + "-" +
                                hexString.substring(8,12) + "-" +
                                hexString.substring(12,16) + "-" +
                                hexString.substring(16,20) + "-" +
                                hexString.substring(20,32);

                        //Here is your Major value
                        int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

                        //Here is your Minor value
                        int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

                    }

                    // Parse the device name, if it's null we get it from the BleUtil
                    // As it's a Le device
                    BleAdvertisedData badata = BleUtil.parseAdertisedData(scanRecord);
                    if( deviceName == null ) {
                        deviceName = badata.getName();
                    }

                    // Create a hashmap to store the details
                    HashMap<String, String> deviceDetails = new HashMap<String, String>();
                    deviceDetails.put("name", deviceName);
                    deviceDetails.put("level", String.valueOf(rssi));
                    deviceDetails.put("ibeacon", "yes");

                    // Use the new method so we can filter out existing entries
                    addDeviceToList(deviceDetails);

                }
            });
        }
    };

    // So we can add all devices via this function
    private void addDeviceToList(HashMap<String, String> device) {

        Boolean deviceAlreadyAdded = false;

        //Check to see if the device is not in the list
        for (int x=0; x < deviceList.size(); x++) {
            if (!deviceAlreadyAdded) {
                HashMap<String, String> tempDevice = deviceList.get(x);
                if (device.get("name").equals(tempDevice.get("name"))) {
                    Log.d("SCT2", device.get("name") + " already in there");
                    deviceAlreadyAdded = true;
                }
            }
        }

        // If the name is null, then don't add it
        if (device.get("name")!=null && !deviceAlreadyAdded) {
            Log.d("SCT2", "Successfully added " + device.get("name"));
            deviceList.add(device);
        }


    }

    // Populate the list view - we do this so we can keep the setAdaptor in one place
    private void aggregateDeviceList() {
        bluetoothList.setAdapter(new AdaptorBluetooth(getActivity(), deviceList));
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // for apple beacon devices
    private static String bytesToHex(byte[] bytes) {

        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);

    }

}


