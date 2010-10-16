package com.senspodapp.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

import com.senspodapp.service.IDeviceManagerService;
import com.senspodapp.service.IDeviceManagerServiceCallback;
import com.senspodapp.service.MessageType;

public class SenspodApp extends Activity {
	// Debugging
	private static final String TAG = "SenspodApp";
	private static final boolean D = true;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// Layout Views
	private TextView mTitle;
	private ListView mSentencesView;
	private ArrayAdapter<String> mSentencesArrayAdapter;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;

	private Button mBtnConnect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.default_preferences, false);

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		//Set up the button to connect to the sensor
		mBtnConnect = (Button)findViewById(R.id.button_connect);
		mBtnConnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				//launchDeviceListActivity();
			}  
		});  

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mSentencesArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mSentencesView = (ListView) findViewById(R.id.rawsentences);
		mSentencesView.setAdapter(mSentencesArrayAdapter);

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
		if (D) Log.d(TAG, "++ ON START ++");

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
		if (D) Log.d(TAG, "+ ON RESUME +");
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
		/* TODO
        if (mBluetoothSensorService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothSensorService.getState() == BluetoothSensorService.STATE_NONE) {
            	// Start the Bluetooth services
            	//mBluetoothSensorService.start();
            }
        }
		 */
	}

	private void setupCommonService() {
	}

	private void setupSenspodService() {
		/* TODO
    	if (mBluetoothSensorService != null) return;

    	if (D) Log.d(TAG, "setupSenspodService()");

        setupCommonService();

        // Initialize the BluetoothSensorService to perform bluetooth connections
        mBluetoothSensorService = new CitySenspodService(this, mHandler);
		 */
	}

	private void setupSimulatorService() {
		/* TODO
    	if (mBluetoothSensorService != null) return;

        Toast.makeText(this, "Starting Simulator service...", Toast.LENGTH_LONG).show();

        if (D) Log.d(TAG, "setupSimulatorService()");

        setupCommonService();

        // Initialize the BluetoothSensorService to replay a logfile
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
		 */
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D) Log.d(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D) Log.d(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (D) Log.d(TAG, "--- ON DESTROY ---");
	}

	// The Handler that gets information back from the BluetoothSensorService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/* TODO
            case MessageProtocol.MESSAGE_STATE_CHANGE:
                if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothSensorService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mSentencesArrayAdapter.clear();
            		//locationmanager.requestLocationUpdates("gps", 1000, 0.1f, locationListener);

                    break;
                case BluetoothSensorService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothSensorService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
            		//if (locationListener != null) locationmanager.removeUpdates(locationListener);
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
                mSentencesArrayAdapter.add(line + ";l=" + line.length());

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
			 */
			default:
				super.handleMessage(msg);
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D) Log.d(TAG, "onActivityResult " + resultCode);
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
				// TODO
				//mBluetoothSensorService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a session
				setupSenspodService();
			} 
			else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private MenuItem connectMenuItem = null;
	private MenuItem disconnectMenuItem = null;

	private void onBluetoothStateChanged() {
		/* TODO
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
	*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);

		connectMenuItem = menu.findItem(R.id.menu_connect);
		disconnectMenuItem = menu.findItem(R.id.menu_disconnect);
		onBluetoothStateChanged();

		return true;
	}

	public void launchDeviceListActivity() {
		/* TODO
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
		*/
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			launchDeviceListActivity();
			return true;
		case R.id.menu_disconnect:
			// Disconnect any connected devices
			// TODO
			//if (mBluetoothSensorService != null) mBluetoothSensorService.stop();
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
		}
		return false;
	}


	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "status changed");
	}

	private IDeviceManagerService mService = null;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = IDeviceManagerService.Stub.asInterface(service);

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				mService.registerCallback(mCallback);
			} 
			catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			Toast.makeText(SenspodApp.this, "Remote service connected",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;

			// As part of the sample, tell the user what happened.
			Toast.makeText(SenspodApp.this, "Remote service disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};

	boolean mIsBound = false;

	private OnClickListener mBindListener = new OnClickListener() {
		public void onClick(View v) {
			// Establish connection with the service.
			bindService(new Intent(IDeviceManagerService.class.getName()),
					mConnection, Context.BIND_AUTO_CREATE);
			mIsBound = true;
			//mCallbackText.setText("Binding.");
		}
	};

	private OnClickListener mUnbindListener = new OnClickListener() {
		public void onClick(View v) {
			if (mIsBound) {
				// If we have received the service, and hence registered with
				// it, then now is the time to unregister.
				if (mService != null) {
					try {
						mService.unregisterCallback(mCallback);
					} 
					catch (RemoteException e) {
						// There is nothing special we need to do if the service
						// has crashed.
					}
				}

				// Detach our existing connection.
				unbindService(mConnection);
				//mKillButton.setEnabled(false);
				mIsBound = false;
			}
		}
	};

	private OnClickListener mKillListener = new OnClickListener() {
		public void onClick(View v) {
			// To kill the process hosting our service, we need to know its
			// PID.  Conveniently our service has a call that will return
			// to us that information.
			if (mService != null) {
				try {
					int pid = 0;//mService.getPid();
					// Note that, though this API allows us to request to
					// kill any process based on its PID, the kernel will
					// still impose standard restrictions on which PIDs you
					// are actually able to kill.  Typically this means only
					// the process running your application and any additional
					// processes created by that app as shown here; packages
					// sharing a common UID will also be able to kill each
					// other's processes.
					android.os.Process.killProcess(pid);
					if (false) throw new RemoteException();
				} 
				catch (RemoteException ex) {
					// Recover gracefully from the process hosting the
					// server dying.
					// Just for purposes of the sample, put up a notification.
					Toast.makeText(SenspodApp.this,
							"Remote call failed",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	/**
	 * This implementation is used to receive callbacks from the remote
	 * service.
	 */
	private IDeviceManagerServiceCallback mCallback = new IDeviceManagerServiceCallback.Stub() {
		/**
		 * Note that IPC calls are dispatched through a thread
		 * pool running in each process, so the code executing here will
		 * NOT be running in our main thread like most other things -- so,
		 * to update the UI, we need to use a Handler to hop over there.
		 */
		public void receivedSentenceData(String sensorId, String sentence)
		throws RemoteException {
			// TODO Auto-generated method stub
			mHandler.sendMessage(mHandler.obtainMessage(MessageType.SENTENCE, sentence));
		}
	};
}