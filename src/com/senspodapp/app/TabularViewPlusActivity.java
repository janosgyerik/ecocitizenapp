package com.senspodapp.app;

import java.util.HashMap;

import com.senspodapp.parser.PsenSentenceParser;

import android.R.style;
import android.graphics.Color;
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
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT; 
	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	
	private HashMap<String, Integer> hmDataType = new HashMap<String, Integer>();
	private int ROWID = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.tabularplusview);
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
			if(!hmDataType.containsKey(parser.getName())){
			    TableRow tr = new TableRow(this);
		
				TextView name = new TextView(this);
				TextView metric = new TextView(this);
				TextView value = new TextView(this);

				name.setText(parser.getName());
				name.setTextAppearance(this, style.TextAppearance_Medium);
				
				metric.setText(parser.getMetric());
				metric.setTextAppearance(this,style.TextAppearance_Medium);
				
				value.setText(parser.getStrValue());
				value.setTextAppearance(this,style.TextAppearance_Medium);
				value.setGravity(Gravity.RIGHT);
                value.setId(ROWID);
				
				tr.addView(name);
				tr.addView(metric);
				tr.addView(value);
				
				name.setTextColor(Color.RED);
				metric.setTextColor(Color.RED);
				value.setTextColor(Color.YELLOW);
				
				hmDataType.put(parser.getName(),ROWID);
				mSentencesTbl.addView(tr, new TableLayout.LayoutParams(FP, WC));
				ROWID++;
			}
			else if(hmDataType.containsKey(parser.getName())){
				TextView updateValue = (TextView)findViewById(hmDataType.get(parser.getName()));
				updateValue.setText(parser.getStrValue());
			}
		}
	}
}