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
		// TODO Auto-generated method stub
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
