package com.titan2x.android.senspod;

import java.text.DecimalFormat;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

public class CitySenspod extends Activity implements LocationListener {
    // Debugging
    private static final String TAG = "CitySenspod";
    private static final boolean D = true;
    private boolean debugMode = false;
    private boolean simulatorMode = false;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView mTitle;
    private TextView mCo2View;
    private TextView mLatView;
    private TextView mLonView;
    private ListView mSentencesView;
    private ArrayAdapter<String> mSentencesArrayAdapter;
    
    //Count of the ReceivedData
    private int count = 1;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the senspod services
    private BluetoothSensorService mBluetoothSensorService = null;
    
    private static DecimalFormat latlonFormat = new DecimalFormat("* ###.00000");
    private Button button01;
    // Member object for uploading measurements to the map server
    private SensormapUploaderService mSensormapUploaderService = null;

    private LocationManager locationmanager = null;
    private Location lastKnownLocation = null;
    private LocationListener locationListener = this;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        //Set up the button to connect to the sensor
        button01 =(Button)findViewById(R.id.Button1);

        button01.setVisibility(4);
        
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        
        mSentencesArrayAdapter = new DequeArrayAdapter<String>(new LinkedList<String>(), this, R.layout.message);
        mSentencesView = (ListView) findViewById(R.id.rawsentences);
        mSentencesView.setAdapter(mSentencesArrayAdapter);

		locationmanager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		for (String provider : locationmanager.getAllProviders()) {
			Log.d(TAG, "provider=" + provider);
			Log.d(TAG, "provider=" + provider + ", isEnabled=" + locationmanager.isProviderEnabled(provider));
		}
		lastKnownLocation = locationmanager.getLastKnownLocation("gps");

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

    private void setupCommonService() {
        mCo2View = (TextView) findViewById(R.id.co2value);
        mCo2View.setGravity(Gravity.CENTER);
        
        mLatView = (TextView) findViewById(R.id.lat);
        mLonView = (TextView) findViewById(R.id.lon);
        
        mSensormapUploaderService = new SensormapUploaderService(this);
    }
    
    private void setupSenspodService() {
        Log.d(TAG, "setupSenspodService()");

        setupCommonService();
        
        // Initialize the BluetoothSensorService to perform bluetooth connections
        mBluetoothSensorService = new CitySenspodService(this, mHandler);
    }

    private void setupSimulatorService() {
        Log.d(TAG, "setupSimulatorService()");

        debugMode = true;
        simulatorMode = true;
        setupCommonService();
        
        // Initialize the BluetoothSensorService to replay a logfile
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
                    mLatView.setText("");
                    mLonView.setText("");
                    mSentencesArrayAdapter.clear();
            		locationmanager.requestLocationUpdates("gps", 1000, 0.1f, locationListener);

                    break;
                case BluetoothSensorService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothSensorService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
            		if (locationListener != null) locationmanager.removeUpdates(locationListener);
                    break;
                }
                onBluetoothStateChanged();
                break;
            case MessageProtocol.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                EnvDataMessage envmsg = new EnvDataMessage(readBuf);

                if (envmsg.co2 != null) {
                	String imgname = "co2level_" + envmsg.co2.level;
                	int resID = getResources().getIdentifier(imgname, "drawable", "com.titan2x.android.senspod");
                	LinearLayout treepage = (LinearLayout) findViewById(R.id.treepage);
                	treepage.setBackgroundResource(resID);

                	String co2val = String.valueOf((int)envmsg.co2.ppm);   
                	// add Very simple one-letter indicator: . -> o -> O -> . ->
                	
                	mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                	switch(count%3){
                	case 1:
                		mTitle.append("->o");
                		break;
                	case 2:
                		mTitle.append("->O");
                		break;
                	case 0:
                	    mTitle.append("->.");
                	break;
                	
                	}
                	count++;
                	mCo2View.setText(co2val);
                	mCo2View.setGravity(Gravity.CENTER);
                	if (debugMode) {
                		mSentencesArrayAdapter.add("CO2;l=" + readBuf.length);
                	}
                	
                	if (simulatorMode) {
                    	mSensormapUploaderService.receivedCO2(envmsg.co2, envmsg.gprmc);
                	}
                	else {
                    	mSensormapUploaderService.receivedCO2(envmsg.co2, lastKnownLocation);
                	}
                }

                /*
                if (envmsg.gprmc != null) {
                	mLatView.setText(latlonFormat.format(convertNMEA(envmsg.gprmc.latitude)));
                	mLonView.setText(latlonFormat.format(convertNMEA(envmsg.gprmc.longitude)));
                }
                */
                
                if (envmsg.sentence != null && debugMode) {
                	mSentencesArrayAdapter.add(envmsg.sentence.str + ";l=" + readBuf.length);
                }

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
    
    private MenuItem connectMenuItem = null;
    private MenuItem disconnectMenuItem = null;
    private MenuItem debugOnMenuItem = null;
    private MenuItem debugOffMenuItem = null;
    
    private void onBluetoothStateChanged() {
        if (mBluetoothSensorService != null) {
        	if (connectMenuItem != null && disconnectMenuItem != null) {
        		switch (mBluetoothSensorService.getState()) {
        		case BluetoothSensorService.STATE_CONNECTED:
        			connectMenuItem.setVisible(false);
        			disconnectMenuItem.setVisible(true);
        			break;
        		case BluetoothSensorService.STATE_NONE:
        		case BluetoothSensorService.STATE_CONNECTING:
        		default:
        			connectMenuItem.setVisible(true);
        			disconnectMenuItem.setVisible(false);
        			break;
        		}
        	}
        }    	
    }
    
    private void onDebugModeChanged() {
    	if (debugOnMenuItem != null && debugOffMenuItem != null) {
    		if (debugMode) {
    			debugOnMenuItem.setVisible(false);
    			debugOffMenuItem.setVisible(true);
    		}
    		else {
    			debugOnMenuItem.setVisible(true);
    			debugOffMenuItem.setVisible(false);
    			mSentencesArrayAdapter.clear();
    		}
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        
        connectMenuItem = menu.findItem(R.id.menu_connect);
        disconnectMenuItem = menu.findItem(R.id.menu_disconnect);
        onBluetoothStateChanged();
        
        debugOnMenuItem = menu.findItem(R.id.menu_debug_on);
        debugOffMenuItem = menu.findItem(R.id.menu_debug_off);
        onDebugModeChanged();
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_connect:
            // Launch the DeviceListActivity to see devices and do scan
        	Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        case R.id.menu_disconnect:
            // Disconnect any connected devices
        	if (mBluetoothSensorService != null) mBluetoothSensorService.stop();
        	//set the connect to sensor button visible
        	button01.setVisibility(0);
        	
        	final Intent serverNewIntent = new Intent(this, DeviceListActivity.class);
            button01.setOnClickListener(new View.OnClickListener(){   
                public void onClick(View v) {   
                     startActivityForResult(serverNewIntent, REQUEST_CONNECT_DEVICE);
                }  
            });  
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
        case R.id.menu_quit:
        	finish();
        	break;
        case R.id.menu_debug_on:
        	debugMode = true;
        	onDebugModeChanged();
        	break;
        case R.id.menu_debug_off:
        	debugMode = false;
        	onDebugModeChanged();
        	break;
        }
        return false;
    }


	public void onLocationChanged(Location location) {
		lastKnownLocation = location;
		if (location != null) {
			mLatView.setText(latlonFormat.format(location.getLatitude()));
			mLonView.setText(latlonFormat.format(location.getLongitude()));
		}
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "status changed");
	}
}