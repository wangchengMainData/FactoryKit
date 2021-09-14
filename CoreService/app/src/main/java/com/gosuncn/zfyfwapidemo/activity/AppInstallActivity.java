package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import com.gosuncn.zfyfw.api.LedManager;


import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;

import com.gosuncn.zfyfw.module.appinstall.InstallResultReceiver;
import com.gosuncn.zfyfw.module.appinstall.InstallUtils;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.R;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;
import android.widget.LinearLayout;

public class AppInstallActivity extends Activity {
    private static final String TAG = AppInstallActivity.class.getSimpleName();
    private LinearLayout mRootView;
    private Button installBtn;
    private Button installQuietlyBtn;
    private Button uninstallQuietlyBtn;

    private boolean isUsbEnabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = new LinearLayout(this);
        mRootView.setOrientation(1);
        installBtn = new Button(this);
        setContentView(mRootView);
        installBtn.setText("App Install");
        installBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                isUsbEnabled = (isUsbEnabled ? false : true);
//                GSFWManager.getInstance().setMassStorageEnabled(isUsbEnabled);
                String sdcardPath = getStoragePath(AppInstallActivity.this.getApplicationContext(), true);
                Log.d(TAG, "startInstallActivity 1 sdcardPath:"+sdcardPath);
                String downPath = AppInstallActivity.this.getApplicationContext().getCacheDir().getAbsolutePath();
                Log.d(TAG, "startInstallActivity 1 downPath:"+downPath);
                // downPath:/data/user/0/com.gosuncn.zfyhwapidemo/cache

                startInstallActivity(AppInstallActivity.this.getApplicationContext(),
                        new File(sdcardPath + "/apk/gxx_ptt_36_v3.6.0_201910251323.apk"));
            }
        });
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,60
        );
        installBtn.setLayoutParams(params);
        mRootView.addView(installBtn);

        installQuietlyBtn = new Button(this);
        installQuietlyBtn.setText("App Install Quietly");
        installQuietlyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installQuietly();
            }
        });
        installQuietlyBtn.setLayoutParams(params);
        mRootView.addView(installQuietlyBtn);

        uninstallQuietlyBtn = new Button(this);
        uninstallQuietlyBtn.setText("App Uninstall Quietly");
        uninstallQuietlyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstallQuietly();
            }
        });
        uninstallQuietlyBtn.setLayoutParams(params);
        mRootView.addView(uninstallQuietlyBtn);

    }

    public void startInstallActivity(Context context, File apkFile) {

        Log.d(TAG, "startInstallActivity 1");
        if (apkFile == null || !apkFile.exists()) return;

        Log.d(TAG, "startInstallActivity 2");
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

    // PLM14773, install app quietly, wmd, 2020.0720
    private void installQuietly(){
        InstallUtils.install(AppInstallActivity.this, getStoragePath(AppInstallActivity.this.getApplicationContext(), true) +"/1.apk", InstallResultReceiver.class);
    }

    private void uninstallQuietly(){
        InstallUtils.uninstall(AppInstallActivity.this, "com.hgs.ptt");
    }
}
