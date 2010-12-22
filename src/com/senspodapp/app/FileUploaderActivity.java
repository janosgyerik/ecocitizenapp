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

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class FileUploaderActivity extends FileManagerActivity {
	// Debugging
	private static final String TAG = "FileUploaderActivity";
	private static final boolean D = true;

	@Override
	protected int getLayoutResID() {
		return R.layout.fileuploader;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		ListView internalFilesListView = (ListView)findViewById(R.id.internal_storage);
		internalFilesListView.setOnItemClickListener(new ItemClickListener(internalFilesArrayAdapter, getFilesDir()));

		ListView externalFilesListView = (ListView)findViewById(R.id.external_storage);
		externalFilesListView.setOnItemClickListener(new ItemClickListener(externalFilesArrayAdapter, externalDir));
		
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
				if (file.isFile() && upload(file, true)) { 
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
	
	class ItemClickListener implements OnItemClickListener {
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		
		public ItemClickListener(ArrayAdapter<String> filesArrayAdapter, File basedir) {
			mFilesArrayAdapter = filesArrayAdapter;
			mBasedir = basedir;
		}
		
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
			ListView listView = (ListView) parent;
			String filename = (String) listView.getItemAtPosition(position);
			
			if (!filename.startsWith(getString(R.string.label_none))) {
				File file = new File(mBasedir + "/" + filename);
				if (file.isFile() && upload(file, false)) { 
					mFilesArrayAdapter.remove(filename);
					if (mFilesArrayAdapter.isEmpty()) {
						mFilesArrayAdapter.add(getString(R.string.label_none));
					}
				}
			}
		}
	}
	
	private boolean testUpload() {
		FileUploader uploader = new FileUploader(PreferenceManager.getDefaultSharedPreferences(this), null);

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
	
	private boolean upload(File file, boolean quiet) {
		FileUploader uploader = new FileUploader(PreferenceManager.getDefaultSharedPreferences(this), file);
		FileUploader.Status status = uploader.upload();
		
		if (status == FileUploader.Status.SUCCESS) {
			return file.delete();
		}
		
		if (quiet) return false;
		
		int msgID;
		switch (status) {
		case EMPTY_FILE:
			msgID = R.string.fileuploader_msg_empty_file;
			Toast.makeText(this, msgID, Toast.LENGTH_SHORT).show();
			return true;
		case EXCEPTION:
			msgID = R.string.fileuploader_msg_exception;
			break;
		case LOGIN_FAILED:
			msgID = R.string.fileuploader_msg_login_failed;
			break;
		case SERVER_UNREACHABLE:
			msgID = R.string.fileuploader_msg_server_unreachable;
			break;
		case STARTSESSION_FAILED:
			msgID = R.string.fileuploader_msg_startsession_failed;
			break;
		case UPLOAD_INTERRUPTED:
			msgID = R.string.fileuploader_msg_upload_interrupted;
			break;
		default:
			msgID = R.string.fileuploader_msg_unknown_error;
		}
		Toast.makeText(this, msgID, Toast.LENGTH_SHORT).show();
		return false;
	}
}