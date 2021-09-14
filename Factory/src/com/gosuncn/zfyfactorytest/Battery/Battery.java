/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Battery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;

/***
 * 充电状态：充电（usb）
 * 当前电量：3
 * 电池电量：100
 * 电源状态：很好
 * 当前电压：3916mV
 * 电源温度：21.0C
 * 电源技术：Li-ion
 * 自启动的时间：02-04
 */
public class Battery extends BaseActivity {

    String TAG = "Battery";
    String resultString = Utils.RESULT_FAIL;
    private Intent batteryIntent = new Intent();

    final String CAPACITY = "/sys/class/power_supply/battery/capacity";
    //final String VOLTAGE_NOW = "/sys/class/power_supply/battery/batt_vol";
    final String STATUS = "/sys/class/power_supply/battery/status";
    final String CURRENT_NOW = "/sys/class/power_supply/battery/current_now";
    // name="usbonline">/sys/class/power_supply/usb/online</string>//
    // name="aconline">/sys/class/power_supply/ac/online</string>

    private static final int MSG_REFRESH_INFO = 1000;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(MSG_REFRESH_INFO == msg.what){
                if(!isFinishing()){
                    setConfirmText(getBatteryInfo());
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_INFO, 2000);

                    String voltage = batteryIntent.getStringExtra("voltage");
                    int voltageInt = 0;
                    try{
                        voltageInt = Integer.parseInt(voltage);
                    }catch (Exception e){
                        e.printStackTrace();
                        voltageInt = 0;
                    }
                    if(voltageInt > 3000 && Framework.quickTestEnabled){
                        onPositiveCallback();
                    }
                }
            }
        }
    };



    @Override
    public void finish() {

        Utils.writeCurMessage(this, TAG, resultString);
        super.finish();
    }

    private void init(Context context)
    { 
        resultString = Utils.RESULT_FAIL;
    }

    private String getBatteryInfo(){
        boolean ret = false;
        String tmp = null;
        float voltage = 0;
        float current = 0;
        StringBuffer result = new StringBuffer("");

        result.append(getString(R.string.battery_info_status_label));
        result.append(batteryIntent.getStringExtra("status"));
        result.append("\n");
        result.append(getString(R.string.battery_info_level_label));
        result.append(batteryIntent.getIntExtra("level", 0));
        result.append("%\n");
        result.append(getString(R.string.battery_info_scale_label));
        result.append(batteryIntent.getIntExtra("scale", 0));
        result.append("%\n");
        result.append(getString(R.string.battery_info_health_label));
        result.append(batteryIntent.getStringExtra("health"));
        result.append("\n");
//        tmp = getBatteryInfo(VOLTAGE_NOW);
//        if (tmp != null) {
//            voltage = Float.valueOf(tmp);
//            if (voltage > 1000)
//                voltage = voltage / 1000;
            result.append(getString(R.string.battery_info_voltage_label));
            result.append(batteryIntent.getStringExtra("voltage")+"mV");
            result.append("\n");
//        }
        tmp = getBatteryInfo(CURRENT_NOW);
        if (tmp != null) {
            int current_now = Integer.parseInt(tmp)/1000;
            result.append(getString(R.string.battery_info_current_label));
            result.append(current_now+"mA");
            result.append("\n");
        }
        result.append(getString(R.string.battery_info_temperature_label));
        result.append(batteryIntent.getStringExtra("temperature"));
        result.append("\n");
        result.append(getString(R.string.battery_info_technology_label));
        result.append(batteryIntent.getStringExtra("technology"));
        result.append("\n");

        try {
            long bootTime = SystemClock.elapsedRealtime() / 1000;
            int min = ((int) ((bootTime % 3600) / 60));
            int hour = ((int) (bootTime % (24 * 3600) / 3600));
            int day = ((int) (bootTime / (24 * 3600)));

            result.append(getString(R.string.battery_info_uptime));
            result.append(new StringBuffer(String.valueOf(day)).append(":").append(String.valueOf(hour)).append(":").append(String.valueOf(min)));
            result.append("\n");
        }catch (Exception e){
            e.printStackTrace();
        }

        return result.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.battery);
        init(this);



        setContentView(loadDefaultConfirmText(""));
//        toast(result);
//        if (ret)
//            pass();
//        else
//            fail(null);
    }

    @Override
    protected void onPositiveCallback() {
        pass();
    }

    @Override
    protected void onNegativeCallback() {
        fail(null);
    }

    private String getBatteryInfo(String path) {

        File mFile;
        FileReader mFileReader;
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[] = null;
            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");
                logd(status[0]);
                return status[0];
            } catch (IOException e) {
                loge(e);
            }
        } catch (FileNotFoundException e) {
            loge(e);
        }
        return null;
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        resultString=Utils.RESULT_FAIL;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mIntentReceiver, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mIntentReceiver);
        mHandler.removeMessages(MSG_REFRESH_INFO);
    }

    void pass() {
        setResult(RESULT_OK);
        resultString=Utils.RESULT_PASS;
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                int status = intent.getIntExtra("status", 0);
                int health = intent.getIntExtra("health", 0);
                boolean present = intent.getBooleanExtra("present", false);
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 0);
                int icon_small = intent.getIntExtra("icon-small", 0);
                int plugged = intent.getIntExtra("plugged", 0);
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                int temperature = intent.getIntExtra("temperature", 0);
                String technology = intent.getStringExtra("technology");
                String statusString = "";
                double f = 0;
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = getString(R.string.battery_info_status_unknown);
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        switch (plugged) {
                            case BatteryManager.BATTERY_PLUGGED_AC:
                                statusString = getString(R.string.battery_info_status_charging) + getString(R.string.battery_info_status_charging_ac);
                                break;
                            case BatteryManager.BATTERY_PLUGGED_USB:
                                statusString = getString(R.string.battery_info_status_charging) + getString(R.string.battery_info_status_charging_usb);
                                break;
                            default:
                                statusString = getString(R.string.battery_info_status_charging);
                                break;
                        }
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = getString(R.string.battery_info_status_discharging);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = getString(R.string.battery_info_status_not_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = getString(R.string.battery_info_status_full);
                        break;
                }
                batteryIntent.putExtra("status", statusString);
                batteryIntent.putExtra("level", level);
                batteryIntent.putExtra("scale", scale);
                batteryIntent.putExtra("voltage",String.valueOf(voltage));
                String healthString = "";
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        healthString = getString(R.string.battery_info_health_unknown);
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        healthString = getString(R.string.battery_info_health_good);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        healthString = getString(R.string.battery_info_health_overheat);
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        healthString = getString(R.string.battery_info_health_dead);
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        healthString = getString(R.string.battery_info_health_over_voltage);
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        healthString = getString(R.string.battery_info_health_unspecified_failure);
                        break;
                }
                batteryIntent.putExtra("health", healthString);
                batteryIntent.putExtra("temperature", "" + (temperature / 10) + getString(R.string.battery_info_temperature_c));
                batteryIntent.putExtra("technology", technology);

//            BatteryLog.this.status.setText(statusString);
//            BatteryLog.this.health.setText(healthString);
//            BatteryLog.this.level.setText(""+level);
//            BatteryLog.this.scale.setText(""+scale);
//            BatteryLog.this.voltage.setText(""+voltage+" mV");
//            BatteryLog.this.temperature.setText(""+temperature/10+" "+getString(R.string.temperature_c));
//
//            if (plugged == BatteryManager.BATTERY_PLUGGED_AC)
//            {
//                BatteryLog.this.current_title.setText(getString(R.string.battery_info_average_current_label));
//            }else
//            {
//                BatteryLog.this.current_title.setText(getString(R.string.battery_info_current_label));
//            }
//            //double f = Float.valueOf(getInfo("cat /sys/devices/platform/battery/FG_Battery_CurrentConsumption")) / 10.0f;
//            //double f = Float.valueOf(getInfo("cat /sys/devices/platform/battery/power_supply/battery/BatteryAverageCurrent")) / 10.0f;
//            if(f > 10000){f/=1000000;}
//            if(f < -1000) {f *= 1000;}
//            if(f > -1){
//                if(f < 1)
//                {f += 100.0 ;}
//            }
//            Log.d("zengjun", "mCmdStringFG_Battery_CurrentConsumption ==" + (mCmdString + "FG_Battery_CurrentConsumption"));
//            BatteryLog.this.current.setText(""+ String.valueOf((Math.round(f*10)/10.0)) +" mA");
//            BatteryLog.this.technology.setText(technology);

                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                    Log.d("Battery", "" + intent.getIntExtra("level", 0));
                    Log.d("Battery", "" + intent.getIntExtra("scale", 0));
                    Log.d("Battery", "" + intent.getIntExtra("voltage", 0));
                    Log.d("Battery", "" + intent.getIntExtra("temperature", 0));
                    Log.d("Battery",
                            "ss"
                                    + intent.getIntExtra("status",
                                    BatteryManager.BATTERY_STATUS_CHARGING));
                    Log.d("Battery", "" + intent.getIntExtra("plugged", 0));
                    Log.d("Battery",
                            ""
                                    + intent.getIntExtra("health",
                                    BatteryManager.BATTERY_HEALTH_UNKNOWN));
                }

                mHandler.sendEmptyMessageDelayed(MSG_REFRESH_INFO, 100);

        }
    };
}
