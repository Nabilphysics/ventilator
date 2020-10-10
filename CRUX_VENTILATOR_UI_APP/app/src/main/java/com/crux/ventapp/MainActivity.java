package com.crux.ventapp;

/*
 * CRUX Open Source Ventilator
  https://www.cruxbd.com
  Version: Demo 4.0
  Team Member:
  1) Syed Razwanul Haque(Nabil)
   Team Lead, Research, Coding
  2) Maruf Hossain
   Research, Mechanical & 3D Design
  Licensing Under Processing
  3) Shafi
     UI Software
  4) Hasan
     3D Design, Assembling
 */

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    // declaring necessary variables

    AppCompatButton btnPair, btnSearch;
    SwitchCompat btSwitch;
    TextView btConditionText;
    ImageView btConditionImage;

    private BluetoothAdapter myBluetoothAdapter = null;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();

    // this variable is used for bluetooth on intent
    public static final int REQUEST_ENABLE_BT = 10;

    // Declaring variable for permission (required for api 23 or higher)
    private static String TAG = "PermissionDemo";
    private static final int REQUEST_READ_AND_WRITE_STORAGE = 112;

    AlertDialog.Builder builder;

    ProgressDialog searchProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing variables

        searchProgressDialog = new ProgressDialog(this);
        btnPair = findViewById(R.id.btnPair);
        btnSearch = findViewById(R.id.btnSearch);
        btConditionText = findViewById(R.id.btContionText);
        btSwitch = findViewById(R.id.btSwitch);
        btConditionImage = findViewById(R.id.btConditionImage);

        // getting bluetooth adapter from static method
        // it means we will get a default bluetooth adapter if the device has any
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Register broadcast for getting change in bluetooth hardware of the device
        // This will help to monitor what changes is going in bluetooth hardware
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        // event for search progress dialog
        searchProgressDialog.setMessage("Scanning...");
        searchProgressDialog.setCancelable(false);
        // action for clicking BUTTON_NEGATIVE
        // dialog should be dismissed when cancel is clicked
        searchProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            dialog.dismiss();
            myBluetoothAdapter.cancelDiscovery();
        });

        // pair button event listener
        btnPair.setOnClickListener(v -> {
            // getting all the paired button
            Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();

            if (pairedDevices == null || pairedDevices.size() == 0) {
                showToast("No Paired Devices Found");
            } else {
                ArrayList<BluetoothDevice> list = new ArrayList<>();
                list.addAll(pairedDevices);
                Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
                intent.putParcelableArrayListExtra("device.list", list);
                startActivity(intent);
            }
        });

        // search button event listener

        btnSearch.setOnClickListener(v -> myBluetoothAdapter.startDiscovery());

        // bluetooth switch change event listener

        btSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (buttonView.isChecked()) {
                makeBtEnable();

            } else {
                myBluetoothAdapter.disable();
                disableIndicator();
            }
        });


        // if by default bluetooth is on make everything enable

        if (myBluetoothAdapter.isEnabled()) {

            enableIndicator();
        } else {

            btnPair.setEnabled(false);
            btnSearch.setEnabled(false);
        }


        // adding some more intent event to the broadcast receiver
        IntentFilter intent = new IntentFilter();

        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, intent);


        // calling check permission method
        checkPermission();

    }


    private void pairedDevicesList() {
        pairedDevices = myBluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {

            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);


    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            // Make an intent to start next activity.
            // Intent i = new Intent(MainActivity.this, LedTestingActivity.class);
            //Change the activity.
            // i.putExtra(LedTestingActivity.STRING_EXTRA, address); //this will be received at ledControl (class) Activity
            // startActivity(i);
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {

                    enableIndicator();
                    showToast("Blutooth Enabled");

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                mDeviceList = new ArrayList<>();
                searchProgressDialog.show();

            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                showToast("Found device " + device.getName());

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                searchProgressDialog.dismiss();
                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(newIntent);

            }

        }
    };

    @Override
    public void onPause() {
        if (myBluetoothAdapter != null) {
            if (myBluetoothAdapter.isDiscovering()) {
                myBluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //  unregister the  receiver.
        unregisterReceiver(mReceiver);
    }


    // method regarding indication of bluetooth is disabled

    public void disableIndicator() {

        // turn off bluetooth
        myBluetoothAdapter.disable();
        btConditionImage.setImageResource(R.drawable.bt_off);
        btConditionText.setText(R.string.bt_condition_text_off);
        btConditionText.setTextColor(Color.BLACK);
        btnPair.setEnabled(false);
        btnSearch.setEnabled(false);


    }


    // method for making bluetooth turn on

    public void makeBtEnable() {

        if (myBluetoothAdapter == null) {
            showToast("Your device doesn't support bluetooth communication");
        } else {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, REQUEST_ENABLE_BT);

        }

    }


    // method regarding indication of bluetooth is enabled

    public void enableIndicator() {
        btConditionImage.setImageResource(R.drawable.bt_on);
        btConditionText.setText(R.string.bt_condition_text_on);
        btConditionText.setTextColor(Color.MAGENTA);
        btnPair.setEnabled(true);
        btnSearch.setEnabled(true);
        btSwitch.setChecked(true);

    }

    // universal method for all toasts

    public void showToast(String toastMesg) {

        Toast.makeText(getBaseContext(), toastMesg, Toast.LENGTH_SHORT).show();
    }


    // overriding this method for more user interaction with bluetooth
    // we are taking more decision based on whether user has turned on bluetooth or not
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            btSwitch.setChecked(false);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Warning !!!")
                    .setMessage("Turning on bluetooth is necessary for this app. Please keep it turn on.")
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            makeBtEnable();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showToast("User denied turning on bluetooth");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }

    }

    // method for checking permission for Location access
    protected void checkPermission() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //ask for the permission in android M

            // taking constant for write and read
            int permissionFineLocation = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionCoarseLocation = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            // checking if both write and read are granted or not
            if (permissionFineLocation != PackageManager.PERMISSION_GRANTED || permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission to record denied");

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Without Location permission bluetooth searching will not work properly.Please allow these required permissions")
                            .setTitle("Permission required")
                            .setIcon(android.R.drawable.ic_dialog_info);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            Log.i(TAG, "Clicked");
                            makeRequest();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else makeRequest();
            }
        }
    }


    // method for requesting user permission
    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_READ_AND_WRITE_STORAGE);
    }


    // overriding this method to get more user interaction feedback during permission granting
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            // check if it is our given permission code or not
            //if yes
            case REQUEST_READ_AND_WRITE_STORAGE: {

                // if no permission is granted

                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    // We are requesting user with a altert dialog to give us the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Searching bluetooth device is not possible without these permissions.")
                            .setTitle("Warning")
                            .setIcon(android.R.drawable.ic_dialog_alert);


                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {
                            Log.i(TAG, "Clicked");
                            makeRequest();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "You can add this permission anytime from settings later.", Toast.LENGTH_LONG).show();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.setCancelable(false);

                    //Toast.makeText(this, "Permission has been denied by user", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permission has been granted by user");

                } else {

                    Toast.makeText(this, "Permission has been grated by user", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Permission has been denied by user");

                }
            }
        }
    }
}

