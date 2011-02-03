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

import com.ecocitizen.app.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FileRemoverActivity extends FileManagerActivity {
	// Debugging
	private static final String TAG = "FileRemoverActivity";
	private static final boolean D = true;
	
	@Override
	protected int getLayoutResID() {
		return R.layout.fileremover;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		ListView internalFilesListView = (ListView)findViewById(R.id.internal_storage);
		internalFilesListView.setOnItemClickListener(new ItemClickListener(internalFilesArrayAdapter, getFilesDir()));

		ListView externalFilesListView = (ListView)findViewById(R.id.external_storage);
		externalFilesListView.setOnItemClickListener(new ItemClickListener(externalFilesArrayAdapter, externalDir));
		
		findViewById(R.id.btn_delete_all_internal).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(FileRemoverActivity.this)
				.setMessage(R.string.msg_are_you_sure)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new DeleteAllAsyncTask(
								internalFilesArrayAdapter, 
								getFilesDir(), 
								internalFilenames, 
								R.id.btn_delete_all_internal
								).execute();
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

		findViewById(R.id.btn_delete_all_external).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(FileRemoverActivity.this)
				.setMessage(R.string.msg_are_you_sure)
				.setCancelable(true)
				.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new DeleteAllAsyncTask(
								externalFilesArrayAdapter, 
								externalDir,
								externalFilenames, 
								R.id.btn_delete_all_external
								).execute();
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
	
	class DeleteAllAsyncTask extends AsyncTask<Void, String, Void> {
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		int mDeleteBtnId;
		
		public DeleteAllAsyncTask(
				ArrayAdapter<String> filesArrayAdapter, 
				File basedir, 
				List<String> filenames,
				int deleteBtnId) {
			mFilesArrayAdapter = filesArrayAdapter;
			mBasedir = basedir;
			mDeleteBtnId = deleteBtnId;			
		}
		
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			findViewById(mDeleteBtnId).setEnabled(false);
		}
		
		protected Void doInBackground(Void... params) {
			LinkedList<String> filenames = new LinkedList<String>();
			for (int i = 0; i < mFilesArrayAdapter.getCount(); ++i) {
				filenames.add(mFilesArrayAdapter.getItem(i));
			}
			for (String filename : filenames) {
				File file = new File(mBasedir + "/" + filename);
				if (file.isFile() && file.delete()) {
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
			findViewById(mDeleteBtnId).setEnabled(true);
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
				if (file.isFile() && file.delete()) { 
					mFilesArrayAdapter.remove(filename);
					if (mFilesArrayAdapter.isEmpty()) {
						mFilesArrayAdapter.add(getString(R.string.label_none));
					}
				}
			}
		}
	}	
}
