package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;


import com.gosuncn.zfyfw.api.LedManager;
import com.gosuncn.zfyfw.service.GSFWManager;



import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.gosuncn.zfyhwapidemo.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class KeyActivity extends Activity {
    private static final String TAG = KeyActivity.class.getSimpleName();

    TextView mContentTV = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GSFWManager.getInstance().setHomeKeyDispatched(getWindow());

        mContentTV = new TextView(this);
        setContentView(mContentTV);
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
//        GSFWManager.getInstance().setHomeKeyDispatched(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GSFWManager.ACTION_KEYEVENT);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
//        GSFWManager.getInstance().setHomeKeyDispatched(false);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keycode = intent.getIntExtra(GSFWManager.KEY_CODE, 0);
            int keytype = intent.getIntExtra(GSFWManager.KEY_TYPE, 0);

            Log.d(TAG, "onReceive action:" + intent.getAction()+" code:"+keycode + " type:"+keytype);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_HOME:

            case GSFWManager.KEYCODE_CAMERA:
            case GSFWManager.KEYCODE_AUDIO:
            case GSFWManager.KEYCODE_PTT:
            case GSFWManager.KEYCODE_VIDEO:
            case GSFWManager.KEYCODE_MARK:
            case GSFWManager.KEYCODE_SOS:

                mContentTV.setText("KeyCode: " + keyCode);
                return true;

            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            default:
                break;
        }
        return false;
    }
}
