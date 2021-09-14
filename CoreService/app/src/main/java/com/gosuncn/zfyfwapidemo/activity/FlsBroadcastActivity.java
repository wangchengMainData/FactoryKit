package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObservable;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;

import com.gosuncn.zfyfw.R;
import com.gosuncn.zfyfw.api.LedManager;


import java.io.IOException;


public class FlsBroadcastActivity extends Activity {
    private static final String TAG = FlsBroadcastActivity.class.getSimpleName();

    public static final String ACTION_FLASHLIGHT_EVENT = "android.intent.action.gosuncn.zfy.FlashLightEvent";
    public static final String FLASHLIGHT_STATUS = "status";
    public static final int FLASHLIGHT_STATUS_ON = 1;
    public static final int FLASHLIGHT_STATUS_OFF = 0;
    private flsBroadcastreceiver fbreceiver;
    private boolean flag = false;
    private ImageView mFlsLight ;
    private Button msend;
    private SurfaceHolder mholder;
    private SurfaceView msurfaceview;
    private TextView mtv;
    Context mContext;
    private Camera mCamera;
    private CameraManager mcameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mcameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        setContentView(R.layout.fls);
        mContext = getApplicationContext();
        mtv =findViewById(R.id.mtv1);
        msurfaceview = findViewById( R.id.surfaceView);
        mholder = msurfaceview.getHolder();//存放预览界面
        mholder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mCamera = getCamera();
                if (mCamera != null) {
                    try {
                        mCamera.setDisplayOrientation(0);//预览界面旋转90度
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();//启动预览
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCamera();
            }
        });
        mCamera=getCamera();
        if (mCamera!=null){
            try {
                mCamera.setPreviewDisplay( mholder );
                mCamera.startPreview();
            } catch (IOException e) {
                releaseCamera();
                e.printStackTrace();
            }
        }
        mholder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        regist();
        initGobalsettingsvalue();//init persist.sys.zfy.fls to 0
        msend =findViewById(R.id.send_br);
        msend.setText("改值为１");
        mFlsLight = findViewById(R.id.switcher);
        mFlsLight.setImageResource(R.drawable.fls_off);//模拟下拉菜单闪光灯不可点击
        flag = false;
        //if fls inactive ,send broadcast
        mFlsLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flag) {
                    LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_IRLED);
                    mFlsLight.setImageResource(R.drawable.fls_on);
                }else
                {
                    LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_IRLED);
                    mFlsLight.setImageResource(R.drawable.fls_off);
                }
                flag=!flag;
            }
        });
        msend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBroadcast();
            }
        });
    }

    //app deal with below
    public class flsBroadcastreceiver  extends BroadcastReceiver{//广播接收并设置值
        @Override
        public void onReceive(Context context,Intent intent){
            Log.d(TAG,"Broadcast receive success !!!");
            if(intent.getAction().equals(ACTION_FLASHLIGHT_EVENT)) {

                if (intent.getIntExtra(FLASHLIGHT_STATUS, 3) == FLASHLIGHT_STATUS_ON) {
                    Log.d(TAG, "App turn the light on");
                    mFlsLight.setEnabled(true);
                    mFlsLight.setImageResource(R.drawable.fls_on);
                    flag = true;
                    LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_IRLED);
                    Settings.Global.putInt(mContext.getContentResolver(),
                            "persist.sys.zfy.fls", 1);//change to 1
                    int value = Settings.Global.getInt(mContext.getContentResolver(),
                            "persist.sys.zfy.fls", 0);
                    Log.d(TAG, "PUT VALUE SUCCESS VALUE NOW : " + value);
                    mtv.setText("persist.sys.zfy.fls:" + value);
                } else if (intent.getAction().equals(ACTION_FLASHLIGHT_EVENT)) {
                    if (intent.getIntExtra(FLASHLIGHT_STATUS, 3) == FLASHLIGHT_STATUS_OFF) {
                        Log.d(TAG, "App turn the light off");
                        mFlsLight.setEnabled(false);//open button
                        mFlsLight.setImageResource(R.drawable.fls_off);
                        flag = false;
                        LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_IRLED);
                        Settings.Global.putInt(mContext.getContentResolver(),
                                "persist.sys.zfy.fls", 0);//change to 0
                        int value = Settings.Global.getInt(mContext.getContentResolver(),
                                "persist.sys.zfy.fls", 0);
                        Log.d(TAG, "PUT VALUE SUCCESS VALUE NOW : " + value);
                        mtv.setText("persist.sys.zfy.fls:" + value);
                    }
                }
            }
        }
    }

    private void initGobalsettingsvalue()
    {
        Settings.Global.putInt(mContext.getContentResolver(),
                "persist.sys.zfy.fls",0);
        int value = Settings.Global.getInt(mContext.getContentResolver(),
                "persist.sys.zfy.fls",0);
        Log.d(TAG,"VALUE BEFORE:"+value);
        mtv.setText("persist.sys.zfy.fls:"+value);
    }

    private void sendBroadcast()
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_FLASHLIGHT_EVENT);
        if(!flag){
        Log.d(TAG,"now is close hope to turn on");
        msend.setText("改值为０");
        intent.putExtra(FLASHLIGHT_STATUS,FLASHLIGHT_STATUS_ON);}
        else{
            Log.d(TAG,"now is open hope to turn off");
            intent.putExtra(FLASHLIGHT_STATUS,FLASHLIGHT_STATUS_OFF);
            msend.setText("改值为１");
            int value = Settings.Global.getInt(mContext.getContentResolver(),
                    "persist.sys.zfy.fls",0);
            Log.d(TAG,"VALUE:"+value);
        }
        sendBroadcast(intent);
        Log.d(TAG,"off sended");
    }

    private Camera getCamera() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                return mCamera;
            } catch (Exception ex) {

                return null;
            }
        }
        return mCamera;
    }
    private void releaseCamera(){//释放相机
        if (mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void regist()
    {
        fbreceiver = new flsBroadcastreceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FLASHLIGHT_EVENT);
        registerReceiver(fbreceiver, intentFilter);
        Log.d(TAG,"Broadcast register >>>>>>>>>>>>>>>>>>>>>>>>");
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(fbreceiver);
        Log.d(TAG,"Broadcast unregister <<<<<<<<<<<<<<<<<<<<<<<<<");
    }




}
