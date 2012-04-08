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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.ecocitizen.common.HttpHelper;

public class RegisterClientPreference extends Preference {
	public RegisterClientPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private Context mContext;
	private SharedPreferences mSettings;
	private HttpHelper mHttpHelper;

	@Override
	protected void onClick() {
		mContext = this.getContext();
		mHttpHelper = new HttpHelper(mContext);
		mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		new RegisterClientAsyncTask().execute((Void)null);
	}

	class RegisterClientAsyncTask extends AsyncTask<Void, Void, String[]> {
		ProgressDialog mmProgressDialog;

		protected void onPreExecute() {
			mmProgressDialog = ProgressDialog.show(mContext, 
					mContext.getString(R.string.title_register_client), 
					mContext.getString(R.string.msg_register_client));
		}

		protected String[] doInBackground(Void... params) {
			String[] usernameAndApiKey = mHttpHelper.registerClient();
			return usernameAndApiKey;
		}

		@Override
		protected void onPostExecute(String[] usernameAndApiKey) {
			if (mmProgressDialog.isShowing()) mmProgressDialog.cancel();
			
			String username = usernameAndApiKey[0];
			SharedPreferences.Editor editor = mSettings.edit();
			editor.putString("username", username);
			editor.commit();

			String api_key = usernameAndApiKey[1];
			editor = mSettings.edit();
			editor.putString("api_key", api_key);
			editor.commit();
		}
	}
}
