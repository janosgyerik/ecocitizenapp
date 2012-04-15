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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ecocitizen.service.FileSaverService;

abstract class FileManagerActivity extends Activity {
	// Debugging
	private static final String TAG = "FileManagerActivity";
	private static final boolean D = false;
	
	protected List<String> internalFilenames;
	protected List<String> externalFilenames;
	protected static String externalDirname;
	protected static File externalDir;
	static {
		externalDirname = String.format(
				"%s/%s",
				Environment.getExternalStorageDirectory(),
				FileSaverService.EXTERNAL_TARGETDIR
		);
		externalDir = new File(externalDirname);
	}
	
	protected ArrayAdapter<String> externalFilesArrayAdapter;
	protected ArrayAdapter<String> internalFilesArrayAdapter;

	abstract protected int getLayoutResID();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(getLayoutResID());

		buildFilenameLists();
		createDummyFiles();

		if (internalFilenames.isEmpty() && externalFilenames.isEmpty()) {
			findViewById(R.id.msg_no_files).setVisibility(View.VISIBLE);
		}
		else {
			if (!internalFilenames.isEmpty()) {
				internalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
				configureArrayAdapter(internalFilesArrayAdapter, internalFilenames);
				ListView internalFilesListView = (ListView)findViewById(R.id.internal_storage);
				internalFilesListView.setAdapter(internalFilesArrayAdapter);
				findViewById(R.id.internal_storage_section).setVisibility(View.VISIBLE);
			}

			if (!externalFilenames.isEmpty()) {
				externalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
				configureArrayAdapter(externalFilesArrayAdapter, externalFilenames);
				ListView externalFilesListView = (ListView)findViewById(R.id.external_storage);
				externalFilesListView.setAdapter(externalFilesArrayAdapter);
				findViewById(R.id.external_storage_section).setVisibility(View.VISIBLE);
			}
		}
	}
	
	protected void buildFilenameLists() {
		buildInternalFilenamesList();
		buildExternalFilenamesList();
	}
	
	private void buildInternalFilenamesList() {
		internalFilenames = new LinkedList<String>();
		for (String filename : fileList()) {
			if ((filename.startsWith(FileSaverService.FILENAME_PREFIX) 
					|| filename.startsWith(FileSaverService.FILENAME_PREFIX_LEGACY)) 
					&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) { 
				internalFilenames.add(filename);
			}
		}
	}
	
	private void buildExternalFilenamesList() {
		externalFilenames = new LinkedList<String>();
		if (externalDir.isDirectory()) {
			for (File file : externalDir.listFiles()) {
				String filename = file.getName();
				if ((filename.startsWith(FileSaverService.FILENAME_PREFIX) 
						|| filename.startsWith(FileSaverService.FILENAME_PREFIX_LEGACY)) 
						&& filename.endsWith(FileSaverService.FILENAME_EXTENSION)) { 
					externalFilenames.add(file.getName());
				}
			}
		}
	}
	
	void configureArrayAdapter(ArrayAdapter<String> arrayAdapter, List<String> filenames) {
		for (String filename : filenames) {
			arrayAdapter.add(filename);
		}
		
		if (filenames.isEmpty()) {
			arrayAdapter.add(getString(R.string.label_none));
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
			Toast.makeText(FileManagerActivity.this, "No external storage or not writable", Toast.LENGTH_LONG).show();
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
					Toast.makeText(FileManagerActivity.this, "Could not create file...", Toast.LENGTH_LONG).show();
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
