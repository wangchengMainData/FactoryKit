package com.gosuncn.zfyfw;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;
import android.os.Process;
import android.view.ThreadedRenderer;
import android.util.Log;
import android.content.Context;
import java.util.Iterator;
import java.util.List;

import com.gosuncn.zfyfw.service.MainService;

public class MainApp extends Application {

    static final String TAG = MainApp.class.getSimpleName();

    MainService mMainService;
    public static Context mContext;
    public MainApp() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        boolean isMainProcess = false;
		mContext = getApplicationContext();
        Log.d(TAG, "onCreate #S");
        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List processes = am.getRunningAppProcesses();
        Iterator i = processes.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo appInfo = (ActivityManager.RunningAppProcessInfo)(i.next());
            if (appInfo.pid == Process.myPid()) {
                Log.d(TAG, "onCreate processName:" + appInfo.processName);
                isMainProcess =  (getApplicationContext().getPackageName().equals(appInfo.processName));
                break;
            }
        }
        Log.d(TAG, "onCreate isMainProcess:" + isMainProcess);
        if (isMainProcess) {
            Log.d(TAG, "onCreate MainService #S");
//            mMainService = new MainService(this);
            // start MainService service
            startService(new Intent(this,MainService.class));
            Log.d(TAG, "onCreate MainService #E");
            ThreadedRenderer.enableForegroundTrimming();
        }
        Log.d(TAG, "onCreate #E");
    }
}
