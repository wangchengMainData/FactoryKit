/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Framework;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.nfc.NfcAdapter;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;
import com.gosuncn.logger.LogManager;
import com.gosuncn.logger.MainActivity;
import com.gosuncn.zfyfactorytest.SDCard.FormatService;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;

public class EventReceiver extends BroadcastReceiver {
	private static final String TAG = "EventReceiver";

	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();
		logd(action);
		Log.d(TAG, "onReceive action:"+action);
		if (Values.FACTORY_MODE) {
			if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
//				if (Values.FACTORY_MODE)
//					Utils.createShortcut(context, Framework.class);
				// configSoundEffects(context, false);
				if(LogManager.isLogServiceEnalbed) {
					Intent sintent = new Intent();
					sintent.setComponent(new ComponentName("com.gosuncn.zfyfactorytest", "com.gosuncn.logger.LogService"));
					sintent.setAction("android.intent.action.gosuncn.syslog.boot");
					context.startService(sintent);
				}

				if("0".equals(SystemProperties.get("persist.sys.nfcinit", "0"))){
					SystemProperties.set("persist.sys.nfcinit", "1");
					try {
						NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
						nfcAdapter.disable();
					}catch (Exception e){
						e.printStackTrace();
					}
				}

				if("0".equals(SystemProperties.get("persist.sys.sdform", "0"))) {
					SystemProperties.set("persist.sys.sdform", "1");
					FormatService.startTask(context.getApplicationContext());
				}

			} else if (TelephonyIntents.SECRET_CODE_ACTION.equals(action)) {
				Intent logkitIntent = new Intent(context, Framework.class);
				context.startActivity(logkitIntent);
			}
		}
	}

	private void configSoundEffects(Context context, boolean enable) {
		AudioManager mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		if (enable)
			mAudioManager.loadSoundEffects();
		else
			mAudioManager.unloadSoundEffects();
	}

	private static void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}

	private static void loge(Object e) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}
}
