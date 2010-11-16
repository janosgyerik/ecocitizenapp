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
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
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

	private static final String MSG_COMING_SOON = "Coming soon ..."; // TODO

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		setContentView(R.layout.fileuploader);

		//createDummyInternalFile();
		//createDummyExternalFile();

		showInternalFiles();
		showExternalFiles();

		Button btnUploadAll = (Button) findViewById(R.id.btn_upload_all);
		btnUploadAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(FileUploaderActivity.this, MSG_COMING_SOON, Toast.LENGTH_LONG).show();
			}
		});

		// TODO if the lists are empty, show message and cancel button
	}

	void showInternalFiles() {
		ListView listView = (ListView) findViewById(R.id.internal_storage);
		listView.setOnItemClickListener(this);

		ArrayAdapter<String> filesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		listView.setAdapter(filesArrayAdapter);

		String[] fileList = fileList();
		if (fileList.length > 0) {
			for (String filename : fileList()) {
				filesArrayAdapter.add(filename);
			}
		}
		else {
			filesArrayAdapter.add(getString(R.string.label_none));
		}
	}

	void showExternalFiles() {
		ListView listView = (ListView) findViewById(R.id.external_storage);
		listView.setOnItemClickListener(this);

		ArrayAdapter<String> filesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		listView.setAdapter(filesArrayAdapter);

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
				filesArrayAdapter.add(filename);
			}
		}
		else {
			filesArrayAdapter.add(getString(R.string.label_none));
		}
	}

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
