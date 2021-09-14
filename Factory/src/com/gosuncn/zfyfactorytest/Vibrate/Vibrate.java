/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Vibrate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;

public class Vibrate extends BaseActivity {

	private Handler mHandler = new Handler();
	private final long VIBRATOR_ON_TIME = 1000;
	private final long VIBRATOR_OFF_TIME = 500;
	String TAG = "Vibrate";
	private static Context mContext;
	Vibrator mVibrator = null;
	long[] pattern = { VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME };

	@Override
	public void finish() {

		super.finish();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.vibrate);
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if(SHOW_RESULT_DIALOG)
			showDialog();

	}

	@Override
	protected void onPause() {

		mHandler.removeCallbacks(mRunnable);
		mVibrator.cancel();
		super.onPause();
	}

	@Override
	protected void onResume() {

		mHandler.postDelayed(mRunnable, 0);
		super.onResume();
	}

	@Override
	protected void onPositiveCallback() {
		mPositiveListener.onClick(null, 0);
	}

	@Override
	protected void onNegativeCallback() {
		mNegativeListener.onClick(null, 0);
	}

	private Runnable mRunnable = new Runnable() {

		public void run() {

			mHandler.removeCallbacks(mRunnable);
			mVibrator.vibrate(pattern, 0);
		}
	};

	private DialogInterface.OnClickListener mPositiveListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialoginterface, int i) {

			setResult(RESULT_OK);
			Utils.writeCurMessage(mContext, TAG, "Pass");
			finish();
		}
	};
	private DialogInterface.OnClickListener mNegativeListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialoginterface, int i) {

			setResult(RESULT_CANCELED);
			Utils.writeCurMessage(mContext, TAG, "Failed");
			finish();
		}
	};

	private void showDialog() {

		new AlertDialog.Builder(this).setTitle(R.string.vibrate_confirm)
				.setPositiveButton

				(R.string.pass, mPositiveListener).setNegativeButton

				(R.string.fail, mNegativeListener).setCancelable(false).show();

	}

	@SuppressWarnings("unused")
	private void loge(Object e) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	@SuppressWarnings("unused")
	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}

}
