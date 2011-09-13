/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
 *
 * This file is part of EcoCitizen.
 *
 * EcoCitizen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EcoCitizen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EcoCitizen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ecocitizen.app;

import com.ecocitizen.app.util.FinishActivityClickListener;
import com.ecocitizen.common.DebugFlagManager;

import android.app.Activity;
import android.content.Intent;
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

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DisconnectDeviceActivity extends Activity {
	// Debugging
	private static final String TAG = "DisconnectDeviceActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(DisconnectDeviceActivity.class);

	// Return Intent extra
	public static String EXTRA_LOGFILE_DEVICE = "logfile";
	public static String DISCONNECT_ALL = "all";

	// Member fields

	private ArrayAdapter<String> mLogfileDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.disconnect_device);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button disconnectAllButton = (Button) findViewById(R.id.button_disconnect_all);

		Button btnClose = (Button) findViewById(R.id.button_close);
		btnClose.setOnClickListener(new FinishActivityClickListener(this));
		
		//try {
		Bundle extras = getIntent().getExtras();
		
		
		if (extras != null) {
			String[] device_name = extras.getStringArray("device_name");
			String[] device_id = extras.getStringArray("device_id");

			mLogfileDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.logfile_name);

			for (int i = 0; i < device_name.length; i++) {
				mLogfileDevicesArrayAdapter.add(device_name[i] + "\n" + device_id[i]);
			}
			
			ListView logfileListView = (ListView) findViewById(R.id.devices);
			logfileListView.setAdapter(mLogfileDevicesArrayAdapter);
			logfileListView.setOnItemClickListener(mLogfileDeviceClickListener);
			findViewById(R.id.devices_section).setVisibility(View.VISIBLE);
		}
		
		disconnectAllButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Create the result Intent and include the device name
				Intent intent = new Intent();
				intent.putExtra(EXTRA_LOGFILE_DEVICE, DISCONNECT_ALL);

				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

	// The on-click listener for logfile devices
	private OnItemClickListener mLogfileDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			String device = ((TextView) v).getText().toString();
			String name_id[] = device.split("\n");

			// Create the result Intent and include the device name
			Intent intent = new Intent();
			intent.putExtra(EXTRA_LOGFILE_DEVICE, name_id[0]);

			Log.d("aaaaaaaaaaa", name_id[0]);
			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

}
