package com.senspodapp.app;

import java.text.DecimalFormat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.senspodapp.parser.Co2SentenceParser;
import com.senspodapp.service.BundleKeys;

public class SenspodApp extends DeviceManagerClient {
	// Debugging
	private static final String TAG = "SenspodApp";
	private static final boolean D = true;

	// Layout Views
	private TextView mCo2View;
	private static DecimalFormat co2Format = new DecimalFormat("0");
    private TextView mLatView;
    private TextView mLonView;
    private static DecimalFormat latLonFormat = new DecimalFormat("* ###.00000");
    
	private Button mBtnConnect;
	private Button mBtnDisconnect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.default_preferences, false);

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up layout components
		mCo2View = (TextView)findViewById(R.id.co2);
		mLatView = (TextView)findViewById(R.id.lat);
		mLonView = (TextView)findViewById(R.id.lon);
		
		// Set up the button to connect/disconnect sensors
		mBtnConnect = (Button)findViewById(R.id.btn_connect_device);
		mBtnConnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				connectSensor();
			}  
		});
		mBtnDisconnect = (Button)findViewById(R.id.btn_disconnect_device);
		mBtnDisconnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				disconnectSensor();
			}  
		});
		mBtnDisconnect.setVisibility(View.GONE);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
		}

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.getString("default_username", "") == "") {
			String username = this.getString(R.string.username);
			String map_server_url = this.getString(R.string.map_server_url);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("default_username", username);
			editor.putString("default_sensormapurl", map_server_url);
			editor.commit();
			mTitle.setText("reset username");
		}
		else {
			mTitle.setText("username: " + settings.getString("default_username", "jack"));
		}
	}

	@Override
	void receivedSentenceBundle(Bundle bundle) {
		String line = bundle.getString(BundleKeys.SENTENCE);
		if (parser.match(line)) {
			mCo2View.setText(co2Format.format(parser.getFloatValue()));
			
			// TODO: get GPS from bundle and display lat/lon
		}
	}
	
	Co2SentenceParser parser = new Co2SentenceParser();
	
	@Override
	void receivedSentenceLine(String line) {
		// Never called, because we work with the Bundle in receivedSentenceBundle
	}
	
	@Override
	void setConnectedDeviceName(String connectedDeviceName) {
		super.setConnectedDeviceName(connectedDeviceName);
		if (connectedDeviceName == null) {
			mBtnConnect.setVisibility(View.VISIBLE);
			mBtnDisconnect.setVisibility(View.GONE);
		}
		else {
			mBtnConnect.setVisibility(View.GONE);
			//mBtnDisconnect.setVisibility(View.VISIBLE);
		}
	}
}