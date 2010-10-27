package com.senspodapp.app;

import com.senspodapp.parser.BatterySentenceParser;
import com.senspodapp.parser.Co2SentenceParser;
import com.senspodapp.parser.CoxSentenceParser;
import com.senspodapp.parser.HumiditySentenceParser;
import com.senspodapp.parser.NoiseSentenceParser;
import com.senspodapp.parser.NoxSentenceParser;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TextView;

public class TabularViewActivity extends SimpleDeviceManagerClient {
	// Debugging
	private static final String TAG = "TabularViewActivity";
	private static final boolean D = true;

	// Layout Views
	private TableLayout mSentencesTbl;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.tabularview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.tabularview_activity);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		mSentencesTbl = (TableLayout)findViewById(R.id.tblsentences);
		setupCommonButtons();
	}

	
	CoxSentenceParser coxParser = new CoxSentenceParser();
	BatterySentenceParser battParser = new BatterySentenceParser();
	HumiditySentenceParser humParser = new HumiditySentenceParser();
	NoiseSentenceParser noiseParser = new NoiseSentenceParser();
	NoxSentenceParser noxParser = new NoxSentenceParser();
	Co2SentenceParser co2Parser = new Co2SentenceParser();

	@Override
	void receivedSentenceLine(String line) {
		if (battParser.match(line)) {
			TextView batt_metric = (TextView) mSentencesTbl.findViewById(R.id.batt_metric);
			TextView batt_value = (TextView) mSentencesTbl.findViewById(R.id.batt_value);
			batt_metric.setText(battParser.getMetric());
			batt_value.setText(battParser.getStrValue());
		}
		
		else if (coxParser.match(line)) {
			TextView cox_metric = (TextView) mSentencesTbl.findViewById(R.id.cox_metric);
			TextView cox_value = (TextView) mSentencesTbl.findViewById(R.id.cox_value);
			cox_metric.setText(coxParser.getMetric());
			cox_value.setText(coxParser.getStrValue());

		}
		else if (noiseParser.match(line)) {
			TextView noise_metric = (TextView) mSentencesTbl.findViewById(R.id.noise_metric);
			TextView noise_value = (TextView) mSentencesTbl.findViewById(R.id.noise_value);
			noise_metric.setText(noiseParser.getMetric());
			noise_value.setText(noiseParser.getStrValue());

		}
		else if (humParser.match(line)) {
			TextView hum_metric = (TextView) mSentencesTbl.findViewById(R.id.hum_metric);
			TextView hum_value = (TextView) mSentencesTbl.findViewById(R.id.hum_value);
			hum_metric.setText(humParser.getMetric());
			hum_value.setText(humParser.getStrValue());
		}

		else if (coxParser.match(line)) {
			TextView cox_metric = (TextView) mSentencesTbl.findViewById(R.id.cox_metric);
			TextView cox_value = (TextView) mSentencesTbl.findViewById(R.id.cox_value);
			cox_metric.setText(coxParser.getMetric());
			cox_value.setText(coxParser.getStrValue());
		}

		else if (noxParser.match(line)) {
			TextView nox_metric = (TextView) mSentencesTbl.findViewById(R.id.nox_metric);
			TextView nox_value = (TextView) mSentencesTbl.findViewById(R.id.nox_value);
			nox_metric.setText(noxParser.getMetric());
			nox_value.setText(noxParser.getStrValue());
		}
		
		else if (co2Parser.match(line)) {
			TextView co2_metric = (TextView) mSentencesTbl.findViewById(R.id.co2_metric);
			TextView co2_value = (TextView) mSentencesTbl.findViewById(R.id.co2_value);
			co2_metric.setText(co2Parser.getMetric());
			co2_value.setText(co2Parser.getStrValue());
		}

	}
}