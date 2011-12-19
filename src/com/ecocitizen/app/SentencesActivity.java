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

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.bundlewrapper.SensorDataBundleWrapper;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SentencesActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "SentencesActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(SentencesActivity.class);

	// Layout Views
	private ListView mSentencesView;
	private ArrayAdapter<String> mSentencesArrayAdapter;
	private Button mBtnTextMode;
	private Button mBtnHexaMode;
	private String mMode = TEXT;
	private static final String  HEXA = "hexadecimal";
	private static final String  TEXT = "text";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.sentences);

		setupPrivateButtons();

		mSentencesArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mSentencesView = (ListView) findViewById(R.id.sentences);
		mSentencesView.setAdapter(mSentencesArrayAdapter);
	}

	
	void setupPrivateButtons() {

		mBtnTextMode = (Button) findViewById(R.id.btn_text_mode);
		mBtnTextMode.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mMode = TEXT;
				mBtnTextMode.setVisibility(View.GONE);
				mBtnHexaMode.setVisibility(View.VISIBLE);
			}
		});

		mBtnTextMode.setVisibility(View.GONE);

		mBtnHexaMode = (Button) findViewById(R.id.btn_hexa_mode);
		mBtnHexaMode.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mMode = HEXA;
				mBtnHexaMode.setVisibility(View.GONE);
				mBtnTextMode.setVisibility(View.VISIBLE);
			}
		});
	}
	
	@Override
	void receivedSensorDataBundle(SensorDataBundleWrapper bundle) {
		String line = bundle.getSensorData();
		
		if (mMode.equals(HEXA)) {
			StringBuilder builder = new StringBuilder();
			for (int b : line.getBytes()) {
				builder.append(Integer.toHexString(b));
				builder.append(" ");
			}
			mSentencesArrayAdapter.add(builder.toString());
		}
		else {
			mSentencesArrayAdapter.add(line);
		}
	}
}
