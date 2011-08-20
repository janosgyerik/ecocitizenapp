/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ecocitizen.app;

import java.util.HashSet;
import java.util.Set;

import com.ecocitizen.common.DebugFlagManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(DeviceListActivity.class);

    // Return Intent extra
    public static String EXTRA_BLUETOOTH_MAC = "bt_mac";
    public static String EXTRA_LOGFILE_DEVICE = "logfile";

    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private Set<String> mNewDevicesSet;
    
    private Button scanButton;
    
    private ArrayAdapter<String> mLogfileDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        scanButton = (Button) findViewById(R.id.button_scan);

        // Setup the logfile devices list
        mLogfileDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mLogfileDevicesArrayAdapter.add(getString(R.string.logfile_co2sample1));
        mLogfileDevicesArrayAdapter.add(getString(R.string.logfile_co2sample2));
        mLogfileDevicesArrayAdapter.add(getString(R.string.logfile_pollutionsample1));
        ListView logfileListView = (ListView) findViewById(R.id.logfile_devices);
        logfileListView.setAdapter(mLogfileDevicesArrayAdapter);
        logfileListView.setOnItemClickListener(mLogfileDeviceClickListener);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter != null) {
            // Initialize array adapters. One for already paired devices and
            // one for newly discovered devices
            mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
            mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
            mNewDevicesSet = new HashSet<String>();

            // Find and set up the ListView for paired devices
            ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
            pairedListView.setAdapter(mPairedDevicesArrayAdapter);
            pairedListView.setOnItemClickListener(mDeviceClickListener);

            // Find and set up the ListView for newly discovered devices
            ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
            newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
            newDevicesListView.setOnItemClickListener(mDeviceClickListener);

            // Register for broadcasts when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(mReceiver, filter);

            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);

            scanButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    doDiscovery();
                    v.setEnabled(false);
                }
            });
            
            // Get a set of currently paired devices
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
                findViewById(R.id.paired_devices).setVisibility(View.VISIBLE);
                for (BluetoothDevice device : pairedDevices) {
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else {
                String noDevices = getResources().getText(R.string.none_paired).toString();
                mPairedDevicesArrayAdapter.add(noDevices);
            }
        }
        else {
        	scanButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();

            // Unregister broadcast listeners
            this.unregisterReceiver(mReceiver);
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");
        
        // clear results of a previous discovery
        mNewDevicesArrayAdapter.clear();
        mNewDevicesSet.clear();

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_BLUETOOTH_MAC, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The on-click listener for logfile devices
    private OnItemClickListener mLogfileDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	if (mBtAdapter != null) {
        		// Cancel discovery because it's costly
        		mBtAdapter.cancelDiscovery();
        	}

            String logfileDeviceName = ((TextView) v).getText().toString();

            // Create the result Intent and include the device name
            Intent intent = new Intent();
            intent.putExtra(EXTRA_LOGFILE_DEVICE, logfileDeviceName);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // Turn on sub-title for new devices
                    findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
                    findViewById(R.id.new_devices).setVisibility(View.VISIBLE);

                	String address = device.getAddress();
                	if (! mNewDevicesSet.contains(address)) {
                		mNewDevicesSet.add(address);
                		mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                	}
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesSet.isEmpty()) {
                    // Turn on sub-title for new devices
                    findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesArrayAdapter.add(noDevices);
                }
                scanButton.setEnabled(true);
            }
        }
    };

}
