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

import java.text.DecimalFormat;

import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecocitizen.common.DeviceHandlerFactory;
import com.ecocitizen.common.bundlewrapper.SensorDataBundleWrapper;
import com.ecocitizen.common.parser.SensorData;
import com.ecocitizen.common.parser.SensorDataFilter;
import com.ecocitizen.common.parser.SensorDataParser;
import com.ecocitizen.common.parser.SensorDataType;

public class TreeViewActivity extends AbstractMainActivity {
	// Debugging
	private static final String TAG = "TreeViewActivity";
	private static final boolean D = false;

	// Layout Views
	private TextView mCO2Val_1View;
	private TextView mCO2Val_2View;
	private static DecimalFormat co2Format = new DecimalFormat("0");
	private TextView mCO2NameView;
	private TextView mCO2UnitView;
	private TextView mLatNameView;
	private TextView mLatValView;
	private TextView mLonNameView;
	private TextView mLonValView;
	
	private String sensorID_1 = null;
	private String sensorID_2 = null;

	private static final String TREEBG_PREFIX = "treebg_";
	private static final String TREEBG_PACKAGE = "com.ecocitizen.app";
	private static final String TREEBG_TYPE = "drawable";
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Set up the window layout
		setContentView(R.layout.treeview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up layout components
		mCO2Val_1View = (TextView)findViewById(R.id.co2val_1);
		mCO2Val_2View = (TextView)findViewById(R.id.co2val_2);
		mCO2NameView = (TextView)findViewById(R.id.co2name);
		mCO2UnitView = (TextView)findViewById(R.id.co2unit);
		mLatNameView = (TextView)findViewById(R.id.latname);
		mLatValView = (TextView)findViewById(R.id.latval);
		mLonNameView = (TextView)findViewById(R.id.lonname);
		mLonValView = (TextView)findViewById(R.id.lonval);

		// Set up the button to connect/disconnect sensors
		setupCommonButtons();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "++ ON START ++");
	}

	private static SensorDataFilter filter = 
		new SensorDataFilter(SensorDataType.CO2);

	@Override
	void receivedSensorDataBundle(SensorDataBundleWrapper bundle) {
		SensorDataParser parser = 
			DeviceHandlerFactory.getInstance().getParser(bundle.getSensorName(), bundle.getSensorId());

		for (SensorData data : parser.getSensorData(bundle.getSensorData(), filter)) {
			setCO2Val(bundle.getSensorId(), co2Format.format(data.getFloatValue()));
			mCO2NameView.setText(data.getName());
			mCO2UnitView.setText(data.getUnit());

			String imgname = TREEBG_PREFIX + data.getLevel();
			int resID = getResources().getIdentifier(imgname, TREEBG_TYPE, TREEBG_PACKAGE);
			if (resID == 0) {
				resID = R.drawable.treebg_max;
			}

			LinearLayout treepage = (LinearLayout) findViewById(R.id.treepage);
			treepage.setBackgroundResource(resID);

			mLatNameView.setText("lat.=");
			mLonNameView.setText("long.=");

			Location location = bundle.getLocation();

			if (location == null) {
				mLatValView.setText(getString(R.string.common_na));
				mLonValView.setText(getString(R.string.common_na));
			}
			else {
				String lat_val[], lon_val[];

				lat_val = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS).split(":", 0);
				lon_val = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS).split(":", 0);
				mLatValView.setText(lat_val[0] + "º" + lat_val[1] + "'" +
						lat_val[2].substring(0, lat_val[2].indexOf('.')) + "\"");
				mLonValView.setText(lon_val[0] + "º" + lon_val[1] + "'" +
						lon_val[2].substring(0, lon_val[2].indexOf('.')) + "\"");
			}
		}
	}

	void setCO2Val(String sensorID, String value) {
		if (sensorID_1 == null) {
			sensorID_1 = sensorID;
		} 
		else if (!sensorID_1.equals(sensorID)) {
			sensorID_2 = sensorID;
			mCO2Val_2View.setVisibility(View.VISIBLE);
		}

		if (sensorID_1.equals(sensorID)) {
			mCO2Val_1View.setText(value);
		} 
		else if (sensorID_2.equals(sensorID)) {
			mCO2Val_2View.setText(value);
		}
	}
}
