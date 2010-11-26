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
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.senspodapp.service.FileSaverService;

public class FileUploaderActivity extends Activity implements OnItemClickListener {
	// Debugging
	private static final String TAG = "FileUploaderActivity";
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

	private static final String MSG_COMING_SOON = "Coming soon ..."; // TODO

	private static final int INTERNAL_TYPE = 1 ;
	private static final int EXTERNAL_TYPE = 2;
	
	private AlertDialog.Builder mItemClickDialog;
	private static String deleteFileName;
	private static int deleteFrom;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.fileuploader);

		buildFilenameLists();
		createDummyFiles();

		externalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		internalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		
		showInternalFiles();
		showExternalFiles();

		Button btnUploadAll = (Button) findViewById(R.id.btn_upload_all);
		btnUploadAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(FileUploaderActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
			}
		});

		final AlertDialog.Builder deleteAllDialog = new AlertDialog.Builder(this);
		deleteAllDialog.setMessage(R.string.msg_are_you_sure)
		.setCancelable(true)
		.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				deleteAll();
			}
		})
		.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		Button btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
		btnDeleteAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				deleteAllDialog.show();
			}
		});
		
		final AlertDialog.Builder deleteSingleDialog = new AlertDialog.Builder(this);
		deleteSingleDialog.setMessage(R.string.msg_are_you_sure)
		.setCancelable(true)
		.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				deleteSingle();
			}
		})
		.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		mItemClickDialog = new AlertDialog.Builder(this);
		mItemClickDialog
		.setPositiveButton(R.string.btn_upload_single, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Toast.makeText(FileUploaderActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
			}
		})
		.setNegativeButton(R.string.btn_delete_single, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				deleteSingleDialog.show();
			}
		});
		
		// TODO if the lists are empty, show message and cancel button
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
		internalListView.setOnItemClickListener(this);
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
		externalListView.setOnItemClickListener(this);
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

	void deleteSingle() {
		if (deleteFrom == INTERNAL_TYPE) {
			deleteFile(deleteFileName);
			internalFilesArrayAdapter.remove(deleteFileName);
			if (internalFilesArrayAdapter.isEmpty()) {
				internalFilesArrayAdapter.add(getString(R.string.label_none));
			}
		} 
		else {
			File fileDelete = new File(externalDirname, deleteFileName); 
			fileDelete.delete();
			externalFilesArrayAdapter.remove(deleteFileName);
			if (externalFilesArrayAdapter.isEmpty()) {
				externalFilesArrayAdapter.add(getString(R.string.label_none));
			}
		}
	}
	
	
	class deleteTask extends AsyncTask<Void, String, Void> {
		protected Void doInBackground(Void... params) {
			String[] fileList = fileList();
			if (fileList.length > 0) {
				for (String filename : fileList()) {
					deleteFile(filename);
					publishProgress(String.valueOf(INTERNAL_TYPE), filename);
				}
			}
			
			if (isExternalStorageWritable()) {
				if (externalDir.isDirectory()) {
					for (File file : externalDir.listFiles()) {
						String filename = file.getName();
						if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
								&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) {
							file.delete();
							publishProgress(String.valueOf(EXTERNAL_TYPE), filename);
						}
					}
				}
			}
			
			return null;
		}

		protected void onProgressUpdate(String...progress) {
			if (progress[0].equals(String.valueOf(INTERNAL_TYPE))) {
				internalFilesArrayAdapter.remove(progress[1]);
				if (internalFilesArrayAdapter.isEmpty()) {
					internalFilesArrayAdapter.add(getString(R.string.label_none));
				}
			}
			else {
				externalFilesArrayAdapter.remove(progress[1]);
				if (externalFilesArrayAdapter.isEmpty()) {
					externalFilesArrayAdapter.add(getString(R.string.label_none));
				}
			}
		}

		protected void onPostExecute(Void result) {
			setProgressBarIndeterminateVisibility(false);
			Button btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
			btnDeleteAll.setEnabled(true);
		}
	}
	
	void deleteAll() {
		setProgressBarIndeterminateVisibility(true);
		Button btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
		btnDeleteAll.setEnabled(false);
		new deleteTask().execute();		
	}

	public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
		ListView listView = (ListView) parent;
		String item = (String) listView.getItemAtPosition(position);
		if (!item.endsWith(getString(R.string.label_none))) {
			deleteFileName = item;
			if (listView.equals((ListView) findViewById(R.id.internal_storage))) {
				deleteFrom = INTERNAL_TYPE; 
			}
			else {
				deleteFrom = EXTERNAL_TYPE; 
			}
			mItemClickDialog.show();
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
			Toast.makeText(FileUploaderActivity.this, "No external storage or not writable", Toast.LENGTH_LONG).show();
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
					Toast.makeText(FileUploaderActivity.this, "Could not create file...", Toast.LENGTH_LONG).show();
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
}
