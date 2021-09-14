package com.gosuncn.zfyfw.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

/**
 * @author: Administrator
 * @date: 2020/5/11
 */
public class BatteryListener {

    public String TAG = BatteryListener.class.getSimpleName();

    private Context mContext;

    private BatteryBroadcastReceiver receiver;

    private BatteryStateListener mBatteryStateListener;

    int mLevel = 0;

    public BatteryListener(Context context) {
        mContext = context;
        receiver = new BatteryBroadcastReceiver();
    }

    public void register(BatteryStateListener listener) {
        mBatteryStateListener = listener;
        if (receiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_BATTERY_LOW);
            filter.addAction(Intent.ACTION_BATTERY_OKAY);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            mContext.registerReceiver(receiver, filter);
        }
    }

    public void unregister() {
        if (receiver != null) {
            mContext.unregisterReceiver(receiver);
        }
    }

    private class BatteryBroadcastReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String acyion = intent.getAction();
                switch (acyion) {
                    case Intent.ACTION_BATTERY_CHANGED://电量发生改变
                        if (mBatteryStateListener != null) {
                            mLevel++;
                            int  plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                            Log.e(TAG, "BatteryBroadcastReceiver --> onReceive--> ACTION_BATTERY_CHANGED level ="+mLevel+",level="+level+",scale="+scale);
                            mBatteryStateListener.onStateChanged( level ,plugged>0?true:false);
                        }
                        break;
                    case Intent.ACTION_BATTERY_LOW://电量低
                        if (mBatteryStateListener != null) {
                            Log.e(TAG, "BatteryBroadcastReceiver --> onReceive--> ACTION_BATTERY_LOW");
                            mBatteryStateListener.onStateLow();
                        }
                        break;
                    case Intent.ACTION_BATTERY_OKAY://电量充满
                        if (mBatteryStateListener != null) {
                            Log.e(TAG, "BatteryBroadcastReceiver --> onReceive--> ACTION_BATTERY_OKAY");
                            mBatteryStateListener.onStateOkay();
                        }
                        break;
                    case Intent.ACTION_POWER_CONNECTED://接通电源
                        if (mBatteryStateListener != null) {
                            Log.e(TAG, "BatteryBroadcastReceiver --> onReceive--> ACTION_POWER_CONNECTED");
                            mBatteryStateListener.onStatePowerConnected();
                        }
                        break;
                    case Intent.ACTION_POWER_DISCONNECTED://拔出电源
                        if (mBatteryStateListener != null) {
                            Log.e(TAG, "BatteryBroadcastReceiver --> onReceive--> ACTION_POWER_DISCONNECTED");
                            mBatteryStateListener.onStatePowerDisconnected();
                        }
                        break;
                }
            }
        }
    }

    public interface BatteryStateListener {
        public void onStateChanged(int percent,boolean ischarging);

        public void onStateLow();

        public void onStateOkay();

        public void onStatePowerConnected();

        public void onStatePowerDisconnected();
    }
}
