package com.senspodapp.app;

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

public class PrepareGpsActivity extends Activity implements LocationListener {
	// Debugging
	private static final String TAG = "PrepareGpsActivity";
	private static final boolean D = true;
    
	private static final String PROVIDER = LocationManager.GPS_PROVIDER;
	private static final int    MIN_TIME = 1000;
	private static final float  MIN_DISTANCE = .1f;
	private static final String CHANGE_BUTTON="Close";
	// Layout Views
	private TableLayout gpsTbl;
	private LocationManager mLocationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.preparegps);
		setProgressBarIndeterminateVisibility(true);
		gpsTbl = (TableLayout)findViewById(R.id.tblgps);
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(PROVIDER, MIN_TIME, MIN_DISTANCE, this);
		
		((Button) findViewById(R.id.btn_cancel))
		.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cancel();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(this);
	}
	
	private void cancel() {
		mLocationManager.removeUpdates(this);
		finish();
	}

	public void onLocationChanged(Location location) {
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
		
		Button btn_cancel =((Button) findViewById(R.id.btn_cancel));
		btn_cancel.setText(CHANGE_BUTTON);
		
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
