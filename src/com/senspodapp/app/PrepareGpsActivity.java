package com.senspodapp.app;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PrepareGpsActivity extends Activity implements LocationListener {
    private LocationManager mLocationManager;
	static final String PROVIDER = LocationManager.GPS_PROVIDER;
	static final int    MIN_TIME = 1000;
	static final float  MIN_DISTANCE = .1f;
	private Button mBtnCancel;
	private TextView gpsText;
	private static final String Msg = "Activated GPS, waiting for current location...";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preparegps);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestLocationUpdates();
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
        gpsText = (TextView)findViewById(R.id.TextGps);
        gpsText.setText(Msg);
    }
  
    @Override
    public void onLocationChanged(Location location) {
        Log.v("----gps---", "was activated");
        finish();
    }
    
	void requestLocationUpdates() {
		if (mLocationManager == null) return;
		mLocationManager.requestLocationUpdates(PROVIDER, MIN_TIME, MIN_DISTANCE, this);
	}
    
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}


}
