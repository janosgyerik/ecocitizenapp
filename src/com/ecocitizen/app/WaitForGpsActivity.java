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

import com.ecocitizen.common.DebugFlagManager;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

/*
 * To test in emulator:
 * 0. The AVD must have GPS support enabled in Hardware properties.
 * 1. telnet localhost 5554
 * 2. geo fix -82.411629 28.054553
 *            ^^^ LONG   ^^^ LAT
 */
public class WaitForGpsActivity extends Activity implements LocationListener {
	// Debugging
	private static final String TAG = "WaitForGpsActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(WaitForGpsActivity.class);

	private static final String PROVIDER = LocationManager.GPS_PROVIDER;
	private static final int    MIN_TIME = 1000;
	private static final float  MIN_DISTANCE = .1f;

	private LocationManager mLocationManager;

	private Button mBtnCancel;
	private Button mBtnScanAgain;
	private Button mBtnClose;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.waitforgps);

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		mBtnCancel = (Button) findViewById(R.id.btn_cancel);
		mBtnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cancel();
			}
		});
		mBtnCancel.setVisibility(View.GONE);

		mBtnScanAgain = (Button) findViewById(R.id.btn_scan_again);
		mBtnScanAgain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				beginScan();
			}
		});

		mBtnClose = (Button) findViewById(R.id.btn_close);
		mBtnClose.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		mBtnClose.setVisibility(View.GONE);

		beginScan();
	}

	void beginScan() {
		setProgressBarIndeterminateVisibility(true);

		mLocationManager.requestLocationUpdates(PROVIDER, MIN_TIME, MIN_DISTANCE, this);

		//mBtnCancel.setVisibility(View.VISIBLE);
		mBtnScanAgain.setVisibility(View.GONE);
		//mBtnClose.setVisibility(View.GONE);
	}

	@Override
	protected void onDestroy() {
		mLocationManager.removeUpdates(this);
		super.onDestroy();
	}

	private void cancel() {
		mLocationManager.removeUpdates(this);
		finish();
	}

	public void onLocationChanged(Location location) {
		if (D) Log.d(TAG, "onLocationChanged");

		mLocationManager.removeUpdates(this);

		setProgressBarIndeterminateVisibility(false);		

		//mBtnCancel.setVisibility(View.GONE);
		mBtnScanAgain.setVisibility(View.VISIBLE);
		//mBtnClose.setVisibility(View.VISIBLE);

		TableLayout gpsTbl = (TableLayout)findViewById(R.id.tblgps);

		TextView latitude_value = (TextView) gpsTbl.findViewById(R.id.latitude_value);
		latitude_value.setText(String.valueOf(location.getLatitude()));

		TextView longitude_value = (TextView) gpsTbl.findViewById(R.id.longitude_value);
		longitude_value.setText(String.valueOf(location.getLongitude()));

		TextView accuracy_value = (TextView) gpsTbl.findViewById(R.id.accuracy_value);
		accuracy_value.setText(String.valueOf(location.getAccuracy()));

		TextView altitude_value = (TextView) gpsTbl.findViewById(R.id.altitude_value);
		altitude_value.setText(String.valueOf(location.getAltitude()));

		TextView bearing_value = (TextView) gpsTbl.findViewById(R.id.bearing_value);
		bearing_value.setText(String.valueOf(location.getBearing()));
	}

	public void onProviderDisabled(String provider) {
		if (D) Log.d(TAG, "onProviderDisabled");
	}

	public void onProviderEnabled(String provider) {
		if (D) Log.d(TAG, "onProviderEnabled");
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		if (D) Log.d(TAG, "onStatusChanged");
	}
}
