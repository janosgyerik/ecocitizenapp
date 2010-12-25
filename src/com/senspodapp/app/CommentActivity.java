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

import java.util.Calendar;
import java.util.TimeZone;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CommentActivity extends Activity {
	// Debugging
	private static final String TAG = "CommentActivity";
	private static final boolean D = true;
	private static final String MSG_COMING_SOON = "Coming soon ..."; // TODO
	private TextView mYear;
	private TextView mMonth;
	private TextView mDay;
	private Calendar mCalendar;
	private Spinner  mHourSpinner;
	private Spinner  mMinuteSpinner;
	private ArrayAdapter <String> mHourAdapter;
	private ArrayAdapter <String> mMinuteAdapter;
	private String[] mHours;
	private String[] mMinutes;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		setContentView(R.layout.comment);
		mHours = new String[24];
		mMinutes = new String[60];

		for (int count = 0; count < 24; ++count) {
			mHours[count] = String.valueOf(count);
		}

		for (int count = 0; count < 60; ++count) {
			mMinutes[count] = String.valueOf(count);
		}

		mHourAdapter = new ArrayAdapter <String> (this, R.layout.customized_simple_spinner_item, mHours);
		mMinuteAdapter = new ArrayAdapter <String> (this, R.layout.customized_simple_spinner_item, mMinutes);
		mCalendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
		setTime();
		Button btnUploadAll = (Button) findViewById(R.id.btn_comment);
		btnUploadAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(CommentActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
			}
		});
	}

	void setTime() {
		mYear = (TextView) findViewById(R.id.comment_year);
		mMonth = (TextView) findViewById(R.id.comment_month);
		mDay = (TextView) findViewById(R.id.comment_day);
		mHourSpinner = (Spinner) findViewById(R.id.hour_spinner);
		mMinuteSpinner = (Spinner) findViewById(R.id.minute_spinner);

		mYear.setText(String.valueOf(mCalendar.get(Calendar.YEAR)));
		mMonth.setText(String.valueOf(mCalendar.get(Calendar.MONTH) + 1));
		mDay.setText(String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)));
		
		mHourSpinner.setAdapter(mHourAdapter);
		mHourSpinner.setSelection(mCalendar.get(Calendar.HOUR_OF_DAY));
		mMinuteSpinner.setAdapter(mMinuteAdapter);
		mMinuteSpinner.setSelection(mCalendar.get(Calendar.MINUTE));
	}

}
