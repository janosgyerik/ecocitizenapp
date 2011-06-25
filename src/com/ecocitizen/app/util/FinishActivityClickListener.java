package com.ecocitizen.app.util;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class FinishActivityClickListener implements OnClickListener {
	
	private final Activity activity;
	
	public FinishActivityClickListener(Activity activity) {
		this.activity = activity;
	}

	public void onClick(View v) {
		activity.finish();
	}

}
