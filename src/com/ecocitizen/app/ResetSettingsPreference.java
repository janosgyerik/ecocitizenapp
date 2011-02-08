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
