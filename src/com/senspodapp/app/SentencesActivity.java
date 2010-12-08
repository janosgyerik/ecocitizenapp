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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SentencesActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "SentencesActivity";
	private static final boolean D = true;

	// Layout Views
	private ListView mSentencesView;
	private ArrayAdapter<String> mSentencesArrayAdapter;
	private Button mBtnTextMode;
	private Button mBtnHexaMode;
	private String Mode = TEXT;
	private static final String  HEXA = "hexadecimal";
	private static final String  TEXT = "text";
	private static final String  SPACE = " ";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.sentences);

		setupCommonButtons();
		setupPrivateButtons();

		mSentencesArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
		mSentencesView = (ListView) findViewById(R.id.sentences);
		mSentencesView.setAdapter(mSentencesArrayAdapter);
	}

	
	void setupPrivateButtons() {

		mBtnTextMode = (Button) findViewById(R.id.btn_text_mode);
		mBtnTextMode.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Mode = TEXT;
				mBtnTextMode.setVisibility(View.GONE);
				mBtnHexaMode.setVisibility(View.VISIBLE);
			}
		});

		mBtnTextMode.setVisibility(View.GONE);

		mBtnHexaMode = (Button) findViewById(R.id.btn_hexa_mode);
		mBtnHexaMode.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Mode = HEXA;
				mBtnHexaMode.setVisibility(View.GONE);
				mBtnTextMode.setVisibility(View.VISIBLE);
			}
		});
	}
	
	@Override
	void receivedSentenceLine(String line) {
		if(line != null){
			if(Mode.equals(HEXA)){
				byte[] bytes = line.getBytes();
				String newLine = "";
				for(int i=0;i<bytes.length;i++){
					newLine = String.format(
							"%s%s%s",
							newLine,
							SPACE,
							Integer.toHexString(bytes[i])
					);
				}
				line = newLine;
			}
		}
		mSentencesArrayAdapter.add(line);
	}
}