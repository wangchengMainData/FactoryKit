package com.gosuncn.zfyfw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.gosuncn.zfyfw.api.FastSwitchingMachine;
import com.gosuncn.zfyfw.broadcast.BatteryListener;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.service.SystemTools;
import com.gosuncn.zfyfw.share.IPreference;
import com.gosuncn.zfyfw.share.SharedUtils;
import com.gosuncn.zfyfw.view.RingProgressView;
import android.provider.Settings;

/**
 * @author: Administrator
 * @date: 2020/5/7
 */
public class PowerOnOffActivity extends Activity implements BatteryListener.BatteryStateListener {
    public String PowerTAG = "PowerOffActivity";
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    public static final int POWER_OFF_TIME_LEN = 8000;
    public static final int POWER_ON_TIME_LEN = 8000;
    public static final String  LAUNCHE_PACKAGE_NAME = "com.gosuncn.android.recorder";
    private SoundPool mSoundpool;
    private int mCurrentId;
    private ImageView mBgImage = null;
    public enum  ActivityMode{IDLE, POWERON,POWEROFF}
    private ActivityMode mCurrentMode = ActivityMode.POWEROFF;
    private RelativeLayout  mProgressContainer;
    private BatteryListener mBatteryListener;
    private RingProgressView mRingProgressView;
    private boolean mPlugged = false;  //记录USB线是否是插着的
    private int mChargeLevel = 0;
    private AnimationDrawable mAnimationDrawable;
    private Vibrator vibrator = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_power_onoff);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        mBgImage = (ImageView)findViewById(R.id.main_bg);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        loadModeData();

        Intent intent = getIntent();
        int status = intent.getIntExtra("status", -1);
        Log.d(PowerTAG,"onCreate status:"+status);
        SystemTools.adbController(this,status);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);//must store the new intent unless getIntent() will return the old one

        Log.d(PowerTAG,"onNewIntent ");
        loadModeData();

        Intent recvIntent = getIntent();
        int status = recvIntent.getIntExtra("status", -1);
        Log.d(PowerTAG,"onNewIntent status:"+status);
        SystemTools.adbController(this,status);
    }

    private void loadModeData(){
        initAudioPlayer();
        String key = SystemProperties.get("persist.sys.zfy.qbs","0");
        mBgImage.setBackgroundResource(R.drawable.power);
        mAnimationDrawable = (AnimationDrawable)mBgImage.getBackground();
        Log.d(PowerTAG,"loadModeData key ="+ key);
        switch ( key ){
            case "0":
            case "1":
                mCurrentMode = PowerOnOffActivity.ActivityMode.POWEROFF;
                initCharge();
                playPowerOffAnim();
                dealThingsForShutdodwn();
                break;
            case "3":
                mCurrentMode = PowerOnOffActivity.ActivityMode.POWERON;
                initCharge();
                playPowerOnAnim();
                dealThingsForOpen();
                break;
            case "2":
                mCurrentMode = PowerOnOffActivity.ActivityMode.IDLE;
                break;
        }
        keepScreenOn();
        setSystemValue( 1 );
        SystemProperties.set("persist.sys.gsfk.key", "1");
    }

    private void initAudioPlayer(){
        if(Build.VERSION.SDK_INT > 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(2);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_SYSTEM);
            builder.setAudioAttributes(attrBuilder.build());
            mSoundpool = builder.build();
        }else{
            mSoundpool = new SoundPool(2, AudioManager.STREAM_SYSTEM, 0);
        }
    }

    private void initCharge(){
        mBatteryListener = new BatteryListener(this);
        mBatteryListener.register( this );
        mProgressContainer = findViewById(R.id.progress_container);
        mProgressContainer.setVisibility(View.GONE);
        mRingProgressView = findViewById(R.id.ringProgress);
        mRingProgressView.setCurrentProgress(mChargeLevel);

    }


    private void setSystemValue( int value){
        SystemProperties.set("persist.sys.zfy.qbs", ""+value);
        //Settings.Global.putInt(PowerOnOffActivity.this.getContentResolver(),"persist.sys.zfy.qbs", value);
    }


    /*
    *这里面调用这个APP，不然对话框位置不对
    *
    * */

    private void dealThingsForShutdodwn(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                saveCurrentState();
                //关闭Laucher
                SystemTools.forceStopApp( LAUNCHE_PACKAGE_NAME ,PowerOnOffActivity.this);
                SystemTools.setGpsState(PowerOnOffActivity.this,false);
                SystemTools.sendPowerOffBroadcast( PowerOnOffActivity.this );

            }
        }).start();
    }


    private void dealThingsForOpen(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                readSavedState();
            }
        }).start();
    }

    /*
    * 播放动画
    * */
    private void runAnim(){
        mAnimationDrawable.setOneShot(true);
        if(mAnimationDrawable.isRunning())
        {
            mAnimationDrawable.stop();//停止
        }
        mAnimationDrawable.start();
    }
    private void playPowerOffAnim(){
        keepScreenOn();
        runAnim();
        mCurrentMode = ActivityMode.POWEROFF;
        loadingPowerOffData();
    }

    private void  playPowerOnAnim(){
        vibrator.vibrate(100);
        keepScreenOn();
        runAnim();
        mCurrentMode = ActivityMode.POWERON;
        loadingPowerOnData();
    }

    private void loadingPowerOffData(){
        mSoundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mSoundpool.play(mCurrentId,1,1,0,0,1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentMode = ActivityMode.IDLE;
                        setSystemValue( 2 );
// PLM17068, battery led display, wmd, 2020.0722
                        PowerOnOffActivity.this.sendBroadcast(new Intent("android.intent.action.gosuncn.zfy.qbs.poweroff")); // wmd
                        SystemProperties.set("persist.sys.gsfk.key", "0");
                        stopPlayer();
                        sleepScreen();
                        vibrator.vibrate(300);
                        mBgImage.setBackgroundColor(Color.parseColor("#000000"));
                    }
                },POWER_OFF_TIME_LEN);
            }
        });
        mCurrentId = mSoundpool.load(this, R.raw.audio, 1);
    }


    private void loadingPowerOnData(){
        mSoundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mSoundpool.play(mCurrentId,1,1,0,0,1);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
// PLM17068, battery led display, wmd, 2020.0722
                        PowerOnOffActivity.this.sendBroadcast(new Intent("android.intent.action.gosuncn.zfy.qbs.poweron")); // wmd
                        mProgressContainer.setVisibility(View.GONE);
                        stopPlayer();
                        finish();
                    }
                },POWER_ON_TIME_LEN);
            }
        });
        mCurrentId = mSoundpool.load(this, R.raw.audio, 1);
    }

    private void stopPlayer(){
        if( mSoundpool!= null) {
            mSoundpool.stop(mCurrentId);
        }
    }

    /*
    * 屏蔽按键事件，Power 按键 和 HOME事件目前屏蔽失败
    * */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d("keyCode","keyCode="+keyCode);
        switch (keyCode){
           case KeyEvent.KEYCODE_BACK:
                return true;

            case KeyEvent.KEYCODE_POWER:
                if( mCurrentMode ==  ActivityMode.IDLE){

                    return true;
                }
                return super.onKeyDown(keyCode, event);

        }
        return true;
    }

    /*
    * 点亮屏幕与关闭，熄灭屏幕无效果.  gotoSleep来休眠
    * */
    PowerManager.WakeLock mWakelock = null;
    private void keepScreenOn(){
        FastSwitchingMachine.getInstance().sleepSystemControl( 0 );
        Log.d(PowerTAG,"keepScreenOn1");
    }

    private void sleepScreen(){
        if( mPlugged ){
            showChargingUi( mChargeLevel );
        }else{
            FastSwitchingMachine.getInstance().sleepSystemControl( 1);
        }
        Log.d(PowerTAG,"sleepScreen1");
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(null!=mSoundpool){
            mSoundpool.release();
        }
        if(null!=mBatteryListener) {
            mBatteryListener.unregister();
        }
        super.onDestroy();
        SystemProperties.set("persist.sys.gsfk.key", "0");
    }


    private void showChargingUi( int percent){
        if(mProgressContainer.getVisibility() != View.VISIBLE){
            mProgressContainer.setVisibility(View.VISIBLE);
        }
        mRingProgressView.setCurrentProgress( percent);
        mRingProgressView.postInvalidate();
    }

    /*
     * save data
     * */

    private void saveCurrentState(){

        if(SystemTools.isAirplaneModeOn(this)){
            SharedUtils.put("air_state",1,this);
        }else{
            SharedUtils.put("air_state",0,this);
            SystemTools.setAirplaneMode(true,PowerOnOffActivity.this);
        }
    }

    private void readSavedState(){

        int airPlaneMode = SharedUtils.get("air_state",
                IPreference.DataType.INTEGER,this);
        if( airPlaneMode == 1 ){
            if(!SystemTools.isAirplaneModeOn(this)) {
                SystemTools.setAirplaneMode(true, this);
            }
        }else{
            SystemTools.setAirplaneMode(false,this);
        }

    }

    //充电状态监听
    @Override
    public void onStateChanged(int percent,boolean ischarging) {
        mPlugged = ischarging;
        mChargeLevel = percent;
    }

    @Override
    public void onStateLow() {

    }

    @Override
    public void onStateOkay() {
        if(ActivityMode.IDLE == mCurrentMode){

        }
    }

    @Override
    public void onStatePowerConnected() {
        mPlugged = true;
        if( ActivityMode.IDLE == mCurrentMode  ){
            mProgressContainer.setVisibility(View.VISIBLE);
            keepScreenOn();
            showChargingUi( mChargeLevel );
        }
    }

    @Override
    public void onStatePowerDisconnected() {
        mPlugged = false;
        if(mProgressContainer.getVisibility() == View.VISIBLE){
            mProgressContainer.setVisibility(View.INVISIBLE);
            sleepScreen();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

}
