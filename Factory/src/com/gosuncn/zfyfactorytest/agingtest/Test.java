package com.gosuncn.zfyfactorytest.agingtest;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfw.api.LedManager;

import java.text.DecimalFormat;

public class Test extends Activity {
    private static String TAG = "Test";
    private Vibrator mvibrator;
    private CameraManager cameraManager;
    private LinearLayout fullscreen = null;
    private int[] color = {-65536, -16711936, -16776961, -1};//red,green,blue,white
    private int[] initStartingTime = {0,0,0};
    private int colorNum = 0;
    private boolean[] flagAll;
    private MediaPlayer mp;
    private TextView mtimeview;
    private String finaltimestr;
    SharedPreferences sharedPreferences ;
    SharedPreferences.Editor meditor;
    private static final int MESSAGE_HANDLE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        sharedPreferences = getSharedPreferences("timefile",MODE_PRIVATE);
        meditor = sharedPreferences.edit();
        Intent intent = getIntent();
        boolean[] flag = intent.getBooleanArrayExtra("flag");//tag which item choose
        flagAll = flag;
        startTimer(flag[0]);
        if (flag[1])//SPK
        {
            mp = MediaPlayer.create(this,R.raw.agingtest_test);
            mp.setLooping(true);
            mp.start();

        }
        if (flag[2])//VIBRATE
        {
            mvibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
            mvibrator.vibrate(new long[]{0,1000,2000,1000},2);
        }
        if (flag[3])//RED
        {
            enableFlashirled(true);
        }
        if (flag[4])//FLASHLIGHT
        {
            enableFlash(true);
        }


    }

    private void initView(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.agingtest_lcd);
        fullscreen = findViewById(R.id.LL);
        mtimeview = (TextView) this.findViewById(R.id.timeview);
    }

    private void startTimer(boolean isLcdSelected){
        Message msg = mHandler.obtainMessage();
        msg.what = MESSAGE_HANDLE;
        msg.obj = initStartingTime;
        msg.arg1 = isLcdSelected ? 1: 0;
        mHandler.sendMessage(msg);
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MESSAGE_HANDLE) {
                if(mHandler == null) return;
                Message msgNew = mHandler.obtainMessage();
                msgNew.what = MESSAGE_HANDLE;

                int[] time = timeCounter((int[])msg.obj);          //timer
                msgNew.obj = time;

                if(msg.arg1 == 1) {                                //color
                    msgNew.arg1 = 1;
                    fullscreen.setBackgroundColor(color[colorNum]);
                    colorNum++;
                    if (colorNum == color.length)
                        colorNum = 0;
                }
                mHandler.sendMessageDelayed(msgNew,1000);
            }
        }
    };

    private int[] timeCounter(int[] time){
        time[2] ++;
        if(time[2] == 60) {
            time[2] = 0;
            time[1]++;
        }
        if(time[1] == 60){
            time[1] = 0;
            time[0] ++;
        }
        finaltimestr =  ((time[0]/10 == 0 ? (("0" + time[0])) : (time[0]))) + "时"+
                ((time[1]/10 == 0 ? (("0" + time[1])) : (time[1]))) + "分"+
                ((time[2]/10 == 0 ? (("0" + time[2])) : (time[2]))) + "秒";
        mtimeview.setText(finaltimestr);
        return time;
    }

    private void enableFlashirled(boolean state){
        if(state){
            LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_IRLED);
        }else{
            LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_IRLED);
        }
    }

    private void enableFlash(boolean state) {//打开闪光灯
        try {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraManager.setTorchMode("0", state);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    @Override
    protected void onDestroy() {
        Log.e(TAG,"destroy");
        super.onDestroy();
        if(flagAll[1] || mp != null) {
            mp.stop();//音乐停止
            mp.release();
        }
        if(flagAll[2] || mvibrator != null){
            mvibrator.cancel();//震动停止
        }
        if(flagAll[3]) enableFlashirled(false);//红外灯
        if(flagAll[4]) enableFlash(false);//闪光灯停止
        if(mHandler != null){
            mHandler.removeMessages(MESSAGE_HANDLE);
            mHandler = null;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra("result",finaltimestr);
            setResult(3, intent);
            meditor.putString("lasttime",finaltimestr);
            meditor.commit();
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onResume() {
        SystemProperties.set("persist.sys.gsfk.key", "1");
        super.onResume();
    }

    @Override
    protected void onPause() {
        SystemProperties.set("persist.sys.gsfk.key", "0");
        super.onPause();
    }
}