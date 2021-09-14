package com.gosuncn.zfyfw;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;


import com.gosuncn.zfyfw.service.VpnConnectService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class EmptyActivity extends Activity {
    String TAG = EmptyActivity.class.getSimpleName();
    private Map<String,String> mVpn;
    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        Log.e(TAG, "EmptyActivity started");
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        Intent actionIntent = getIntent();
        Map<String,String> vpn =(HashMap<String, String>) actionIntent.getSerializableExtra("config");
        if(vpn == null) {
            stopEstablish();
            Log.d(TAG,"There's no any vpn config reached,closing vpn connection!");
        }else {
            mVpn = vpn;
            int action = actionIntent.getIntExtra(VpnConnectService.VPN_TAG, VpnConnectService.VPN_STOP);
            if (action == VpnConnectService.VPN_START) {
                startEstablish();
            } else if (action == VpnConnectService.VPN_STOP) {
                stopEstablish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult");
        if (resultCode == RESULT_OK) {
            Log.e(TAG, "onActivityResult.RESULT_OK");
            Intent intentStart = new Intent(this, VpnConnectService.class);
            intentStart.putExtra(VpnConnectService.VPN_TAG,VpnConnectService.VPN_START);
            intentStart.putExtra("config",(Serializable) mVpn);
            Log.e(TAG, "onActivityResult  startService before");
            startServiceAsUser(intentStart, UserHandle.SYSTEM);
            Log.e(TAG, "onActivityResult  startService after");
        }
    }


    private void startEstablish(){
        Log.e(TAG, "action = START");
        Intent intentPrepare = VpnService.prepare(MainApp.mContext);
        if (intentPrepare != null) {
            startActivityForResult(intentPrepare, 0);
        } else {
            onActivityResult(0,RESULT_OK,null);
        }
    }

    private void stopEstablish(){
        Log.e(TAG, "action = STOP");
        Intent intentStop = new Intent(this,VpnConnectService.class);
        intentStop.putExtra(VpnConnectService.VPN_TAG,VpnConnectService.VPN_STOP);
        startServiceAsUser(intentStop, UserHandle.SYSTEM);
    }
}