/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Flashlight;

import static com.gosuncn.zfyfactorytest.Values.HasFlashlightFile;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfactorytest.Framework.MainApp;

public class Flashlight extends BaseActivity {

	private static Context mContext;
	String TAG = "Flashlight";
	Camera mycam = null;
	private CameraManager cameraManager;//wmd
	Parameters camerPara = null;
	final byte[] ON = { '1'};
	final byte[] OFF = { '0' };
	private static String deviceNode = "/sys/class/flashlight/flash_led";

	@Override
	public void finish() {

		super.finish();
	}

	private void init(Context context) {

		setResult(RESULT_CANCELED);
		mContext = context;

		int index = getIntent().getIntExtra(Values.KEY_SERVICE_INDEX, -1);
		if (index >= 0) {

			Map<String, ?> item = (Map<String, ?>) MainApp.getInstance().mItemList
					.get(index);
			HashMap<String, String> paraMap = (HashMap<String, String>) item
					.get("parameter");
			String device = paraMap.get("path");
			if (device != null)
				deviceNode = device;
		}

		setContentView(loadDefaultConfirmText(getResources().getString(R.string.flashlight_confirm)));
	}

	void enableDevice(String fileNode, boolean enable) {
		FileOutputStream fileOutputStream;
		try {

			fileOutputStream = new FileOutputStream(fileNode);
			if (enable)
				fileOutputStream.write(ON);
			else
				fileOutputStream.write(OFF);
			fileOutputStream.close();

		} catch (Exception e) {
			loge(e);
		}
	}

	@Override
	protected void onDestroy() {
			enableDevice(deviceNode, false);

		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		init(getApplicationContext());

		//wmd
/*		if (!HasFlashlightFile) {
			mycam = Camera.open();
			camerPara = mycam.getParameters();
			camerPara.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mycam.setParameters(camerPara);
		} else {

			enableDevice(deviceNode, true);

		}*/
		if(SHOW_RESULT_DIALOG) {
			showDialog(Flashlight.this);
		}
	}

	// wmd
//	private void enableFlash(boolean state){
//		try {
//			cameraManager.setTorchMode("0", state);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}

	@Override
	protected void onResume() {
		super.onResume();
		enableDevice(deviceNode,true);
		Log.d(TAG,"enableDevice(true) success onresume");

	}

	@Override
	protected void onPositiveCallback() {
		mPositiveLisnener.onClick(null, 0);
	}

	@Override
	protected void onNegativeCallback() {
		mNegativeLisnener.onClick(null, 0);
	}

	protected void onPause() {
		super.onPause();
		enableDevice(deviceNode,false);
		Log.d(TAG,"enableDevice(false) success onPause");
	}

	DialogInterface.OnClickListener mPositiveLisnener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog,
							int which) {

			setResult(RESULT_OK);
			Utils.writeCurMessage(mContext, TAG, "Pass");
			finish();
		}
	};

	DialogInterface.OnClickListener mNegativeLisnener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog,
							int which) {

			setResult(RESULT_CANCELED);
			Utils.writeCurMessage(mContext, TAG, "Failed");
			finish();
		}
	};

	void showDialog(final Flashlight fl) {

		new AlertDialog.Builder(fl)
				.setTitle(getString(R.string.flashlight_confirm))
				.setPositiveButton(getString(R.string.yes), mPositiveLisnener)
				.setNegativeButton(getString(R.string.no), mNegativeLisnener).setCancelable(false).show();
	}

	private void loge(Object e) {

		if (e == null)
			return;
		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}
}
