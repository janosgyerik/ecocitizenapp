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
import java.io.FileFilter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FileUploaderActivity extends Activity implements OnItemClickListener{

	private ListView internalListView;
	private ListView externallListView;
	private File internalDir;
	private File externalDir;
	private Button mButton;
	private static final String msg = "Coming soon ...";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fileuploader);

		internalDir = getFilesDir();
		externalDir = Environment.getExternalStorageDirectory();
		
		mButton = (Button) findViewById(R.id.btn_uploader);
		mButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(FileUploaderActivity.this, msg, Toast.LENGTH_LONG).show();
			}
		});
		
		internalListView = (ListView) findViewById(R.id.internalFilelist);
		internalListView.setOnItemClickListener(this);
		updateList(internalDir,internalListView);
	
		externallListView = (ListView) findViewById(R.id.externalFilelist);
		externallListView.setOnItemClickListener(this);
		updateList(externalDir,externallListView);

	}


	final private FileFilter filter = new FileFilter() {
		public boolean accept(File pathname) {
			if(pathname.getName().startsWith("session") == true){
				return true;
			}
			return false;
		}
	};

	private void updateList(File dir,ListView listView){
		ArrayAdapter<String> mfilesAdapter = new ArrayAdapter<String>(this, R.layout.message);
		if(dir.listFiles(filter)!=null&&dir.listFiles(filter).length != 0){
			File[] files = dir.listFiles(filter);
			for(File file : files){
				mfilesAdapter.add(file.getName());
			}
		}
		else{
			String noFiles = getResources().getText(R.string.File_Saver_None).toString();
			mfilesAdapter.add(noFiles);
		}
		listView.setAdapter(mfilesAdapter);
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Toast.makeText(FileUploaderActivity.this, msg, Toast.LENGTH_LONG).show();
	}

}
