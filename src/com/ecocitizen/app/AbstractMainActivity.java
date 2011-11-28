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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public abstract class AbstractMainActivity extends DeviceManagerClient {
	
	private ImageButton mBtnConnect;
	private ImageButton mBtnDisconnect;
	private ImageButton mBtnAddNote;

	public void setupCommonButtons() {
		mBtnConnect = (ImageButton)findViewById(R.id.btn_connect_device);
		mBtnConnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				connectSensor();
			}  
		});
		
		mBtnDisconnect = (ImageButton)findViewById(R.id.btn_disconnect_device);
		mBtnDisconnect.setOnClickListener(new View.OnClickListener(){   
			public void onClick(View v) {   
				disconnectSensor();
			}  
		});
		
		mBtnAddNote = (ImageButton) findViewById(R.id.btn_addnote);
		mBtnAddNote.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startAddNoteActivity();
			}
		});
		
		//change requirement as follows
		//it is true when there is no connected device. 
		//TODO get list of connected devices when initializing view
		if (true) {
			mBtnDisconnect.setVisibility(View.GONE);
			mBtnAddNote.setVisibility(View.GONE);
		}
	}
	
	@Override
	void onConnectedDevicesUpdated() {
		super.onConnectedDevicesUpdated();
		if (mConnectedDevices.isEmpty()) {
			mBtnDisconnect.setVisibility(View.GONE);
			mBtnAddNote.setVisibility(View.GONE);
		}
		else {
			//mBtnDisconnect.setVisibility(View.VISIBLE);
			mBtnAddNote.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_map:
			startActivity(new Intent(this, MapViewActivity.class));
			return true;
		case R.id.menu_fileuploader:
			startActivity(new Intent(this, FileUploaderActivity.class));
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;    
		case R.id.menu_debugtools:
			startActivity(new Intent(this, DebugToolsActivity.class));
			return true;
		case R.id.menu_quit:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_quit)
			.setCancelable(true)
			.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					shutdown();
					finish();
				}
			})
			.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).show();
			return true;
		case R.id.menu_disconnect:
			disconnectSensor();
			return true;
		}
		return false;
	}
}
