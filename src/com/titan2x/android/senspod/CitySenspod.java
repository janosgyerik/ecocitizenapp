package com.titan2x.android.senspod;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Date;
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
import android.preference.PreferenceManager;
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

import com.senspodapp.app.DeviceListActivity;
import com.senspodapp.app.R;
import com.senspodapp.app.SettingsActivity;
import com.titan2x.envdata.sentences.CO2Sentence;

public class CitySenspod extends Activity implements LocationListener {
    // Debugging
    private static final String TAG = "CitySenspod";
    private static final boolean D = true;
    private boolean debugMode = false;

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
    
    // Count of received data messages
    private int mMessageCount = 1;
    
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the senspod services
    private BluetoothSensorService mBluetoothSensorService = null;
    
    private static DecimalFormat latlonFormat = new DecimalFormat("* ###.00000");
    private Button mBtnConnect;
    // Member object for uploading measurements to the map server
    private SensormapUploaderService mSensormapUploaderService = null;

    private LocationManager locationmanager = null;
    private Location lastLocation = null;
    private Date lastLocationDate = new Date();
    private LocationListener locationListener = this;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        PreferenceManager.setDefaultValues(this, R.xml.default_preferences, false);
        
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        //Set up the button to connect to the sensor
        mBtnConnect = (Button)findViewById(R.id.button_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener(){   
            public void onClick(View v) {   
            	launchDeviceListActivity();
            }  
        });  
        
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

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
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
        	} 
        	else {
        		setupSenspodService();
        	}
        }
        else {
        	setupSimulatorService();
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
        
        String username = getString(R.string.username);
        String map_server_url = getString(R.string.map_server_url);
        String sensorId = "apiv3";
        mSensormapUploaderService = new SensormapUploaderService(username, map_server_url, sensorId);
    }
    
    private void setupSenspodService() {
    	if (mBluetoothSensorService != null) return;
    	
        Log.d(TAG, "setupSenspodService()");

        setupCommonService();
        
        // Initialize the BluetoothSensorService to perform bluetooth connections
        mBluetoothSensorService = new CitySenspodService(this, mHandler);
    }

    private void setupSimulatorService() {
    	if (mBluetoothSensorService != null) return;
    	
        Toast.makeText(this, "Starting Simulator service...", Toast.LENGTH_LONG).show();
        
        Log.d(TAG, "setupSimulatorService()");

        debugMode = true;
        setupCommonService();
        
        // Initialize the BluetoothSensorService to replay a logfile
        String sensorId = getString(R.string.logplayer_sensor_id);
        String filename = getString(R.string.logplayer_filename);
        int messageInterval = getResources().getInteger(R.integer.logplayer_msg_interval);
        try {
        	InputStream instream = getAssets().open(filename);
            mBluetoothSensorService = new SampleSenspodService(mHandler, sensorId, instream, messageInterval);
        }
        catch (IOException e) {
        	mBluetoothSensorService = null;
            Toast.makeText(this, "Logfile does not exist, cannot start Simulator", Toast.LENGTH_LONG).show();
        }
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
                String line = new String((byte[])msg.obj);
                int indexOf_dollar = line.indexOf('$'); 
                if (indexOf_dollar > -1) {
                	line = line.substring(indexOf_dollar);
                }
                if (line.startsWith("$PSEN,CO2")) {
                	CO2Sentence co2 = new CO2Sentence(line);
                	String imgname = "co2level_" + co2.level;
                	int resID = getResources().getIdentifier(imgname, "drawable", "com.titan2x.android.senspod");
                	LinearLayout treepage = (LinearLayout) findViewById(R.id.treepage);
                	treepage.setBackgroundResource(resID);

                	String co2val = String.valueOf((int)co2.ppm);                
                	
                	mCo2View.setText(co2val);
                	mCo2View.setGravity(Gravity.CENTER);
                }

                mSensormapUploaderService.receivedSentence(line, lastLocation, lastLocationDate);

                if (debugMode) {
            		mSentencesArrayAdapter.add(line + ";l=" + line.length());
            	}

            	++mMessageCount;
                TextView messageIndicator = (TextView) findViewById(R.id.title_message_indicator);
                messageIndicator.setText("Msg#" + mMessageCount + " ");

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
        	
    		switch (mBluetoothSensorService.getState()) {
    		case BluetoothSensorService.STATE_CONNECTED:
    		case BluetoothSensorService.STATE_CONNECTING:
    			mBtnConnect.setVisibility(View.GONE);
    			break;
    		case BluetoothSensorService.STATE_NONE:
    		default:
    			mBtnConnect.setVisibility(View.VISIBLE);
    			break;
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
    
    public void launchDeviceListActivity() {
    	if (mBluetoothAdapter == null) {
    		if (mBluetoothSensorService != null) {
    			((SampleSenspodService)mBluetoothSensorService).start(); 
    		}
    	}
    	else {
            // Launch the DeviceListActivity to see devices and do scan
        	Toast.makeText(this, R.string.msg_coming_soon, Toast.LENGTH_SHORT).show();
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    	}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_connect:
        	launchDeviceListActivity();
            return true;
        case R.id.menu_disconnect:
            // Disconnect any connected devices
        	if (mBluetoothSensorService != null) mBluetoothSensorService.stop();
        	break;
        case R.id.menu_settings:
        	startActivity(new Intent(this, SettingsActivity.class));
        	break;    
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
		lastLocation = location;
		lastLocationDate = new Date();
		if (location != null) {
			mLatView.setText(latlonFormat.format(location.getLatitude()));
			mLonView.setText(latlonFormat.format(location.getLongitude()));
		}
		else {
			mLatView.setText("N.A.");
			mLonView.setText("N.A.");
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