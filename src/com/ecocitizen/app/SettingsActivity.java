/*
 * Copyright (C) 2010-2012 Eco Mobile Citizen
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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		customInitPreferenceGroup(this.getPreferenceScreen());
	}
	
	private void customInitPreferenceGroup(PreferenceGroup preferenceGroup) {
		for (int i = 0; i < preferenceGroup.getPreferenceCount(); ++i) {
			Preference pref = preferenceGroup.getPreference(i);
			if (pref instanceof PreferenceGroup) {
				customInitPreferenceGroup((PreferenceGroup) pref);
			}
			else {
				updatePreferenceEditor(pref);
			}	
		}
	}

	private void updatePreferenceEditor(Preference pref) {
		updatePreferenceEditor(pref, pref.getKey());
	}

	private void updatePreferenceEditor(Preference pref, String key) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
	    if (pref instanceof EditTextPreference) {
	    	String value = settings.getString(key, "");
	        EditTextPreference editTextPref = (EditTextPreference) pref;
	        editTextPref.setSummary(value);
	        editTextPref.setText(value);
	    }
	    else if (pref instanceof CheckBoxPreference) {
	    	CheckBoxPreference checkBoxPref = (CheckBoxPreference) pref;
	    	checkBoxPref.setChecked(settings.getBoolean(key, false));
	    }
	}

	@Override
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

	@Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updatePreferenceEditor(findPreference(key), key);
	}
}
