package com.senspodapp.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.senspodapp.service.FileSaverService;

abstract class FileManagerActivity extends Activity {
	// Debugging
	private static final String TAG = "FileManagerActivity";
	private static final boolean D = true;
	
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

		if (internalFilenames.isEmpty() && externalFilenames.isEmpty()) {
			new AlertDialog.Builder(FileManagerActivity.this)
			.setMessage(R.string.msg_no_files)
			.setCancelable(false)
			.setPositiveButton(R.string.btn_close, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FileManagerActivity.this.finish();
				}
			})
			.show();
		}

		internalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		configureArrayAdapter(internalFilesArrayAdapter, internalFilenames);
		ListView internalFilesListView = (ListView)findViewById(R.id.internal_storage);
		internalFilesListView.setAdapter(internalFilesArrayAdapter);

		externalFilesArrayAdapter = new ArrayAdapter<String>(this, R.layout.filename);
		configureArrayAdapter(externalFilesArrayAdapter, externalFilenames);
		ListView externalFilesListView = (ListView)findViewById(R.id.external_storage);
		externalFilesListView.setAdapter(externalFilesArrayAdapter);
	}
	
	protected void buildFilenameLists() {
		buildInternalFilenamesList();
		buildExternalFilenamesList();
	}
	
	private void buildInternalFilenamesList() {
		internalFilenames = new LinkedList<String>();
		for (String filename : fileList()) {
			if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
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
				if (filename.startsWith(FileSaverService.FILENAME_PREFIX) 
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
	
	// TODO
	int getFileRecordCount(File basedir, String filename) {
		String path = basedir + "/" + filename;
		File file = new File(path);
		int count = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					++count;
					if (line.indexOf("_S") > -1 || line.indexOf("_G") > -1) {
						int start = -1;
						while ((start = line.indexOf("_S", start + 1)) > -1 
								|| ((start = line.indexOf("_G", start + 1)) > -1)) {
							++count;
						}
					}
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return count;
	}
}