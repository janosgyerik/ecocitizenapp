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

import java.text.DecimalFormat;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.DeviceHandlerFactory;
import com.ecocitizen.common.bundlewrapper.SensorDataBundleWrapper;
import com.ecocitizen.common.parser.SensorData;
import com.ecocitizen.common.parser.SensorDataFilter;
import com.ecocitizen.common.parser.SensorDataParser;
import com.ecocitizen.common.parser.SensorDataType;

public class MultiSensorViewActivity extends AbstractMainActivity {
	// Debugging
	private static final String TAG = "MultiSensorViewActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(MultiSensorViewActivity.class);

	private static DecimalFormat valFormat = new DecimalFormat("###.#");

	// Constants
	private final int BOXES_NUM = 4;
	
	private TextView mLatView;
	private TextView mLonView;
	private TextView mTemperatureView;
	private TextView mTemperatureUnitView;
	private TextView mHumidityView;
	private TextView mHumidityUnitView;
	
	private TextView[] mBoxName = new TextView[BOXES_NUM];
	private TextView[] mBoxVal = new TextView[BOXES_NUM];
	private TextView[] mBoxUnit = new TextView[BOXES_NUM];
	private ImageView[] mBoxImage = new ImageView[BOXES_NUM];
	
	private static final String TREE_PREFIX = "tree_";
	private static final String TREE_PACKAGE = "com.ecocitizen.app";
	private static final String TREE_TYPE = "drawable";

	private static SensorDataFilter filter = 
		new SensorDataFilter(SensorDataType.CO2, SensorDataType.NOx, 
				SensorDataType.COx, SensorDataType.Noise, 
				SensorDataType.Humidity, SensorDataType.Temperature);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.multisensorview);

		mLatView = (TextView)findViewById(R.id.lat_val);
		mLonView = (TextView)findViewById(R.id.lon_val);
		mTemperatureView = (TextView)findViewById(R.id.Temperature_val);
		mTemperatureUnitView = (TextView)findViewById(R.id.Temperature_unit);
		mHumidityView = (TextView)findViewById(R.id.Humidity_val);
		mHumidityUnitView = (TextView)findViewById(R.id.Humidity_unit);
		
		int boxIds[] = {R.id.boxtop_left, R.id.boxtop_right, R.id.boxbottom_left, R.id.boxbottom_right};
		
		for (int i = 0; i < boxIds.length; ++i) {  
            LinearLayout box = (LinearLayout) findViewById(boxIds[i]);  
            mBoxName[i] = (TextView)box.findViewById(R.id.box_name);  
            mBoxName[i].setText("--");
            
            mBoxVal[i] = (TextView)box.findViewById(R.id.box_val);  
            mBoxVal[i].setText("--");
            
            mBoxUnit[i] = (TextView)box.findViewById(R.id.box_metric); 
            mBoxUnit[i].setText("--");
            
            mBoxImage[i] = (ImageView)box.findViewById(R.id.box_image); 
            mBoxImage[i].setImageResource(R.drawable.tree_min);
		}
		
		setupCommonButtons();
	}

	@Override
	void receivedSensorDataBundle(SensorDataBundleWrapper bundle) {
		
		Location location = bundle.getLocation();
		
		if (location == null) {
			mLatView.setText(R.string.common_na);
			mLonView.setText(R.string.common_na);
		}
		else {
			String lat_val[], lon_val[];
			
			lat_val = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS).split(":", 0);
			lon_val = Location.convert(location.getLongitude(), Location.FORMAT_SECONDS).split(":", 0);
			mLatView.setText(lat_val[0] + "º" + lat_val[1] + "'" +
					lat_val[2].substring(0, lat_val[2].indexOf('.')) + "\"");
			mLonView.setText(lon_val[0] + "º" + lon_val[1] + "'" +
					lon_val[2].substring(0, lon_val[2].indexOf('.')) + "\"");
		}
	
		SensorDataParser parser = 
			DeviceHandlerFactory.getInstance().getParser(bundle.getSensorName(), bundle.getSensorId());
		
		for (SensorData data : parser.getSensorData(bundle.getSensorData(), filter)) {
			switch (data.getDataType()) {
			case CO2:
				updateBox(data, 0);
				break;
			case NOx:
				updateBox(data, 1);
				break;
			case COx:
				updateBox(data, 2);
				break;
			case Noise:
				updateBox(data, 3);
				break;
			case Humidity:
				mHumidityView.setText(data.getStrValue());
				mHumidityUnitView.setText(data.getUnit());
				break;
			case Temperature:
				mTemperatureView.setText(data.getStrValue());
				mTemperatureUnitView.setText(data.getUnit());
				break;
			}
		}
	}
	
	void updateBox(SensorData data, int i) {
		mBoxName[i].setText(data.getName());
		mBoxVal[i].setText(valFormat.format(data.getFloatValue()));
		mBoxUnit[i].setText(data.getUnit());
		
		String imgname = TREE_PREFIX + data.getLevel();
		int resID = getResources().getIdentifier(imgname, TREE_TYPE, TREE_PACKAGE);
		if (resID == 0) {
			resID = R.drawable.tree_max;
		}
		mBoxImage[i].setImageResource(resID);
	}
}
