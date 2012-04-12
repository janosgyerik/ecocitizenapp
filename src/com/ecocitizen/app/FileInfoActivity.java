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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class FileInfoActivity extends Activity {
	// Debugging
	private static final String TAG = "FileInfoActivity";
	private static final boolean D = false;
	
	// Members
	protected File mFile;
	
	// Constants
	public static final String BUNDLEKEY_FILENAME = "fn";
	
	static final int PREVIEWLINES = 5; // first N and last N lines will be shown in preview
	
	static final String[] ZZBYTEMARKER = new String[]{ "", "kb", "mb", "gb" };
	static final DecimalFormat ZZBYTEFORMAT = new DecimalFormat("#.#"); // max 1 fraction digit
	
	public static String bytesToString(long bytes) {
		double val = bytes;
		int i = 0;
		for (; i < ZZBYTEMARKER.length && val > 1000; ++i) val /= 1000;
		return ZZBYTEFORMAT.format(val) + ZZBYTEMARKER[i];
	}
	
	public final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy MMM dd HH:mm");

	public static String timeToString(long time) {
		return DATEFORMAT.format(new Date(time));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		setContentView(R.layout.fileinfo);
		findViewById(R.id.content).setVisibility(View.INVISIBLE);
		
		String filename = getIntent().getExtras().getString(BUNDLEKEY_FILENAME);
		mFile = new File(filename);
		((TextView)findViewById(R.id.fileinfo_filename)).setText(mFile.getName());
		((TextView)findViewById(R.id.fileinfo_size)).setText(bytesToString(mFile.length()));
		((TextView)findViewById(R.id.fileinfo_date)).setText(timeToString(mFile.lastModified()));

		// more details are loaded in an async task
		new LoadFileInfoAsyncTask().execute();

		findViewById(R.id.btn_close).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	public static int getRecordNum(File file) {
		int cnt = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				while (reader.readLine() != null) {
					++cnt;
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
		}
		return cnt;
	}
	
	public static String getContentPreview(File file, int recordnum) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				int cnt = 0;
				StringBuffer buffer1 = new StringBuffer();
				StringBuffer buffer2 = new StringBuffer();
				String line;
				while ((line = reader.readLine()) != null) {
					if (cnt < PREVIEWLINES) buffer1.append(line + "\n");
					else if (cnt >= recordnum - PREVIEWLINES) buffer2.append(line + "\n");
					++cnt;
				}
				if (recordnum > PREVIEWLINES * 2) {
					buffer1.append("...\n");
				}
				return buffer1.toString() + buffer2.toString();
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
		}
		return "";
	}
	
	class LoadFileInfoAsyncTask extends AsyncTask<Void, Void, Integer> {
		ProgressDialog mProgressDialog;
		
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(FileInfoActivity.this, "",
					FileInfoActivity.this.getString(R.string.msg_loading_fileinfo), 
					false, true, 
					new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							finish();
						}
			});
		}
		
		protected Integer doInBackground(Void... params) {
			return getRecordNum(mFile);
		}

		protected void onPostExecute(Integer recordnum) {
			((TextView)findViewById(R.id.fileinfo_recordnum)).setText(String.valueOf(recordnum));
			((TextView)findViewById(R.id.fileinfo_content)).setText(getContentPreview(mFile, recordnum));
			
			mProgressDialog.dismiss();
			findViewById(R.id.content).setVisibility(View.VISIBLE);
		}
	}
	
}
