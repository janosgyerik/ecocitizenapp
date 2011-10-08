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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debugtools);		
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		tabHost.setup();
		TabHost.TabSpec spec;
		Intent intent; 

		intent = new Intent().setClass(this, TreeViewActivity.class);

		spec = tabHost.newTabSpec("Tree").setIndicator("Tree",
				res.getDrawable(R.drawable.tab_console))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, MultiSensorViewActivity.class);
		spec = tabHost.newTabSpec("MultiSensor").setIndicator("MultiSensor",
				res.getDrawable(R.drawable.tab_sentences))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, MapViewActivity.class);
		spec = tabHost.newTabSpec("Map").setIndicator("Map",
				res.getDrawable(R.drawable.tab_waitforgps))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTabByTag("Tree");
	}
}
