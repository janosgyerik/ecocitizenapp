package com.ecocitizen.app.test;

import com.ecocitizen.app.TreeViewActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class SanityTest extends ActivityInstrumentationTestCase2<TreeViewActivity> {

	private TreeViewActivity mActivity;
	private TextView mView;

	public SanityTest(String pkg, Class<TreeViewActivity> activityClass) {
		super(pkg, activityClass);
	}
	
	public SanityTest() {
		this("com.ecocitizen.app", TreeViewActivity.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
		mView = (TextView) mActivity.findViewById(com.ecocitizen.app.R.id.co2val);
	}
	
	public void testPreconditions() {
		assertNotNull(mView);
	}

}
