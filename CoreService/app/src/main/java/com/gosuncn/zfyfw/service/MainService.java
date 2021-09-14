package com.gosuncn.zfyfw.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.gosuncn.zfyfw.EmptyActivity;
import com.gosuncn.zfyfw.MainApp;
import com.gosuncn.zfyfw.module.appinstall.InstallResultReceiver;
import com.gosuncn.zfyfw.module.appinstall.InstallUtils;
import com.gosuncn.zfyfw.module.appinstall.UninstallResultReceiver;
import com.gosuncn.zfyfw.nv.NvUtilExt;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gosuncn.providers.GosuncnSettings;
import vendor.qti.hardware.bodytemp.V1_0.IBodyTemp;
import vendor.qti.hardware.bodytemp.V1_0.IBodyTempCallback;

public class MainService extends Service {
    static final String TAG = MainService.class.getSimpleName();
    //    Context mContext;
    CoreService mCoreService;
//
//    private static MainService mMainService;
//
//    public static MainService getInstance() {
//        return mMainService;
//    }
//
//    public MainService(Application application) {
//        Log.d(TAG, "MainService #S");
//        mCoreService = new CoreService();
//
//        Log.d(TAG, "MainService mCoreService:"+mCoreService);
//
//        ServiceManager.addService(GSFWManager.SERVICE_NAME, mCoreService);
//        Log.d(TAG, "MainService addService #E");
//        mMainService = this;
//    }


    @Override
    public void onCreate() {
        super.onCreate();

        mCoreService = new CoreService();

        Log.d(TAG, "MainService mCoreService:" + mCoreService);

        ServiceManager.addService(GSFWManager.SERVICE_NAME, mCoreService);
        // register broadcasts
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

