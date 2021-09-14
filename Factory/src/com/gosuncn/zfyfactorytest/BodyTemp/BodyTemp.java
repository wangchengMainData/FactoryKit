/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.BodyTemp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;

public class BodyTemp extends Activity {

    static String TAG = "BodyTemp";
    Button passButton = null;
    Button failButton = null;
    Button bt_start = null;
    TextView   temp_tv ;
    Context mContext;
    Handler mHandler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bodytemp);
        mContext = this;
        bindView();
        mHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String result = (String)msg.obj;
                temp_tv.setText(result);
            }
        };
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "bodytemp click");
                GSFWManager.getInstance().requestBodytemp();
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
    }

    void bindView() {
        bt_start = findViewById(R.id.bt_temp);
        bt_start.setText(getString(R.string.bodytemp_start));
        temp_tv = findViewById(R.id.body_temp);
        passButton = (Button) findViewById(R.id.pass);
        failButton = (Button) findViewById(R.id.fail);
        passButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                pass();
            }
        });

        failButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                fail(null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSFWManager.getInstance().registerBodyTempCallback(mISettingsContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSFWManager.getInstance().unresigterBodyTempCallback(mISettingsContentObserver);
    }

    private ISettingsContentObserver.Stub mISettingsContentObserver = new ISettingsContentObserver.Stub() {
        @Override
        public void onchanged(int type, int value, List<String> valueList) throws RemoteException {
            Log.d(TAG, "onchanged type:" + type + " value:" + value + " valueList:" + valueList.toString());
            if(valueList != null && valueList.size() > 0) {
                final String result = getString(R.string.bodytemp_temp) + valueList.get(0);
                Message msg = mHandler.obtainMessage(1);
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }
    };

    void fail(Object msg) {
        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utils.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {
        setResult(RESULT_OK);
        Utils.writeCurMessage(this, TAG, "Pass");
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

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

}
