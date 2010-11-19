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
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
	public ArrayAdapter<String> externalFilesArrayAdapter;
    public ArrayAdapter<String> internalFilesArrayAdapter;
    private ListView externallistView;
    private ListView internallistView;
	private static final String MSG_COMING_SOON = "Coming soon ..."; // TODO
	private static final long WAIT_TIME = 400;
    private static final int INTERNAL_TYPE = 1 ;
    private static final int EXTERNAL_TYPE = 2;
    private static final int BACK_DEFAULT = 3;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.fileuploader);

		//createDummyInternalFile();
		//createDummyExternalFile();

		externalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		internalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		externallistView = (ListView) findViewById(R.id.external_storage);
		internallistView = (ListView) findViewById(R.id.internal_storage);
		
		showInternalFiles();
		showExternalFiles();

		Button btnUploadAll = (Button) findViewById(R.id.btn_upload_all);
		btnUploadAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(FileUploaderActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
			}
		});

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_delete_all)
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
				builder.show();
			}
		});
		
		
		
		// TODO if the lists are empty, show message and cancel button
	}

	void showInternalFiles() {
		
		internallistView.setOnItemClickListener(this);

		internallistView.setAdapter(internalFilesArrayAdapter);

		String[] fileList = fileList();
		if (fileList.length > 0) {
			for (String filename : fileList()) {
				internalFilesArrayAdapter.add(filename);
			}
		}
		else {
			internalFilesArrayAdapter.add(getString(R.string.label_none));
		}
	}

	void showExternalFiles() {
		
		externallistView.setOnItemClickListener(this);

		externallistView.setAdapter(externalFilesArrayAdapter);

		String basedirPath = String.format(
				"%s/%s",
				Environment.getExternalStorageDirectory(),
				FileSaverService.EXTERNAL_TARGETDIR
		);

		List<String> filenames = new LinkedList<String>();

		File basedir = new File(basedirPath);
		if (basedir.isDirectory()) {
			for (File file : basedir.listFiles()) {
				String filename = file.getName();
				if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
						&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) { 
					filenames.add(file.getName());
				}
			}
		}

		if (!filenames.isEmpty()) {
			for (String filename : filenames) {
				externalFilesArrayAdapter.add(filename);
			}
		}
		else {
			externalFilesArrayAdapter.add(getString(R.string.label_none));
		}
	}

	class deleteTask extends AsyncTask<Handler ,String ,Void>{
		private Handler mhandler;
		protected Void doInBackground(Handler... params) {
			mhandler = params[0];
			String[] fileList = fileList();
			if (fileList.length > 0) {
				for (String filename : fileList()) {
					deleteFile(filename);
					publishProgress(String.valueOf(INTERNAL_TYPE),filename);
					SystemClock.sleep(WAIT_TIME);
				}
			}

			String basedirPath = String.format(
					"%s/%s",
					Environment.getExternalStorageDirectory(),
					FileSaverService.EXTERNAL_TARGETDIR
			);

			File basedir = new File(basedirPath);
			if (basedir.isDirectory()) {
				for (File file : basedir.listFiles()) {
					String filename = file.getName();
					if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
							&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) { 
						File fileDelete = new File(basedirPath,filename); 
						if (fileDelete.exists()) fileDelete.delete();
						publishProgress(String.valueOf(EXTERNAL_TYPE),filename);
						SystemClock.sleep(WAIT_TIME);
					}
				}
			}
			publishProgress(String.valueOf(BACK_DEFAULT),null);
			return null;
		}

		protected void onProgressUpdate(String...progress){
			mhandler.obtainMessage(Integer.parseInt(progress[0]), progress[1]).sendToTarget();
		}
		
	}
	
	void deleteAll(){
		setProgressBarIndeterminateVisibility(true);
		Button btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
		btnDeleteAll.setVisibility(View.GONE);
		new deleteTask().execute(handler);		
	}
	
	final Handler handler = new Handler() {
		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INTERNAL_TYPE:
				internalFilesArrayAdapter.remove((String)msg.obj);
				internallistView.setAdapter(internalFilesArrayAdapter);
				break;
			case EXTERNAL_TYPE:
				externalFilesArrayAdapter.remove((String)msg.obj);
				externallistView.setAdapter(externalFilesArrayAdapter);
				break;
			case BACK_DEFAULT:
				setProgressBarIndeterminateVisibility(false);
				Button btnDeleteAll = (Button) findViewById(R.id.btn_delete_all);
				btnDeleteAll.setVisibility(View.VISIBLE);
				if(internalFilesArrayAdapter.isEmpty())
				internalFilesArrayAdapter.add(getString(R.string.label_none));
				if(externalFilesArrayAdapter.isEmpty())
				externalFilesArrayAdapter.add(getString(R.string.label_none));
				break;
			default:
				break;
			}
		}
	};
	private void createDummyInternalFile() {
		String datestr = "DTZ";
		String filename;
		filename = String.format(
				"%s%s.%s",
				FileSaverService.FILENAME_PREFIX,
				datestr,
				FileSaverService.FILENAME_EXTENSION
		);

		try {
			FileOutputStream output = openFileOutput(filename, Context.MODE_PRIVATE);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void createDummyExternalFile() {
		String datestr = "DTZ";
		String filename;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			filename = String.format(
					"%s/%s/%s%s.%s",
					Environment.getExternalStorageDirectory(),
					FileSaverService.EXTERNAL_TARGETDIR,
					FileSaverService.FILENAME_PREFIX,
					datestr,
					FileSaverService.FILENAME_EXTENSION
			);
		} 
		else {
			Toast.makeText(FileUploaderActivity.this, "No external storage", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			File file = new File(filename);
			if (!file.exists()) {
				if (file.createNewFile()) {
				}
				else {
					Toast.makeText(FileUploaderActivity.this, "Could not create file...", Toast.LENGTH_LONG).show();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Toast.makeText(FileUploaderActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
	}
}
