/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.WiFi;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;

public class WiFi extends BaseActivity {

	private WifiLock mWifiLock;
	private WifiManager mWifiManager;
	private List<ScanResult> wifiScanResult;
	private TextView mTextView;
	final int SCAN_INTERVAL = 4000;
	final int OUT_TIME = 30000;
	IntentFilter mFilter = new IntentFilter();
	String resultString = Utils.RESULT_FAIL;
	static String TAG = "WiFi";
	private boolean scanResultAvailabe = false;
	private static Context mContext = null;

	@Override
	public void finish() {

		// User may press back key while showing the AP list.
		if (wifiScanResult != null && wifiScanResult.size() > 0) {
			setResult(RESULT_OK);
			resultString = Utils.RESULT_PASS + "\n" + wifiInfos;
		}

		try {
			mCountDownTimer.cancel();
			if (true == mWifiLock.isHeld())
				mWifiLock.release();
		} catch (Exception e) {
			loge(e);
		}
		Utils.writeCurMessage(this, TAG, resultString);
		super.finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

//		setContentView(R.layout.wifi);
		setContentView(loadDefaultConfirmText(R.string.wifi_text));

		mContext = getApplicationContext();
		bindView();
		getService();

		/** Keep Wi-Fi awake */
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
		if (false == mWifiLock.isHeld())
			mWifiLock.acquire();

		switch (mWifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			enableWifi(true);
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			getConfirmText().setText(getString(R.string.wifi_is_closing));
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			getConfirmText().setText(getString(R.string.wifi_state_unknown));
			break;
		default:
			break;
		}

		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

		mCountDownTimer.start();
	}

	@Override
	protected void onResume() {

		registerReceiver(mReceiver, mFilter);
		super.onResume();
	}

	@Override
	protected void onPositiveCallback() {
		pass(wifiInfos);
	}

	@Override
	protected void onNegativeCallback() {
		fail(null);
	}

	@Override
	protected void onPause() {

		unregisterReceiver(mReceiver);
		super.onPause();
	}

	private void enableWifi(boolean enable) {

		if (mWifiManager != null)
			mWifiManager.setWifiEnabled(enable);
	}

	void bindView() {

//		mTextView = (TextView) findViewById(R.id.wifi_hint);
//		mTextView.setText(getString(R.string.wifi_text));
	}

	void getService() {

		mWifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
	}

	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, SCAN_INTERVAL) {

		private int tickCount = 0;

		@Override
		public void onFinish() {

			logd("Timer Finish");
			if (wifiScanResult == null || wifiScanResult.size() == 0) {
				getConfirmText().setText(getString(R.string.wifi_scan_null));
			}
		}

		@Override
		public void onTick(long arg0) {

			tickCount++;
			logd("Timer Tick");
			// At least conduct startScan() 3 times to ensure wifi's scan
			if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
				mWifiManager.startScan();
				// When screen is dim, SCAN_RESULTS_AVAILABLE_ACTION cannot be
				// got.
				// So get it actively
				if (tickCount >= 4 && !scanResultAvailabe) {
					wifiScanResult = mWifiManager.getScanResults();
//					scanResultAvailabe = true;
					mHandler.sendEmptyMessage(0);
				}
			}

		}
	};

	CountDownTimer mDelayTimer = new CountDownTimer(0, 0) {

		@Override
		public void onFinish() {

			logd("mDelayTimer Finish");
			pass(wifiInfos);
		}

		@Override
		public void onTick(long arg0) {

			logd("mDelayTimer Tick");
		}
	};

	static String wifiInfos = "";
	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			String s = getString(R.string.wifi_scan_find_ap);
			wifiInfos = "";
			if (wifiScanResult != null && wifiScanResult.size() > 0) {
				int j = 0;
				for (int i = 0; i < wifiScanResult.size(); i++) {
					logd(wifiScanResult.get(i));
					if(!TextUtils.isEmpty(wifiScanResult.get(i).SSID)) {
						s += " " + j++ + ": " + wifiScanResult.get(i).SSID + "\n";
						wifiInfos += " " + i + ": "
								+ wifiScanResult.get(i).toString() + "\n";
					}
				}
				getConfirmText().setText(s);
				if(Values.AUTO_TEST_ENABLED || Framework.quickTestEnabled) {
					mDelayTimer.start();
				}

			} else {
				getConfirmText().setText(getString(R.string.wifi_scan_null));
			}
		};
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(Context c, Intent intent) {

			logd(intent.getAction() + mWifiManager.getWifiState());
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
					.getAction())) {
				if (!scanResultAvailabe) {
					wifiScanResult = mWifiManager.getScanResults();
//					scanResultAvailabe = true;
					mHandler.sendEmptyMessage(0);

					if(wifiScanResult == null || wifiScanResult.size() == 0){
						enableWifi(false);
					}
				}
			}else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
				if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
					enableWifi(true);
				}
			}
		}

	};

	void fail(Object msg) {

		loge(msg);
//		toast(msg);
		setResult(RESULT_CANCELED);
		resultString = Utils.RESULT_FAIL;
		finish();
	}

	void pass(String msg) {

		setResult(RESULT_OK);
		resultString = Utils.RESULT_PASS;
		Utils.enableWifi(mContext, false);
		finish();
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
