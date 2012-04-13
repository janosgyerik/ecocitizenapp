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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.ecocitizen.common.HttpHelper;

public class FileInfoWithUploadActivity extends FileInfoActivity {
	// Debugging
	private static final String TAG = "FileInfoWithUploadActivity";
	private static final boolean D = false;
	
	// Members
	private Button mBtnUpload;
	private Button mBtnDelete;
	
	private HttpHelper mHttpHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		mHttpHelper = new HttpHelper(this.getApplicationContext());
		
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
						finish();
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

	class UploadFileAsyncTask extends AsyncTask<Void, Integer, HttpHelper.Status> {
		private final FileUploader mFileUploader;
		
		ProgressDialog mProgressDialog;
		ProgressDialog mCancelProgressDialog = null;
		boolean mCompleted = false;
		
		public UploadFileAsyncTask(FileUploader fileUploader) {
			mFileUploader = fileUploader;
		}
		
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(FileInfoWithUploadActivity.this, "",
					FileInfoWithUploadActivity.this.getString(R.string.msg_uploading), false, true, 
					new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							if (! mCompleted) {
								mFileUploader.cancel();
								mCancelProgressDialog = ProgressDialog.show(FileInfoWithUploadActivity.this, "", 
										FileInfoWithUploadActivity.this.getString(R.string.fileuploader_msg_upload_canceling), false, false);
							}
						}
			});
		}
		
		protected HttpHelper.Status doInBackground(Void... params) {
			return mFileUploader.upload();
		}

		protected void onPostExecute(HttpHelper.Status status) {
			mCompleted = true;
			if (mProgressDialog.isShowing()) mProgressDialog.cancel();
			if (mCancelProgressDialog != null) mCancelProgressDialog.cancel();
			
			if (status == HttpHelper.Status.SUCCESS) {
				mBtnUpload.setVisibility(View.GONE);
				Toast.makeText(FileInfoWithUploadActivity.this, R.string.msg_upload_success, Toast.LENGTH_SHORT).show();
				return;
			}
			
			int msgID;
			switch (status) {
			case SUCCESS:
				return;
			/*
			case EMPTY_FILE:
				msgID = R.string.fileuploader_msg_empty_file;
				Toast.makeText(FileInfoWithUploadActivity.this, msgID, Toast.LENGTH_SHORT).show();
				mBtnUpload.setVisibility(View.GONE);
				return;
				*/
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
			case INTERRUPTED:
				msgID = R.string.fileuploader_msg_upload_interrupted;
				break;
			case CANCELLED:
				msgID = R.string.fileuploader_msg_upload_cancelled;
				break;
			default:
				msgID = R.string.fileuploader_msg_unknown_error;
			}
			Toast.makeText(FileInfoWithUploadActivity.this, msgID, Toast.LENGTH_LONG).show();
		}
	}
	
	private void performUploadFile() {
		new UploadFileAsyncTask(new FileUploader(mHttpHelper, mFile)).execute();
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
}
