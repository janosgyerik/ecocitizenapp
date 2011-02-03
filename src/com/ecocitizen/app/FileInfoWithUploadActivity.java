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

import com.ecocitizen.app.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class FileInfoWithUploadActivity extends FileInfoActivity {
	// Debugging
	private static final String TAG = "FileInfoWithUploadActivity";
	private static final boolean D = true;
	
	// Members
	private Button mBtnUpload;
	private Button mBtnDelete;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		mBtnUpload = (Button) findViewById(R.id.btn_upload);
		mBtnUpload.setVisibility(View.VISIBLE);
		mBtnUpload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performUploadFile();
			}
		});
		
		mBtnDelete = (Button) findViewById(R.id.btn_delete);
		mBtnDelete.setVisibility(View.VISIBLE);
		mBtnDelete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(FileInfoWithUploadActivity.this)
				.setMessage(R.string.msg_are_you_sure)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						performDeleteFile();
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

	class UploadFileAsyncTask extends AsyncTask<Void, Integer, FileUploader.Status> {
		FileUploader mFileUploader;
		ProgressDialog mProgressDialog;
		
		public UploadFileAsyncTask(SharedPreferences prefs, File file) {
			mFileUploader = new FileUploader(prefs, file);
		}
		
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(FileInfoWithUploadActivity.this, 
					"", FileInfoWithUploadActivity.this.getString(R.string.msg_uploading), false, false);
		}
		
		protected FileUploader.Status doInBackground(Void... params) {
			return mFileUploader.upload();
		}

		protected void onPostExecute(FileUploader.Status status) {
			mProgressDialog.cancel();
			
			if (status == FileUploader.Status.SUCCESS) {
				mBtnUpload.setVisibility(View.GONE);
				Toast.makeText(FileInfoWithUploadActivity.this, R.string.msg_upload_success, Toast.LENGTH_SHORT).show();
				return;
			}
			
			int msgID;
			switch (status) {
			case EMPTY_FILE:
				msgID = R.string.fileuploader_msg_empty_file;
				Toast.makeText(FileInfoWithUploadActivity.this, msgID, Toast.LENGTH_SHORT).show();
				mBtnUpload.setVisibility(View.GONE);
				return;
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
			Toast.makeText(FileInfoWithUploadActivity.this, msgID, Toast.LENGTH_LONG).show();
		}
	}
	
	private void performUploadFile() {
		new UploadFileAsyncTask(PreferenceManager.getDefaultSharedPreferences(this), mFile).execute();
	}
	
	private void performDeleteFile() {
		if (mFile.delete()) {
			mBtnUpload.setVisibility(View.GONE);
			mBtnDelete.setVisibility(View.GONE);
			Toast.makeText(this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(this, R.string.msg_delete_failure, Toast.LENGTH_SHORT).show();
		}
	}

	/*
	class UploadFileAsyncTask extends AsyncTask<Void, Integer, Void> {
		File mFile;
		int mRecordsInFile;
		
		ProgressDialog mProgressDialog;
		
		public UploadFileAsyncTask(File file, int recordsInFile) {
			mFile = file;
			mRecordsInFile = recordsInFile;
		}
		
		protected void onPreExecute() {
			// TODO remove hardcoded text
			mProgressDialog = ProgressDialog.show(FileInfoWithUploadActivity.this, 
					"", "Uploading file. Please wait...", false, true);
		}
		
		protected Void doInBackground(Void... params) {
			// TODO do upload in background
			// TODO FileUploader.uploadAsync
			int cnt = 0;
			while (true) { // uploader.isCompleted
				try {
					// TODO make this configurable
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				cnt += 10;
				if (cnt >= 100) break;
				publishProgress(cnt);
			}
			// publishProgress(filename);
			
			return null; // TODO return success/failure of the upload
		}

		protected void onProgressUpdate(Integer...progress) {
			mProgressDialog.setProgress(progress[0]);
		}

		protected void onPostExecute(Void result) {
			mProgressDialog.cancel();
		}
	}
	*/
}
