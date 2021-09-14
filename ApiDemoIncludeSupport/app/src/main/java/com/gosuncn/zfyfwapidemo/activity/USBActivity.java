package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.gosuncn.zfyfw.api.LedManager;



import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.os.Handler;


import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyhwapidemo.R;

public class USBActivity extends Activity {
    private static final String TAG = USBActivity.class.getSimpleName();
    private Button usbBtn;
    private boolean isUsbEnabled = false;
    private Handler mHandler;

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            usbBtn.setText("Mass Storage:"+(GSFWManager.getInstance().isMassStorageEnabled() ? "enabled" : "disabled"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usbBtn = new Button(this);
        setContentView(usbBtn);
        usbBtn.setText("Mass Storage:"+(GSFWManager.getInstance().isMassStorageEnabled() ? "enabled" : "disabled"));
        usbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isUsbEnabled = (isUsbEnabled ? false : true);
                GSFWManager.getInstance().setMassStorageEnabled(isUsbEnabled);
                mHandler.postDelayed(mRunnable, 5000);
            }
        });
        mHandler = new Handler(getMainLooper());
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)usbBtn.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        usbBtn.setLayoutParams(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
