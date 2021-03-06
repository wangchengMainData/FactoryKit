/*
 * Copyright (c) 2013-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.PSensor;

import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfactorytest.Framework.MainApp;

public class PSensorService extends Service {

	private final static String TAG = "PSensorService";
	private static boolean result = false;
	private static Context mContext = null;
	private static int index = -1;
	private static SensorManager PSensorManager = null;
	private static Sensor mPSensor = null;
	private static PSensorListener mPSensorListener;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private static String pre_value = INIT_VALUE;
	private final int MIN_COUNT = 2;
	private final static String i2C_CMD = "i2cdetect -y -r 0 0x48 0x48";
	private static boolean WORKROUND = false;
	private final static int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

	private final static Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			Bundle bundle = msg.getData();
			String output = (String) bundle.get(Values.KEY_OUTPUT);

			if (output.contains("--"))
				fail(null);
			else
				pass();
		};
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return -1;
		index = intent.getIntExtra(Values.KEY_SERVICE_INDEX, -1);
		if (index < 0)
			return -1;

		init(getApplicationContext());
		start();
		// finish();

		return super.onStartCommand(intent, flags, startId);
	}

	private void init(Context context) {
		mContext = context;
		result = false;

		PSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (PSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mPSensor = PSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mPSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mPSensorListener = new PSensorListener(this);

	}

	private void start() {
		logd("");
		if (WORKROUND) {
			Utils.exec(i2C_CMD, mHandler);
		} else {
			if (!PSensorManager.registerListener(mPSensorListener, mPSensor,
					SENSOR_DELAY)) {
				fail(getString(R.string.sensor_register_fail));
			}
		}

	}

	public static void finish(boolean writeResult) {

		if (writeResult) {
			Map<String, String> item = (Map<String, String>) MainApp
					.getInstance().mItemList.get(index);
			if (result) {
				item.put("result", Utils.RESULT_PASS);
				Utils.saveStringValue(mContext, item.get("title"),
						Utils.RESULT_PASS);
				Utils.writeCurMessage(TAG, Utils.RESULT_PASS);
			} else {
				item.put("result", Utils.RESULT_FAIL);
				Utils.saveStringValue(mContext, item.get("title"),
						Utils.RESULT_FAIL);
				Utils.writeCurMessage(TAG, Utils.RESULT_FAIL);
			}
			mContext.sendBroadcast(new Intent(Values.BROADCAST_UPDATE_MAINVIEW));
		}

		if (!WORKROUND)
			try {
				PSensorManager.unregisterListener(mPSensorListener, mPSensor);
			} catch (Exception e) {
			}

	};

	public class PSensorListener implements SensorEventListener {

		private int count = 0;

		public PSensorListener(Context context) {

			super();
		}

		public void onSensorChanged(SensorEvent event) {

			// PSensor event.value has 3 equal value. Value only can be 1 and 0
			synchronized (this) {
				if (event.sensor.getType() == SENSOR_TYPE) {
					logd(event.values.length + ":" + event.values[0] + " "
							+ event.values[0] + " " + event.values[0] + " ");
					String value = "(" + event.values[0] + ", "
							+ event.values[1] + ", " + event.values[2] + ")";
					if (value != pre_value)
						count++;
					if (count >= MIN_COUNT)
						pass();
					pre_value = value;
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

	public static void finish() {
		finish(true);
	}

	@Override
	public void onDestroy() {
		logd("");
		finish(false);
		super.onDestroy();
	}

	static void fail(Object msg) {
		loge(msg);
		result = false;
		finish();
	}

	static void pass() {

		result = true;
		finish();
	}

	private void logd(Object s) {

		if (Values.SERVICE_LOG) {
			Thread mThread = Thread.currentThread();
			StackTraceElement[] mStackTrace = mThread.getStackTrace();
			String mMethodName = mStackTrace[3].getMethodName();

			s = "[" + mMethodName + "] " + s;
			Log.d(TAG, s + "");
		}
	}

	private static void loge(Object e) {

		if (e == null)
			return;
		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
