package com.senspodapp.app;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class SimpleDeviceManagerClient extends DeviceManagerClient {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.simpledevicemanagerclient, menu);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			connectSensor();
			return true;
		case R.id.menu_disconnect:
			disconnectSensor();
			break;
		case R.id.menu_quit:
			finish();
			break;
		}
		return false;
	}
}