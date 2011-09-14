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
import java.util.HashMap;

import android.R.style;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.DeviceHandlerFactory;
import com.ecocitizen.common.bundlewrapper.SentenceBundleWrapper;
import com.ecocitizen.common.parser.SensorData;
import com.ecocitizen.common.parser.SensorDataParser;

public class TabularViewPlusActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "TabularViewPlusActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(TabularViewPlusActivity.class);

	// Layout Views
	private TableLayout mSentencesTbl;

	// Constants
	private static final int TR_WIDTH = ViewGroup.LayoutParams.WRAP_CONTENT; 
	private static final int TR_HEIGHT = ViewGroup.LayoutParams.FILL_PARENT;
	private static final int NAME_COLOR = Color.WHITE;
	private static final int VALUE_COLOR = Color.YELLOW;
	
	private static final DecimalFormat LATLON_FORMAT = new DecimalFormat("* ###.00000");
	
	private HashMap<String, Integer> hmDataType = new HashMap<String, Integer>();
	private int mRowID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.tabularviewplus);

		mSentencesTbl = (TableLayout)findViewById(R.id.tblsentencesplus);
		
		setupCommonButtons();
	}

	@Override
	void receivedSentenceBundle(SentenceBundleWrapper bundle) {
		Location location = bundle.getLocation();
		
		if (location == null) {
		}
		else {
			updateRowWithGpsData("Latitude", LATLON_FORMAT.format(location.getLatitude()));
			updateRowWithGpsData("Longitude", LATLON_FORMAT.format(location.getLongitude()));
			updateRowWithGpsData("Accuracy", LATLON_FORMAT.format(location.getAccuracy()));
			updateRowWithGpsData("Altitude", LATLON_FORMAT.format(location.getAltitude()));
			updateRowWithGpsData("Speed", LATLON_FORMAT.format(location.getSpeed()));
			updateRowWithGpsData("Bearing", LATLON_FORMAT.format(location.getBearing()));
		}
		
		SensorDataParser parser = 
			DeviceHandlerFactory.getInstance().getParser(bundle.getSensorName(), bundle.getSensorId());

		for (SensorData data : parser.getSensorData(bundle.getSentenceLine())) {
			updateRowWithSensorData(data);
		}
	}
	
	private void updateRowWithGpsData(String name, String value) {
		updateRow(name, "", value);
	}
	
	private void updateRowWithSensorData(SensorData data) {
		updateRow(data.getName(), data.getUnit(), data.getStrValue());
	}
	
	private void updateRow(String name, String unit, String value) {
		if (hmDataType.containsKey(name)) {
			TextView valueView = (TextView) findViewById(hmDataType.get(name));
			valueView.setText(value);
		}
		else {
			addRow(name, unit, value);
		}
	}
	
	private void addRow(String name, String unit, String value) {
		++mRowID;
		
		TableRow tr = new TableRow(this);

		TextView nameView = new TextView(this);
		TextView unitView = new TextView(this);
		TextView valueView = new TextView(this);

		nameView.setText(name);
		nameView.setTextAppearance(this, style.TextAppearance_Medium);
		nameView.setTextColor(NAME_COLOR);
		nameView.setPadding(3, 3, 3, 3);

		unitView.setText(unit);
		unitView.setTextAppearance(this, style.TextAppearance_Medium);
		unitView.setTextColor(NAME_COLOR);
		unitView.setPadding(3, 3, 3, 3);

		valueView.setText(value);
		valueView.setTextAppearance(this, style.TextAppearance_Medium);
		valueView.setTextColor(VALUE_COLOR);
		valueView.setGravity(Gravity.RIGHT);
		valueView.setPadding(3, 3, 3, 3);
		valueView.setId(mRowID);

		tr.addView(nameView);
		tr.addView(unitView);
		tr.addView(valueView);

		hmDataType.put(name, mRowID);
		mSentencesTbl.addView(tr, new TableLayout.LayoutParams(TR_HEIGHT, TR_WIDTH));
	}
}
