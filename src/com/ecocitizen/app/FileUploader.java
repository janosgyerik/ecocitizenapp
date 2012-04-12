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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ecocitizen.common.HttpHelper;
import com.ecocitizen.common.HttpHelper.Status;
import com.ecocitizen.common.bundlewrapper.SummaryBundleWrapper;
import com.ecocitizen.service.FileSaverService;

public class FileUploader {
	// Debugging
	private static final String TAG = "FileUploader";
	private static final boolean D = false;

	private final HttpHelper mHttpHelper; 
	private final File mFile;
	
	private boolean mCancelRequested = false;
	
	public FileUploader(HttpHelper httpHelper, File file) {
		mHttpHelper = httpHelper;
		mFile = file;
	}
	
	boolean waitForSendHttpHead(String line) {
		/* TODO skip $GPRMC sentences
		 * Do not upload GPS sentences.
		 * This is not a very good thing to do (not clean).
		 * But, the thing is, GPS sentences are kind of useless,
		 * because GPS information is attached anyway using Android's
		 * own GPS, which in our experience so far is better than
		 * the GPS of sensors. So, these sentences are useless,
		 * and just take up unnecessary bandwidth.
		 * In the long term however, this kind of hard coding
		 * should be controllable by advanced settings screen or something.
		 */
		if (line.indexOf(",$GP") > -1) return true;
		
		return mHttpHelper.sendStore(line);
	}

	public Status upload() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));
			try {
				String line = reader.readLine();
				if (line != null) {
					if (!mHttpHelper.isServerReachable()) return mHttpHelper.getLastStatus(); 
					
					if (!mHttpHelper.isLoginOK()) return mHttpHelper.getLastStatus();
					
					if (!mHttpHelper.isStartSessionOK()) return mHttpHelper.getLastStatus();
					
					String summary = mFile.getName()
						.replace(FileSaverService.FILENAME_PREFIX, "")
						.replace("." + FileSaverService.FILENAME_EXTENSION, "");
					waitForSendHttpHead(SummaryBundleWrapper.formatMessage(summary));
				}
				else {
					return mHttpHelper.getLastStatus();
				}
				do {
					line = line.replace(" ", "");
					if (!waitForSendHttpHead(line)) {
						return mHttpHelper.getLastStatus();
					}
				}
				while ((line = reader.readLine()) != null && ! mCancelRequested);
				
				if (mCancelRequested) {
					return mHttpHelper.getLastStatus();
				}
				
				mHttpHelper.sendEndSession();
			} 
			catch (IOException e) {
				e.printStackTrace();
				return Status.EXCEPTION;
			}
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return Status.EXCEPTION;
		}

		return Status.SUCCESS;
	}

	public void cancel() {
		mCancelRequested = true;
		mHttpHelper.cancel();
	}
}
