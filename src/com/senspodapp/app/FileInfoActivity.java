package com.senspodapp.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class FileInfoActivity extends Activity {
	// Debugging
	private static final String TAG = "FileInfoActivity";
	private static final boolean D = true;
	
	// Constants
	public static final String BUNDLEKEY_FILENAME = "fn";
	
	static final int PREVIEWLINES = 3; // first N and last N lines will be shown in preview
	
	static final String[] ZZBYTEMARKER = new String[]{ "", "kb", "mb", "gb" };
	static final DecimalFormat ZZBYTEFORMAT = new DecimalFormat("#.#"); // max 1 fraction digit
	
	public static String bytesToString(long bytes) {
		double val = bytes;
		int i = 0;
		for (; i < ZZBYTEMARKER.length && val > 1000; ++i) val /= 1000;
		return ZZBYTEFORMAT.format(val) + ZZBYTEMARKER[i];
	}
	
	public final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy MMM dd HH:mm");

	public static String timeToString(long time) {
		return DATEFORMAT.format(new Date(time));
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");
		
		setContentView(R.layout.fileinfo);
		
		String filename = getIntent().getExtras().getString(BUNDLEKEY_FILENAME);
		File file = new File(filename);
		((TextView)findViewById(R.id.fileinfo_filename)).setText(file.getName());
		((TextView)findViewById(R.id.fileinfo_size)).setText(bytesToString(file.length()));
		((TextView)findViewById(R.id.fileinfo_date)).setText(timeToString(file.lastModified()));
		int recordnum = getRecordNum(file);
		((TextView)findViewById(R.id.fileinfo_recordnum)).setText(String.valueOf(recordnum));
		((TextView)findViewById(R.id.fileinfo_content)).setText(getContentPreview(file, recordnum));

		findViewById(R.id.btn_close).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	public static int getRecordNum(File file) {
		int cnt = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				while (reader.readLine() != null) {
					++cnt;
				}
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
		}
		return cnt;
	}
	
	public static String getContentPreview(File file, int recordnum) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			try {
				int cnt = 0;
				StringBuffer buffer1 = new StringBuffer();
				StringBuffer buffer2 = new StringBuffer();
				String line;
				while ((line = reader.readLine()) != null) {
					if (cnt < PREVIEWLINES) buffer1.append(line + "\n");
					else if (cnt >= recordnum - PREVIEWLINES) buffer2.append(line + "\n");
					++cnt;
				}
				if (recordnum > PREVIEWLINES * 2) {
					buffer1.append("...\n");
				}
				return buffer1.toString() + buffer2.toString();
			} catch (IOException e) {
			}
		} catch (FileNotFoundException e) {
		}
		return "";
	}
}
