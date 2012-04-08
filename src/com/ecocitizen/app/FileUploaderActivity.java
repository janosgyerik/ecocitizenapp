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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ecocitizen.common.DebugFlagManager;

public class FileUploaderActivity extends FileManagerActivity {
	// Debugging
	private static final String TAG = "FileUploaderActivity";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(FileUploaderActivity.class);
	
	enum StorageType {
		INTERNAL,
		EXTERNAL
	}

	// Members
	private StorageType mCurrentStorageType;
	private File mCurrentFile;
	
	@Override
	protected int getLayoutResID() {
		return R.layout.fileuploader;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		ListView internalFilesListView = (ListView)findViewById(R.id.internal_storage);
		internalFilesListView.setOnItemClickListener(new ItemClickListener(StorageType.INTERNAL, internalFilesArrayAdapter, getFilesDir()));

		ListView externalFilesListView = (ListView)findViewById(R.id.external_storage);
		externalFilesListView.setOnItemClickListener(new ItemClickListener(StorageType.EXTERNAL, externalFilesArrayAdapter, externalDir));
		
		findViewById(R.id.btn_close).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	class ItemClickListener implements OnItemClickListener {
		StorageType mStorageType;
		ArrayAdapter<String> mFilesArrayAdapter;
		File mBasedir;
		
		public ItemClickListener(StorageType storageType, ArrayAdapter<String> filesArrayAdapter, File basedir) {
			mStorageType = storageType;
			mFilesArrayAdapter = filesArrayAdapter;
			mBasedir = basedir;
		}
		
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
			ListView listView = (ListView) parent;
			String filename = (String) listView.getItemAtPosition(position);
			
			// these will be used when the FileInfo activity returns
			mCurrentStorageType = mStorageType;
			mCurrentFile = new File(mBasedir + "/" + filename);
			
			Intent intent = new Intent(FileUploaderActivity.this, FileInfoWithUploadActivity.class);
			Bundle params = new Bundle();
			params.putString(FileInfoActivity.BUNDLEKEY_FILENAME, mBasedir + "/" + filename);
			intent.putExtras(params);
			startActivityForResult(intent, mStorageType.ordinal());
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!mCurrentFile.exists()) { 
			if (mCurrentStorageType == StorageType.INTERNAL) {
				internalFilesArrayAdapter.remove(mCurrentFile.getName());
			}
			else if (mCurrentStorageType == StorageType.EXTERNAL) {
				externalFilesArrayAdapter.remove(mCurrentFile.getName());
			}
		}
	}
}
