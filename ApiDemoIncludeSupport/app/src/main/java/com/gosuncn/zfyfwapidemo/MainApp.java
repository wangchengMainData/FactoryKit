package com.gosuncn.zfyhwapidemo;

import android.app.Application;
import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {
    private static String TAG = MainApp.class.getSimpleName();

    private static MainApp mMainApp;
    private static Context mContext;
    public List<? extends Map<String, ?>> mItemList;

    public static MainApp getInstance() {
        if (mMainApp == null)
            mMainApp = new MainApp();
        return mMainApp;
    }

    public MainApp() {
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();
    }

    public static class ApiItem {
        String key;
        String name;
        String enable;
        String classname;
    }
}
