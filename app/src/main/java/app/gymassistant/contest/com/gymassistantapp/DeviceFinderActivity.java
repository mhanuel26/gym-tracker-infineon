package app.gymassistant.contest.com.gymassistantapp;

/**
 * Created by mhanuel on 9/29/17.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceFinderActivity extends Activity {
    private static final String TAG = DeviceFinderActivity.class.getSimpleName();
    private String deviceAddress = "";

    private Button btnConnect;
    private TextView tvDevice;

    private BluetoothAdapter mBluetoothAdapter;
    ArrayList<BluetoothDevice> myDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_finder_layout);

        android.widget.Button btnConnect = (android.widget.Button) findViewById(R.id.btn_connect);
        TextView tvDevice = (TextView) findViewById(R.id.tv_device);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!deviceAddress.isEmpty())
                    connect();
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "No default Bluetooth adapter. Device likely does not support bluetooth.");
            return;
        }

        if(!mBluetoothAdapter.isEnabled()){
            Log.w(TAG, "Enable BT");
            mBluetoothAdapter.enable();
        }

        Set<BluetoothDevice> allDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice d: allDevices){
            if(isNanoHubDevice(d)){
                myDevices.add(d);
            }
        }

        Log.w(TAG, "myDevices size = " + myDevices.size());
        if(myDevices.size() == 0){
            Toast.makeText(this, "No device found", Toast.LENGTH_LONG).show();
            // If no device was already paired let's try to discover
            Log.w(TAG, "No device found");
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            myDevices.clear();
            Boolean stat = mBluetoothAdapter.startDiscovery();
            if(stat)
                Log.w(TAG, "discovering started");
            else
                Log.w(TAG, "Could not start discovery");
        }else {

            tvDevice.setText("Device: " + myDevices.get(0).getName() + "\t" + myDevices.get(0).getAddress());
            deviceAddress = myDevices.get(0).getAddress();

        }
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.w(TAG, "New Device Found = " + device.getName());
                if(isNanoHubDevice(device)){
                    myDevices.add(device);
                    tvDevice.setText("Device: " + device.getName() + "\t" + device.getAddress());
                    deviceAddress = device.getAddress();
                }
            }
        }
    };

    private boolean isNanoHubDevice(BluetoothDevice d) {
        String deviceName = d.getName().toLowerCase();
        Log.w(TAG, "Found device name:" + deviceName);
        return deviceName.equals("ifx_nanohub");

    }

    private void connect() {
        Intent gymAssistant = new Intent(getApplicationContext(), AIDialogSampleActivity.class);
        gymAssistant.putExtra("deviceAddress", deviceAddress);
        startActivity(gymAssistant);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        try{
            unregisterReceiver(mReceiver);
        }catch (IllegalArgumentException e){
            // pass
        }

    }

}

