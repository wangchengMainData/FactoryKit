package com.gosuncn.zfyfactorytest.EngineeringCamera;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.os.CountDownTimer;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.api.LedManager;
import android.hardware.Camera.Size;
import java.util.List;
import android.util.Log;

import com.gosuncn.zfyfactorytest.R;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class EngineeringCamera extends Activity{
    private static final String TAG="EngineeringCamera";
    private static final int FAST_CLICK_DELAY_TIME = 1500;
    private static long lastClickTime = 0;
    private final int FILECOUNT = 10;
    private final int MAX_VIDEO_LENGTH = 30; // s
    private final int GREEN = 0;
    private final int RED = 1;
    private final int BLUE = 2;
    private final int OFF = 3;
    private final int INIT_COLOR_NUM = 4;
    private int colorNum = INIT_COLOR_NUM;
    private int ledcolor = GREEN;
    private Boolean isSoundRecording = false;
    private Boolean isVideoRecording = false;
    private Boolean safetoTakepicture = false;
    private  String mphotoname="photo";
    private  String mvideoname="video";
    //photo filepath, /storage/self/primary/engineeringTestFile
    private static String PATH=Environment.getExternalStorageDirectory()+"/engineeringTestFile/";
    private ImageView mphotoButton;
    private Camera mCamera;
    private SurfaceView msurfaceview;
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mholder;
    private Button mtakephoto,msoundrecord,mvideotape;
    private Vibrator mvibrator;
    private CameraManager mcameraManager;//FLS
    private MediaPlayer mmediaPlayer;//SPK
    private Camera.PictureCallback mPicturecallback;
    private CountDownTimer mCountDownTimer;
    private File[] allfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.engineeringcamera);
        mphotoButton = findViewById(R.id.img_camera);
        msurfaceview = findViewById( R.id.surfaceView);
        mcameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
        mCountDownTimer = new CountDownTimer(  //ledtimecounter
                (colorNum * 3) * 1000 + 200, 1000) {
            public void onTick(long arg0) {
                setColor((ledcolor++%4));
            }
            public void onFinish() {
                setColor(OFF);
            }
        };
        mholder = msurfaceview.getHolder();
        mholder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mCamera = getCamera();
                if (mCamera != null) {
                    try {
                        Camera.Parameters parameters = mCamera.getParameters();
                        Size mSize = getOptimalPreviewSize(240,320);
                        Log.d(TAG,"w =" + mSize.width + ",h =" + mSize.height);
                        parameters.setPreviewSize(mSize.width,mSize.height);
                        parameters.setPictureSize(864,480);
                        mCamera.setParameters(parameters);
                        mCamera.setDisplayOrientation(0);
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();
                        safetoTakepicture = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                safetoTakepicture = true;
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCamera();
                safetoTakepicture = false;
            }
        });
        mholder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCamera=getCamera();
        if (mCamera!=null){
            try {
                mCamera.setPreviewDisplay( mholder );
                mCamera.startPreview();
                safetoTakepicture = true;
            } catch (IOException e) {
                releaseCamera();
                e.printStackTrace();
            }
        }

        mPicturecallback = new Camera.PictureCallback() {//handle photo
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                if (null != data) {
                    safetoTakepicture = false;
                    mCamera.stopPreview();
                    File tempFileDir = new File(PATH);
                    if (!tempFileDir.exists()) {
                        tempFileDir.mkdir();
                    }
                    CountLimit();//numcheck
                    File tempFile = new File(tempFileDir, mphotoname + System.currentTimeMillis() + ".jpg");
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    bitmap = rotateBitmap(bitmap, 0);
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                        try {
                            bos.flush();
                            bos.close();
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                    Toast.makeText(getBaseContext(), "拍照成功", Toast.LENGTH_SHORT).show();
                    mCamera.startPreview();
                    safetoTakepicture = true;
                }
            }
        };
        mphotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMediaRecorder != null)
                Toast.makeText(getBaseContext(),"请先停止录像",Toast.LENGTH_SHORT).show();
                else
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        setColor(OFF);
        releaseCamera();
        enableFlashIR(false);
        if(isSoundRecording) {
            mmediaPlayer.stop();
            mmediaPlayer.release();
            mvibrator.cancel();
            turnLightOff();
        }
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        StopVideoRecording();
        super.onDestroy();
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
//    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
//        if (degrees == 0 || null == bitmap) {
//            return bitmap;
//        }
//        Matrix matrix = new Matrix();
//        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
//        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        bitmap.recycle();
//        return bmp;
//    }

    private void releaseCamera(){
        if (mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
    }

    public synchronized  void turnLightOn() {//flashlight on
        if (mCamera == null) {
            return;
        }
        Camera.Parameters mParameters = mCamera.getParameters();
        if (mParameters == null) {
            return;
        }
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(mParameters);
    }

    public synchronized  void turnLightOff() {//flash light off
        if (mCamera == null) {
            return;
        }
        Camera.Parameters mParameters = mCamera.getParameters();
        if (mParameters == null) {
            return;
        }
        mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(mParameters);
    }

    public void StartVideoRecording(){
        mCamera.unlock();
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//sound
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//video
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        mMediaRecorder.setMaxDuration(MAX_VIDEO_LENGTH * 1000);//30s
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener(){
            @Override
            public void onInfo(MediaRecorder mr,int what,int extra){
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    turnOffVideoKey();
                }
            }
        });
        File tempfilEDir = new File(PATH);
        if(!tempfilEDir.exists())
            tempfilEDir.mkdir();
        CountLimit();
        mMediaRecorder.setOutputFile(PATH+ mvideoname
        +System.currentTimeMillis()+".mp4");
        mMediaRecorder.setPreviewDisplay(mholder.getSurface());
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (IOException e) {
            StopVideoRecording();
            e.printStackTrace();
        }
    }

    private void turnOffVideoKey()
    {
        StopVideoRecording();
        enableFlashIR(false);
        setColor(OFF);
        mCountDownTimer.cancel();
        isVideoRecording = false;
        mphotoButton.setImageResource(R.drawable.go_back_normal);
        if (isSoundRecording) {
            mmediaPlayer.stop();
            mmediaPlayer.release();
            mvibrator.cancel();
            turnLightOff();
            isSoundRecording = false;
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

    public void StopVideoRecording(){
        if(mMediaRecorder != null) {
            mCamera.lock();
//            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private void enableFlashIR(boolean state){//irledcontrol
        if(state){
            LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_IRLED);
        }else{
            LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_IRLED);
        }
    }

    private void CountLimit()
    {
        File file = new File(PATH);
        allfiles = file.listFiles();
        String[] all = file.list();
        if(allfiles.length < FILECOUNT) {
            Log.d(TAG, "all :" + all + ", all file :" + allfiles);
            return;
        }
        else {
            try {
                allfiles[allfiles.length - 1 ].delete();
            }catch (Exception e)
            {
                Log.d(TAG,"delete() fail");
            }
        }
    }

    private void setColor(int color) {//ledcontrol

        boolean red = false, green = false, blue = false;
        switch (color) {
            case RED:
                red = true;
                LedManager.getInstance().setLedColor(LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_RED);
                break;
            case GREEN:
                green = true;
                LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_BATTERY);
                break;
            case BLUE:
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
                LedManager.getInstance().setLedColor(LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_YELLOW);
                blue = true;
                break;
            case OFF:
            default:
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
                break;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case GSFWManager.KEYCODE_HOME: return true;
            case GSFWManager.KEYCODE_BACK:return true;
            case GSFWManager.KEYCODE_MENU:return true;
            case GSFWManager.KEYCODE_PTT: return true;
            case GSFWManager.KEYCODE_SOS: return true;
            case GSFWManager.KEYCODE_MARK: return true;
            case GSFWManager.KEYCODE_POWER: return true;
            case GSFWManager.KEYCODE_CAMERA:
            try {
                if (safetoTakepicture) {
                    mCamera.takePicture(null, null, mPicturecallback);
                }
            }catch(Exception e)
            {
                e.printStackTrace();
                Log.d(TAG,"picturecallback fail");
            }
                return true;
            case GSFWManager.KEYCODE_AUDIO:
                if(!isSoundRecording) {
                    mphotoButton.setImageResource(R.drawable.go_back_pressed);
                    turnLightOn();
                    mvibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);//震动
                    mvibrator.vibrate(new long[]{500,1000,500,2000}, 1);
                    mmediaPlayer = MediaPlayer.create(EngineeringCamera.this, R.raw.agingtest_test);
                    mmediaPlayer.setLooping(true);
                    mmediaPlayer.start();
                    isSoundRecording = true;
                }
                return true;
            case GSFWManager.KEYCODE_VIDEO:
                long currentClickTime = System.currentTimeMillis();
                    if (!isVideoRecording) {
                        StartVideoRecording();
                        enableFlashIR(true);
                        mCountDownTimer.start();
                        isVideoRecording = true;
                        mphotoButton.setImageResource(R.drawable.go_back_pressed);
                    } else if(currentClickTime - lastClickTime <= FAST_CLICK_DELAY_TIME)
                    {
                        Toast.makeText(getBaseContext(),"操作过快",Toast.LENGTH_SHORT).show();
                    }else {
                        turnOffVideoKey();
                }
                lastClickTime = currentClickTime;
                return true;
        }
        return super.onKeyDown(keyCode, event);
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
