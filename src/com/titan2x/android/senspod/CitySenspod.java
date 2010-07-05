package com.titan2x.android.senspod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

public class CitySenspod extends Activity {
    // Debugging
    private static final String TAG = "CitySenspod";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private TextView mCo2View;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the senspod services
    private BluetoothSensorService mBluetoothSensorService = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            //Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Bluetooth is not available, starting simulator instead", Toast.LENGTH_LONG).show();
            //finish();
        	setupSimulatorService();
            return;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupSenspodService() will then be called during onActivityResult
        if (mBluetoothAdapter != null) {
        	if (!mBluetoothAdapter.isEnabled()) {
        		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        		// Otherwise, setup the senspod service
        	} else {
        		if (mBluetoothSensorService == null) setupSenspodService();
        	}
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothSensorService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothSensorService.getState() == BluetoothSensorService.STATE_NONE) {
            	// Start the Bluetooth services
            	//mBluetoothSensorService.start();
            }
        }
    }

    private void setupSenspodService() {
        Log.d(TAG, "setupSenspodService()");

        // Initialize the BluetoothSensorService to perform bluetooth connections
        mBluetoothSensorService = new CitySenspodService(this, mHandler);
    }

    private void setupSimulatorService() {
        Log.d(TAG, "setupSimulatorService()");

        mCo2View = (TextView) findViewById(R.id.co2value);
        mCo2View.setGravity(Gravity.CENTER);
        
        // Initialize the BluetoothSensorService to perform bluetooth connections
        mBluetoothSensorService = new SampleSenspodService(this, mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (mBluetoothSensorService != null) mBluetoothSensorService.stopAllThreads();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    // The Handler that gets information back from the BluetoothSensorService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MessageProtocol.MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothSensorService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mCo2View.setText("");
                    break;
                case BluetoothSensorService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothSensorService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MessageProtocol.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //mInputStreamArrayAdapter.add(readMessage);
                
                String[] vals = readMessage.trim().split(",");
                String val_co2level = vals[vals.length - 1];
                String val_co2 = vals[vals.length - 2];
                
                String imgname = "co2level_" + Integer.parseInt(val_co2level);
                int resID = getResources().getIdentifier(imgname, "drawable", "com.titan2x.android.senspod");
                LinearLayout treepage = (LinearLayout) findViewById(R.id.treepage);
                treepage.setBackgroundResource(resID);

                String co2val = String.valueOf((int)Float.parseFloat(val_co2));                
                mCo2View.setText(co2val);
                mCo2View.setGravity(Gravity.CENTER);

                break;
            case MessageProtocol.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(MessageProtocol.DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MessageProtocol.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(MessageProtocol.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mBluetoothSensorService.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a session
                setupSenspodService();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        //MenuItem disconnectMenuItem = menu.findItem(R.id.menu_disconnect);
        //disconnectMenuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_connect:
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.menu_disconnect:
            // Disconnect any connected devices
        	if (mBluetoothSensorService != null) mBluetoothSensorService.stop();
            return true;
        case R.id.menu_home:
        	// go to home page (showing current pollution level, illustrated)
        	break;
        case R.id.menu_graph:
        	// go to graph page (pollution graph showing past N minutes)
            Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
        	break;
        case R.id.menu_map:
        	// go to map page (pollution map showing past N minutes)
            Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
        	break;
        }
        return false;
    }

}