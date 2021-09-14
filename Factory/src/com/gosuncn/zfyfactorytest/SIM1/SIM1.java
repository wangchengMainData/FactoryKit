/*
 * Copyright (c) 2011-2015, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.SIM1;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;

import com.gosuncn.zfyfactorytest.R;

public class SIM1 extends BaseActivity {

    String TAG = "SIM1";
    int SLOT_ID = 0;
    String resultString = "Failed";
    String toastString = "";
    boolean result = false;

    @Override
    public void finish() {

        Utils.writeCurMessage(TAG, resultString);

        logd(resultString);
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logd("");

        super.onCreate(savedInstanceState);
        setContentView(loadDefaultConfirmText(""));
        TelephonyManager telManager = (TelephonyManager) getSystemService(Activity.TELEPHONY_SERVICE);
        String iccid = "";
        if ("dsds"
                .equals(Utils.getSystemProperties(Values.PROP_MULTISIM, null))) {

            int[] subId = SubscriptionManager.getSubId(SLOT_ID);
            iccid = TelephonyManager.getDefault().getSimSerialNumber(subId[0]);
        } else {
            iccid = telManager.getSimSerialNumber();
        }

        logd("iccid:" + iccid);

        if(TextUtils.isEmpty(iccid)) {
            SubscriptionInfo info = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoForSimSlotIndex(0);
            logd("iccid :" + info);
            if (info != null) {
                iccid = info.getIccId();
                logd("2 iccid:" + iccid);
            }
        }

        if (iccid != null && !iccid.equals("")) {
            result = true;
            toastString = "iccid: " + iccid;
        }

        if(result && Framework.quickTestEnabled){
            onPositiveCallback();
            return;
        }

        if(Values.AUTO_TEST_ENABLED) {
            if (result) {
                setResult(RESULT_OK);
                resultString = Utils.RESULT_PASS;
                toast(toastString);

            } else {
                setResult(RESULT_CANCELED);
                resultString = Utils.RESULT_FAIL;
            }
            finish();
        }else{
//            setContentView(loadDefaultConfirmText(!TextUtils.isEmpty(iccid) ? ("ICCID:\n" + iccid) : getString(R.string.sim_not_existed)));
            String operator = telManager.getSimOperator();
            Log.d(TAG,"operator: "+operator);

            StringBuffer sb = new StringBuffer(getString(R.string.sim_status));

            switch(telManager.getSimState()){
                case TelephonyManager.SIM_STATE_ABSENT :sb.append(getString(R.string.sim_status_absent));break;
                case TelephonyManager.SIM_STATE_UNKNOWN :sb.append(getString(R.string.sim_status_unknown));break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED :sb.append(getString(R.string.sim_status_network_locked));break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED :sb.append(getString(R.string.sim_status_pin_required));break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED :sb.append(getString(R.string.sim_status_puk_required));break;
                case TelephonyManager.SIM_STATE_READY :sb.append(getString(R.string.sim_status_ready));break;
            }
            sb.append("\n");

            sb.append(getString(R.string.sim_iccid) + (!TextUtils.isEmpty(iccid) ? iccid : getString(R.string.status_unknown))+"\n");
            sb.append(getString(R.string.sim_operator_code) +( !TextUtils.isEmpty(operator) ? operator : getString(R.string.status_unknown))+"\n");

            getConfirmText().setText(sb.toString());
        }
    }

    @Override
    protected void onPositiveCallback() {
        setResult(RESULT_OK);
        resultString = Utils.RESULT_PASS;
        finish();
    }

    @Override
    protected void onNegativeCallback() {
        setResult(RESULT_CANCELED);
        resultString = Utils.RESULT_FAIL;
        finish();
    }

    public void toast(Object s) {

        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
