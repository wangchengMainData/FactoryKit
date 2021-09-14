/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.CameraFront;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ViewfinderView;
import com.journeyapps.barcodescanner.camera.CameraSettings;


public class CameraFront extends Activity {

	private Button passButton, failButton;
	private String resultString = Utils.RESULT_FAIL;
	final static String TAG = "CameraFront";
	private Context mContext = null;
	private CaptureManager capture;
	private DecoratedBarcodeView barcodeView;
	private ViewfinderView vfv;
	private TextView status_text;
	private int camera_situation ;
	private boolean beephasbeenplayed = false;
	private TextView result_text;
	private static final int BEEPNOW = 1;
	SoundPool soundPool;
	Vibrator mVibrator;
	private AudioManager audioManager;
	private int defaultVolume = 1;

	@Override
	public void finish() {
		Utils.writeCurMessage(TAG, resultString);
		logd(resultString);
		if(mVibrator != null || soundPool != null) {
			Log.d(TAG,"finish() release");
			mVibrator.cancel();
			soundPool.release();
		}
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,defaultVolume,0);
		super.finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
		audioManager =(AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);
		if(audioManager != null) {
			Log.d(TAG,"here");
			defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,0);
		}
		capture.onResume();
	}

	protected void onPause() {
		super.onPause();
		if(mVibrator != null || soundPool != null) {
			Log.d(TAG,"finish() release");
			mVibrator.cancel();
			soundPool.release();
		}
		capture.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		capture.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		camera_situation = FindCameraBackorFront();
		setResult(RESULT_CANCELED);
		bindView();
		init();
		capture = new CaptureManager(this, barcodeView);
		barcodeView.decodeContinuous(barcodeCallback);
		CameraSettings camerasettings = barcodeView.getCameraSettings();
		openCameraFront(camerasettings);
        if (camera_situation == 0 || camera_situation == -2) { //only back or no camera
            fail();
            toast(getResources().getString(R.string.cameraback_fail_open));
        }
	}


	void bindView() {

		setContentView(R.layout.camera_front);
		vfv = findViewById(R.id.zxing_viewfinder_view);
		barcodeView = findViewById(R.id.zxing_barcode_scanner);
		status_text = findViewById(R.id.status_text);
		passButton = (Button) findViewById(R.id.camera_pass);
		failButton = (Button) findViewById(R.id.camera_fail);
		result_text = findViewById(R.id.result);
	}

	void pass() {
		setResult(RESULT_OK);
		Utils.writeCurMessage(mContext, TAG, "Pass");
		finish();
	}

	void fail() {
		setResult(RESULT_CANCELED);
		Utils.writeCurMessage(mContext, TAG, "Failed");
		finish();
	}

	void init() {
		vfv.setMaskColor(getResources().getColor(android.R.color.transparent));
		vfv.setLaserVisibility(false);

		status_text.setText(getResources().getString(R.string.camerafront_cameratocode));
		passButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				pass();
			}
		});
		failButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {
				fail();
			}
		});

	}
	private void play(){
		mVibrator = (Vibrator)getApplicationContext().getSystemService(VIBRATOR_SERVICE);
		mVibrator.vibrate(100);
		if (Build.VERSION.SDK_INT >= 21) {
			SoundPool.Builder builder = new SoundPool.Builder();
			builder.setMaxStreams(1);
			AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
			attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
			builder.setAudioAttributes(attrBuilder.build());
			soundPool = builder.build();
		} else {
			soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		}
		final int voiceId = soundPool.load(getApplicationContext(), R.raw.tip, 1);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
				if (status == 0) {
					soundPool.play(voiceId, 1, 1, 1, 0, 1);
				}
			}
		});
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(BEEPNOW == msg.what){
				play();
			}
		}
	};
	private BarcodeCallback barcodeCallback = new BarcodeCallback() {
		@Override
		public void barcodeResult(BarcodeResult result) {
			if (result != null) {
				if(!beephasbeenplayed){
					Message msg = handler.obtainMessage();
					msg.what = BEEPNOW;
					handler.sendMessage(msg);
					beephasbeenplayed = true;
				}
				if (Values.AUTO_TEST_ENABLED || Framework.quickTestEnabled) {
					status_text.setTextColor(getResources().getColor(R.color.green));
					status_text.setText(getResources().getString(R.string.cameraback_codeconfirm));
					result_text.setText(result.getText());
                    result_text.setTextColor(getResources().getColor(android.R.color.holo_green_light));
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							pass();
						}
					},1000);
				} else {
					status_text.setTextColor(getResources().getColor(R.color.green));
					status_text.setText(getResources().getString(R.string.cameraback_codeconfirm));
					result_text.setText(result.getText());
                    result_text.setTextColor(getResources().getColor(android.R.color.holo_green_light));

//                    db.setVisibility(View.GONE);
//                    mSurfaceView.setVisibility(View.VISIBLE);
				}
			}
		}
	};

//	private void startCamera() {
//
//		if (mCamera != null) {
//			try {
//				Camera.Parameters parameters = mCamera.getParameters();
//				parameters.setPictureFormat(PixelFormat.JPEG);
//				// parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//				parameters.setRotation(CameraInfo.CAMERA_FACING_BACK);
//				Size mSize = getOptimalPreviewSize(240,320);
//				Log.d(TAG,"w =" + mSize.width + ",h =" + mSize.height);
//				parameters.setPreviewSize(mSize.width,mSize.height);
//				mCamera.setParameters(parameters);
//				mCamera.startPreview();
//			} catch (Exception e) {
//				loge(e);
//			}
//		}

//	}
//	private Size getOptimalPreviewSize( int w, int h) {
//		List<android.hardware.Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
//		final double ASPECT_TOLERANCE = 0.1;
//		double targetRatio = (double) w/h;
//		if (sizes == null) return null;
//
//		Size optimalSize = null;
//		double minDiff = Double.MAX_VALUE;
//
//		int targetHeight = h;
//
//		// Try to find an size match aspect ratio and size
//		for (Size size : sizes) {
//			double ratio = (double) size.width / size.height;
//			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
//			if (Math.abs(size.height - targetHeight) < minDiff) {
//				optimalSize = size;
//				minDiff = Math.abs(size.height - targetHeight);
//			}
//		}
//
//		// Cannot find the one match the aspect ratio, ignore the requirement
//		if (optimalSize == null) {
//			minDiff = Double.MAX_VALUE;
//			for (Size size : sizes) {
//				if (Math.abs(size.height - targetHeight) < minDiff) {
//					optimalSize = size;
//					minDiff = Math.abs(size.height - targetHeight);
//				}
//			}
//		}
//		return optimalSize;
//	}

//	private void stopCamera() {
//
//		if (mCamera != null) {
//			try {
//				if (mCamera.previewEnabled())
//					mCamera.stopPreview();
//				mCamera.release();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public static int FindCameraBackorFront()
	{
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		if(cameraCount == 2) return -1;    //2 camera
		else { //only one camera       	   //1 camera
			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					return 0;   //back
				} else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					return 1;	//front
				}
			}
		}
		return -2;// 0 camera
	}

	private void logd(Object d) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		d = "[" + mMethodName + "] " + d;
		Log.d(TAG, d + "");
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

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}

	public void openCameraFront(CameraSettings camerasettings){
		if( camera_situation == -1 ) //normal
		{
			camerasettings.setCameraId(1);
		}
		else if( camera_situation == 1 ) //only front
		{
			try {
				camerasettings.setCameraId(1);
			} catch (Exception e) {
					camerasettings.setCameraId(0);//该情况异常，根据函数获取到exist值１，表示前摄
					// 存在，但是无法open ，open(0)会打开前摄，王成，0807．＊该场景：后摄像头有问题＊
					Log.d(TAG, "camera error occurred , now open 0 ");
			}
		}
		else{ //only back or no camera
			toast(getString(R.string.cameraback_fail_open));
			fail();
		}
	}
}
