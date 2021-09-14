package com.gosuncn.zfyhwapidemo.activity;

// Bug503, OTA - installpackage run failed, wmd, 2020.0624

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import com.gosuncn.zfyfw.api.LedManager;


import android.os.Handler;
import android.os.RecoverySystem;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;

import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.R;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;

public class UpdateActivity extends Activity {
    private static final String TAG = UpdateActivity.class.getSimpleName();
    private Button usbBtn;
    private boolean isUsbEnabled = false;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler(getMainLooper());
        usbBtn = new Button(this);
        setContentView(usbBtn);
        usbBtn.setText("Update");
        usbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isUsbEnabled = (isUsbEnabled ? false : true);
//                GSFWManager.getInstance().setMassStorageEnabled(isUsbEnabled);
                String sdcardPath = getStoragePath(UpdateActivity.this.getApplicationContext(), true);
                Log.d(TAG, "UpdateActivity 1 sdcardPath:"+sdcardPath);
                GSFWManager.getInstance().recoverySystemInstallPackage(sdcardPath + "/update/update.zip");

                try {
                    File updateFile = new File(sdcardPath + "/update/update.zip");
                    String canonicalPath = updateFile.getCanonicalPath();
                    Log.d(TAG, "UpdateActivity 1 canonicalPath:" + canonicalPath);
                    String absolutePath = updateFile.getAbsolutePath();
                    Log.d(TAG, "UpdateActivity 1 absolutePath:" + absolutePath);
                }catch (IOException ioe){

                }
//                Log.d(TAG, "UpdateActivity verifyPackage");
//                try {
//                    RecoverySystem.verifyPackage(updateFile, new RecoverySystem.ProgressListener() {
//                                @Override
//                                public void onProgress(int position) {
//                                    Log.d(TAG, "UpdateActivity onProgress position:" + position);
//                                    if (100 == position) {
//                                        mHandler.postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                Log.d(TAG, "UpdateActivity installPackage start");
//                                                try {
//                                                    RecoverySystem.installPackage(UpdateActivity.this.getApplicationContext(), updateFile);
//                                                }catch (IOException ioe){
//
//                                                }
//                                                Log.d(TAG, "UpdateActivity installPackage finished");
//                                            }
//                                        }, 2000);
//                                    }
//                                }
//                            },
//                            new File("/system/etc/security/otacerts.zip"));
//                }catch (IOException ioe){
//
//                }catch (GeneralSecurityException gse){
//
//                }



//                String downPath = UpdateActivity.this.getApplicationContext().getCacheDir().getAbsolutePath();
//                Log.d(TAG, "UpdateActivity 1 downPath:"+downPath);
//                // downPath:/data/user/0/com.gosuncn.zfyhwapidemo/cache
//
//                UpdateActivity(UpdateActivity.this.getApplicationContext(),
//                        new File(sdcardPath + "/apk/gxx_ptt_36_v3.6.0_201910251323.apk"));
            }
        });
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)usbBtn.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        usbBtn.setLayoutParams(params);
    }

    public void UpdateActivity(Context context, File apkFile) {

        Log.d(TAG, "UpdateActivity 1");
        if (apkFile == null || !apkFile.exists()) return;

        Log.d(TAG, "UpdateActivity 2");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        Uri uri = FileProvider.getUriForFile(context.getApplicationContext(),
                "com.gosuncn.zfyhwapidemo.fileprovider", apkFile);

//        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/9016-4EF8:apk/gxx_ptt_36_v3.6.0_201910251323.apk");

        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getCpuStats();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    private static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HardwarePropertiesManager mHardwarePropertiesManager = null;
    private void getCpuStats(){
        mHardwarePropertiesManager = (HardwarePropertiesManager) getSystemService(
                HARDWARE_PROPERTIES_SERVICE);
        CpuUsageInfo[] cpuUsageInfos = mHardwarePropertiesManager.getCpuUsages();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < cpuUsageInfos.length; i++) {
            sb.append("CPU" + i + ":" + cpuUsageInfos[i].getTotal() + "," + cpuUsageInfos[i].getActive() + "\n");
        }
        Log.d(TAG, "getCpuStats cpuUsageInfos:"+sb.toString());
    }
}
