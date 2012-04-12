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

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MapViewActivity extends MapActivity {
	// Debugging
	private static final String TAG = "MapViewActivity";
	private static final boolean D = false;
	
	private static final int INITIAL_ZOOM_LEVEL = 15;
	private static final int DEFAULT_LAT = (int)(36.050252855791314 * 1e6); 
	private static final int DEFAULT_LON = (int)(140.1186993117676 * 1e6);
	private static final String PPM_PACKAGE = "com.ecocitizen.app";
	private static final String PPM_TYPE = "drawable";

	private MapController mMapController;
	private List<Overlay> mOList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapview);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		MapView map = (MapView)findViewById(R.id.androidmap);
		mMapController = map.getController();
		mMapController.setZoom(INITIAL_ZOOM_LEVEL);
		mMapController.setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
		map.setBuiltInZoomControls(true);
		mOList = map.getOverlays();

		// draw test
		int resID = getResources().getIdentifier("ppm_min", PPM_TYPE, PPM_PACKAGE);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resID);
		PpmOverlay overlay = new PpmOverlay(bmp, new GeoPoint(DEFAULT_LAT, DEFAULT_LON));
		mOList.add(overlay);

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/*
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
	void receivedSensorDataBundle(SensorDataBundleWrapper bundle) {
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
	 */

}
