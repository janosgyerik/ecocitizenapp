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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.Util;

public class FileUploaderActivity extends FileManagerActivity {
	// Debugging
	private static final String TAG = "FileUploaderActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(FileUploaderActivity.class);
	
	enum StorageType {
		INTERNAL,
		EXTERNAL
	}

	// Members
	private StorageType mCurrentStorageType;
	private File mCurrentFile;
	
	private String mUserAgentString = null;
	
	@Override
	protected int getLayoutResID() {
		return R.layout.fileuploader;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		PackageInfo packageInfo = null;
		try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
		}
		mUserAgentString = Util.getUserAgentString(packageInfo);
		
		ListView internalFilesListView = (ListView)findViewById(R.id.internal_storage);
		internalFilesListView.setOnItemClickListener(new ItemClickListener(StorageType.INTERNAL, internalFilesArrayAdapter, getFilesDir()));

		ListView externalFilesListView = (ListView)findViewById(R.id.external_storage);
		externalFilesListView.setOnItemClickListener(new ItemClickListener(StorageType.EXTERNAL, externalFilesArrayAdapter, externalDir));
		
		findViewById(R.id.btn_upload_all_internal).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(FileUploaderActivity.this)
				.setMessage(R.string.msg_are_you_sure)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (testUpload()) {
							new UploadAllAsyncTask(
									internalFilesArrayAdapter, 
									getFilesDir(), 
									internalFilenames, 
									R.id.btn_upload_all_internal
							).execute();
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.show();
			}
		});

		findViewById(R.id.btn_upload_all_external).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(FileUploaderActivity.this)
				.setMessage(R.string.msg_are_you_sure)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (testUpload()) {
							new UploadAllAsyncTask(
									externalFilesArrayAdapter, 
									externalDir,
									externalFilenames, 
									R.id.btn_upload_all_external
							).execute();
						}
					}
				})
				.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.show();
			}
		});
		
		findViewById(R.id.btn_close).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	class UploadAllAsyncTask extends AsyncTask<Void, String, Void> {
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		int mUploadBtnId;
		
		public UploadAllAsyncTask(
				ArrayAdapter<String> filesArrayAdapter, 
				File basedir, 
				List<String> filenames,
				int uploadBtnId) {
			mFilesArrayAdapter = filesArrayAdapter;
			mBasedir = basedir;
			mUploadBtnId = uploadBtnId;			
		}
		
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			findViewById(mUploadBtnId).setEnabled(false);
		}
		
		protected Void doInBackground(Void... params) {
			LinkedList<String> filenames = new LinkedList<String>();
			for (int i = 0; i < mFilesArrayAdapter.getCount(); ++i) {
				filenames.add(mFilesArrayAdapter.getItem(i));
			}
			for (String filename : filenames) {
				File file = new File(mBasedir + "/" + filename);
				if (file.isFile() && upload(file)) { 
					publishProgress(filename);
				}
			}
			return null;
		}

		protected void onProgressUpdate(String...progress) {
			mFilesArrayAdapter.remove(progress[0]);
		}

		protected void onPostExecute(Void result) {
			if (mFilesArrayAdapter.isEmpty()) {
				mFilesArrayAdapter.add(getString(R.string.label_none));
			}
			setProgressBarIndeterminateVisibility(false);
			findViewById(mUploadBtnId).setEnabled(true);
		}
	}
	
	
	private boolean testUpload() {
		FileUploader uploader = new FileUploader(PreferenceManager.getDefaultSharedPreferences(this), null, mUserAgentString);

		if (!uploader.isServerReachable()) {
			Toast.makeText(this, R.string.fileuploader_msg_server_unreachable, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (!uploader.isLoginOK()) {
			Toast.makeText(this, R.string.fileuploader_msg_login_failed, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	private boolean upload(File file) {
		FileUploader uploader = new FileUploader(PreferenceManager.getDefaultSharedPreferences(this), file, mUserAgentString);
		FileUploader.Status status = uploader.upload();
		
		if (status == FileUploader.Status.SUCCESS) return true;
		return false;
	}
	
	class ItemClickListener implements OnItemClickListener {
		StorageType mStorageType;
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		
		public ItemClickListener(StorageType storageType, ArrayAdapter<String> filesArrayAdapter, File basedir) {
			mStorageType = storageType;
			mFilesArrayAdapter = filesArrayAdapter;
			mBasedir = basedir;
		}
		
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
			ListView listView = (ListView) parent;
			String filename = (String) listView.getItemAtPosition(position);
			
			// these will be used when the FileInfo activity returns
			mCurrentStorageType = mStorageType;
			mCurrentFile = new File(mBasedir + "/" + filename);
			
			Intent intent = new Intent(FileUploaderActivity.this, FileInfoWithUploadActivity.class);
			Bundle params = new Bundle();
			params.putString(FileInfoActivity.BUNDLEKEY_FILENAME, mBasedir + "/" + filename);
			intent.putExtras(params);
			startActivityForResult(intent, mStorageType.ordinal());
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!mCurrentFile.exists()) { 
			if (mCurrentStorageType == StorageType.INTERNAL) {
				internalFilesArrayAdapter.remove(mCurrentFile.getName());
			}
			else if (mCurrentStorageType == StorageType.EXTERNAL) {
				externalFilesArrayAdapter.remove(mCurrentFile.getName());
			}
		}
	}
}
