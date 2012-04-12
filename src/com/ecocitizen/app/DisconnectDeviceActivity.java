/*
 * Copyright (C) 2010-2012 Eco Mobile Citizen
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ecocitizen.app.util.FinishActivityClickListener;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DisconnectDeviceActivity extends Activity {
	// Debugging
	private static final String TAG = "DisconnectDeviceActivity";
	private static final boolean D = false;

	// Return Intent extra
	public static String DEVICE_ID = "device_id";
	public static String DISCONNECT_ALL = "all";

	// Member fields
	private ArrayAdapter<String> mDevicesArrayAdapter;
	private String[] mDeviceIds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.disconnect_device);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		Button disconnectAllButton = (Button) findViewById(R.id.button_disconnect_all);
		Button btnClose = (Button) findViewById(R.id.button_close);
		btnClose.setOnClickListener(new FinishActivityClickListener(this));
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String[] deviceNames = extras.getStringArray("device_names");
			mDeviceIds = extras.getStringArray("device_ids");

			mDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

			for (int i = 0; i < deviceNames.length; ++i) {
				mDevicesArrayAdapter.add(deviceNames[i] + "\n" + mDeviceIds[i].replace('_', ':'));
			}
			
			ListView devicesListView = (ListView) findViewById(R.id.devices);
			devicesListView.setAdapter(mDevicesArrayAdapter);
			devicesListView.setOnItemClickListener(mDeviceClickListener);
		}
		
		disconnectAllButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Create the result Intent and include the device name
				Intent intent = new Intent();
				intent.putExtra(DISCONNECT_ALL, "DISCONNECT_ALL");

				// Set result and finish this Activity
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
			// Create the result Intent and include the device id
			Intent intent = new Intent();
			intent.putExtra(DEVICE_ID, mDeviceIds[pos]);

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

}
