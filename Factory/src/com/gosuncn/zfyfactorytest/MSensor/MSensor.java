/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.MSensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;



public class MSensor extends BaseActivity {
//	private CompassRotation mCompassRotation;
	private SensorManager mSensorManager = null;
	private Sensor mMSensor = null;
//	private Sensor aSensor = null;
	private MSensorListener mMSensorListener;
	TextView mSensorResult,mDegree;
	float[] mMagnValues;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private static String pre_value = INIT_VALUE;
	private final int MIN_COUNT = 10;
	private float startAngle = 0;
	ImageView compass;
	String TAG = "MSensor";
	private final static int SENSOR_TYPE_M = Sensor.TYPE_MAGNETIC_FIELD;
//	private final static int SENSOR_TYPE_A = Sensor.TYPE_GRAVITY;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME;

	@Override
	public void finish() {

		try {

			mSensorManager.unregisterListener(mMSensorListener, mMSensor);
//			mSensorManager.unregisterListener(mMSensorListener, aSensor);
		} catch (Exception e) {
		}
		super.finish();
	}

	void bindView() {
//        aSensorResult = (TextView) findViewById(R.id.asensor_result);
		mSensorResult = (TextView) findViewById(R.id.msensor_result);
		mDegree =(TextView) findViewById(R.id.msensor_degree);
//		compass = findViewById(R.id.compass);
//		mCompassRotation = new CompassRotation();
//		mCompassRotation.setDuration(200);
//		mCompassRotation.setFillAfter(true);
	}

	void getService() {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			mSensorResult.setText(getString(R.string.service_get_fail));
			return;
		}

		mMSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_M);
//		aSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE_A);
		if (mMSensor == null  ) {
			mSensorResult.setText("磁力传感器:"+getString(R.string.sensor_get_fail));
			return;
		}
//		if (aSensor == null  ) {
//			mSensorResult.setText("Asensor:"+getString(R.string.sensor_get_fail));
//			return;
//		}

		mMSensorListener = new MSensorListener(this);
		if (!mSensorManager.registerListener(mMSensorListener, mMSensor,
				SENSOR_DELAY)) {
			mSensorResult.setText("磁力传感器:"+getString(R.string.sensor_register_fail));
		}
//		mMSensorListener = new MSensorListener(this);
//		if (!mSensorManager.registerListener(mMSensorListener, aSensor,
//				SENSOR_DELAY)) {
//			aSensorResult.setText("Asensor:"+getString(R.string.sensor_register_fail));
//		}
	}

	void updateView(Object s1,Object s2,Object s3) {

		mSensorResult.setText("磁力传感器:" + "\n" + s1 + "\n" +s2 + "\n" + s3);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
//		setContentView(loadDefaultConfirmText(""));
		setContentView(R.layout.msensor);
		bindView();
		updateView(value,null,null);
//		updateView2(value);
		getService();

	}

	@Override
	protected void onPositiveCallback() {
		pass();
	}

	@Override
	protected void onNegativeCallback() {
		fail(null);
	}

	void fail(Object msg) {

		loge(msg);
//		toast(msg);
		setResult(RESULT_CANCELED);
		Utils.writeCurMessage(this, TAG, "Failed");
		finish();
	}

	void pass() {

		// toast(getString(R.string.test_pass));
		setResult(RESULT_OK);
		Utils.writeCurMessage(this, TAG, "Pass");
		finish();

	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

		if (mSensorManager == null || mMSensorListener == null
				|| mMSensor == null)
			return;
		mSensorManager.unregisterListener(mMSensorListener, mMSensor);
//		mSensorManager.unregisterListener(mMSensorListener, aSensor);
	}

	public class MSensorListener implements SensorEventListener {

		private int count = 0;

		public MSensorListener(Context context) {

			super();
		}

		public void onSensorChanged(SensorEvent event) {

			// MSensor event.value has 3 equal value.
			synchronized (this) {
				if (event.sensor.getType() == SENSOR_TYPE_M ) {
                    logd(event.values.length + ":" + event.values[0] + " "
                            + event.values[0] + " " + event.values[0] + " ");
					String value1 = "x: " + event.values[0];
					String value2 = "y: " + event.values[1];
					String value3 = "z: " + event.values[2];
					updateView(value1,value2,value3);
					String value = "(" + event.values[0] + ", "
							+ event.values[1] + ", " + event.values[2] + ")";
					mMagnValues = event.values;
				}
//				if(event.sensor.getType() == SENSOR_TYPE_A) {
//					String value = "(" + event.values[0] + ", "
//							+ event.values[1] + ", " + event.values[2] + ")";
//					updateView2(value);
//					accValues = event.values;
//				}
				if(mMagnValues != null ){
//					calculateOrientation();
					if(Framework.quickTestEnabled) {
						pass();
					}
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

//	private void calculateOrientation(){
//	 	float[] values = new float[3];
//		float[] R = new float[9];
//		SensorManager.getRotationMatrix(R,null,accValues,mMagnValues);
//		SensorManager.getOrientation(R,values);
//		values[0] = (float)Math.toDegrees((values[0]));
//		int degree = 180-(int)values[0];
//        if(degree >= 170 && degree < 190)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_north)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if(degree >= 100 && degree <170)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_northeast)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if(degree >= 80 && degree <100)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_east)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if(degree >= 10 && degree < 80)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_southeast)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if((degree >= 0 && degree < 10) || (degree >= 350 && degree <360))
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_south)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if(degree >= 280 && degree < 350)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_southwest)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if(degree >= 260 && degree <280)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_west)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//        if(degree >= 190 && degree < 260)
//			mDegree.setText(getString(com.gosuncn.zfyfactorytest.R.string.msensor_northwest)+degree
//					+getString(com.gosuncn.zfyfactorytest.R.string.msensor_degree));
//		float presentAngle = values[0];
//		if(startAngle ==(-presentAngle))
//			return;
//		presentAngle = (-presentAngle);
//		mCompassRotation.setStartAngle(startAngle);
//		mCompassRotation.setEndAngle(presentAngle);
//		compass.startAnimation(mCompassRotation);
//		startAngle = presentAngle;
//	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
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
