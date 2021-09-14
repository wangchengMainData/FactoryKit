package com.gosuncn.zfyfw.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;

/**
 * @author: Administrator
 * @date: 2020/6/4
 */
public class SystemEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if( intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){

            Log.d("nv_read_test","BroadcastReceiver BOOT_COMPLETED ");
            SystemProperties.set("persist.sys.gsfk.key", "0");
            SystemProperties.set("persist.sys.zfy.qbs", "1");
        }
    }
}
