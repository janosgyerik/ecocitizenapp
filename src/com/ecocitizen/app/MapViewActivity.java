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

import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.bundlewrapper.SentenceBundleWrapper;


public class MapViewActivity extends AbstractMainActivity {
	// Debugging
	private static final String TAG = "MapViewActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(MapViewActivity.class);
	
	private TextView mLatNameView;
	private TextView mLatValView;
	private TextView mLonNameView;
	private TextView mLonValView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Set up the window layout
		setContentView(R.layout.mapview);
		
		mLatNameView = (TextView)findViewById(R.id.latname);
		mLatValView = (TextView)findViewById(R.id.latval);
		mLonNameView = (TextView)findViewById(R.id.lonname);
		mLonValView = (TextView)findViewById(R.id.lonval);
		
		// Set up the button to connect/disconnect sensors
		setupCommonButtons();		
	}
	
	@Override
	void receivedSentenceBundle(SentenceBundleWrapper bundle) {
		Location location = bundle.getLocation();
		
		mLatNameView.setText("lat.=");
		mLonNameView.setText("long.=");
		
		if (location == null) {
			mLatValView.setText(R.string.common_na);
			mLonValView.setText(R.string.common_na);
		} else {
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
