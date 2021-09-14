package com.gosuncn.zfyfactorytest.SWVersion;

import android.os.Bundle;
import android.os.SystemProperties;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.R;

public class SWVersion extends BaseActivity {

    private static final String TAG = SWVersion.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        StringBuffer info = new StringBuffer("");

        info.append("Software Version:\n");
        info.append(SystemProperties.get("ro.build.customer.version"));
        info.append("\n");
        info.append(getResources().getString(R.string.DeviceInfo_confirm));
        setContentView(loadDefaultConfirmText(info.toString()));
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

