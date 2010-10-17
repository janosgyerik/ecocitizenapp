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
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.senspodapp.service.BundleKeys;
import com.senspodapp.service.IDeviceManagerService;
import com.senspodapp.service.IDeviceManagerServiceCallback;
import com.senspodapp.service.MessageType;

public class SentencesActivity extends Activity {
	// Debugging
	private static final String TAG = "SentencesActivity";
	private static final boolean D = true;

	// Layout Views
	private TextView mTitle;
	private ListView mSentencesView;
	private ArrayAdapter<String> mSentencesArrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.sentences);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		
		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText("Sentences");
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mSentencesArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mSentencesView = (ListView) findViewById(R.id.sentences);
		mSentencesView.setAdapter(mSentencesArrayAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "++ ON START ++");
		
		connectDeviceManager();
	}

	void connectDeviceManager() {
		// Start the service if not already running
		startService(new Intent(IDeviceManagerService.class.getName()));

		// Establish connection with the service.
		bindService(new Intent(IDeviceManagerService.class.getName()),
				mConnection, Context.BIND_AUTO_CREATE);
	}

	void disconnectDeviceManager() {
		if (mService != null) {
			try {
				mService.unregisterCallback(mCallback);
			} 
			catch (RemoteException e) {
				// There is nothing special we need to do if the service
				// has crashed.
				Log.e(TAG, "Exception during unregister callback.");
			}

			mService = null;

			// Detach our existing connection.
			unbindService(mConnection);
		}
	}

	@Override
	public void onDestroy() {
		disconnectDeviceManager();

		super.onDestroy();
		if (D) Log.d(TAG, "--- ON DESTROY ---");
	}

	// The Handler that gets information back from the DeviceManagerService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENTENCE:
				String line = (String)msg.obj;
				int indexOf_dollar = line.indexOf('$'); 
				if (indexOf_dollar > -1) {
					line = line.substring(indexOf_dollar);
				}
				mSentencesArrayAdapter.add(line);
				break;
			case MessageType.SENSORCONNECTION_SUCCESS:
				setConnectedDeviceName((String)msg.obj);
				break;
			case MessageType.SENSORCONNECTION_NONE:
			case MessageType.SENSORCONNECTION_FAILED:
			case MessageType.SENSORCONNECTION_LOST:
				setConnectedDeviceName(null);
				break;
			default:
				// drop any other messages
				break;
			}
		}
	};

	private void setConnectedDeviceName(String connectedDeviceName) {
		if (connectedDeviceName == null) {
			mTitle.setText(R.string.title_not_connected);
		}
		else {
			mTitle.setText(R.string.title_connected_to);
			mTitle.append(connectedDeviceName);
		}
	}

	private IDeviceManagerService mService = null;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = IDeviceManagerService.Stub.asInterface(service);

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				setConnectedDeviceName(mService.getConnectedDeviceName());
				mService.registerCallback(mCallback);
			} 
			catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
				Log.e(TAG, "Exception during register callback.");
			}

			Toast.makeText(SentencesActivity.this, "Remote service connected",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;

			setConnectedDeviceName(null);

			Toast.makeText(SentencesActivity.this, "Remote service crashed",
					Toast.LENGTH_SHORT).show();
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
		public void receivedSentenceBundle(Bundle bundle) {
			final String sentence = bundle.getString(BundleKeys.SENTENCE);
			mHandler.obtainMessage(MessageType.SENTENCE, sentence).sendToTarget();
		}

		public void receivedSensorConnectionNone() {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_NONE).sendToTarget();
		}

		public void receivedSensorConnectionFailed(String deviceName) {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_FAILED, deviceName).sendToTarget();
		}

		public void receivedSensorConnectionLost(String deviceName) {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_LOST, deviceName).sendToTarget();
		}

		public void receivedSensorConnectionSuccess(String deviceName) {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_SUCCESS, deviceName).sendToTarget();
		}
	};
}