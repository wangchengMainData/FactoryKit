/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.IRCut;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.gosuncn.zfyfw.api.LedManager;
import com.gosuncn.zfyfactorytest.CameraFront.CameraFront;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.hardware.Camera.Size;
import java.util.List;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfactorytest.Framework.MainApp;
import com.gosuncn.zfyfw.service.GSFWManager;

public class IRCut extends Activity implements SurfaceHolder.Callback{

    private static Context mContext;
    String TAG = IRCut.class.getSimpleName();
    private CameraManager cameraManager;//wmd
    final byte[] ON = { '1'};
    final byte[] OFF = { '0' };
    private Camera mCamera = null;
    private TextView text;
    private boolean isIRCutOn;
    private Button switchButton,passButton,failButton;
    private String resultString = Utils.RESULT_FAIL;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private static String deviceNode = "/sys/class/gpio/ircut";

    @Override
    public void finish() {
        switchDevice(true);//turnon when quit
        stopCamera();
        Log.d(TAG,"finish()");
        super.finish();
    }

    private void init(Context context) {

        setResult(RESULT_CANCELED);
        mContext = context;

        int index = getIntent().getIntExtra(Values.KEY_SERVICE_INDEX, -1);
        if (index >= 0) {

            Map<String, ?> item = (Map<String, ?>) MainApp.getInstance().mItemList
                    .get(index);
            HashMap<String, String> paraMap = (HashMap<String, String>) item
                    .get("parameter");
            String device = paraMap.get("path");
            if (device != null)
                deviceNode = device;
        }
    }

    void switchDevice(boolean state) {
        FileOutputStream fileOutputStream;
        try {

            fileOutputStream = new FileOutputStream(deviceNode);
            if (state)
                fileOutputStream.write(ON);
            else
                fileOutputStream.write(OFF);
            fileOutputStream.close();

        } catch (Exception e) {
            loge(e);
        }
    }

    String readNode() {
        StringBuilder sb = new StringBuilder("");
        boolean isIRCutOn ;
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(deviceNode);
            byte[] a = new byte[1024];
            int len = fileInputStream.read(a);
            while(len > 0){
                sb.append(new String(a,0,len));
                len = fileInputStream.read(a);
            }
            fileInputStream.close();
        } catch (Exception e) {
            loge(e);
        }
        return sb.toString().substring(sb.toString().length()-2,sb.toString().length()-1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init(getApplicationContext());
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(readNode().equals("1")) isIRCutOn = true;
        else isIRCutOn = false;
        Log.d(TAG,"isIRCutOn = " + isIRCutOn);
        bindView();
    }
    void bindView() {
        setContentView(R.layout.ircut);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(IRCut.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        text = findViewById(R.id.ircut_tip);
        switchButton = (Button) findViewById(R.id.ircut_switch);
        passButton = (Button) findViewById(R.id.pass);
        failButton = (Button) findViewById(R.id.fail);
        passButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {

                setResult(RESULT_OK);
                Utils.writeCurMessage(mContext, TAG, "Pass");
                finish();
            }
        });
        failButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {

                setResult(RESULT_CANCELED);
                Utils.writeCurMessage(mContext, TAG, "Failed");
                finish();
            }
        });
        switchButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View arg0) {
                switchDevice(!isIRCutOn);
                isIRCutOn = !isIRCutOn;
            }
        });
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
        int cameraExist = CameraFront.FindCameraBackorFront();
        if( cameraExist == -1 ) //normal
        {
            try {
                mCamera = Camera.open(0);
            } catch (Exception exception) {
                mCamera = null;
            }
        }
        else if( cameraExist == 0 ) //only back
        {
            try {
                mCamera = Camera.open(0);
            } catch (Exception exception) {
                try {
                    mCamera = Camera.open(1);
                    Log.d(TAG, "camera error occurred , now open 1 ");
                } catch (Exception e1) {
                    mCamera = null;
                }
            }
        }
        else{ //only front or no camera
            mCamera = null;
        }
        if (mCamera == null) {
            text.setText("摄像头无法打开，点击按钮注意听滤光片是否有声音！");
        } else {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
                finish();
            }
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
                               int h) {

        logd("surfaceChanged +" +w+","+h);
        startCamera();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {

        logd("surfaceDestroyed");
        stopCamera();
    }

    private void startCamera() {

        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setRotation(CameraInfo.CAMERA_FACING_BACK);
                Size mSize = getOptimalPreviewSize(240,173);
                Log.d(TAG,"w =" + mSize.width + ",h =" + mSize.height);
                parameters.setPreviewSize(mSize.width,mSize.height);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                loge(e);
            }
        }

    }

    private void stopCamera() {
        if (mCamera != null) {
            try {
                if (mCamera.previewEnabled())
                    mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Size getOptimalPreviewSize( int w, int h) {
        List<android.hardware.Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w/h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
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
}
