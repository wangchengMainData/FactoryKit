/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Led;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.MainApp;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfw.api.LedManager;

import java.util.HashMap;
import java.util.Map;

public class Led extends BaseActivity {

    private static final String TAG = "Led";
    private static Context mContext;
//    private final int GREEN = 0;
    private final int RED = 0;
    private final int BLUE = 1;
    private final int OFF = 2;
    private final int INIT_COLOR_NUM = 3;
    private int color = RED;
    final byte[] LIGHT_ON = {'2', '5', '5'};
    final byte[] LIGHT_OFF = {'0'};
    private static final String SYS_LEDS_PATH = "/sys/class/leds/";
    String RED_LED_DEV = SYS_LEDS_PATH + "red/brightness";
    String GREEN_LED_DEV = SYS_LEDS_PATH + "green/brightness";
    String BLUE_LED_DEV = SYS_LEDS_PATH + "blue/brightness";

    private int colorNum = INIT_COLOR_NUM;
    CountDownTimer mCountDownTimer = new CountDownTimer(
            60*1000, 1000) {

        public void onTick(long arg0) {
            logd("");
            setColor((color++ % 3));
        }

		public void onFinish() {

			logd("");
			if(SHOW_RESULT_DIALOG) {
				showDialog();
			}
			setColor(OFF);
		}
	};

	@Override
	public void finish() {

		super.finish();
	}

	private void init(Context context) {

		setResult(RESULT_CANCELED);
		mContext = context;
		colorNum = INIT_COLOR_NUM;

		int index = getIntent().getIntExtra(Values.KEY_SERVICE_INDEX, -1);
		if (index >= 0) {

			Map<String, ?> item = (Map<String, ?>) MainApp.getInstance().mItemList
					.get(index);
			HashMap<String, String> paraMap = (HashMap<String, String>) item
					.get("parameter");
			String red = paraMap.get("red");
			String green = paraMap.get("green");
			String blue = paraMap.get("blue");
			if (red != null)
				RED_LED_DEV = SYS_LEDS_PATH + red + "/brightness";
			else
				colorNum--;
			if (green != null)
				GREEN_LED_DEV = SYS_LEDS_PATH + green + "/brightness";
			else
				colorNum--;
			if (blue != null)
				BLUE_LED_DEV = SYS_LEDS_PATH + blue + "/brightness";
			else
				colorNum--;
		}

        setContentView(R.layout.tricolor_led);
        TextView textView = (TextView) findViewById(R.id.led_hint);
        StringBuffer newStr = new StringBuffer("");
        if (colorNum == 3) {
            newStr = new StringBuffer(getResources().getString(R.string.led_tri_text));
        } else if (colorNum == 2)
            newStr = new StringBuffer(getResources().getString(R.string.led_dual_text));

        newStr.append("\n");
        newStr.append(getResources().getString(R.string.led_confirm));
        textView.setText(newStr.toString());
        color = RED;
        mCountDownTimer.start();
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		init(getApplicationContext());

	}

	@Override
	protected void onPositiveCallback() {
		passListener.onClick(null, 0);
	}

	@Override
	protected void onNegativeCallback() {
		failListener.onClick(null, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		setColor(OFF);
		mCountDownTimer.cancel();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void showDialog() {

		new AlertDialog.Builder(Led.this).setMessage(R.string.led_confirm)
				.setPositiveButton(R.string.yes, passListener)
				.setNegativeButton(R.string.no, failListener).show();
	}

	OnClickListener passListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			setResult(RESULT_OK);
			Utils.writeCurMessage(mContext, TAG, "Pass");
			finish();
		}
	};

	OnClickListener failListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			setResult(RESULT_CANCELED);
			Utils.writeCurMessage(mContext, TAG, "Failed");
			finish();
		}
	};

	// REQ116,lights,wmd,2020.0407
	private void setColor(int color) {

        logd("set:" + color);
        switch (color) {
//            case GREEN:
//                LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_BATTERY);
//                //LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
//                break;
            case RED:
                LedManager.getInstance().setLedColor(LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_RED);
                //LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
                break;
            case BLUE:
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
                LedManager.getInstance().setLedColor(LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_YELLOW);
                //LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
                break;
            case OFF:
            default:
//			LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
                break;
        }
/*		try {
			FileOutputStream fRed = new FileOutputStream(RED_LED_DEV);
			fRed.write(red ? LIGHT_ON : LIGHT_OFF);
			fRed.close();
			FileOutputStream fGreen = new FileOutputStream(GREEN_LED_DEV);
			fGreen.write(green ? LIGHT_ON : LIGHT_OFF);
			fGreen.close();
			FileOutputStream fBlue = new FileOutputStream(BLUE_LED_DEV);
			fBlue.write(blue ? LIGHT_ON : LIGHT_OFF);
			fBlue.close();

		} catch (Exception e) {
			loge(e);
		}*/

	}

	void logd(Object d) {

		Log.d(TAG, "" + d);
	}

	void loge(Object e) {

		Log.e(TAG, "" + e);
	}
}
