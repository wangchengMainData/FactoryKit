package com.gosuncn.zfyfactorytest.BackLight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;


public class BackLight extends BaseActivity {
    private static final String TAG = BackLight.class.getSimpleName();

    private static final int MSG_AUTO_ADJUST_BRIGHTNESS = 100;
    private float mCurrentBrightness = 1.0f;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(MSG_AUTO_ADJUST_BRIGHTNESS == msg.what){
                if(!isFinishing()) {
                    autoAdjustBrightness(true);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(loadDefaultConfirmText(getResources().getString(R.string.backlight_confirm)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(MSG_AUTO_ADJUST_BRIGHTNESS, 300);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_AUTO_ADJUST_BRIGHTNESS);
        mCurrentBrightness = 1.0f;
        autoAdjustBrightness(false);
    }

    private void autoAdjustBrightness(boolean autoEnabled){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if(autoEnabled) {
            mCurrentBrightness = (mCurrentBrightness > 0.1f) ? 0.1f : 1.0f;
        }
        lp.screenBrightness = mCurrentBrightness;
        getWindow().setAttributes(lp);

        if(autoEnabled)
            mHandler.sendEmptyMessageDelayed(MSG_AUTO_ADJUST_BRIGHTNESS, 3000);
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
