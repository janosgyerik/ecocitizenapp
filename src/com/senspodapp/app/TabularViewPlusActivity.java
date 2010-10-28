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

import java.util.HashMap;

import com.senspodapp.parser.PsenSentenceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TabularViewPlusActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "TabularViewPlusActivity";
	private static final boolean D = true;
	
	// Layout Views
	private TableLayout mSentencesTbl;
	
	private final int TR_WIDTH = ViewGroup.LayoutParams.WRAP_CONTENT; 
	private final int TR_HEIGHT = ViewGroup.LayoutParams.FILL_PARENT;
	
	private HashMap<String, Integer> hmDataType = new HashMap<String, Integer>();
	private int mRowID = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.tabularviewplus);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.tabularviewplus_activity);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mSentencesTbl = (TableLayout)findViewById(R.id.tblsentencesplus);
		
		setupCommonButtons();
	}

	PsenSentenceParser parser = new PsenSentenceParser();

	@Override
	void receivedSentenceLine(String line) {
		if (parser.match(line)) {
			if (!hmDataType.containsKey(parser.getName())) {
				TableRow tr = new TableRow(this);
				TextView name = new TextView(this);
				TextView metric = new TextView(this);
				TextView value = new TextView(this);

				name.setText(parser.getName());
				metric.setText(parser.getMetric());
				value.setText(parser.getStrValue());
				value.setGravity(Gravity.RIGHT);
				value.setId(mRowID);

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
}