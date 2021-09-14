/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.gosuncn.zfyfactorytest.GSensor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class GSensor extends BaseActivity {

	private SensorManager mSensorManager = null;
	private Sensor mGSensor = null;
	private GSensorListener mGSensorListener;
	TextView mTextView;
	Button cancelButton;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private static String pre_value = INIT_VALUE;
	private final int MIN_COUNT = 10;
	String TAG = "GSensor";
	String i2C_CMD = "i2cdetect -y -r 0 0x1c 0x1c";
	private static boolean WORKROUND = false;
	private float[] accValues;
	private final static int SENSOR_TYPE = Sensor.TYPE_ACCELEROMETER;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;
	private ImageView gsensorArrow;

	@Override
	public void finish() {
		if (!WORKROUND)
			try {
				mSensorManager.unregisterListener(mGSensorListener, mGSensor);
			} catch (Exception e) {
				loge(e);
			}
		super.finish();
	}

	void bindView() {

		mTextView = (TextView) findViewById(R.id.gsensor_result);
		gsensorArrow = findViewById(R.id.gsensor_arrow);

	}

	void getService() {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			mTextView.setText(getString(R.string.service_get_fail));
			return;
		}

		mGSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mGSensor == null) {
			mTextView.setText(getString(R.string.sensor_get_fail));
			return;
		}

		mGSensorListener = new GSensorListener(this);
		if (!mSensorManager.registerListener(mGSensorListener, mGSensor,
				SENSOR_DELAY)) {
			mTextView.setText(getString(R.string.sensor_register_fail));
		}
	}

	void updateView(Object s) {
//		getConfirmText().setText("GSensor: " + s);
		mTextView.setText(TAG + " : " + s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (WORKROUND) {
			exec(i2C_CMD);
		} else {
//			setContentView(loadDefaultConfirmText(""));
			setContentView(R.layout.gsensor);
			bindView();
			updateView(value);
			getService();
		}

	}

	@Override
	protected void onPositiveCallback() {
		pass();
	}

	@Override
	protected void onNegativeCallback() {
		fail(null);
	}

	void exec(final String para) {

		new Thread() {

			public void run() {
				try {
					logd(para);

					Process mProcess;
					String paras[] = para.split(" ");
					for (int i = 0; i < paras.length; i++)
						logd(i + ":" + paras[i]);
					mProcess = Runtime.getRuntime().exec(paras);
					mProcess.waitFor();

					InputStream inStream = mProcess.getInputStream();
					InputStreamReader inReader = new InputStreamReader(inStream);
					BufferedReader inBuffer = new BufferedReader(inReader);
					String s;
					String data = "";
					while ((s = inBuffer.readLine()) != null) {
						data += s + "\n";
					}
					logd(data);
					int result = mProcess.exitValue();
					logd("ExitValue=" + result);
					Message message = new Message();
					if (data.contains("--"))
						message.obj = false;
					else
						message.obj = true;
					message.setTarget(mHandler);
					message.sendToTarget();

				} catch (Exception e) {
					logd(e);
				}

			}
		}.start();

	}

	Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			boolean res = (Boolean) msg.obj;
			if (res)
				pass();
			else
				fail(null);

		};
	};

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

		if (mSensorManager == null || mGSensorListener == null
				|| mGSensor == null)
			return;
		mSensorManager.unregisterListener(mGSensorListener, mGSensor);
	}

	public class GSensorListener implements SensorEventListener {

		private int count = 0;

		public GSensorListener(Context context) {

			super();
		}

		public void onSensorChanged(SensorEvent event) {

			synchronized (this) {
				if (event.sensor.getType() == SENSOR_TYPE) {
					logd(event.values.length + ":" + event.values[0] + " "
							+ event.values[1] + " " + event.values[2] + " ");
					String value = "(" + event.values[0] + ", "
							+ event.values[1] + ", " + event.values[2] + ")";
					updateView(value);
					accValues = event.values;
					if(Math.abs(accValues[0]) <=2 && Math.abs(accValues[1]) <=2 && (int)accValues[2] ==9)
					{
						gsensorArrow.setImageResource(R.drawable.normalarrow);
					}else
					{
						gsensorArrow.setImageResource(R.drawable.arrow);
						if(accValues[1] < 0 && Math.abs(accValues[0]) < 3)
							gsensorArrow.setRotation(180);
						if(accValues[1] > 0 && Math.abs(accValues[0]) < 3)
							gsensorArrow.setRotation(0);
						if(accValues[0] < 0 && Math.abs(accValues[1]) < 3)
							gsensorArrow.setRotation(-90);
						if(accValues[0] > 0 && Math.abs(accValues[1]) < 3)
							gsensorArrow.setRotation(90);
					}
					if (value != pre_value)
						count++;
					if(Values.AUTO_TEST_ENABLED || Framework.quickTestEnabled) {
						if (count >= MIN_COUNT)
							pass();
					}
					pre_value = value;
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

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
