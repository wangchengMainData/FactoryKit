package com.gosuncn.zfyfw.service;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gosuncn.zfyfw.service.ICoreService;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;

import static android.content.Context.USB_SERVICE;

/**
 * GSFWManager类用于访问系统API
 */
public class GSFWManager {
    private static final String TAG = GSFWManager.class.getSimpleName();
    private static final boolean DEBUG = !"user".equals(Build.TYPE);
    public static final String SERVICE_NAME = "zfyfw";

    // "com.gosuncn.zfyfw.service.ICoreService"
    public static final int BINDER_CODE_DISABLE_SPECIFIC_SYSTEMAPP = 5001;
    public static final int BINDER_CODE_IS_SPECIFIC_SYSTEMAPP = 5002;
    public static final int BINDER_CODE_BODYTEMP_REQUEST = 5003;
    public static final int BINDER_CODE_UPDATE_REQUEST = 5004;
    public static final int BINDER_CODE_FACTORY_FLAG = 5005;
    public static final int BINDER_CODE_SILENT_INSTALL = 5006;
    public static final int BINDER_CODE_SILENT_UNINSTALL = 5007;
    public static final int BINDER_CODE_MOBILEDATA = 5008;
    public static final int BINDER_CODE_GET_USBFUNCTIONS = 5009;
    public static final int BINDER_CODE_SET_USBFUNCIION = 5010;
    public static final int BINDER_CODE_WLANMODE = 5011;
    public static final int BINDER_CODE_SET_ADB_INSTALLUNINSTALL_POLICIES = 5012;
    public static final int BINDER_CODE_GET_ADB_INSTALLUNINSTALL_POLICIES = 5013;
    public static final int BINDER_CODE_INSTALL_RESULT = 5014;
    public static final int BINDER_CODE_UNINSTALL_RESULT = 5015;
    public static final int BINDER_CODE_GET_ROOT_STATE = 5016;
    public static final int BINDER_CODE_GET_SYSTEM_INTEGRITY = 5017;
    public static final int BINDER_CODE_LIST_CERTIFICATES = 5018;
	public static final int BINDER_CODE_ESTABLISHVPNCONNECTION = 5019;
    public static final int BINDER_CODE_DISESTABLISHVPNCONNECTION = 5020;
	public static final int BINDER_CODE_GET_FACTROY_FLAG = 5021;
    public static final int BINDER_CODE_GET_FLASH_POLICY = 5022;
    public static final int BINDER_CODE_SET_SYSTEM_TIME = 5023;
		
    private Method getStringMethod = null;
    private static GSFWManager mGSFWManager;
    private ICoreService mICoreService;


    private GSFWManager() {
        mICoreService = getServiceInterface();
    }

    /**
     * 获取GSFWManager实例
     *
     * @return
     */
    public static GSFWManager getInstance() {
        if (mGSFWManager == null) {
            synchronized (GSFWManager.class) {
                if (mGSFWManager == null) {
                    mGSFWManager = new GSFWManager();
                }
            }
        }

        return mGSFWManager;
    }

    /* ############################################################################################# */
    /*                                           Common                                              */
    /* ############################################################################################# */

    private static ICoreService getServiceInterface() {
        /* get a handle to NFC service */
        IBinder b = null;

        //b = ServiceManager.getService(SERVICE_NAME);

        try {
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
            b = (IBinder) getServiceMethod.invoke(null, new Object[]{SERVICE_NAME});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(DEBUG) {
            Log.d(TAG, "getServiceInterface b:" + b);
        }

        if (b == null) {
            return null;
        }
        return ICoreService.Stub.asInterface(b);
    }

    public boolean registerSettingsContentObserver(int type, ISettingsContentObserver observer){
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            if(DEBUG) {
                Log.d(TAG, "registerSettingsContentObserver #S mICoreService:" + mICoreService);
            }
            result = mICoreService.registerSettingsContentObserver(type, observer);
            if(DEBUG) {
                Log.d(TAG, "registerSettingsContentObserver #E mICoreService:" + mICoreService);
            }
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean unregisterSettingsContentObserver(ISettingsContentObserver observer){
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            if(DEBUG) {
                Log.d(TAG, "unregisterSettingsContentObserver #S mICoreService:" + mICoreService);
            }
            result = mICoreService.unregisterSettingsContentObserver(observer);
            if(DEBUG) {
                Log.d(TAG, "unregisterSettingsContentObserver #E mICoreService:" + mICoreService);
            }
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                           Common                                              */
    /* ############################################################################################# */

    public static final int REMOTE_CALLBACK_TYPE_TEMP = 9001;

    /* ############################################################################################# */
    /*                                           测温                                                 */
    /* ############################################################################################# */

    /**
     * 注册请求体温回调
     * @param observer
     * @return
     */
    public boolean registerBodyTempCallback(ISettingsContentObserver observer){
        return registerSettingsContentObserver(REMOTE_CALLBACK_TYPE_TEMP, observer);
    }

    /**
     * 注销体温请求回调
     * @param observer
     * @return
     */
    public boolean unresigterBodyTempCallback(ISettingsContentObserver observer){
        return unregisterSettingsContentObserver(observer);
    }

    /**
     * 请求体温
     * @return
     */
    public boolean requestBodytemp() {
        boolean result = false;
        int type = REMOTE_CALLBACK_TYPE_TEMP;

        IBinder coreServiceIBinder = getCoreServiceIBinder();

        if (coreServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeInt(type);
            if(DEBUG) {
                Log.d(TAG, "requestBodytemp get service ibinder ok");
            }
            try {
                coreServiceIBinder.transact(GSFWManager.BINDER_CODE_BODYTEMP_REQUEST, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "requestBodytemp transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = true;
            } catch (RemoteException e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "requestBodytemp transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "requestBodytemp transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "requestBodytemp get service ibinder failed: null");
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                           按键                                                 */
    /* ############################################################################################# */

    /**
     * MENU按键
     */
    public static final int KEYCODE_MENU = 82;
    /**
     * HOME按键
     */
    public static final int KEYCODE_HOME = 3;
    /**
     * BACK按键
     */
    public static final int KEYCODE_BACK = 4;

    /**
     * Camera按键
     */
    public static final int KEYCODE_CAMERA = 66;
    /**
     * AUDIO按键
     */
    public static final int KEYCODE_AUDIO = 25;  // VOLUME_DOWN
    /**
     * POWER按键
     */
    public static final int KEYCODE_POWER = 26;

    /**
     * PTT按键，和KeyEvent.KEYCODE_PTT同步
     */
    public static final int KEYCODE_PTT = 88;
    /**
     * VIDEO按键，和KeyEvent.KEYCODE_VIDEO同步
     */
    public static final int KEYCODE_VIDEO = 24; // VOLUME_UP
    /**
     * MARK按键，和KeyEvent.KEYCODE_MARK同步
     */
    public static final int KEYCODE_MARK = 89;
    /**
     * SOS按键，和KeyEvent.KEYCODE_SOS同步
     */
    public static final int KEYCODE_SOS = 87;
    /**
     * 大电池插入，和KeyEvent.KEYCODE_BATIN同步
     */
    public static final int KEYCODE_BATIN = 355;
    /**
     * 大电池拔出，和KeyEvent.KEYCODE_BATOUT同步
     */
    public static final int KEYCODE_BATOUT = 356;

    /**
     * Home按键dispatached标志<br/>
     * 需要反射调用<br/>
     * 程序片段：<br/>
     * activity中：<br/>
     * public void onCreate(Bundle savedInstanceState) {<br/>
     * super.onCreate(savedInstanceState);<br/>
     * getWindow().addPrivateFlags(GSFWManager.FLAG_HOMEKEY_DISPATCHED);<br/>
     */
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x01000000; // Android 9 Pie

    /**
     * 设置Home Dispatched，App自己处理home按键
     * @param window
     * @return
     * 程序片段：<br/>
     * activity中：<br/>
     * import com.gosuncn.zfyfw.service.GSFWManager; <br/>
     * public void onCreate(Bundle savedInstanceState) {<br/>
     * super.onCreate(savedInstanceState);<br/>
     * GSFWManager.getInstance().setHomeKeyDispatched(getWindow()); <br/>
     */
    public boolean setHomeKeyDispatched(Window window){
        boolean result = false;
        try {
            Method goMethod = Window.class.getDeclaredMethod("addPrivateFlags", int.class);
            goMethod.setAccessible(true);
            goMethod.invoke(window, GSFWManager.FLAG_HOMEKEY_DISPATCHED);
            result = true;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                           按键广播                                             */
    /* ############################################################################################# */

    public static final String ACTION_KEYEVENT = "android.intent.action.gosuncn.zfy.KeyEvent";
    public static final String KEY_CODE = "keycode";
    public static final String KEY_TYPE = "type";
    public static final int KEYEVENT_CLICK = 1;
    public static final int KEYEVENT_LONGPRESS = 2;

    /* ############################################################################################# */
    /*                                           应用管理                                             */
    /* ############################################################################################# */

    // REQ264,preset apk,wmd,2020.0506



    IBinder mCoreServiceIBinder = null;

    public IBinder getCoreServiceIBinder() {
        if(mCoreServiceIBinder == null) {
            // 获取服务IBinder方式
            // 1.源码编译
            // Android.mk:去掉LOCAL_SDK_VERSION，使用LOCAL_PRIVATE_PLATFORM_APIS，如：
            // //LOCAL_SDK_VERSION := current
            // LOCAL_PRIVATE_PLATFORM_APIS := true
            //notificationIBinder = ServiceManager.checkService("package");

            // 2.IDE编译
            // 通过反射获取服务IBinder
            try {
                Method getServiceMethod = Class.forName("android.os.ServiceManager").
                        getDeclaredMethod("checkService", new Class[]{String.class});
                mCoreServiceIBinder = (IBinder) getServiceMethod.invoke(null, new Object[]{SERVICE_NAME});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mCoreServiceIBinder;
    }

    /**
     * 设置可卸载预置应用的状态
     * @param status 0-卸载 1-安装
     * @param apklist 应用报名列表
     * @return
     */
    public boolean setSpecificSystemAppsStatus(int status, List<String> apklist) {
        boolean result = false;

        IBinder coreServiceIBinder = getCoreServiceIBinder();

        if (coreServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeInt(status);
            data.writeStringList(apklist);
            if(DEBUG) {
                Log.d(TAG, "setSpecificSystemAppsStatus get service ibinder ok");
            }
            try {
                coreServiceIBinder.transact(GSFWManager.BINDER_CODE_DISABLE_SPECIFIC_SYSTEMAPP, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "setSpecificSystemAppsStatus transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = true;
            } catch (RemoteException e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "setSpecificSystemAppsStatus transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "setSpecificSystemAppsStatus transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "setSpecificSystemAppsStatus get service ibinder failed: null");
        }
        return result;
    }

    //REQ264,preset apk,wmd,2020.0518
    IBinder mPackageManagerIBinder = null;

    public IBinder getPackageManagerIBinder() {
        if(mPackageManagerIBinder == null) {
            // 获取服务IBinder方式
            // 1.源码编译
            // Android.mk:去掉LOCAL_SDK_VERSION，使用LOCAL_PRIVATE_PLATFORM_APIS，如：
            // //LOCAL_SDK_VERSION := current
            // LOCAL_PRIVATE_PLATFORM_APIS := true
            //notificationIBinder = ServiceManager.checkService("package");

            // 2.IDE编译
            // 通过反射获取服务IBinder
            try {
                Method getServiceMethod = Class.forName("android.os.ServiceManager").
                        getDeclaredMethod("checkService", new Class[]{String.class});
                mPackageManagerIBinder = (IBinder) getServiceMethod.invoke(null, new Object[]{"package"});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mPackageManagerIBinder;
    }

    /**
     * 判断是否是可卸载的预置应用
     * @param pkgName
     * @return
     */
    public boolean isSpecificSystemApps(String pkgName) {
        boolean result = false;

        IBinder packageManagerIBinder = getPackageManagerIBinder();

        if (packageManagerIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.content.pm.IPackageManager");
            data.writeString(pkgName);
            if(DEBUG) {
                Log.d(TAG, "isSpecificSystemApps get service ibinder ok");
            }
            try {
                packageManagerIBinder.transact(GSFWManager.BINDER_CODE_IS_SPECIFIC_SYSTEMAPP, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "isSpecificSystemApps transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = (intValue == 1) ? true : false;
            } catch (RemoteException e) {
                mPackageManagerIBinder = null;
                Log.d(TAG, "isSpecificSystemApps transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mPackageManagerIBinder = null;
                Log.d(TAG, "isSpecificSystemApps transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "isSpecificSystemApps get service ibinder failed: null");
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                           系统升级                                             */
    /* ############################################################################################# */
    public boolean recoverySystemInstallPackage(String updateFilePath) {
        boolean result = false;

        IBinder packageManagerIBinder = getPackageManagerIBinder();

        if (packageManagerIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.content.pm.IPackageManager");
            data.writeString(updateFilePath);
            if(DEBUG) {
                Log.d(TAG, "recoverySystemInstallPackage get service ibinder ok");
            }
            try {
                packageManagerIBinder.transact(GSFWManager.BINDER_CODE_UPDATE_REQUEST, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "recoverySystemInstallPackage transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = (intValue == 1) ? true : false;
            } catch (RemoteException e) {
                mPackageManagerIBinder = null;
                Log.d(TAG, "recoverySystemInstallPackage transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mPackageManagerIBinder = null;
                Log.d(TAG, "recoverySystemInstallPackage transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "recoverySystemInstallPackage get service ibinder failed: null");
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                           应用入口                                             */
    /* ############################################################################################# */

    /**
     * 拨号界面
     * @param context
     */
    public static void startDialerActivity(Context context) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + " "));
        context.startActivity(intent);
    }

    /**
     * 工程相机
     * @param context
     */
    public static void startEngineeringCameraActivity(Context context) {
        ComponentName componentName = new ComponentName("com.gosuncn.zfyfactorytest",
                "com.gosuncn.zfyfactorytest.EngineeringCamera.EngineeringCamera");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        context.startActivity(intent);
    }

    /**
     * 相机界面
     * @param context
     */
    public static void startSystemCameraActivity(Context context) {
        ComponentName componentName = new ComponentName("org.codeaurora.snapcam",
                "com.android.camera.CameraActivity");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 设置界面
     * @param context
     */
    public static void startSystemSettingsActivity(Context context) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        context.startActivity(intent);
    }

    /**
     * 投射界面
     * @param context
     * @param wifiDisplayOpen 是否打开投射开关
     */
    public static void startWifiDisplaySettingsActivity(Context context, boolean wifiDisplayOpen) {
        Intent intent = new Intent(Settings.ACTION_CAST_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);

        if(wifiDisplayOpen) {
            // 系统应用调用生效； 普通应用调用失败，权限不够；
            try {
                Settings.Global.putInt(context.getContentResolver(),
                        "wifi_display_on"/*Settings.Global.WIFI_DISPLAY_ON*/, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 跳转到蓝牙设置页面
     * @param context
     */
    public static void startBluetoothSettingsActivity(Context context) {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.putExtra("entryName", "zfyapp");
        context.startActivity(intent);
    }

    public static void startNetworkPermissionActivity(Context context) {
        ComponentName componentName = new ComponentName("com.qti.csm",
                "com.qti.csm.permission.PermissionActivity");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        context.startActivity(intent);
    }

    /* ############################################################################################# */
    /*                                           Mass Storage API                                    */
    /* ############################################################################################# */
    // Bug346, add mass_storage api, wmd, 2020.0528

    public static final int BINDER_CODE_SET_MASS_STORAGE_ENABLED = 5001;
    public static final int BINDER_CODE_IS_MASS_STORAGE_ENABLED = 5002;

    IBinder mUsbServiceIBinder = null;

    public IBinder getUsbServiceIBinder() {
        if(mUsbServiceIBinder == null) {
            // 获取服务IBinder方式
            // 1.源码编译
            // Android.mk:去掉LOCAL_SDK_VERSION，使用LOCAL_PRIVATE_PLATFORM_APIS，如：
            // //LOCAL_SDK_VERSION := current
            // LOCAL_PRIVATE_PLATFORM_APIS := true
            //notificationIBinder = ServiceManager.checkService("package");

            // 2.IDE编译
            // 通过反射获取服务IBinder
            try {
                Method getServiceMethod = Class.forName("android.os.ServiceManager").
                        getDeclaredMethod("checkService", new Class[]{String.class});
                mUsbServiceIBinder = (IBinder) getServiceMethod.invoke(null, new Object[]{Context.USB_SERVICE});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mUsbServiceIBinder;
    }

    /**
     * 挂载/卸载U盘（大容量存储）
     * @param isEnabled
     * @return
     */
    public boolean setMassStorageEnabled(boolean isEnabled) {
        boolean result = false;

        IBinder usbServiceIBinder = getUsbServiceIBinder();

        if (usbServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.hardware.usb.IUsbManager");
            data.writeInt(isEnabled ? 1 : 0);
            if(DEBUG) {
                Log.d(TAG, "setMassStorageEnabled get service ibinder ok");
            }
            try {
                usbServiceIBinder.transact(GSFWManager.BINDER_CODE_SET_MASS_STORAGE_ENABLED, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "setMassStorageEnabled transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = (intValue == 1) ? true : false;
            } catch (RemoteException e) {
                mUsbServiceIBinder = null;
                Log.d(TAG, "setMassStorageEnabled transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mUsbServiceIBinder = null;
                Log.d(TAG, "setMassStorageEnabled transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "setMassStorageEnabled get service ibinder failed: null");
        }
        return result;
    }

    /**
     * U盘是否已挂载
     * @return
     */
    public boolean isMassStorageEnabled() {
        boolean result = false;

        IBinder usbServiceIBinder = getUsbServiceIBinder();

        if (usbServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.hardware.usb.IUsbManager");
            if(DEBUG) {
                Log.d(TAG, "isMassStorageEnabled get service ibinder ok");
            }
            try {
                usbServiceIBinder.transact(GSFWManager.BINDER_CODE_IS_MASS_STORAGE_ENABLED, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "isMassStorageEnabled transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = (intValue == 1) ? true : false;
            } catch (RemoteException e) {
                mUsbServiceIBinder = null;
                Log.d(TAG, "isMassStorageEnabled transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mUsbServiceIBinder = null;
                Log.d(TAG, "isMassStorageEnabled transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "isMassStorageEnabled get service ibinder failed: null");
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                         下拉手电筒广播      　                                   */
    /* ############################################################################################# */
    public static final String ACTION_FLASHLIGHT_EVENT = "android.intent.action.gosuncn.zfy.FlashLightEvent";
    public static final String FLASHLIGHT_STATUS = "status";
    public static final int FLASHLIGHT_STATUS_ON = 1;
    public static final int FLASHLIGHT_STATUS_OFF = 0;

    /* ############################################################################################# */
    /*                                           工模标志                                             */
    /* ############################################################################################# */
    public boolean setFactoryResultFlag(String flag) {
        boolean result = false;

        IBinder coreServiceIBinder = getCoreServiceIBinder();

        if (coreServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeString(flag);
            if(DEBUG) {
                Log.d(TAG, "setFactoryResultFlag get service ibinder ok");
            }
            try {
                coreServiceIBinder.transact(GSFWManager.BINDER_CODE_FACTORY_FLAG, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "setFactoryResultFlag transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = true;
            } catch (RemoteException e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "setFactoryResultFlag transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "setFactoryResultFlag transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "setFactoryResultFlag get service ibinder failed: null");
        }
        return result;
    }
    public String getFactoryResetFlag() {
        String result = null;
        IBinder coreServiceIBinder = getCoreServiceIBinder();
        if (coreServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            if (DEBUG) {
                Log.d(TAG, "getFactoryResultFlag get service ibinder ok");
            }
            try {
                coreServiceIBinder.transact(GSFWManager.BINDER_CODE_GET_FACTROY_FLAG, data, reply, 0);
                if (DEBUG) {
                    Log.d(TAG, "getFactoryResultFlag transact ok");
                }
                reply.readException();
//                result = true;
                result = reply.readString();
            } catch (RemoteException e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "getFactoryResultFlag transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mCoreServiceIBinder = null;
                Log.d(TAG, "getFactoryResultFlag transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        } else {
            Log.d(TAG, "getFactoryResultFlag get service ibinder failed: null");
        }
        return result;
    }


    /* ############################################################################################# */
    /*                                      获取sim卡运营商                                           */
    /*                           返回值０联通，１移动，２电信，-1无卡或国外卡                             */
    /* ############################################################################################# */

    public int getplmnName() {
        String plmn = getStringMethod("gsm.sim.operator.numeric", "00000");
        String numeric = getStringMethod("gsm.operator.numeric", "");
        int plmnToint;
        try {
            plmnToint = Integer.parseInt(plmn);
        } catch (NumberFormatException e) {
            plmnToint = 1;
        }
        if (!TextUtils.isEmpty(numeric) && plmnToint >= 46000 && plmnToint <= 460011) {
            if ("46001".equals(plmn) || "46006".equals(plmn) || "46009".equals(plmn) || "46010".equals(plmn)) {
                return 0; //context.getString(R.string.china_unicom);
            } else if ("46000".equals(plmn) || "46002".equals(plmn) || "46004".equals(plmn) || "46007".equals(plmn)
                    || "46008".equals(plmn)) {
                return 1;//context.getText(R.string.china_mobile);
            } else
                return 2;//context.getText(R.string.china_telecom);
        } else
            return -1;

    }

    public String getStringMethod(final String key, final String def) {
        try {
            if (getStringMethod == null) {
                getStringMethod = Class.forName("android.os.SystemProperties").getMethod("get", String.class, String.class);
            }
            return ((String) getStringMethod.invoke(null, key, def)).toString();
        } catch (Exception e) {
            return def;
        }
    }

    /* ############################################################################################# */
    /*                                           Iptables                                            */
    /* ############################################################################################# */
    public static final int BINDER_CODE_DISPATCH_IPTABLES_CMD = 5001;

    IBinder mNMSIBinder = null;

    private IBinder getNMSManager() {
        if(mNMSIBinder == null) {
            // 获取服务IBinder方式
            // 1.源码编译
            // Android.mk:去掉LOCAL_SDK_VERSION，使用LOCAL_PRIVATE_PLATFORM_APIS，如：
            // //LOCAL_SDK_VERSION := current
            // LOCAL_PRIVATE_PLATFORM_APIS := true
            //NMSIBinder = ServiceManager.checkService("notification");

            // 2.IDE编译
            // 通过反射获取服务IBinder
            try {
                Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("checkService", new Class[]{String.class});
                mNMSIBinder = (IBinder) getServiceMethod.invoke(null, new Object[]{"network_management"});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mNMSIBinder;
    }

    public boolean dispatchIptablesCmd(String cmd) {
        boolean result = false;

        IBinder NMSIBinder = getNMSManager();
        Log.d(TAG, "dispatchIptablesCmd NMSIBinder:"+NMSIBinder);
        if (NMSIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.os.INetworkManagementService");
            data.writeString(cmd);
            Log.d(TAG, "dispatchIptablesCmd get service ibinder ok");
            try {
                NMSIBinder.transact(BINDER_CODE_DISPATCH_IPTABLES_CMD, data, reply, 0);
                Log.d(TAG, "dispatchIptablesCmd transact ok");
                reply.readException();
                int intValue = reply.readInt();
                result = true;
            } catch (RemoteException e) {
                Log.d(TAG, "dispatchIptablesCmd transact failed: remote exception happend ", e);
            } catch (Exception e) {
                Log.d(TAG, "dispatchIptablesCmd transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "dispatchIptablesCmd get service ibinder failed: null");
        }
        return result;
    }

    public static final int REMOTE_CALLBACK_TYPE_SILENT_INSTALL = 9002;

    public void installAppSilent(String apkfilepath) {
        Log.d(TAG,"apkfile:" +apkfilepath);
        IBinder coreServiceIBinder = getCoreServiceIBinder();
        if (coreServiceIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeString(apkfilepath);
            try {
                coreServiceIBinder.transact(BINDER_CODE_SILENT_INSTALL,data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
    }
    public void uninstallAppSilent(String pkgname){
        Log.d(TAG,"uninstall :" + pkgname);
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeString(pkgname);
            try {
                coreServiceIBinder.transact(BINDER_CODE_SILENT_UNINSTALL,data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
    }

    public void setMobileDataEnabled(Boolean state){
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            if(state)
                data.writeInt(1);
            else
                data.writeInt(0);
            try {
                coreServiceIBinder.transact(BINDER_CODE_MOBILEDATA,data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
    }

    public Long getCurrentUsbFunction(){
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            try {
                coreServiceIBinder.transact(BINDER_CODE_GET_USBFUNCTIONS,data, reply, 0);
                reply.readException();
                Log.d("wcwc","reply.long = " + reply.readLong());
                return reply.readLong();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
        return null;
    }

    public void setCurrentUsbFunction(Long funciton){
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeLong(funciton);
            try {
                coreServiceIBinder.transact(BINDER_CODE_SET_USBFUNCIION,data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
    }

    public void setWlanMode(int mode){
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeInt(mode);
            try {
                coreServiceIBinder.transact(BINDER_CODE_WLANMODE,data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
    }

    /**
     * 设置应用adb方式安装/卸载功能控制状态
     * @param mode 功能模式
     * 0：允许使用adb方式安装/卸载终端应用；
     * 1：不允许使用adb方式安装/卸载终端应用。
     * @return 成功返回true；失败返回false。
     */
    public boolean setAdbInstallUninstallPolicies(int mode) {
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeInt(mode);
            try {
                coreServiceIBinder.transact(BINDER_CODE_SET_ADB_INSTALLUNINSTALL_POLICIES, data, reply, 0);
                reply.readException();
                return (reply.readInt() != 0) ? true : false;
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
        return false;
    }

    /**
     * 获取当前adb方式安装/卸载功能管控状态
     * @return 返回值为当前adb方式安装/卸载功能管控状态，参见setAdbInstallUnistallPolicies方法的参数mode
     */
    public int getAdbInstallUninstallPolicies() {
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            try {
                coreServiceIBinder.transact(BINDER_CODE_GET_ADB_INSTALLUNINSTALL_POLICIES, data, reply, 0);
                reply.readException();
                return reply.readInt();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
        return 0;
    }

    /**
     * ROOT状态检测
     * @return 设备已ROOT返回true；设备未ROOT返回false
     */
    public boolean getRootState() {
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            try {
                coreServiceIBinder.transact(BINDER_CODE_GET_ROOT_STATE, data, reply, 0);
                reply.readException();
                return (reply.readInt() != 0) ? true : false;
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
        return false;
    }

    /**
     * 系统完整性检测
     * @return 统完整性未被破坏返回true；系统完整性被破坏返回false
     */
    public boolean getSystemIntegrity() {
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            try {
                coreServiceIBinder.transact(BINDER_CODE_GET_SYSTEM_INTEGRITY, data, reply, 0);
                reply.readException();
                return (reply.readInt() != 0) ? true : false;
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
        return false;
    }

    /**
     * 以Base64编码列举终端密码模块内存储的所有数字证书
     * @return 成功：返回包含所有数字证书的列表；失败：返回空指针null
     */
    public String[] listCertificates() {
        IBinder coreServiceIBinder  = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            List<String> arrayList = new ArrayList<String>();
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            try {
                coreServiceIBinder.transact(BINDER_CODE_LIST_CERTIFICATES, data, reply, 0);
                reply.readException();
                int len = reply.readInt();
                if(len > 0) {
                    String[] list = new String[len];
                    reply.readStringArray(list);
                    return list;
                }else{
                    return null;
                }
//                reply.readStringList(arrayList);
//                int arrayListSize = arrayList.size();
//                if(arrayListSize > 0) {
//                    String[] array = (String[]) arrayList.toArray(new String[arrayListSize]);
//                    return array;
//                }else{
//                    return null;
//                }
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                reply.recycle();
                data.recycle();
            }
        }
        return null;
    }
	
	    /**
     * 连接Vpn
     * @param vpnconfig Map<String,String>类型的用户自定义vpn的配置
     */
    public void establishVpnConnection(Map<String,String> vpnconfig){
            IBinder establishVpnIBinder = getCoreServiceIBinder();
            if (establishVpnIBinder != null) {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
                data.writeMap(vpnconfig);
                Log.d(TAG,vpnconfig.toString());
                try {
                    establishVpnIBinder.transact(BINDER_CODE_ESTABLISHVPNCONNECTION, data, reply, 0);
                    reply.readException();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    data.recycle();
                    reply.recycle();
                }
            }
    }


    /**
     * 断开Vpn的连接
     */
    public void disEstablishVpnConnection(){
        IBinder establishVpnIBinder = getCoreServiceIBinder();
        if(establishVpnIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            try {
                establishVpnIBinder.transact(BINDER_CODE_DISESTABLISHVPNCONNECTION, data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                data.recycle();
                reply.recycle();
            }
        }
    }

    public boolean setSystemTime(long millis){
        int result = 0;
        IBinder coreServiceIBinder = getCoreServiceIBinder();
        if(coreServiceIBinder != null){
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeLong(millis);
            try {
                coreServiceIBinder.transact(BINDER_CODE_SET_SYSTEM_TIME, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                data.recycle();
                reply.recycle();
            }
        }
        return (result != 0);
    }
}
