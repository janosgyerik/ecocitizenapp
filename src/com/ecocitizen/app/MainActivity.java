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
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);		
		
		TabHost tabHost = getTabHost();
		tabHost.setup();
		TabHost.TabSpec spec;
		Intent intent; 
		ImageButton btn;

		intent = new Intent().setClass(this, TreeViewActivity.class);
		btn = new ImageButton(this);
		btn.setImageResource(R.drawable.tab_tree);
		btn.setPadding(0, 0, 0, 0);
		spec = tabHost.newTabSpec("Tree").setContent(intent).setIndicator(btn);
		tabHost.addTab(spec);
		
		intent = new Intent().setClass(this, MultiSensorViewActivity.class);
		btn = new ImageButton(this);
		btn.setImageResource(R.drawable.tab_multisensor);
		btn.setPadding(0, 0, 0, 0);
		spec = tabHost.newTabSpec("MultiSensor").setContent(intent).setIndicator(btn);
		tabHost.addTab(spec);

		tabHost.setCurrentTabByTag("Tree");
	}
}
