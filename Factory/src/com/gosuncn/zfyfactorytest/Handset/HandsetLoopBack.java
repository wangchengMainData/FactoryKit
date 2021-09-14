/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Handset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.media.MediaPlayer.OnCompletionListener;
import android.widget.TextView;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import android.Manifest;
import android.os.Environment;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import static android.icu.lang.UCharacter.LineBreak.SPACE;
import static android.os.Build.VERSION_CODES.BASE;
import static java.lang.Thread.sleep;
import android.os.Handler;
import com.gosuncn.zfyfw.service.GSFWManager;

public class HandsetLoopBack extends Activity {
	static String TAG = "HandsetLoopBack";
	static final int MAX_LENGTH = 1000 * 60 * 10;
	String mAudiofilePath = Environment.getExternalStorageDirectory() + "/abest/";
	Button passButton;
	Button failButton;
	Button mStartorStop;
	AudioManager mAudioManager;
	Context mContext;
	Boolean isRecording = false;
	MediaRecorder mMediaRecorder;
	MediaPlayer mMediaPlayer;
	TextView recordflag, volumeNow;
	private int defaultVolume;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logd("");
		setContentView(R.layout.handset_loopback);
		mStartorStop = findViewById(com.gosuncn.zfyfactorytest.R.id.handsetloopback_startorstop);
		recordflag = findViewById(com.gosuncn.zfyfactorytest.R.id.handsetloopback_flag);
		GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
		volumeNow = findViewById(com.gosuncn.zfyfactorytest.R.id.handsetloopback_volume);
		volumeNow.setText(getString(R.string.handsetloopback_volume)+":0 db");
		volumeNow.setTextSize(40);
		volumeNow.setTextColor(getResources().getColor(R.color.red));
		getService();
		init();
		setAudio();
		mStartorStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isRecording) {
					try {
						mStartorStop.setText(R.string.handsetloopback_stop_to_play);
						record();
						isRecording = true;
						recordflag.setText(R.string.handsetloopback_recording);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					cancelRecord();
					isRecording = false;
					mStartorStop.setText(R.string.handsetloopback_start_record);
					recordflag.setText(R.string.handsetloopback_playing);
					try {
						replay();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	@Override
	protected void onResume() {
		mHandler.removeCallbacks(mUpdateMicStatusTimer);
		super.onResume();
	}

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mUpdateMicStatusTimer);
		super.onPause();
	}

	@Override
	public void finish() {
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION,
				AudioSystem.FORCE_NONE);
		mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,defaultVolume,0);
		cancelRecord();
		cancelReplay();
		mHandler.removeCallbacks(mUpdateMicStatusTimer);
		super.finish();
	}

	void cancelReplay() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	void showWarningDialog(String title) {

		new AlertDialog.Builder(this)
				.setTitle(title)
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {

							}
						}).setCancelable(false).show();
	}

	public void setAudio() {
//		mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//		 mAudioManager.setSpeakerphoneOn(false);
//		AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION,
//				AudioSystem.FORCE_HEADPHONES);
		float ratio = 0.5f;
		defaultVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,maxVolume,0);
	}

	void init() {
		mContext = getApplicationContext();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		passButton = (Button) findViewById(R.id.pass);
		failButton = (Button) findViewById(R.id.fail);

		passButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				pass();
			}
		});

		failButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				fail("");

			}
		});

	}

	void record() throws IllegalStateException, IOException {
		cancelReplay();
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		File tempfilEDir = new File(mAudiofilePath);
		if (!tempfilEDir.exists())
			tempfilEDir.mkdir();
		mMediaRecorder.setOutputFile(mAudiofilePath
				+ "test.wav");
		mMediaRecorder.prepare();
		mMediaRecorder.start();
		updateMicStatus();
	}

	void replay() throws IllegalArgumentException, IllegalStateException,
			IOException {
		// mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		// Replaying sound right now by record();
		File file = new File(mAudiofilePath + "test.wav");
		FileInputStream mFileInputStream = new FileInputStream(file);
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.reset();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		mMediaPlayer.setDataSource(mFileInputStream.getFD());
		mMediaPlayer.prepare();
		mMediaPlayer.start();
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mPlayer) {
				recordflag.setText(R.string.handsetloopback_record_again);
				cancelReplay();
				File file = new File(mAudiofilePath);
				file.delete();
			}
		});

	}

	void cancelRecord() {
		if (mMediaRecorder != null) {
			mMediaRecorder.setOnErrorListener(null);
			mMediaRecorder.setPreviewDisplay(null);
			try {
				mMediaRecorder.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}


	void getService() {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	private void updateMicStatus() {
		mHandler.removeCallbacks(mUpdateMicStatusTimer);
		if (mMediaRecorder != null) {
			int ratio = mMediaRecorder.getMaxAmplitude() / BASE;
			int db = 0;
			if (ratio > 1)
				db = (int) (20 * Math.log10(ratio));
			if(db > 0) {
				volumeNow.setText(getString(R.string.handsetloopback_volume)+":"+String.valueOf(db)+" db");
				volumeNow.setTextSize(40);
				volumeNow.setTextColor(getResources().getColor(R.color.green));
			}
			mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
		}
	}

	private final Handler mHandler = new Handler();
	private Runnable mUpdateMicStatusTimer = new Runnable() {
		public void run() {
			try {
				updateMicStatus();
				sleep(400);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	void fail(Object msg) {

		if (msg != null) {
			loge(msg);
		}
		setResult(RESULT_CANCELED);
		Utils.writeCurMessage(this, TAG, "Failed");
		finish();
	}

	void pass() {

		setResult(RESULT_OK);
		Utils.writeCurMessage(this, TAG, "Pass");
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

	@SuppressWarnings("unused")
	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				return true;
			case KeyEvent.KEYCODE_MENU:
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
