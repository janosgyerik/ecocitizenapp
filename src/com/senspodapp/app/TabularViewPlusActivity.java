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

import java.text.DecimalFormat;
import java.util.HashMap;

import com.senspodapp.parser.PsenSentenceParser;
import com.senspodapp.service.BundleKeys;

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
	private final static String LAT = "latitude";
	private final static String LON = "longitude";
	private static DecimalFormat latlonFormat = new DecimalFormat("* ###.00000");


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

	PsenSentenceParser parser = new PsenSentenceParser();

	@Override
	void receivedSentenceBundle(Bundle bundle) {
		Bundle locationBundle = bundle.getBundle(BundleKeys.LOCATION_BUNDLE);
		if (!hmDataType.containsKey(LAT)) {
			TableRow tr = new TableRow(this);

			TextView name = new TextView(this);
			TextView value = new TextView(this);

			name.setText(LAT);
			name.setTextAppearance(this, style.TextAppearance_Medium);
			name.setTextColor(columnColor);

			value.setText("N.A.");
			value.setTextAppearance(this, style.TextAppearance_Medium);
			value.setTextColor(valueColor);
			value.setGravity(Gravity.RIGHT);
			value.setId(mRowID);

			tr.addView(name);
			tr.addView(value);
			hmDataType.put(LAT, mRowID);
			mSentencesTbl.addView(tr, new TableLayout.LayoutParams(TR_HEIGHT, TR_WIDTH));
			mRowID++;
		} 
		else {
			if (locationBundle!=null) {
				Location location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
				TextView updateValue = (TextView) findViewById(hmDataType.get(LAT));
				updateValue.setText(latlonFormat.format(location.getLatitude()));
			}
		}
		if (!hmDataType.containsKey(LON)) {
			TableRow tr = new TableRow(this);

			TextView name = new TextView(this);
			TextView value = new TextView(this);

			name.setText(LON);
			name.setTextAppearance(this, style.TextAppearance_Medium);
			name.setTextColor(columnColor);

			value.setText("N.A.");
			value.setTextAppearance(this, style.TextAppearance_Medium);
			value.setTextColor(valueColor);
			value.setGravity(Gravity.RIGHT);
			value.setId(mRowID);

			tr.addView(name);
			tr.addView(value);
			hmDataType.put(LON, mRowID);
			mSentencesTbl.addView(tr, new TableLayout.LayoutParams(TR_HEIGHT, TR_WIDTH));
			mRowID++;
		} 
		else {
			if (locationBundle!=null) {
				Location location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
				TextView updateValue = (TextView) findViewById(hmDataType.get(LON));
				updateValue.setText(latlonFormat.format(location.getLongitude()));
			}
		}

		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		int indexOf_dollar = line.indexOf('$'); 
		if (indexOf_dollar > -1) {
			line = line.substring(indexOf_dollar);
		}
		if (parser.match(line)) {
			if (!hmDataType.containsKey(parser.getName())) {
				TableRow tr = new TableRow(this);

				TextView name = new TextView(this);
				TextView metric = new TextView(this);
				TextView value = new TextView(this);

				name.setText(parser.getName());
				name.setTextAppearance(this, style.TextAppearance_Medium);
				name.setTextColor(columnColor);

				metric.setText(parser.getMetric());
				metric.setTextAppearance(this, style.TextAppearance_Medium);
				metric.setTextColor(columnColor);

				value.setText(parser.getStrValue());
				value.setTextAppearance(this, style.TextAppearance_Medium);
				value.setTextColor(valueColor);
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

	@Override
	void receivedSentenceLine(String line) {
		// TODO Auto-generated method stub

	}
}
