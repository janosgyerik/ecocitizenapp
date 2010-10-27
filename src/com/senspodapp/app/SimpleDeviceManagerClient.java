package com.senspodapp.app;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public abstract class SimpleDeviceManagerClient extends DeviceManagerClient {
	
	private Button mBtnConnect;
	private Button mBtnDisconnect;
	
	public void  setupCommonButtons(){
		
	 // Set up the button to connect/disconnect sensors
	 mBtnConnect = (Button)findViewById(R.id.btn_connect_device);
	 mBtnConnect.setOnClickListener(new View.OnClickListener(){   
		public void onClick(View v) {   
			connectSensor();
		}  
	 });
	 
	 mBtnDisconnect = (Button)findViewById(R.id.btn_disconnect_device);
	 mBtnDisconnect.setOnClickListener(new View.OnClickListener(){   
		public void onClick(View v) {   
			disconnectSensor();
		}  
	 });
	 mBtnDisconnect.setVisibility(View.GONE);

	}
	
	@Override
	void setConnectedDeviceName(String connectedDeviceName) {
		super.setConnectedDeviceName(connectedDeviceName);
		if (connectedDeviceName == null) {
			mBtnConnect.setVisibility(View.VISIBLE);
			mBtnDisconnect.setVisibility(View.GONE);
		}
		else {
			mBtnConnect.setVisibility(View.GONE);
			//mBtnDisconnect.setVisibility(View.VISIBLE);
		}
	}
	
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