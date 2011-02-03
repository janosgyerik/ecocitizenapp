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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ConsoleActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "ConsoleActivity";
	private static final boolean D = true;

	// Layout Views
	private ListView mSentencesView;
	private ArrayAdapter<String> mSentencesArrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.console);

		setupCommonButtons();

		Button mBtnConnectDM = (Button)findViewById(R.id.btn_connect_dm);
		mBtnConnectDM.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				connectDeviceManager();
			}
		});

		Button mBtnDisconnectDM = (Button)findViewById(R.id.btn_disconnect_dm);
		mBtnDisconnectDM.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				disconnectDeviceManager();
			}
		});

		Button mBtnKillDM = (Button)findViewById(R.id.btn_kill_dm);
		mBtnKillDM.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				killDeviceManager();
			}
		});

		mSentencesArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mSentencesView = (ListView) findViewById(R.id.sentences);
		mSentencesView.setAdapter(mSentencesArrayAdapter);
	}

	@Override
	void receivedSentenceLine(String line) {
		mSentencesArrayAdapter.add(line);
	}
}
