package com.senspodapp.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceManagerConsole extends DeviceManagerClient {
	// Debugging
	private static final String TAG = "DeviceManagerConsole";
	private static final boolean D = true;

	// Layout Views
	private ListView mSentencesView;
	private ArrayAdapter<String> mSentencesArrayAdapter;
	
	// TODO
	// sync state: 
	// # of files not uploaded yet
	// # of sentences received - # of sentences uploaded
	
	// TODO list of connected devices

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.console);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		Button mBtnConnectSensor = (Button)findViewById(R.id.btn_connect);
		mBtnConnectSensor.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				connectSensor();
			}  
		});
		Button mBtnDisconnectSensor = (Button)findViewById(R.id.btn_disconnect);
		mBtnDisconnectSensor.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				disconnectSensor();
			}  
		});

		Button mBtnConnectDM = (Button)findViewById(R.id.btn_dm_connect);
		mBtnConnectDM.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				connectDeviceManager();
			}
		});

		Button mBtnDisconnectDM = (Button)findViewById(R.id.btn_dm_disconnect);
		mBtnDisconnectDM.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				disconnectDeviceManager();
			}
		});

		Button mBtnKillDM = (Button)findViewById(R.id.btn_dm_kill);
		mBtnKillDM.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				killDeviceManager();
			}
		});

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText("Device Manager Console");
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mSentencesArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mSentencesView = (ListView) findViewById(R.id.sentences);
		mSentencesView.setAdapter(mSentencesArrayAdapter);
	}

	@Override
	void receivedSentenceLine(String line) {
		mSentencesArrayAdapter.add(line);
	}
}