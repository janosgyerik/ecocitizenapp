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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ecocitizen.common.bundlewrapper.SentenceBundleWrapper;
import com.ecocitizen.parser.PsenSentenceParser;
import com.ecocitizen.parser.TemperatureSentenceParser;

public class TabularViewPlusActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "TabularViewPlusActivity";
	private static final boolean D = true;

	// Layout Views
	private TableLayout mSentencesTbl;

	// Constants
	private final int TR_WIDTH = ViewGroup.LayoutParams.WRAP_CONTENT; 
	private final int TR_HEIGHT = ViewGroup.LayoutParams.FILL_PARENT;
	private final int columnColor = Color.WHITE;
	private final int valueColor = Color.YELLOW;
	
	private static DecimalFormat latlonFormat = new DecimalFormat("* ###.00000");
	private TextView mLatView;
	private TextView mLonView;
	private TextView mAccuracyView;
	private TextView mAltitudeView;
	private TextView mSpeedView;
	private TextView mBearingView;
	
	private HashMap<String, Integer> hmDataType = new HashMap<String, Integer>();
	private int mRowID = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.tabularviewplus);

		mSentencesTbl = (TableLayout)findViewById(R.id.tblsentencesplus);     
/*		
		Button mBtnComment = (Button) findViewById(R.id.btn_comment);
		mBtnComment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startCommentActivity();
			}
		});
*/		
		setupCommonButtons();
	}

	PsenSentenceParser parser = new PsenSentenceParser();
	TemperatureSentenceParser mTemperatureSentenceParser = new TemperatureSentenceParser();
	
	@Override
	void receivedSentenceBundle(SentenceBundleWrapper bundle) {
		Location location = bundle.getLocation();
		
		if (location == null) {
		}
		else {
			updateGPSdata("Latitude", latlonFormat.format(location.getLatitude()));
			updateGPSdata("Longitude", latlonFormat.format(location.getLongitude()));
			updateGPSdata("Accuracy", latlonFormat.format(location.getAccuracy()));
			updateGPSdata("Altitude", latlonFormat.format(location.getAltitude()));
			updateGPSdata("Speed", latlonFormat.format(location.getSpeed()));
			updateGPSdata("Bearing", latlonFormat.format(location.getBearing()));
			
		}
		
		String line = bundle.getSentenceLine();
		if (parser.match(line)) {
			updateRow(parser);
			if (mTemperatureSentenceParser.match(line)) {
				updateRow(mTemperatureSentenceParser);
			}
			else {
				updateRow(parser);
			}
		}
	}
	
	void updateGPSdata(String name, String value) {
		if (!hmDataType.containsKey(name)) {
			TableRow tr = new TableRow(this);

			TextView nameView = new TextView(this);
			TextView spaceView = new TextView(this);
			TextView valueView = new TextView(this);
			
			nameView.setText(name);
			nameView.setTextAppearance(this, R.style.tabularColumn);
			nameView.setPadding(3, 3, 3, 3);
			
			spaceView.setTextAppearance(this, R.style.tabularColumn);
			spaceView.setPadding(3, 3, 3, 3);
			
			valueView.setText(R.string.common_na);
			valueView.setTextAppearance(this, R.style.tabularValueColumn);
			valueView.setGravity(Gravity.RIGHT);
			valueView.setId(mRowID);
			valueView.setPadding(3, 3, 3, 3);
			
			tr.addView(nameView);
			tr.addView(spaceView);
			tr.addView(valueView);
			

			hmDataType.put(name, mRowID);
			mSentencesTbl.addView(tr, new TableLayout.LayoutParams(TR_HEIGHT, TR_WIDTH));

			++mRowID;
		}
		else {
			TextView updateValue = (TextView) findViewById(hmDataType.get(name));
			updateValue.setText(value);
		}
	}
    
	void updateRow(PsenSentenceParser parser) {
		if (!hmDataType.containsKey(parser.getName())) {
			TableRow tr = new TableRow(this);

			TextView name = new TextView(this);
			TextView metric = new TextView(this);
			TextView value = new TextView(this);

			name.setText(parser.getName());
			name.setTextAppearance(this, style.TextAppearance_Medium);
			name.setTextColor(columnColor);
			name.setPadding(3, 3, 3, 3);

			metric.setText(parser.getMetric());
			metric.setTextAppearance(this, style.TextAppearance_Medium);
			metric.setTextColor(columnColor);
			metric.setPadding(3, 3, 3, 3);

			value.setText(parser.getStrValue());
			value.setTextAppearance(this, style.TextAppearance_Medium);
			value.setTextColor(valueColor);
			value.setGravity(Gravity.RIGHT);
			value.setId(mRowID);
			value.setPadding(3, 3, 3, 3);

			tr.addView(name);
			tr.addView(metric);
			tr.addView(value);

			hmDataType.put(parser.getName(), mRowID);
			mSentencesTbl.addView(tr, new TableLayout.LayoutParams(TR_HEIGHT, TR_WIDTH));

			++mRowID;
		} 
		else if (hmDataType.containsKey(parser.getName())) {
			TextView updateValue = (TextView) findViewById(hmDataType.get(parser.getName()));
			updateValue.setText(parser.getStrValue());
		}
	}
}
