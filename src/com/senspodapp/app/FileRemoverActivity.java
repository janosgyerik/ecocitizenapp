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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.senspodapp.service.FileSaverService;

// TODO: move the common stuff to a FileManager class or something.
public class FileRemoverActivity extends Activity {
	// Debugging
	private static final String TAG = "FileRemoverActivity";
	private static final boolean D = true;
	
	private List<String> internalFilenames;
	private List<String> externalFilenames;
	private static String externalDirname;
	private static File externalDir;
	static {
		externalDirname = String.format(
				"%s/%s",
				Environment.getExternalStorageDirectory(),
				FileSaverService.EXTERNAL_TARGETDIR
		);
		externalDir = new File(externalDirname);
	}
	
	public ArrayAdapter<String> externalFilesArrayAdapter;
	public ArrayAdapter<String> internalFilesArrayAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.fileremover);

		buildFilenameLists();
		createDummyFiles();

		if (internalFilenames.isEmpty() && externalFilenames.isEmpty()) {
			new AlertDialog.Builder(FileRemoverActivity.this)
			.setMessage(R.string.msg_no_files)
			.setCancelable(false)
			.setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FileRemoverActivity.this.finish();
				}
			})
			.show();
		}

		internalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		externalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		
		showInternalFiles();
		showExternalFiles();
		
		((ListView)findViewById(R.id.internal_storage))
		.setOnItemClickListener(new ItemDeleteListener(internalFilesArrayAdapter, getFilesDir()));
		
		((ListView)findViewById(R.id.external_storage))
		.setOnItemClickListener(new ItemDeleteListener(externalFilesArrayAdapter, externalDir));

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
	}
	
	class DeleteAllAsyncTask extends AsyncTask<Void, String, Void> {
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		List<String> mFilenames;
		int mDeleteBtnId;
		
		public DeleteAllAsyncTask(
				ArrayAdapter<String> filesArrayAdapter, 
				File basedir, 
				List<String> filenames,
				int deleteBtnId) {
			mFilesArrayAdapter = filesArrayAdapter;
			mBasedir = basedir;
			mFilenames = filenames;
			mDeleteBtnId = deleteBtnId;			
		}
		
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			findViewById(mDeleteBtnId).setEnabled(false);
		}
		
		protected Void doInBackground(Void... params) {
			for (String filename : mFilenames) {
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
	
	class ItemDeleteListener implements OnItemClickListener {
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		
		public ItemDeleteListener(ArrayAdapter<String> filesArrayAdapter, File basedir) {
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
	
	private void createDummyFiles() {
		int minDummyFiles = getResources().getInteger(R.integer.min_dummy_files);
		
		int i = 0;
		while (internalFilenames.size() < minDummyFiles) {
			if (!createDummyInternalFile(++i)) break;
		}
		
		if (isExternalStorageWritable()) {
			i = 0;
			while (externalFilenames.size() < minDummyFiles) {
				if (!createDummyExternalFile(++i)) break;
			}
		}
	}
	
	private boolean createDummyInternalFile(int index) {
		String datestr = FileSaverService.DATEFORMAT.format(new Date());
		String filename;
		filename = String.format(
				"%s%s-%d.%s",
				FileSaverService.FILENAME_PREFIX,
				datestr,
				index,
				FileSaverService.FILENAME_EXTENSION
		);

		try {
			FileOutputStream output = openFileOutput(filename, Context.MODE_PRIVATE);
			output.close();
			internalFilenames.add(filename);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean isExternalStorageWritable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	private boolean createDummyExternalFile(int index) {
		if (!isExternalStorageWritable()) {
			Toast.makeText(FileRemoverActivity.this, "No external storage or not writable", Toast.LENGTH_LONG).show();
			return false;
		}

		String datestr = FileSaverService.DATEFORMAT.format(new Date());
		String filename = String.format(
				"%s%s-%d.%s",
				FileSaverService.FILENAME_PREFIX,
				datestr,
				index,
				FileSaverService.FILENAME_EXTENSION
		);
		String filepath = externalDirname + "/" + filename;

		try {
			File file = new File(filepath);
			if (!file.exists()) {
				if (file.createNewFile()) {
					externalFilenames.add(filename);
					return true;
				}
				else {
					Toast.makeText(FileRemoverActivity.this, "Could not create file...", Toast.LENGTH_LONG).show();
				}
			}
			else {
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private void buildFilenameLists() {
		internalFilenames = new LinkedList<String>();
		for (String filename : fileList()) {
			if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
					&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) { 
				internalFilenames.add(filename);
			}
		}
		
		externalFilenames = new LinkedList<String>();
		if (externalDir.isDirectory()) {
			for (File file : externalDir.listFiles()) {
				String filename = file.getName();
				if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
						&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) { 
					externalFilenames.add(file.getName());
				}
			}
		}
	}

	void showInternalFiles() {
		ListView internalListView = (ListView) findViewById(R.id.internal_storage);
		internalListView.setAdapter(internalFilesArrayAdapter);

		if (!internalFilenames.isEmpty()) {
			for (String filename : internalFilenames) {
				internalFilesArrayAdapter.add(filename);
			}
		}
		else {
			internalFilesArrayAdapter.add(getString(R.string.label_none));
		}
	}

	void showExternalFiles() {
		ListView externalListView = (ListView) findViewById(R.id.external_storage);
		externalListView.setAdapter(externalFilesArrayAdapter);

		if (!externalFilenames.isEmpty()) {
			for (String filename : externalFilenames) {
				externalFilesArrayAdapter.add(filename);
			}
		}
		else {
			externalFilesArrayAdapter.add(getString(R.string.label_none));
		}
	}	
}