package com.senspodapp.app;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PrepareGpsActivity extends Activity implements LocationListener {
	// Debugging
	private static final String TAG = "PrepareGpsActivity";
	private static final boolean D = true;
    
	private static final String PROVIDER = LocationManager.GPS_PROVIDER;
	private static final int    MIN_TIME = 1000;
	private static final float  MIN_DISTANCE = .1f;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		setContentView(R.layout.preparegps);

		((LocationManager) getSystemService(LOCATION_SERVICE))
		.requestLocationUpdates(PROVIDER, MIN_TIME, MIN_DISTANCE, this);
		
		((Button) findViewById(R.id.btn_cancel))
		.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void onLocationChanged(Location location) {
		finish();
	}

	public void onProviderDisabled(String provider) {
		// nothing to do
	}

	public void onProviderEnabled(String provider) {
		// nothing to do
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// nothing to do
	}
}
