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

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class DebugToolsActivity extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.debugtools);

	    Resources res = getResources();
	    TabHost tabHost = getTabHost();
	    tabHost.setup();
	    TabHost.TabSpec spec;
	    Intent intent; 
	    
	    intent = new Intent().setClass(this, TabularViewPlusActivity.class);
	   
	    spec = tabHost.newTabSpec("tabularView+").setIndicator("TabularView",
	                      res.getDrawable(R.drawable.tab_tabularviewplus))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	   
	    intent = new Intent().setClass(this, SentencesActivity.class);
	    spec = tabHost.newTabSpec("sentences").setIndicator("Sentences",
	                      res.getDrawable(R.drawable.tab_sentences))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, DeviceManagerConsole.class);
	    spec = tabHost.newTabSpec("console").setIndicator("Console",
	                      res.getDrawable(R.drawable.tab_console))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTabByTag("tabularView+");
	}

}
