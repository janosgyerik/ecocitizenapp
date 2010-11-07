/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
 *
 * This file is part of SenspodApp.
 *
 * SenspodApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SenspodApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SenspodApp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.senspodapp.app;

import java.text.DecimalFormat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.senspodapp.parser.Co2SentenceParser;
import com.senspodapp.service.BundleKeys;

public class TreeViewActivity extends DeviceManagerClient {
	// Debugging
	private static final String TAG = "TreeViewActivity";
	private static final boolean D = true;

	// Layout Views
	private TextView mCo2View;
	private static DecimalFormat co2Format = new DecimalFormat("0");
    private TextView mLatView;
    private TextView mLonView;
    private static DecimalFormat latlonFormat = new DecimalFormat("* ###.00000");
    
	private Button mBtnConnect;
	private Button mBtnDisconnect;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

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
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "++ ON START ++");

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (settings.getString("username", "").equals("")) {
			String username = this.getString(R.string.username);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("username", username);
			editor.commit();
		}
		if (settings.getString("map_server_url", "").equals("")) {
			String map_server_url = this.getString(R.string.map_server_url);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("map_server_url", map_server_url);
			editor.commit();
		}
		if (settings.getString("api_key", "").equals("")) {
			String api_key = this.getString(R.string.api_key);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("api_key", api_key);
			editor.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.treeviewactivity, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			connectSensor();
			return true;
		case R.id.menu_disconnect:
			disconnectSensor();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;    
		case R.id.menu_debugtools:
			startActivity(new Intent(this, DebugToolsActivity.class));
			return true;
		case R.id.menu_close:
			shutdown();
			finish();
			return true;
		}
		return false;
	}
	
	Co2SentenceParser parser = new Co2SentenceParser();
	
	@Override
	void receivedSentenceBundle(Bundle bundle) {
		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		if (parser.match(line)) {
			mCo2View.setText(co2Format.format(parser.getFloatValue()));
			
			Bundle locationBundle = bundle.getBundle(BundleKeys.LOCATION_BUNDLE);
			if (locationBundle == null) {
				mLatView.setText("N.A.");
				mLonView.setText("N.A.");
			}
			else {
				Location location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
				mLatView.setText(latlonFormat.format(location.getLatitude()));
				mLonView.setText(latlonFormat.format(location.getLongitude()));
			}
		}
	}
	
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