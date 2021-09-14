/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.SDCard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import java.io.File;

import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;

public class SDCard extends BaseActivity {

	TextView mTextView;
	String TAG = "SDCard";
	private boolean hasSDCard = false;
	private TextView interMem,externalMem,external_tips;
	private String sdcardFsType = "";
	StorageManager mStorageManager;
	@Override
	public void finish() {

		super.finish();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sdcard);
		interMem = findViewById(R.id.sdcard_intel);
		externalMem = findViewById(R.id.sdcard_external);
		external_tips = findViewById(R.id.sdcard_external_tips);
		mStorageManager = (StorageManager)this.getSystemService(Context.STORAGE_SERVICE);

//		setContentView(loadDefaultConfirmText(R.string.memory_check));

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			exec("cat /proc/partitions");
		}
		else {
			showConfirmDialog(this);
		}
	}

	public void getInterMemInfo()
	{
		File path = Environment.getExternalStorageDirectory();
		String str =getString(R.string.sdcard_usage)+ String.valueOf(getAvailableStorgeSize(path))+"/"+String.valueOf(getTotalStorgeSize(path))+"MB";
		interMem.setText(str);

	}

	private String getSdcardFsType(String path){
		if(TextUtils.isEmpty(path)){
			return "";
		}
		try {
			StorageManager sm = SDCard.this.getSystemService(StorageManager.class);
			final List<VolumeInfo> volumes = sm.getVolumes();
			for (VolumeInfo vol : volumes) {
				DiskInfo diskInfo = vol.getDisk();
				if (diskInfo != null && diskInfo.isSd()) {
					Log.d(TAG, "getSdcardFsType path:" + path + " volPath:" + vol.getPath().getAbsolutePath());
					if (path.contains(vol.getPath().getAbsolutePath())) {
						return vol.fsType;
					}
					break;
				}
			}
		}catch (Exception e){

		}
		return "";
	}

	public void getExternalMemInfo()
	{
		File path = new File(getAppRootOfSdCardRemovable());
		String str= getString(R.string.sdcard_usage) + getAvailableStorgeSize(path) + "/"+ getTotalStorgeSize(path) + "MB";
		externalMem.setText(str);
		if(path != null) {
			sdcardFsType = getSdcardFsType(path.getAbsolutePath());
		}
		external_tips.setText(getString(R.string.sdcard_external_tips) + " [ " + sdcardFsType + " ] ");

		if(!"vfat".equals(sdcardFsType)){
			external_tips.setTextColor(getResources().getColor(R.color.red));
		}
	}

	private String getAppRootOfSdCardRemovable()
	{
		String dir = "default";
		StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		Class<?> storageVolumeClazz = null;
		try
		{
			storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Class<VolumeInfo> volumeInfoClass = (Class<VolumeInfo>) Class.forName("android.os.storage.VolumeInfo");
			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
			Object result = getVolumeList.invoke(mStorageManager);
			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++)
			{
				Object storageVolumeElement = Array.get(result, i);
				String path = (String) getPath.invoke(storageVolumeElement);
				File file = new File(path);
				boolean removable = (Boolean)isRemovable.invoke(storageVolumeElement);
				if(removable && file.exists() && file.isDirectory() )
				{
					dir = path;
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return dir;

	}

	public  double getTotalStorgeSize(File path) {
		StatFs mStatFs = new StatFs(path.getPath());
		long blockSize = mStatFs.getBlockSize();
		long totalBlocks = mStatFs.getBlockCount();
		DecimalFormat df = new DecimalFormat("#.00");
		return Double.valueOf(df.format(((totalBlocks * blockSize) / 1024.0 / 1024.0)));
	}

	public  double getAvailableStorgeSize(File path) {

		StatFs mStatFs = new StatFs(path.getPath());
		long blockSize = mStatFs.getBlockSize();
		long availableBlocks = mStatFs.getAvailableBlocks();
		DecimalFormat df = new DecimalFormat("#.00");
		return Double.valueOf(df.format(((availableBlocks * blockSize) / 1024.0 / 1024.0)));
	}

	Handler mHandler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {


			if(Values.AUTO_TEST_ENABLED/* || Framework.quickTestEnabled*/) {
				boolean res = (Boolean) msg.obj;
				if (res)
					pass();

				else
					fail(null);
			}else{
				getInterMemInfo();
				if(getAppRootOfSdCardRemovable().equals("default"))
				{
					external_tips.setText(R.string.sdcard_unmounted);
				}else {
					getExternalMemInfo();
					if(Framework.quickTestEnabled && "vfat".equals(sdcardFsType)){
						pass();
					}
				}
			}

		};
	};

	void exec(final String para) {

		new Thread() {

			public void run() {
				try {
					logd(para);
					String data = Utils.readFile("/proc/partitions");

					logd(data);
					Message message = new Message();
					if (data.contains("mmcblk1")) {
						message.obj = true;
						hasSDCard = true;
					}else
						message.obj = false;
					message.setTarget(mHandler);
					message.sendToTarget();

				} catch (Exception e) {
					logd(e);
				}

			}
		}.start();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPositiveCallback() {
		pass();
	}

	@Override
	protected void onNegativeCallback() {
		fail(null);
	}





	void showConfirmDialog(final Context context) {

		new AlertDialog.Builder(context)
				.setTitle(getString(R.string.sdcard_confirm))
				.setPositiveButton(getString(R.string.yes),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								fail(null);
							}
						})
				.setNegativeButton(getString(R.string.no),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								toast(getString(R.string.sdcard_to_insert));
							}
						}).setCancelable(false).show();
	}

	void fail(Object msg) {

		loge(msg);
		toast(msg);
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
}
