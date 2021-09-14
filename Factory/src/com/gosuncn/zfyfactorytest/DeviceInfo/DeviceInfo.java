package com.gosuncn.zfyfactorytest.DeviceInfo;

import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.R;

public class DeviceInfo extends BaseActivity {

    private static final String TAG = DeviceInfo.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        StringBuffer info = new StringBuffer("");

        String boardStr = SystemProperties.get("ro.product.board");

        info.append("Hardware Band Version:\n");
//        info.append(SystemProperties.get("gsm.version.baseband"));
        info.append(boardStr);
        info.append(" ");
        info.append(SystemProperties.get("ro.build.date"));
        info.append("\n");
        info.append(getResources().getString(R.string.DeviceInfo_confirm));
        setContentView(loadDefaultConfirmText(info.toString()));

        if(!TextUtils.isEmpty(boardStr) && Framework.quickTestEnabled) {
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

