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

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecocitizen.parser.CO2SentenceParser;
import com.ecocitizen.service.BundleKeys;

public class TreeViewActivity extends DeviceManagerClient {
	// Debugging
	private static final String TAG = "TreeViewActivity";
	private static final boolean D = true;

	// Layout Views
	private TextView mCO2ValView;
	private static DecimalFormat co2Format = new DecimalFormat("0");
	private TextView mCO2NameView;
	private TextView mCO2MetricView;
	private TextView mLatView;
	private TextView mLonView;
	private static DecimalFormat latlonFormat = new DecimalFormat("* ###.00000");

	private static final String IMG_PREFIX = "treebg_level_";
	private static final String DEF_PACKAGE = "com.ecocitizen.app";
	private static final String DEF_TYPE = "drawable";

	private Button mBtnConnect;
	private Button mBtnDisconnect;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.treeview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up layout components
		mCO2ValView = (TextView)findViewById(R.id.co2val);
		mCO2NameView = (TextView)findViewById(R.id.co2name);
		mCO2MetricView = (TextView)findViewById(R.id.co2metric);
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
		
		ImageButton mBtnComment = (ImageButton) findViewById(R.id.btn_comment);
		mBtnComment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startCommentActivity();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "++ ON START ++");
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
		case R.id.menu_quit:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_quit)
			.setCancelable(true)
			.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shutdown();
					finish();
				}
			})
			.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).show();
			return true;
		case R.id.menu_preparegps:
			startActivity(new Intent(this, PrepareGpsActivity.class));
			return true;
		case R.id.menu_fileuploader:
			startActivity(new Intent(this, FileUploaderActivity.class));
			return true;			
		}
		return false;
	}

	CO2SentenceParser parser = new CO2SentenceParser();

	@Override
	void receivedSentenceBundle(Bundle bundle) {
		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		if (parser.match(line)) {
			mCO2ValView.setText(co2Format.format(parser.getFloatValue()));
			mCO2NameView.setText(parser.getName());
			mCO2MetricView.setText(parser.getMetric());

			String imgname = IMG_PREFIX + parser.getLevel();
			int resID = getResources().getIdentifier(imgname, DEF_TYPE, DEF_PACKAGE);
			LinearLayout treepage = (LinearLayout) findViewById(R.id.treepage);
			treepage.setBackgroundResource(resID);
			Bundle locationBundle = bundle.getBundle(BundleKeys.LOCATION_BUNDLE);
			if (locationBundle == null) {
				mLatView.setText(getString(R.string.common_na));
				mLonView.setText(getString(R.string.common_na));
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
