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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class ResetSettingsPreference extends Preference {
    public ResetSettingsPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
    protected void onClick() {
		final Context context = this.getContext();
		
		new AlertDialog.Builder(context)
		.setMessage(R.string.msg_are_you_sure)
		.setCancelable(true)
		.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

				String username = context.getString(R.string.username);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("username", username);
				editor.commit();

				String map_server_url = context.getString(R.string.map_server_url);
				editor = settings.edit();
				editor.putString("map_server_url", map_server_url);
				editor.commit();

				String api_key = context.getString(R.string.api_key);
				editor = settings.edit();
				editor.putString("api_key", api_key);
				editor.commit();
				
				boolean rtupload = context.getResources().getBoolean(R.bool.rtupload);
				editor = settings.edit();
				editor.putBoolean("rtupload", rtupload);
				editor.commit();
				
				boolean filesaver = context.getResources().getBoolean(R.bool.filesaver);
				editor = settings.edit();
				editor.putBoolean("filesaver", filesaver);
				editor.commit();
				
				boolean use_external_storage = context.getResources().getBoolean(R.bool.use_external_storage);
				editor = settings.edit();
				editor.putBoolean("use_external_storage", use_external_storage);
				editor.commit();
			}
		})
		.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		})
		.show();
    }
}
