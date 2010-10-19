package com.senspodapp.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class TabularViewActivity extends DeviceManagerClient {
	// Debugging
	private static final String TAG = "TabularViewActivity";
	private static final boolean D = true;

	// Layout Views
	private TableLayout mSentencesTbl;

	// Data types
	private static final String DATA_TYPE_HUM = "Hum";
	private static final String DATA_TYPE_TEMP = "T";
	private static final String DATA_TYPE_NOISE = "Noise";
	private static final String DATA_TYPE_NOX = "NOx";
	private static final String DATA_TYPE_COX = "COx";
	private static final String DATA_TYPE_BATT = "Batt";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.tabularview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		// TODO: add this as Tabular View
		//mTitle.setText(R.string.tabularview_activity);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mSentencesTbl = (TableLayout)findViewById(R.id.tblsentences);
		
		// Set up the button to connect/disconnect sensors
		Button mBtnConnect = (Button)findViewById(R.id.btn_connect_device);
		mBtnConnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				connectSensor();
			}  
		});
		Button mBtnDisconnect = (Button)findViewById(R.id.btn_disconnect_device);
		mBtnDisconnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				disconnectSensor();
			}  
		});
	}

	@Override
	void receivedSentenceLine(String line) {
		String values[] = new String[6];
		values = line.split(",");

		if(values[1].equals(DATA_TYPE_HUM)){

			TextView hum_name =(TextView)mSentencesTbl.findViewById(R.id.Hum_name);
			TextView hum_metric =(TextView)mSentencesTbl.findViewById(R.id.Hum_metric);
			TextView hum_value =(TextView)mSentencesTbl.findViewById(R.id.Hum_value);
			hum_name.setText(values[1]);
			hum_metric.setText(values[2]);
			hum_value.setText(values[3]);
			if(values[4].equals(DATA_TYPE_TEMP)){
				TextView temp_name =(TextView)mSentencesTbl.findViewById(R.id.Temp_name);
				TextView temp_value =(TextView)mSentencesTbl.findViewById(R.id.Temp_value);
				temp_name.setText(values[4]);
				temp_value.setText(values[5]);
			}
		}
		if(values[1].equals(DATA_TYPE_NOISE)){
			TextView noise_name =(TextView)mSentencesTbl.findViewById(R.id.Noise_name);
			TextView noise_metric =(TextView)mSentencesTbl.findViewById(R.id.Noise_metric);
			TextView noise_value =(TextView)mSentencesTbl.findViewById(R.id.Noise_value);
			noise_name.setText(values[1]);
			noise_metric.setText(values[2]);
			noise_value.setText(values[3]);


		}
		if(values[1].equals(DATA_TYPE_NOX)){
			TextView nox_name =(TextView)mSentencesTbl.findViewById(R.id.Nox_name);
			TextView nox_metric =(TextView)mSentencesTbl.findViewById(R.id.Nox_metric);
			TextView nox_value =(TextView)mSentencesTbl.findViewById(R.id.Nox_value);
			nox_name.setText(values[1]);
			nox_metric.setText(values[2]);
			nox_value.setText(values[3]);
		}

		if(values[1].equals(DATA_TYPE_COX)){
			TextView cox_name =(TextView)mSentencesTbl.findViewById(R.id.Cox_name);
			TextView cox_metric =(TextView)mSentencesTbl.findViewById(R.id.Cox_metric);
			TextView cox_value =(TextView)mSentencesTbl.findViewById(R.id.Cox_value);
			cox_name.setText(values[1]);
			cox_metric.setText(values[2]);
			cox_value.setText(values[3]);
		}

		if(values[1].equals(DATA_TYPE_BATT)){
			TextView batt_name =(TextView)mSentencesTbl.findViewById(R.id.Batt_name);
			TextView batt_metric =(TextView)mSentencesTbl.findViewById(R.id.Batt_metric);
			TextView batt_value =(TextView)mSentencesTbl.findViewById(R.id.Batt_value);
			batt_name.setText(values[1]);
			batt_metric.setText(values[2]);
			batt_value.setText(values[3]);
		}

	}
}