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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.ecocitizen.common.HttpHelper;
import com.ecocitizen.common.Util;

public class RegisterClientPreference extends Preference {
	public RegisterClientPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onClick() {
		final Context context = this.getContext();

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
		}
		
		HttpHelper httpHelper = new HttpHelper(settings, Util.getUserAgentString(packageInfo));
		String[] usernameAndApiKey = httpHelper.registerClient();

		String username = usernameAndApiKey[0];
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("username", username);
		editor.commit();

		String api_key = usernameAndApiKey[1];
		editor = settings.edit();
		editor.putString("api_key", api_key);
		editor.commit();
	}
}
