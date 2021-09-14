/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Keypad;

import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfactorytest.Framework.MainApp;
import com.gosuncn.zfyfw.service.GSFWManager;

import static android.view.WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;

public class Keypad extends BaseActivity {

    private static final String TAG = "Keypad";
    private static String resultString = Utils.RESULT_FAIL;
    private static Context mContext;
    private int itemIndex = -1;

    private static final int KEY_MODE_0 = 0;
    private static final int KEY_MODE_1 = 1;
    private static final int KEY_MODE_2 = 2;
    private static final int KEY_MODE_3 = 3;
    private static final int KEY_MODE_4 = 4;

    private final int[] KEYMODE0 = {KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN};
    private final int[] KEYMODE1 = {KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_CAMERA};
    private final int[] KEYMODE2 = {KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_FOCUS};
    // for bacon
    private final int[] KEYMODE3 = {
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_POWER,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_BACK};
    // for G4
        private final int[][] KEYMODE4 = {
        {
            GSFWManager.KEYCODE_CAMERA, //拍照
            GSFWManager.KEYCODE_AUDIO, // 录音
            GSFWManager.KEYCODE_POWER,
            GSFWManager.KEYCODE_MENU,
            GSFWManager.KEYCODE_HOME, // 回放
            GSFWManager.KEYCODE_BACK,
            GSFWManager.KEYCODE_PTT, // PTT
            GSFWManager.KEYCODE_VIDEO, // 录像
            GSFWManager.KEYCODE_MARK, // 标记
            GSFWManager.KEYCODE_SOS // SOS
        },
        {
            R.string.keypad_camera, //拍照
            R.string.keypad_record, // 录音
            R.string.keypad_power,
            R.string.keypad_menu,
            R.string.keypad_home, // 回放
            R.string.keypad_back,
            R.string.keypad_ptt, // PTT
            R.string.keypad_video, // 录像
            R.string.keypad_mark, // 标记
            R.string.keypad_sos // SOS
        }
    };
    private final int[][] KEYMODE = {KEYMODE0, KEYMODE1, KEYMODE2, KEYMODE3, KEYMODE4[0]};

    private final int[][] KEYNAMES = {KEYMODE0, KEYMODE1, KEYMODE2, KEYMODE3, KEYMODE4[1]};

    int[] keyMode;
    int[] keyNames;
    HashMap<Integer, Boolean> keyStatusHashMap = new HashMap<Integer, Boolean>();

    @Override
    public void finish() {
        Utils.writeCurMessage(this, TAG, resultString);
        super.finish();
    }

    @Override
    protected void onResume() {
        SystemProperties.set("persist.sys.gsfk.key", "1");
        super.onResume();
    }

    @Override
    protected void onPause() {
        SystemProperties.set("persist.sys.gsfk.key", "0");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getRemainingKeys(){
        StringBuffer reuslt = new StringBuffer(getString(R.string.keypad_text));
        reuslt.append("\n");
        for (int i = 0; i < keyMode.length; i++) {
            if (!keyStatusHashMap.get(keyMode[i])) {
                if(i > 0){
                    reuslt.append(" ");
                }
                reuslt.append(getString(keyNames[i]));
            }
        }
        return reuslt.toString();
    }

    private void init(Context context) {
        mContext = context;
        resultString = Utils.RESULT_FAIL;
//        setContentView(R.layout.keypad);

        // get keymode
        itemIndex = getIntent().getIntExtra(Values.KEY_SERVICE_INDEX, -1);
        int keymodeIndex = Utils.getIntPara(itemIndex, "KeyMode", 0);
        // for G4
        if (Values.PRODUCT_G4.equals(Utils.getPlatform())) {
            keymodeIndex = KEY_MODE_4;
        }

        keyMode = KEYMODE[keymodeIndex];
        keyNames = KEYNAMES[keymodeIndex];
        for (int i = 0; i < keyMode.length; i++) {
            keyStatusHashMap.put(keyMode[i], false);
        }

        setContentView(loadDefaultConfirmText(getRemainingKeys()));
        getConfirmText().setTextColor(getColor(R.color.red));

        // hide some keys according to keymode on board
   /*     TextView volume_up = (TextView) findViewById(R.id.volume_up);
        TextView volume_down = (TextView) findViewById(R.id.volume_down);
        TextView focusView = (TextView) findViewById(R.id.focus);
        TextView camView = (TextView) findViewById(R.id.camera);
        TextView keypad_record = (TextView) findViewById(R.id.keypad_record);
        TextView keypad_power = (TextView) findViewById(R.id.keypad_power);
        TextView keypad_menu = (TextView) findViewById(R.id.keypad_menu);
        TextView keypad_home = (TextView) findViewById(R.id.keypad_home);
        TextView keypad_back = (TextView) findViewById(R.id.keypad_back);
        TextView keypad_ptt = (TextView) findViewById(R.id.keypad_ptt);
        TextView keypad_video = (TextView) findViewById(R.id.keypad_video);
        TextView keypad_mark = (TextView) findViewById(R.id.keypad_mark);
        TextView keypad_sos = (TextView) findViewById(R.id.keypad_sos);

        if (keymodeIndex == KEY_MODE_0) {
            volume_up.setVisibility(View.VISIBLE);
            volume_down.setVisibility(View.VISIBLE);
        } else if (keymodeIndex == KEY_MODE_1) {
            volume_up.setVisibility(View.VISIBLE);
            volume_down.setVisibility(View.VISIBLE);
            camView.setVisibility(View.VISIBLE);
        } else if (keymodeIndex == KEY_MODE_2) {
            volume_up.setVisibility(View.VISIBLE);
            volume_down.setVisibility(View.VISIBLE);
            camView.setVisibility(View.VISIBLE);
            focusView.setVisibility(View.GONE);
        } else if (keymodeIndex == KEY_MODE_3) { // bacon
            volume_up.setVisibility(View.VISIBLE);
            volume_down.setVisibility(View.VISIBLE);
            keypad_power.setVisibility(View.VISIBLE);
            keypad_menu.setVisibility(View.VISIBLE);
            keypad_home.setVisibility(View.VISIBLE);
            keypad_back.setVisibility(View.VISIBLE);
        } else if (keymodeIndex == KEY_MODE_4) { // G4
            camView.setVisibility(View.VISIBLE);
            keypad_record.setVisibility(View.VISIBLE);
            keypad_power.setVisibility(View.VISIBLE);

            keypad_menu.setVisibility(View.VISIBLE);
            keypad_home.setVisibility(View.VISIBLE);
            keypad_back.setVisibility(View.VISIBLE);

            keypad_ptt.setVisibility(View.GONE);
            keypad_video.setVisibility(View.GONE);
            keypad_mark.setVisibility(View.GONE);
            keypad_sos.setVisibility(View.GONE);
        }*/
    }

    private boolean allKeyPassed() {
        for (int i = 0; i < keyMode.length; i++) {
            if (!keyStatusHashMap.get(keyMode[i]))
                return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init(getApplicationContext());

        // REQ147,factory:key test,wmd,2020.0331
        getWindow().addPrivateFlags(0x01000000/*DISPATCH HOME KEYEVENT TO APP*/);

 /*       Button pass = (Button) findViewById(R.id.pass);
        Button fail = (Button) findViewById(R.id.fail);
        if (SHOW_RESULT_DIALOG) {
            pass.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {

                    pass();
                }
            });

            fail.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {

                    fail(null);
                }
            });
        } else {
            pass.setVisibility(View.GONE);
            fail.setVisibility(View.GONE);
        }*/

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow");
//		try {
//			this.getWindow().setType(TYPE_KEYGUARD_DIALOG);//WindowManager.LayoutParams.TYPE_KEYGUARD); // wmd
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

    @Override
    protected void onPositiveCallback() {
        pass();
    }

    @Override
    protected void onNegativeCallback() {
        fail(null);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "dispatchKeyEvent code:" + event.getKeyCode());

        if(GSFWManager.KEYCODE_CAMERA == event.getKeyCode()){
            keyStatusHashMap.put(event.getKeyCode(), true);
            setConfirmText(getRemainingKeys());

            if (allKeyPassed())
                pass();
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        TextView keyText = null;
        logd(keyCode);
        keyStatusHashMap.put(keyCode, true);
        switch (keyCode) {

//                keyText = (TextView) findViewById(R.id.focus);
//                break;
            case GSFWManager.KEYCODE_CAMERA:
            case GSFWManager.KEYCODE_AUDIO:
//                keyText = (TextView) findViewById(R.id.camera);
//                break;
            case GSFWManager.KEYCODE_POWER:
//                keyText = (TextView) findViewById(R.id.keypad_power);
//                break;
            case GSFWManager.KEYCODE_MENU:
//                keyText = (TextView) findViewById(R.id.keypad_menu);
//                break;
            case GSFWManager.KEYCODE_HOME:
//                keyText = (TextView) findViewById(R.id.keypad_home);
//                break;
            case GSFWManager.KEYCODE_BACK:
//                keyText = (TextView) findViewById(R.id.keypad_back);
//                break;
            case GSFWManager.KEYCODE_PTT:
            case GSFWManager.KEYCODE_VIDEO:
            case GSFWManager.KEYCODE_MARK:
            case GSFWManager.KEYCODE_SOS:
                setConfirmText(getRemainingKeys());
                break;
            default:
                break;
            // add here
            // 录音
            // PTT
            // 录像
            // 标记
            // SOS
        }

//        if (null != keyText) {
//            keyText.setBackgroundResource(R.color.green);
//        }

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean result = super.onKeyUp(keyCode, event);

        if (allKeyPassed())
            pass();

        return result;
    }

    void fail(Object msg) {

        loge(msg);
        setResult(RESULT_CANCELED);
        resultString = Utils.RESULT_FAIL;
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        resultString = Utils.RESULT_PASS;
        finish();
    }

    void logd(Object d) {

        Log.d(TAG, "" + d);
    }

    void loge(Object e) {

        Log.e(TAG, "" + e);
    }

}
