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

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ecocitizen.app.util.FinishActivityClickListener;

public class AddNoteActivity extends Activity {
	// Debugging
	private static final String TAG = "AddNoteActivity";
	private static final boolean D = true;
	
	private static final String MSG_COMING_SOON = "Coming soon ..."; // TODO
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		setContentView(R.layout.addnote);
		
		Calendar calendar = Calendar.getInstance();
		TextView currentDateView = (TextView)findViewById(R.id.addnote_dt);
		currentDateView.setText(calendar.getTime().toLocaleString());
		
		Button btnAddNote = (Button) findViewById(R.id.btn_addnote);
		btnAddNote.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(AddNoteActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
			}
		});
		
		Button btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new FinishActivityClickListener(this));
	}
}
