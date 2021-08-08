package com.crux.ventapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;


public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";

    // declaring necessary variables and objects

    private ListView mListView;
    private com.crux.ventapp.adapters.DeviceListAdapter deviceListAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    RecyclerView rvDeviceList;
    LinearLayoutManager linearLayoutManager;

    Button button;

    // This variables are required for connecting
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetoothAdapter = null;
    static BluetoothSocket btSocket = null;
    BluetoothLeScanner bluetoothLeScanner = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    ConnectBT connectBT;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_list);

        // initializing everything

        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");
        Log.d(TAG, "onCreate total device count: " + mDeviceList.size());
        // mListView = (ListView) findViewById(R.id.lv_paired);
        rvDeviceList = findViewById(R.id.rvDeviceList);
        // deviceListAdapter = new DeviceListAdapter(this);
        //
        // // feeding adapter all the devices
        // deviceListAdapter.setData(mDeviceList);
        //
        // // overriding interface that we have made in adapter class
        // deviceListAdapter.setListener(position -> {
        //     BluetoothDevice device = mDeviceList.get(position);
        //     if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
        //         unpairDevice(device);
        //     } else {
        //         showToast("Pairing...");
        //         pairDevice(device);
        //     }
        // });
        //
        //
        // deviceListAdapter.setCnctBtnListener((position, v) -> {
        //     BluetoothDevice device = mDeviceList.get(position);
        //     if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
        //         address = device.getAddress();
        //         connectBT = new ConnectBT();
        //         connectBT.execute();
        //         button = (Button) v;
        //         Log.d("Mac ADD: ", address);
        //     }
        //
        // });
        //
        //
        // // connecting adapter with list view
        // mListView.setAdapter(deviceListAdapter);
        //

        deviceListAdapter = new com.crux.ventapp.adapters.DeviceListAdapter(mDeviceList);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvDeviceList.setLayoutManager(linearLayoutManager);
        rvDeviceList.addItemDecoration(new DividerItemDecoration(rvDeviceList.getContext(), DividerItemDecoration.VERTICAL));
        rvDeviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnPairButtonClickListener(position -> {
            BluetoothDevice device = mDeviceList.get(position);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                unpairDevice(device);
            } else {
                showToast("Pairing...");
                pairDevice(device);
            }
        });


        deviceListAdapter.setOnConnectButtonClickListener((position, view) -> {
            BluetoothDevice device = mDeviceList.get(position);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                address = device.getAddress();
                connectBT = new ConnectBT();
                connectBT.execute();
                Log.d("Mac ADD: ", address);
            }

        });


        // registering a broadcast listener for getting bluetooth changing update
        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
    }

    // destroying  broadcast listener
    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);

        // cancel async task to get rid of memory leak
        if (connectBT != null) {
            connectBT.cancel(true);
        }
        super.onDestroy();

    }


    // mathod for pairing device
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method for unpairing device
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // broadcast listener
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("Unpaired");
                }

                deviceListAdapter.notifyDataSetChanged();

            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //button.setText("Disconnected");
                showToast("Disconnected");
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //button.setText("Connected");
            }
        }
    };

    // AnynchTask class for connecting with remote bluetooth device

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            //show a progress dialog
            progress = ProgressDialog.show(DeviceListActivity.this, "Connecting...", "Please wait!!!");
        }

        //while the progress dialog is shown, the connection is done in background
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    //get the mobile bluetooth device
                    myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    //connects to the device's address and checks if it's available
                    BluetoothDevice dispositivo = myBluetoothAdapter.getRemoteDevice(address);
                    //create a RFCOMM (SPP) connection
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    //btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                //if the try failed, you can check the exception here
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                showToast("Connection Failed. Is it a SPP Bluetooth? Try again.");
            } else {
                showToast("Connected");
                isBtConnected = true;
                startActivity(new Intent(DeviceListActivity.this, GraphActivity.class));
            }
            progress.dismiss();
        }
    }


    // universal method for all toasts

    public void showToast(String toastMesg) {
        Toast.makeText(getBaseContext(), toastMesg, Toast.LENGTH_SHORT).show();
    }


}