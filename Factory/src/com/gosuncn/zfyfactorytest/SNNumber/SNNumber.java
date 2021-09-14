package com.gosuncn.zfyfactorytest.SNNumber;

import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.R;

public class SNNumber extends BaseActivity {

    private static final String TAG = SNNumber.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        StringBuffer info = new StringBuffer("");

        String serialNumber = android.os.Build.getSerial();

        info.append("SN:\n");
        info.append(serialNumber);
        info.append("\n");
        info.append(getResources().getString(R.string.DeviceInfo_confirm));
        setContentView(loadDefaultConfirmText(info.toString()));

        if(!TextUtils.isEmpty(serialNumber)
                && !serialNumber.equals("unknown")
                && Framework.quickTestEnabled) {
            onPositiveCallback();
        }
    }

    @Override
    protected void onPositiveCallback() {
        setResult(RESULT_OK);
        Utils.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    @Override
    protected void onNegativeCallback() {
        setResult(RESULT_CANCELED);
        Utils.writeCurMessage(this, TAG, "Failed");
        finish();
    }
}

