/*
 * Copyright (c) 2013-2015, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfactorytest.Framework.MainApp.ServiceInfo;
import android.content.ComponentName;

public class AutoService extends IntentService {
	String TAG = "AutoService";
	private static Context mContext = null;

	public AutoService() {
		super("AutoService");
	}

	private int taskCounter = 0;
	private long INTERVAL_TIME = 500;
	private long FINISH_TIME = 30 * 1000;
	// ��ʱ����30s����������finish()����.��ʱ�������ڼ䣬û500ms����һ��onTick()����
	CountDownTimer taskCountDownTimer = new CountDownTimer(FINISH_TIME,
			INTERVAL_TIME) {
		@Override
		public void onTick(long arg0) {
			ArrayList<ServiceInfo> intentArrayList = MainApp.getInstance().intentArrayList;
			if (intentArrayList.size() > 0) {
				for (int i = 0; i < intentArrayList.size(); i++) {
					int delay = 0;
					// �ж��Ƿ���ServiceDelay, ��Щactivity����û�е�
					if (intentArrayList.get(i).paraMap
							.get(Values.KEY_SERVICE_DELAY) != null)
						// ���磺parameter="service=com.gosuncn.zfyfactorytest.LightSensor.LightSensorService;ServiceDelay=3000;"
						// delay = 3000
						delay = Integer.valueOf(intentArrayList.get(i).paraMap
								.get(Values.KEY_SERVICE_DELAY));
					// �ж������õ�ʱ�䣬����3000�Ƿ�� taskCounter * INTERVAL_TIME���
					if (delay == taskCounter * INTERVAL_TIME) {
						Intent intent = new Intent();
						ComponentName componentName = new ComponentName(getPackageName(),"intentArrayList.get(i).intentService" );
						intent.setComponent(componentName);
						/* ������Ϳ��Կ���, ServiceDelay��ʾapp����֮�󣬶�Ӧ�ķ��������������ģ���������ʱServiceDelay֮��������
						 * �����ʱ������˳����бȶԸ������������ʱ�䣬��������Ӧ�ķ���
						 * ���磺parameter="service=com.gosuncn.zfyfactorytest.LightSensor.LightSensorService;ServiceDelay=3000;"
						 * ����ʱ3000ms֮������com.qti.LightSensorLightSensorService�������
						 */
						// ��������
						startService(intent);
						logd(taskCounter
								+ " "
								+ intentArrayList.get(i).intentService
								.getAction());
					}
				}
			}
			taskCounter++;
		}

		@Override
		public void onFinish() {
		}
	};

	private void init(Context context) {

		mContext = context;
		taskCounter = 0;

		// To save test time, enable some devices first
		Utils.enableWifi(mContext, true);
		Utils.enableBluetooth(true);
		Utils.enableGps(mContext, true);
		try {
			Utils.enableNfc(mContext, true);
		} catch (Exception e) {
		}
		Utils.enableCharging(true);
		Utils.configScreenTimeout(mContext, 1800000); // 1 min
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		logd("RunningService=" + MainApp.getInstance().getServiceCounter());
		if (MainApp.getInstance().getServiceCounter() > 0)
			return;
		init(getApplicationContext());

		List<Map<String, ?>> list = (List<Map<String, ?>>) MainApp
				.getInstance().mItemList;
		for (int i = 0; i < list.size(); i++) {
			Map<String, ?> item = list.get(i);
			if ("true".equals(item.get("auto")))
				logd(item.get("title") + " " + item.get("result"));
			// auto = "true"
			if ("true".equals(item.get("auto")))
				// RESULT_FAIL = "failed" || "NULL"
				// ����û�в��Ե�activity����������ж�
				if (Utils.RESULT_FAIL.equals(item.get("result"))
						|| "NULL".equals(item.get("result"))) {

					logd("Add " + item.get("title") + " to service list");
					// ���ҷ���
					// ���磺parameter="service=com.gosuncn.zfyfactorytest.LightSensor.LightSensorService;ServiceDelay=3000;"
					// service = comqti.factory.LightSensor.LightSensorService
					String service = ((HashMap<String, String>) item
							.get("parameter")).get("service");
					Intent serviceIntent = new Intent(service);
					// wmd
					if(service != null && !service.isEmpty()) {
						serviceIntent.setClassName(
								getApplicationContext().getPackageName(),
								service);
					}
					// Values.KEY_SERVICE_INDEX = "key"
					// "key" = i
					serviceIntent.putExtra(Values.KEY_SERVICE_INDEX, i);
					// ��serviceIntent��ӵ���̬������
					MainApp.getInstance().addServiceList(
							new ServiceInfo(serviceIntent, item
									.get("parameter")));
				}
		}

		taskCountDownTimer.start();
	}

	@Override
	public void onDestroy() {
		logd("");
		// if (taskCountDownTimer != null)
		// taskCountDownTimer.cancel();
		super.onDestroy();
	}

	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}

	private void loge(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.e(TAG, s + "");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
