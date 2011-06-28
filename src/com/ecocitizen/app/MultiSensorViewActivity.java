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

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecocitizen.common.SentenceBundle;
import com.ecocitizen.parser.CO2SentenceParser;
import com.ecocitizen.parser.COxSentenceParser;
import com.ecocitizen.parser.NOxSentenceParser;
import com.ecocitizen.parser.NoiseSentenceParser;
import com.ecocitizen.parser.PsenSentenceParser;

public class MultiSensorViewActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "MultiSensorViewActivity";
	private static final boolean D = true;

	private static DecimalFormat valFormat = new DecimalFormat("###.#");

	// Constants
	private final int box_num = 4;
	
	private TextView mLatView;
	private TextView mLonView;
	private TextView mTView;
	private TextView mRHView;
	
	private TextView[] mBoxName = new TextView[box_num];
	private TextView[] mBoxVal = new TextView[box_num];
	private TextView[] mBoxMetric = new TextView[box_num];
	
	PsenSentenceParser[] parser_box = new PsenSentenceParser[box_num];
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		 int boxIds[] = {R.id.boxtop_left, R.id.boxtop_right, R.id.boxbottom_left, R.id.boxbottom_right};
		
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.multisensorview);


		mLatView = (TextView)findViewById(R.id.lat_val);
		mLonView = (TextView)findViewById(R.id.lon_val);
		mTView = (TextView)findViewById(R.id.T_val);
		mRHView = (TextView)findViewById(R.id.RH_val);
		
		for(int i = 0 ; i < boxIds.length; i++) {  
            LinearLayout box = (LinearLayout) findViewById(boxIds[i]);  
            mBoxName[i] = (TextView)box.findViewById(R.id.box_name);  
            mBoxVal[i] = (TextView)box.findViewById(R.id.box_val);  
            mBoxMetric[i] = (TextView)box.findViewById(R.id.box_metric); 
            mBoxName[i].setText("--");
            mBoxVal[i].setText("--");
            mBoxMetric[i].setText("--");
		}
		
		setupCommonButtons();
	}

	@Override
	void receivedSentenceBundle(SentenceBundle bundle) {
		
		Location location = bundle.getLocation();
		parser_box[0] = new CO2SentenceParser();
		parser_box[1] = new NOxSentenceParser();
		parser_box[2] = new COxSentenceParser();
		parser_box[3] = new NoiseSentenceParser();
		
		if (location == null) {
			mLatView.setText(R.string.common_na);
			mLonView.setText(R.string.common_na);
			mTView.setText(R.string.common_na);
			mRHView.setText(R.string.common_na);
		}
		else {
			String lat_val[], lon_val[];
			
			lat_val = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS).split(":", 0);
			lon_val = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS).split(":", 0);
			mLatView.setText(lat_val[0] + "º" + lat_val[1] + "'" +
					lat_val[2].substring(0, lat_val[2].indexOf('.')) + "\"");
			mLonView.setText(lon_val[0] + "º" + lon_val[1] + "'" +
					lon_val[2].substring(0, lon_val[2].indexOf('.')) + "\"");
			mTView.setText("24.5");
			mRHView.setText("35");
		}
	
		
		String line = bundle.getSentenceLine();
		for (int i = 0 ; i < box_num; i++) {
			if (parser_box[i].match(line)) {
				updateBox(parser_box[i], i);
			}
		}
	}
	
	void updateBox(PsenSentenceParser parser, int i) {
		mBoxName[i].setText(parser.getName());
		mBoxVal[i].setText(valFormat.format(parser.getFloatValue()));
		mBoxMetric[i].setText(parser.getMetric());
	}
}
