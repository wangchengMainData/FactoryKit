package com.gosuncn.zfyfw.service;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.AppOpsManager.PackageOps;
import android.app.AppOpsManager.OpEntry;
import android.app.Service;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PermissionInfo;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkInfo;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.BatteryStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gosuncn.bean.ApnItem;
import com.gosuncn.bean.CryptoModuleInfo;
import com.gosuncn.bean.InstallAppInfo;
import com.gosuncn.bean.MediaInfo;
import com.gosuncn.bean.WlanConfigurationInfo;
import com.gosuncn.utils.AppService;
import com.gosuncn.utils.RemoteControl;
import com.gosuncn.zfyfw.EmptyActivity;
import com.gosuncn.zfyfw.MainApp;
import com.gosuncn.zfyfw.R;
import com.gosuncn.zfyfw.VpnEstablishPrepareActivity;
import com.gosuncn.zfyfw.api.LedManager;
import com.gosuncn.zfyfw.module.appinstall.InstallResultReceiver;
import com.gosuncn.zfyfw.module.appinstall.InstallUtils;
import com.gosuncn.zfyfw.module.appinstall.UninstallResultReceiver;
import com.gosuncn.zfyfw.nv.NvUtilExt;
import com.gosuncn.zfyfw.service.ICoreService;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.StatusBarManager;
import ga.mdm.PolicyManagerUtils;
import gosuncn.providers.GosuncnSettings;
import vendor.qti.hardware.bodytemp.V1_0.IBodyTemp;
import vendor.qti.hardware.bodytemp.V1_0.IBodyTempCallback;

import static android.app.usage.NetworkStats.Bucket.DEFAULT_NETWORK_ALL;
import static android.app.usage.NetworkStats.Bucket.METERED_ALL;
import static android.app.usage.NetworkStats.Bucket.STATE_ALL;
import static android.net.NetworkStats.TAG_NONE;
import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;

public class CoreService extends ICoreService.Stub implements IHwBinder.DeathRecipient {
    static final String TAG = CoreService.class.getSimpleName();
    static final boolean DEBUG = !"user".equals(Build.TYPE);
    static final int MSG_ID_SETTINGS_ONCHAGED = 1000;

    private static final String[] unknownStringArray = new String[]{"unknown"};
    private static final String unknownString = "unknown";
    private static List unknownList = new ArrayList<String>();

    private Handler mHandler;

    private IBodyTemp mDaemon;

    private WifiManager mWifiManager;

    private Boolean install_successflag = null;

    private RemoteCallbackList<ISettingsContentObserver> mListeners = new
            RemoteCallbackList<ISettingsContentObserver>();

    public CoreService() {
        mWifiManager = (WifiManager)MainApp.mContext.getSystemService(Context.WIFI_SERVICE);
        unknownList.add("unknown");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (MSG_ID_SETTINGS_ONCHAGED == msg.what) {
                    List<String> valueList = new ArrayList<String>();
                    valueList.add("2");
                    valueList.add("20");
                    notifySettingsOnChanged(99, 88, valueList);
                }
            }
        };
        if(DEBUG) {
            Log.d(TAG, "CoreService");
        }
    }

    @Override
    public boolean registerSettingsContentObserver(int type, ISettingsContentObserver observer) throws RemoteException {
        if(DEBUG) {
            Log.d(TAG, "registerSettingsContentObserver for " + observer.asBinder() + " type:" + type);
        }
        mListeners.register(observer, type);

/*        if (GSFWManager.REMOTE_CALLBACK_TYPE_TEMP == type) {
            sendCommand(type);
        }*/

        //mHandler.sendEmptyMessageDelayed(MSG_ID_SETTINGS_ONCHAGED, 3000);
        return true;
    }

   public String getAppPackageName(int uid ){
        PackageManager pm = MainApp.mContext.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);  //获取
        for(PackageInfo info : packageInfos){
            if( info.applicationInfo.uid == uid ){
                return  info.packageName;
            }
        }
        return null;
    }


    public List getBatteryInfo(Context  context){

        ArrayList<String[]> batteryInfo = new ArrayList<>();

        final BatteryStatsHelper helper = new BatteryStatsHelper(context, false, false);
        helper.create(new Bundle());

        helper.refreshStats(0, -1);
        List<BatterySipper> sippers = helper.getUsageList();
        if (sippers != null && sippers.size() > 0) {
            for (int i=0; i<sippers.size(); i++) {
                String[] appBattery = new String[2];
                final BatterySipper bs = sippers.get(i);
                switch (bs.drainType) {
                    case APP:
                        appBattery[0] = getAppPackageName( bs.uidObj.getUid() );;
                        break;
                }
//                result += BatteryStatsHelper.makemAh( bs.totalPowerMah)+ "\n";
                appBattery[1] = BatteryStatsHelper.makemAh( bs.totalPowerMah);
                if( appBattery[0]!=null&& !appBattery[0].equals("null")){
                    batteryInfo.add(appBattery);
                }
            }
        }

        return batteryInfo;
    }
    @Override
    public boolean unregisterSettingsContentObserver(ISettingsContentObserver observer) throws RemoteException {
        if(DEBUG) {
            Log.d(TAG, "unregisterSettingsContentObserver for " + observer.asBinder());
        }
        if(mHandler.hasMessages(MSG_ID_SETTINGS_ONCHAGED)) {
            mHandler.removeMessages(MSG_ID_SETTINGS_ONCHAGED);
        }
        mListeners.unregister(observer);
        return true;
    }

    private void notifySettingsOnChanged(int type, int value, List<String> valueList) {
        int i = mListeners.beginBroadcast();
        while (i > 0) {
            i--;
            try {
                int cookie = (int) mListeners.getBroadcastCookie(i);
                if(DEBUG) {
                    Log.d(TAG, "notifySettingsOnChanged #S i:" + i + " cookie:" + cookie);
                }
                if (cookie == type) {
                    mListeners.getBroadcastItem(i).onchanged(type, value, valueList);
                }
                if(DEBUG) {
                    Log.d(TAG, "notifySettingsOnChanged #E");
                }

            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
                Log.e(TAG, "notifySettingsOnChanged: RemoteException occurs!");
            }
        }
        mListeners.finishBroadcast();
    }

    @Override
    public void serviceDied(long cookie) {
        Log.v(TAG, "[AS]BodyTemp HAL died");
    }

    public synchronized IBodyTemp getBodyTempDaemon() {
        if (mDaemon == null) {
            if(DEBUG) {
                Log.v(TAG, "[AS]mDaemon was null, reconnect to BodyTemp");
            }
            try {
                mDaemon = IBodyTemp.getService();
            } catch (java.util.NoSuchElementException e) {
                // Service doesn't exist or cannot be opened. Logged below.
            } catch (RemoteException e) {
                Log.e(TAG, "[AS]Failed to get BodyTemp interface", e);
            }
            if (mDaemon == null) {
                Log.w(TAG, "[AS]BodyTemp HIDL not available");
                return null;
            }

            mDaemon.asBinder().linkToDeath(this, 0);

        }
        return mDaemon;
    }

    private IBodyTempCallback mDaemonCallback =
            new IBodyTempCallback.Stub() {

                @Override
                public void onResult(int type, int value, String valueList) {
                    if(DEBUG) {
                        Log.v(TAG, "[AS]BodyTemp callback oonResult type: " + type + " value:" + value + " valueList:" + valueList);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(DEBUG) {
                                Log.v(TAG, "[AS]BodyTemp callback notifySettingsOnChanged s");
                            }
                            final List<String> lists = new ArrayList<String>();
                            lists.add(valueList);
                            notifySettingsOnChanged(GSFWManager.REMOTE_CALLBACK_TYPE_TEMP/*type*/, value, lists);
                            if(DEBUG) {
                                Log.v(TAG, "[AS]BodyTemp callback notifySettingsOnChanged e");
                            }
                        }
                    });
                }
            };

    public synchronized int sendCommand(int type) {
        int result = -1;

        IBodyTemp daemon = getBodyTempDaemon();

        if (daemon != null) {
            try {
                if(DEBUG) {
                    Log.e(TAG, "[AS]FBodyTemp sendCommand s");
                }
                result = daemon.sendCommand(type, mDaemonCallback);
                if(DEBUG) {
                    Log.e(TAG, "[AS]FBodyTemp sendCommand e");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "[AS]FBodyTemp sendCommand Failed to open BodyTemp HAL", e);
            }
        } else {
            Log.v(TAG, "[AS]FBodyTemp sendCommand exit: HAL daemon null");
        }

        if(DEBUG) {
            Log.v(TAG, "[AS]FBodyTemp sendCommand HAL result: " + result);
        }

        return result;
    }

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

    public boolean setSpecificSystemAppsStatus(int status, List<String> apklist) {
        boolean result = false;

        IBinder packageManagerIBinder = getPackageManagerIBinder();

        if (packageManagerIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.content.pm.IPackageManager");
            data.writeInt(status);
            data.writeStringList(apklist);
            if(DEBUG) {
                Log.d(TAG, "setSpecificSystemAppsStatus get service ibinder ok");
            }
            try {
                packageManagerIBinder.transact(GSFWManager.BINDER_CODE_DISABLE_SPECIFIC_SYSTEMAPP, data, reply, 0);
                if(DEBUG) {
                    Log.d(TAG, "setSpecificSystemAppsStatus transact ok");
                }
                reply.readException();
                int intValue = reply.readInt();
                result = true;
            } catch (RemoteException e) {
                mPackageManagerIBinder = null;
                Log.d(TAG, "setSpecificSystemAppsStatus transact failed: remote exception happend ", e);
            } catch (Exception e) {
                mPackageManagerIBinder = null;
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

    public boolean setMobileDataEnabled(final boolean enabled) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) MainApp.mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm.setDataEnabled(enabled);
                return true;
            }
            Method setDataEnabledMethod =
                    tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            setDataEnabledMethod.invoke(tm, enabled);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (DEBUG) {
            Log.v(TAG, "[AS]onTransact code: " + code);
        }
        if (code == GSFWManager.BINDER_CODE_DISABLE_SPECIFIC_SYSTEMAPP) {
            int status = 0;
            List<String> apkList = new ArrayList<String>();
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            status = data.readInt();
            data.readStringList(apkList);
            if (DEBUG) {
                Log.d(TAG, "onTransact disableSpecificSystemApps status:" + status);
                for (int index = 0; index < apkList.size(); index++) {
                    Log.d(TAG, "onTransact disableSpecificSystemApps index:" + index + " pkgName:" + apkList.get(index));
                }
            }
            setSpecificSystemAppsStatus(status, apkList);
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        } else if (code == GSFWManager.BINDER_CODE_BODYTEMP_REQUEST) {
            int type = 0;
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            type = data.readInt();
            sendCommand(type);
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        } else if (code == GSFWManager.BINDER_CODE_FACTORY_FLAG) {
            String flag = "U";
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            flag = data.readString();
            NvUtilExt.getInstance().setFactoryNv2499MMIBit(flag);
            reply.writeNoException();
            reply.writeInt(1);
            return true;
        } else if (code == GSFWManager.BINDER_CODE_SILENT_INSTALL) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            String apkfilepath = data.readString();
            Log.d(TAG, "apkfilepath : " + apkfilepath);
            InstallUtils.install(MainApp.mContext, apkfilepath, InstallResultReceiver.class);
            reply.writeNoException();
            return true;
        } else if (code == GSFWManager.BINDER_CODE_SILENT_UNINSTALL) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            String pkgName = data.readString();
            Log.d(TAG, "pkgName = " + pkgName);
            InstallUtils.uninstall(MainApp.mContext, pkgName);
            reply.writeNoException();
            return true;
        } else if (code == GSFWManager.BINDER_CODE_MOBILEDATA) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            Boolean state = data.readInt() == 1;
            Log.d(TAG, state + "");
            setMobileDataEnabled(state);
            reply.writeNoException();
            return true;
        } else if (code == GSFWManager.BINDER_CODE_GET_USBFUNCTIONS) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            UsbManager usbManager = (UsbManager) MainApp.mContext.getSystemService(Context.USB_SERVICE);
            reply.writeNoException();
            Log.d(TAG, "current usb function: " + usbManager.getCurrentFunctions());
            reply.writeLong(usbManager.getCurrentFunctions());
            return true;
        } else if (code == GSFWManager.BINDER_CODE_SET_USBFUNCIION) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            UsbManager usbManager = (UsbManager) MainApp.mContext.getSystemService(Context.USB_SERVICE);
            Long function = data.readLong();
            usbManager.setCurrentFunctions(function);
            reply.writeNoException();
            return true;
        } else if (code == GSFWManager.BINDER_CODE_INSTALL_RESULT/* app installer finished + result */) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            List<String> value_install = new ArrayList<>();
            value_install.add(data.readBoolean()+"");
            notifySettingsOnChanged(GSFWManager.REMOTE_CALLBACK_TYPE_SILENT_INSTALL,1,value_install);
            Log.d(TAG,"silentInstall result : " + value_install);
            reply.writeNoException();
            return true;
        }else if (code == GSFWManager.BINDER_CODE_UNINSTALL_RESULT/* app installer finished + result */) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            List<String> value_uninstall = new ArrayList<>();
            value_uninstall.add(data.readBoolean()+"");
            Log.d(TAG, "silentUnInstall result : " + value_uninstall);
            notifySettingsOnChanged(GSFWManager.REMOTE_CALLBACK_TYPE_SILENT_INSTALL,0,value_uninstall);
            reply.writeNoException();
            return true;
        }else if(code == GSFWManager.BINDER_CODE_SET_ADB_INSTALLUNINSTALL_POLICIES){
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            int aiupMode = data.readInt();
            boolean result = GosuncnSettings.System.putInt(MainApp.mContext.getContentResolver(),"device_adb_installer_policy", aiupMode);
            String aiupModeStr = "0";
            try{
                aiupModeStr = String.valueOf(aiupMode);
            }catch (Exception e){

            }
            SystemProperties.set("persist.sys.zfy.pmblock", aiupModeStr);
            reply.writeNoException();
            reply.writeInt((result) ? 1 : 0);
            return true;
        }else if(code == GSFWManager.BINDER_CODE_GET_ADB_INSTALLUNINSTALL_POLICIES){
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            int aiupMode = GosuncnSettings.System.getInt(MainApp.mContext.getContentResolver(),"device_adb_installer_policy", 0);
            reply.writeNoException();
            reply.writeInt(aiupMode);
            return true;
        }else if(code == GSFWManager.BINDER_CODE_GET_ROOT_STATE){
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            boolean hasSu = false;
            reply.writeNoException();
            reply.writeInt(hasSu ? 1 : 0);
            return true;
        }else if(code == GSFWManager.BINDER_CODE_GET_SYSTEM_INTEGRITY){
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            String verifiedbootstate = SystemProperties.get("ro.boot.verifiedbootstate", "");
            boolean isBroken = false;
            isBroken = (!"green".equals(verifiedbootstate) && !"orange".equals(verifiedbootstate));
            reply.writeNoException();
            reply.writeInt(isBroken ? 0 : 1);
            return true;
        }else if(code == GSFWManager.BINDER_CODE_LIST_CERTIFICATES){
            String[] list = new String[]{"111","222","333","444"};
            String[] resultList = new String[list.length];
            int index = 0;
            for(String str : list) {
                resultList[index++] = Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
            }

            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            reply.writeNoException();
            reply.writeInt(list.length);
            reply.writeStringArray(resultList);
            return true;
        }else if (code == GSFWManager.BINDER_CODE_ESTABLISHVPNCONNECTION) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            Map<String,String> vpn = new HashMap<>();
            data.readMap(vpn,getClass().getClassLoader());
            Log.e(TAG,"GSF RECEIVE VPN CONNECT:" +vpn);
            Intent intent = new Intent(MainApp.mContext, EmptyActivity.class);
            intent.putExtra(VpnConnectService.VPN_TAG,VpnConnectService.VPN_START);
            intent.putExtra("config",(Serializable)vpn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            MainApp.mContext.startActivity(intent);
            reply.writeNoException();
            return true;
        } else if (code == GSFWManager.BINDER_CODE_DISESTABLISHVPNCONNECTION) {
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            Intent intent = new Intent(MainApp.mContext, EmptyActivity.class);
            intent.putExtra(VpnConnectService.VPN_TAG,VpnConnectService.VPN_STOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            MainApp.mContext.startActivity(intent);
            reply.writeNoException();
            return true;
        }else if(code == GSFWManager.BINDER_CODE_GET_FACTROY_FLAG) {
            String nvflag = "";
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            reply.writeNoException();
            nvflag = NvUtilExt.getInstance().getFactoryNv2499();
            reply.writeString(nvflag);
            return true;
        }else if(code == GSFWManager.BINDER_CODE_GET_FLASH_POLICY){
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            reply.writeNoException();
            int value = getFlashPolicies();
            reply.writeInt(value);
            return true;
        }else if(code == GSFWManager.BINDER_CODE_SET_SYSTEM_TIME){
            data.enforceInterface("com.gosuncn.zfyfw.service.ICoreService");
            reply.writeNoException();
            long millis = data.readLong();
            boolean result = SystemClock.setCurrentTimeMillis2(millis);
            reply.writeInt(result ? 1 : 0);
            return true;
        }

        return super.onTransact(code, data, reply, flags);
    }

    public String[] listIccid() {
        return PolicyManagerUtils.getInstance().listIccid(MainApp.mContext);
    }

    public String[] listImei() {
         return PolicyManagerUtils.getInstance().listImei(MainApp.mContext);
    }

    public String getCryptoModuleInfo(){
        CryptoModuleInfo cryptoModuleInfo = new CryptoModuleInfo();
        cryptoModuleInfo.result = "0";
        cryptoModuleInfo.moduleType = "tfcard";
        cryptoModuleInfo.manufacture = "xdja";
        cryptoModuleInfo.moduleId = "194823";
        Gson gson = new Gson();
        return gson.toJson(cryptoModuleInfo, CryptoModuleInfo.class);
    }

    public String[] listCertificates() {
        return PolicyManagerUtils.listCertificates();
    }

    /**
     * REQ007, VPN establish, disestablish, get VPN state, wmd, 2021.0712
     * VPN连接建立接口
     * @return
     * 0:成功;其他:失败
     */
    public int establishVpnConnection() {
//        Intent intent = new Intent(MainApp.mContext, VpnEstablishPrepareActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
//        MainApp.mContext.startActivity(intent);
        return 0;
    }

    /**
     * VPN连接断开接口
     * @return
     * 0:成功;其他:失败
     */
    public int disestablishVpnConnection() {
//        return PolicyManagerUtils.getInstance().disestablishVpnConnection();
        return 0;
    }

    /**
     * VPN服务状态查询接口
     * @return
     * 0:未启动;1:连接中;2:重试中;3:已建立;4:发生错误;5:已断开
     */
    public int getVpnServiceState() {
        return 0;
    }

    //REQ103, key control, wmd, 2021.0608
//        GSFWManager.java
//        public static final int KEYCODE_VIDEO = 24;
//        public static final int KEYCODE_AUDIO = 25;
//        public static final int KEYCODE_CAMERA = 66;
//        public static final int KEYCODE_SOS = 87;
//        public static final int KEYCODE_PTT = 88;
//        PolicyManagerUtils
//        public static final int GXX_KEY_CONTROL_VIDEO = 0x80;
//        public static final int GXX_KEY_CONTROL_AUDIO = 0x40;
//        public static final int GXX_KEY_CONTROL_CAMERA = 0x20;
//        public static final int GXX_KEY_CONTROL_SOS = 0x10;
//        public static final int GXX_KEY_CONTROL_PTT = 0x08;
    public boolean setSoundRecorderBtn(int mode) {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        if(mode == 0) {
            keyControl |= PolicyManagerUtils.GXX_KEY_CONTROL_AUDIO;
        }else{
            keyControl &= ~PolicyManagerUtils.GXX_KEY_CONTROL_AUDIO;
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, keyControl);
    }

    public int getSoundRecorderBtn() {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        return ((keyControl & PolicyManagerUtils.GXX_KEY_CONTROL_AUDIO) != 0) ? 0 : 1;
    }

    public boolean setVideoRecorderBtn(int mode) {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        if(mode == 0) {
            keyControl |= PolicyManagerUtils.GXX_KEY_CONTROL_VIDEO;
        }else{
            keyControl &= ~PolicyManagerUtils.GXX_KEY_CONTROL_VIDEO;
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, keyControl);
    }

    public int getVideoRecorderBtn() {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        return ((keyControl & PolicyManagerUtils.GXX_KEY_CONTROL_VIDEO) != 0) ? 0 : 1;
    }

    public boolean setPictureBtn(int mode) {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        if(mode == 0) {
            keyControl |= PolicyManagerUtils.GXX_KEY_CONTROL_CAMERA;
        }else{
            keyControl &= ~PolicyManagerUtils.GXX_KEY_CONTROL_CAMERA;
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, keyControl);
    }

    public int getPictureBtn() {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        return ((keyControl & PolicyManagerUtils.GXX_KEY_CONTROL_CAMERA) != 0) ? 0 : 1;
    }

    public boolean setSosBtn(int mode) {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        if(mode == 0) {
            keyControl |= PolicyManagerUtils.GXX_KEY_CONTROL_SOS;
        }else{
            keyControl &= ~PolicyManagerUtils.GXX_KEY_CONTROL_SOS;
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, keyControl);
    }

    public int getSosBtn() {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        return ((keyControl & PolicyManagerUtils.GXX_KEY_CONTROL_SOS) != 0) ? 0 : 1;
    }

    public boolean setPttBtn(int mode) {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        if(mode == 0) {
            keyControl |= PolicyManagerUtils.GXX_KEY_CONTROL_PTT;
        }else{
            keyControl &= ~PolicyManagerUtils.GXX_KEY_CONTROL_PTT;
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, keyControl);
    }

    public int getPttBtn() {
        int keyControl = Settings.Global.getInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_KEY_CONTROL, 0);
        return ((keyControl & PolicyManagerUtils.GXX_KEY_CONTROL_PTT) != 0) ? 0 : 1;
    }

    /**
     * 获取媒体信息
     * ["剩余存储空间":"55KB","已采集的视频数量":"20","已采集的视频长度":"8分钟"，"已采集的音频数量":"10","已采集的音频长度":"22分钟"，"已采集的图片数量":"135张"，]
     * 1. 剩余存储空间：SD卡存在，获取SD卡剩余存储空间； SD卡不存在，获取剩余内置存储空间；
     * 2. 媒体信息：获取/storage/FA05-180E/DCIM/目录下，不区分子目录（如：000000）mp4,wav,jpg的个数与时长
     * @return
     */
    public String getMonitorInfo() {
        MediaInfo mediaInfo = PolicyManagerUtils.getMediaInfo(MainApp.mContext);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("剩余存储空间", PolicyManagerUtils.getExternalStorageAvailableSize(PolicyManagerUtils.getExternalStorageFilePath(MainApp.mContext)));
        jsonObject.addProperty("已采集的视频数量", String.valueOf(mediaInfo.videoCount));
        jsonObject.addProperty("已采集的视频长度", PolicyManagerUtils.formatDuration(mediaInfo.videoDuration));
        jsonObject.addProperty("已采集的音频数量", String.valueOf(mediaInfo.audioCount));
        jsonObject.addProperty("已采集的音频长度", PolicyManagerUtils.formatDuration(mediaInfo.audioDuration));
        jsonObject.addProperty("已采集的图片数量", String.valueOf(mediaInfo.imageCount) + "张");
        String result = jsonObject.toString();
        result = result.replaceAll("\\}", "]").replaceAll("\\{", "[");
        return result;
    }

    /**
     * 禁止终端使用无线网络
     * @param mode　0　禁止，１只允许终端扫描，２允许自主控制
     * @return
     */
    public boolean setWlanPolicies(int mode) {
        if(mode == 0 && mWifiManager.isWifiEnabled()) {//0 则关闭开关
            mWifiManager.setWifiEnabled(false);
        }
        if(mode == 1 ) {//1 则关闭连接
            mWifiManager.disconnect();
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_WLAN_POLICIES,mode);
    }

    public int getWlanPolicies() {
       return Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_WLAN_POLICIES,2);
    }

    /**
     * 禁止终端使用移动数据连接
     * @param mode 0 强制关闭，不允许打开　１ 强制开启，不允许关闭　2　允许自主控制
     * @return true　成功 false   失败
     */
    public boolean setDataConnectivityPolicies(int mode) {
        int currentMode = getDataConnectivityPolicies();
        if(currentMode != 2) {//init
            if(!Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_DATA_CONNECTIVITY_POLICIES,2)) {
                Log.e(TAG,"init dataConnectivityPolicies failed");
                return false;
            }
            Log.e(TAG,"init dataConnectivityPolicies Success");
        }
        if(mode == 0) {
            setMobileDataEnabled(false);
        }
        else{
            setMobileDataEnabled(true);
        }
        boolean setResult = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_DATA_CONNECTIVITY_POLICIES,mode);
        Log.e(TAG,"setDataConnectivityPolicies = " + mode + " : " + setResult);
        return  setResult;
    }

    public int getDataConnectivityPolicies() {
        int data_policies = Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_DATA_CONNECTIVITY_POLICIES,2);
        Log.e(TAG,"getDataConnectivityPolicies = " + data_policies);
        return data_policies;

    }

    /**
     * 禁止终端使用蓝牙
     * @param mode  Y　０　不允许终端使用蓝牙　１仅允许与准许蓝牙连接列表内的蓝牙设备建立蓝牙连接，列表可根据目标设备的蓝牙 MAC地址进行定义 2 允许用户自主控制
     * @param bluetoothInfoList N 仅当 mode=1 时有效，数组中每一项为一个 JSON 格式字符串，格式如下：
     * {"Mac":"00-11-22-33-44-55"}
     * @return  true 成功 false　失败
     */
    public boolean setBluetoothPolicies(int mode, String[] bluetoothInfoList) {
        BluetoothAdapter bm =BluetoothAdapter.getDefaultAdapter();
        if(mode != 2) {//init
            if (!Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_BLUETOOTH_POLICIES, 2)) {
                Log.e(TAG, "BluetoothPolicies init failed");
                return false;
            }
        }
        if(mode == 0){//禁用
            bm.disable();
        }
        if(mode == 1){//受限使用，名单之外不可连接
            Set<BluetoothDevice> devices = bm.getBondedDevices();
            StringBuffer sb = new StringBuffer();
            if(bluetoothInfoList == null || bluetoothInfoList.length == 0)
            {
                Settings.Global.putString(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_INFO_LIST,"");
            }else{
                for(int i = 0; i < bluetoothInfoList.length; i++){
                    sb.append(bluetoothInfoList[i].toUpperCase());
                    if(i != bluetoothInfoList.length - 1)
                        sb.append(",");
                }
                Settings.Global.putString(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_INFO_LIST,sb.toString());
                Log.e(TAG,"sb.toString = " + sb.toString());//write in white list
            }
            if(devices != null){//disconnect,unbind
                String list = Settings.Global.getString(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_INFO_LIST);
                String infodata = list.replace("\"","")
                        .replace("MAC","")
                        .replace("{","")
                        .replace("}","")
                        .replace(":","")
                        .replace("-","");
                String[] infoList = infodata.split(",");
                for(BluetoothDevice device : devices){
                    if(!Arrays.asList(infoList).contains(device.getAddress().replace(":","")))//如果不是白名单，断开连接
                        device.removeBond();
                }
            }
        }
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_POLICIES,mode);
    }

    /**
     * @return  string[0]：功能模式；
     *          string[1]至string[n-1]：仅当mode=1时返回允许连接的特定蓝牙网络信息；
     */
    public String[] getBluetoothPolicies() {
        String[] result = null;
        String infoList = Settings.Global.getString(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_INFO_LIST);
        if(infoList == null){
            result = new String[1];
            result[0] = Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_POLICIES,2) + "";
        }
        else{
            String[] str_temp = infoList.split(",");
            result = new String[1 + str_temp.length];
            result[0] = Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BLUETOOTH_POLICIES,2) + "";
            for(int i = 1; i < str_temp.length + 1; i ++){
                result[i] = str_temp[i - 1];
            }
        }
        return result;
    }

    /**
     *　禁止终端使用NFC功能
     * @param mode 0 禁止终端打开NFC, 1 强制开启NFC, 2 不管控终端的NFC功能
     * @return false 设置失败　true 设置成功
     */
    public boolean setNfcPolicies(int mode) {
        if(mode != 2){
            if(!Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_NFC_POLICIES,2)) {
                return false;
            }
        }
        NfcManager nm = (NfcManager)MainApp.mContext.getSystemService(Context.NFC_SERVICE);
        try {
            if (mode == 0) {
                nm.getDefaultAdapter().disable();
            } else if (mode == 1) {
                nm.getDefaultAdapter().enable();
            }
        }catch (NullPointerException e){
            return false;
        }
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_NFC_POLICIES,mode);
        Log.e(TAG,"NfcPolicies set " + result);
        return result;
    }

    public int getNfcPolicies() {
        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_NFC_POLICIES,2);
        Log.e(TAG,"getNfcPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用红外
     * @param mode　０　不允许终端使用红外　１　允许终端使用红外
     * @return  false 设置失败　true 设置成功
     */
    public boolean setIrPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_IR_POLICIES,mode);
        try {
            SystemProperties.set("persist.sys.gxxirpc", String.valueOf(mode));
        }catch (Exception e){

        }
        if(0 == mode){
            LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_IRLED);
        }
        Log.e(TAG,"setIrPolicies " + result);
        return result;
    }

    public int getIrPolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_IR_POLICIES,1);
        String policiesStr = SystemProperties.get("persist.sys.gxxirpc", "1");
        int policies = 0;
        try {
            policies = Integer.valueOf(policiesStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getIrPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用生物特征识别模块
     * @param mode　０不允许终端使用生物特征识别模块 1 允许终端使用生物特征识别模块
     * @return   false 设置失败　true 设置成功
     */
    public boolean setBiometricRecognitionPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BIOMETRIC_RECOGNITION_POLICIES,mode);
        Log.e(TAG,"setBiometricRecognitionPolicies  " + result);
        return result;
    }

    public int getBiometricRecognitionPolicies() {
        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_BIOMETRIC_RECOGNITION_POLICIES,1);
        Log.e(TAG,"getBiometricRecognitionPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用定位服务
     * @param mode　０ 禁止终端使用定位服务　１强制终端使用定位服务且不允许关闭　２　不对定位服务进行控制
     * @return   false 设置失败　true 设置成功
     */
    public boolean setGpsPolicies(int mode) {
        boolean result = true;
        result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_GPS_POLICIES,mode);
        try {
            SystemProperties.set("persist.sys.gps.gxxgpspc", String.valueOf(mode));
        }catch (Exception e){

        }
//        MainApp.mContext.sendBroadcast(new Intent("android.intent.action.GXX_GPS_POLICY"));
        if(mode == 1) {
            if (Build.VERSION.SDK_INT < 19) {
                Settings.Secure.setLocationProviderEnabled(MainApp.mContext.getContentResolver(),
                        LocationManager.GPS_PROVIDER, true);
            } else
                Settings.Secure.putInt(MainApp.mContext.getContentResolver(),
                        Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
        } else if (mode == 0) {
                if (Build.VERSION.SDK_INT < 19) {
                    Settings.Secure.setLocationProviderEnabled(MainApp.mContext.getContentResolver(),
                            LocationManager.GPS_PROVIDER, false);
                } else
                    Settings.Secure.putInt(MainApp.mContext.getContentResolver(),
                            Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
            }
        Log.e(TAG,"setGpsPolicies  " + result);

        return result;
    }

    public int getGpsPolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_GPS_POLICIES,2);
        String policiesStr = SystemProperties.get("persist.sys.gps.gxxgpspc", "2");
        int policies = 0;
        try {
            policies = Integer.valueOf(policiesStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getGpsPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用USB 接口进行数据传输
     * @param mode     0：不允许终端通过 USB 接口进行数据传输，仅允许充电模式；
     *                 1：不控制 USB 接口的工作模式，支持 MTP 模式、PTP 模式、HOST 模式进行数据传输与调试模式；
     * @return   false 设置失败　true 设置成功
     */
    public boolean setUsbDataPolicies(int mode) {
//        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_USBDATA_POLICIES,mode);
        boolean result = true;
        try {
            SystemProperties.set("persist.sys.gxxusbdp", String.valueOf(mode));
        }catch (Exception e){

        }

        UsbManager usbManager = (UsbManager)MainApp.mContext.getSystemService(Context.USB_SERVICE);
        if(usbManager.getCurrentFunctions() != UsbManager.FUNCTION_NONE) {
            usbManager.setCurrentFunctions(UsbManager.FUNCTION_NONE);
        }

        // REQ024, setUsbDataPolicies
        if(0 == mode) {
            try {
                if (SystemProperties.get("sys.usb.config").contains("mass_storage")) {
                    GSFWManager.getInstance().setMassStorageEnabled(false);
                }
            } catch (Exception e) {

            }
        }

        Log.e(TAG,"setUsbDataPolicies  " + result);
        return result;
    }

    public int getUsbDataPolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_USBDATA_POLICIES,1);
        String policiesStr = SystemProperties.get("persist.sys.gxxusbdp", "1");
        int policies = 0;
        try {
            policies = Integer.valueOf(policiesStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getUsbDataPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端对扩展存储进行读写操作
     * @param mode     0：不允许终端对扩展存储进行读写操作；
     *                 1：仅允许终端对扩展存储进行读操作；
     *                 2：允许终端对扩展存储进行读、写操作
     * @return   false 设置失败　true 设置成功
     */
    public boolean setExternalStoragePolicies(int mode) {
        boolean result = true;
        try {
            SystemProperties.set("persist.sys.gxxstoragemm", String.valueOf(mode));
        }catch (Exception e){

        }
//        result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_EXTERNAL_STORAGE_POLICIES,mode);
        Log.e(TAG,"setExternalStoragePolicies  " + result);

        // Kill related apps ： com.gosuncn.android.recorder ， com.gosuncn.zfymedia
        final ActivityManager am = (ActivityManager) MainApp.mContext.getSystemService(
                Context.ACTIVITY_SERVICE);
        am.forceStopPackage("com.gosuncn.android.recorder");
        am.forceStopPackage("com.gosuncn.zfymedia");

        return result;
    }

    public int getExternalStoragePolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_EXTERNAL_STORAGE_POLICIES,2);
        String policiesStr = SystemProperties.get("persist.sys.gxxstoragemm", "2");
        int policies = 0;
        try {
            policies = Integer.valueOf(policiesStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getExternalStoragePolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用麦克风功能
     * @param mode　０　禁止终端使用麦克风　１允许终端使用麦克风
     * @return  false 设置失败　true 设置成功
     */
    public boolean setMicrophonePolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_MICROPHONE_POLICIES,mode);
        try {
            SystemProperties.set("persist.audio.fluence.gxxmicpc", String.valueOf(mode));
        }catch (Exception e){

        }
        Log.e(TAG,"setMicrophonePolicies  " + result);
        return result;
    }

    public int getMicrophonePolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_MICROPHONE_POLICIES,1);
        String policyStr = SystemProperties.get("persist.audio.fluence.gxxmicpc", "1");
        int policies = 0;
        try {
            policies = Integer.valueOf(policyStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getMicrophonePolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用扬声器功能
     * @param mode　０　禁止终端使用扬声器　１允许终端使用扬声器
     * @return  false 设置失败　true 设置成功
     */
    public boolean setSpeakerPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_SPEAKER_POLICIES,mode);
        try {
            SystemProperties.set("persist.audio.fluence.gxxspkpc", String.valueOf(mode));
        }catch (Exception e){

        }
        Log.e(TAG,"setSpeakerPolicies  " + result);
        return result;
    }

    public int getSpeakerPolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_SPEAKER_POLICIES,1);
        String policyStr = SystemProperties.get("persist.audio.fluence.gxxspkpc", "1");
        int policies = 0;
        try {
            policies = Integer.valueOf(policyStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getSpeakerPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用摄像头功能
     * REQ032, setCameraPolicies, wmd, 2021.0705
     * @param mode　０　禁止终端使用摄像头　１允许终端使用摄像头
     * @return  false 设置失败　true 设置成功
     */
    public boolean setCameraPolicies(int mode) {
        boolean result = true;
        result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_CAMERA_POLICIES, mode);
        try {
            SystemProperties.set("persist.camera.gxxcamerapc", String.valueOf(mode));
        }catch (Exception e){

        }
        Log.e(TAG,"setCameraPolicies  " + result);

        // Kill related apps ： com.gosuncn.android.recorder
        final ActivityManager am = (ActivityManager) MainApp.mContext.getSystemService(
                Context.ACTIVITY_SERVICE);
        am.forceStopPackage("com.gosuncn.android.recorder");

        return result;
    }

    public int getCameraPolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_CAMERA_POLICIES,1);
        String policyStr = SystemProperties.get("persist.camera.gxxcamerapc", "1");
        int policies = 0;
        try {
            policies = Integer.valueOf(policyStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getCameraPolicies :" + policies);
        return policies;
    }

    /**
     * 禁止终端使用闪光灯功能
     * @param mode　０　禁止终端使用闪光灯　１允许终端使用闪光灯
     * @return  false 设置失败　true 设置成功
     */
    public boolean setFlashPolicies(int mode) {
        boolean result = true;
        result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_FLASH_POLICIES, mode);
        try {
            SystemProperties.set("persist.camera.gxxflashpc", String.valueOf(mode));
        }catch (Exception e){

        }
        Log.e(TAG,"setFlashPolicies  " + result);
        return result;
    }

    public int getFlashPolicies() {
//        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_FLASH_POLICIES,1);
        String policyStr = SystemProperties.get("persist.camera.gxxflashpc", "1");
        int policies = 0;
        try {
            policies = Integer.valueOf(policyStr);
        }catch (Exception e){

        }
        Log.e(TAG,"getFlashPolicies :" + policies);
        return policies;
    }

    public boolean setPeripheralPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_PERIPHERAL_POLICIES,mode);
        Log.e(TAG,"setPeripheralPolicies  " + result);
        return result;
    }

    public int getPeripheralPolicies() {
        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_PERIPHERAL_POLICIES,1);
        Log.e(TAG,"getPeripheralPolicies :" + policies);
        return policies;
    }

    public boolean setVoicePolicies(int mode){
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_VOICE_POLICIES,mode);
        Log.e(TAG,"setVoicePolicies  " + result);
        return result;
    }

    public int getVoicePolicies(){
        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_VOICE_POLICIES,1);
        Log.e(TAG,"getVoicePolicies :" + policies);
        return policies;
    }

    public boolean setSmsPolicies(int mode, String regExp){
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_SMS_POLICIES,mode);
        Log.e(TAG,"setSmsPolicies  " + result);
        return result;
    }

    public int getSmsPolicies(){
        int policies =  Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_SMS_POLICIES,1);
        Log.e(TAG,"getSmsPolicies :" + policies);
        return policies;
    }

    public boolean setCaptureScreenPolicies(int mode) {
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_ENABLE_SCREEN_CAPTURE, mode);
    }

    public int getCaptureScreenPolicies() {
        return Settings.Global.getInt( MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_SCREEN_CAPTURE ,1 );
    }

    public boolean setWlanApPolicies(int mode, String[] macInfoList) {
//        int policies = mode>0?1:0;

        StringBuffer strbuffer = new StringBuffer();

        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_ENABLE_WLAN_AP, mode);

        if( mode == 1){
            if( macInfoList == null || macInfoList.length ==0 ){
                Settings.Global.putString(MainApp.mContext.getContentResolver(),
                        PolicyManagerUtils.GXX_ENABLE_WLAN_AP_MACLIST,"");
            }else{
                for(int i=0;i<macInfoList.length;i++){
                    strbuffer.append(macInfoList[i]).append(",");
//                    if(i!= macInfoList.length-1){
//                        strbuffer.append(",");
//                    }
                }
                Settings.Global.putString(MainApp.mContext.getContentResolver(),
                        PolicyManagerUtils.GXX_ENABLE_WLAN_AP_MACLIST,strbuffer.toString());

                Log.d("policies","set setWlanApPolicies policies ="+strbuffer.toString());
            }
        } else if(mode == 0){
            // close wifi ap
            WifiManager wifiManager = (WifiManager)MainApp.mContext.getSystemService(Context.WIFI_SERVICE);
            if(wifiManager.isWifiApEnabled()){
                wifiManager.stopSoftAp();
            }
        }
        PolicyManagerUtils.syncApWhiteListToServer(MainApp.mContext);
        return result;
    }

    public String[] getWlanApPolicies() {

        String[] policies = null;

        int mode = Settings.Global.getInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_ENABLE_WLAN_AP, 2);

        if( mode == 0){
            policies = new String[]{String.valueOf(mode)};
            return  policies;
        }else if( mode == 1){
            String maclist = Settings.Global.getString(MainApp.mContext.getContentResolver(),
                    PolicyManagerUtils.GXX_ENABLE_WLAN_AP_MACLIST);
            Log.d("maclist","get ap maclist ="+maclist);
            if( maclist == null || maclist.equals("")){
                return  policies;
            }else {
                policies = maclist.split(",");
            }
        }
        return  policies;
    }

    /**
     * 禁止用户增加、删除、修改、查看 APN 配置以及选择 APN
     * 0：不允许用户增加、删除、修改、查看 APN 配置以及选择 APN；
     * 1：仅允许用户查看 APN 配置，但不允许其他操作；
     * 2：允许用户增加、删除、修改、查看 APN 信息，及选择使用的 APN；
     * @param mode
     * @return
     */
    public boolean setUserApnMgrPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_USER_APN_MGR_POLICIES, mode);
        return result;
    }

    /**
     * 是否禁止用户增加、删除、修改、查看 APN 配置以及选择 APN
     * 0：不允许用户增加、删除、修改、查看 APN 配置以及选择 APN；
     * 1：仅允许用户查看 APN 配置，但不允许其他操作；
     * 2：允许用户增加、删除、修改、查看 APN 信息，及选择使用的 APN；
     * @return
     */
    public int getUserApnMgrPolicies() {
        int result = Settings.Global.getInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_USER_APN_MGR_POLICIES/*"gxx_user_apn_mgr_policies"*/, 2);
        return result;
    }

    public String executeShellToSetIptables(String commandline) {
//        if(DEBUG){
//            Log.d(TAG, "executeShellToSetIptables "+commandline);
//        }
        String result = AppService.executeShellToSetIptables(commandline);
        if(DEBUG){
            Log.d(TAG, "executeShellToSetIptables result:"+result);
        }
        return result;
    }

    public boolean setUserPasswordPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_USER_PASSWORD_POLICIES, mode);
        return result;
    }

    public int getUserPasswordPolicies() {
        int result = Settings.Global.getInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_USER_PASSWORD_POLICIES, 3);
        return result;
    }

    public boolean setUserTimeMgrPolicies(int mode) {
        int policies = mode>0?1:0;
        if(policies == 0 ) {
            if(1 == Settings.Global.getInt(MainApp.mContext.getContentResolver(), Settings.Global.AUTO_TIME, 0)) {
                Settings.Global.putInt(MainApp.mContext.getContentResolver(), Settings.Global.AUTO_TIME, 0);
            }
            Settings.Global.putInt(MainApp.mContext.getContentResolver(), Settings.Global.AUTO_TIME, 1);
        }
        SystemProperties.set(PolicyManagerUtils.GXX_ENABLE_USER_TIME_MGR_SYS,""+policies);
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_ENABLE_USER_TIME_MGR, policies);
    }

    public int getUserTimeMgrPolicies() {
        return Settings.Global.getInt( MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_USER_TIME_MGR ,1 );
    }

    public boolean setFactoryResetPolicies(int mode) {
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_ENABLE_FACTORY_RESET, mode);
    }

    public int getFactoryResetPolicies() {
        return Settings.Global.getInt( MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_FACTORY_RESET ,1 );
    }

    public boolean setDevelopmentModePolicies(int mode) {
        Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, mode>0 ? 1 : 0);
        MainApp.mContext.sendBroadcast(new Intent("com.android.settingslib.development.DevelopmentSettingsEnabler.SETTINGS_CHANGED"));
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_DEVELOPMENT_MODE_POLICIES, mode);
    }

    public int getDevelopmentModePolicies() {
        Settings.Global.getInt( MainApp.mContext.getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED ,0 );
        return Settings.Global.getInt( MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_DEVELOPMENT_MODE_POLICIES ,1 );
    }

    public boolean setSystemUpdatePolicies(int mode) {
        return Settings.Global.putInt(MainApp.mContext.getContentResolver(), PolicyManagerUtils.GXX_ENABLE_SYS_UPDATE, mode);
    }

    public int getSystemUpdatePolicies() {
        return Settings.Global.getInt( MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_SYS_UPDATE ,1 );
    }

    public boolean setInstallUninstallPolicies(int mode, String[] appList) {
        return PolicyManagerUtils.setInstallUninstallPolicies(MainApp.mContext, mode, appList);
    }

    public String[] getInstallUninstallPolicies() {
        return PolicyManagerUtils.getInstallUninstallPolicies(MainApp.mContext);
    }

    public boolean setSilentInstallUninstallPolicies(int mode, String[] appList) {
        return PolicyManagerUtils.setSilentInstallUninstallPolicies(MainApp.mContext, mode, appList);
    }

    public String[] getSilentInstallUninstallPolicies() {
        return PolicyManagerUtils.getSilentInstallUninstallPolicies(MainApp.mContext);
    }

    public boolean setAdbInstallUninstallPolicies(int mode) {
        if( !(mode == 0 || mode == 1) ){
            return false;
        }
        try {
            SystemProperties.set("persist.sys.zfy.pmblock", String.valueOf(mode));
            return true;
        }catch (Exception e){

        }
        return false;
    }

    public int getAdbInstallUninstallPolicies() {
        String value = SystemProperties.get("persist.sys.zfy.pmblock", "0");
        if("0".equals(value)) {
            return 0;
        }else{
            return 1;
        }
    }

    public boolean installPackage(String pathToAPK) {
        Log.d(TAG, "installPackage apkfilepath : " + pathToAPK);

        // REQ005, installPackage, wmd, 2021.0714
        boolean blocked = PolicyManagerUtils.isBlockedBySilentInstallUninstallPolicies(MainApp.mContext, pathToAPK, null);
        Log.d(TAG, "installPackage blocked = " + blocked);
        if(blocked){
            return false;
        }

        File file = new File(pathToAPK);
        if(!file.exists())
            return false;
        else {
            InstallUtils.install(MainApp.mContext, pathToAPK, InstallResultReceiver.class);
            return true;
        }
    }

    public boolean uninstallPackage(String appPackageName) {
        Log.d(TAG, "uninstallPackage pkgName = " + appPackageName);

        // REQ005, installPackage, wmd, 2021.0714
        boolean blocked = PolicyManagerUtils.isBlockedBySilentInstallUninstallPolicies(MainApp.mContext, null, appPackageName);
        Log.d(TAG, "uninstallPackage blocked = " + blocked);
        if(blocked){
            return false;
        }

        try {
            PackageManager pm = MainApp.mContext.getPackageManager();
            pm.getPackageInfo(appPackageName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "pkgname dosen't exist!");
            return false;
        }
        InstallUtils.uninstall(MainApp.mContext, appPackageName);
        return true;
    }

    public boolean setAppInstallationPolicies(int mode, String[] appPackageNames) {

        int policies = mode>0?1:0;

        StringBuffer strbuffer = new StringBuffer();

        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_POLICIES_APP_INSTALLATION, policies);

        if( appPackageNames == null || appPackageNames.length ==0 ){
            Settings.Global.putString(MainApp.mContext.getContentResolver(),
                    PolicyManagerUtils.GXX_POLICIES_APP_INSTALL_PACKAGES,"");
        }else{
            for(int i=0;i<appPackageNames.length;i++){
                strbuffer.append(appPackageNames[i]);
                if(i!= appPackageNames.length-1){
                    strbuffer.append(",");
                }
            }
            Settings.Global.putString(MainApp.mContext.getContentResolver(),
                    PolicyManagerUtils.GXX_POLICIES_APP_INSTALL_PACKAGES,strbuffer.toString());

            Log.d("policies","set install policies ="+strbuffer.toString());
        }

        return result;
    }

    public String[] getAppInstallationPolicies() {

        String[] packagesArray = null;
        int mode = Settings.Global.getInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_POLICIES_APP_INSTALLATION, 0);

        String packages = Settings.Global.getString(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_POLICIES_APP_INSTALL_PACKAGES);
        Log.d("policies","get install policies ="+packages);
        if( packages == null || packages.equals("")){
            packagesArray = new String[1];
            packagesArray[0] = ""+mode;
        }else {

            String[] arrPackages = packages.split(",");

            if (arrPackages == null || packages.length() == 0) {
                packagesArray = new String[1];
                packagesArray[0] = "" + mode;
            } else {
                packagesArray = new String[arrPackages.length + 1];
                packagesArray[0] = "" + mode;
                for (int i = 0; i < arrPackages.length; i++) {
                    packagesArray[i + 1] = arrPackages[i];
                }
            }
        }
        return packagesArray;
    }

    public boolean setAppUninstallationPolicies(int mode, String[] appPackageNames) {
        int policies = mode>0?1:0;

        StringBuffer strbuffer = new StringBuffer();

        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_POLICIES_APP_UNINSTALLATION, policies);

        if( appPackageNames == null || appPackageNames.length ==0 ){
            Settings.Global.putString(MainApp.mContext.getContentResolver(),
                    PolicyManagerUtils.GXX_POLICIES_APP_UNINSTALL_PACKAGES,"");
        }else{
            for(int i=0;i<appPackageNames.length;i++){
                strbuffer.append(appPackageNames[i]);
                if(i!= appPackageNames.length-1){
                    strbuffer.append(",");
                }
            }
            Settings.Global.putString(MainApp.mContext.getContentResolver(),
                    PolicyManagerUtils.GXX_POLICIES_APP_UNINSTALL_PACKAGES,strbuffer.toString());

            for(int i=0;i<appPackageNames.length;i++) {
                try {
                    InstallUtils.uninstall(MainApp.mContext, appPackageNames[i]);
                }catch (Exception e){

                }
            }
        }
        Log.d("policies","set uninstall policies ="+strbuffer.toString());
        return result;
    }

    public String[] getAppUninstallationPolicies() {
        String[] packagesArray = null;
        int mode = Settings.Global.getInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_POLICIES_APP_UNINSTALLATION, 0);

        String packages = Settings.Global.getString(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_POLICIES_APP_UNINSTALL_PACKAGES);

        Log.d("policies","get uninstall policies ="+packages);
        if( packages == null || packages.equals("")){
            packagesArray = new String[1];
            packagesArray[0] = ""+mode;
        }else {
            String[] arrPackages = packages.split(",");

            if (arrPackages == null || packages.length() == 0) {
                packagesArray = new String[1];
                packagesArray[0] = "" + mode;
            } else {
                packagesArray = new String[arrPackages.length + 1];
                packagesArray[0] = "" + mode;
                for (int i = 0; i < arrPackages.length; i++) {
                    packagesArray[i + 1] = arrPackages[i];
                }
            }
        }
        return packagesArray;
    }

    public boolean setRunAppPolicies(int mode, String[] appPackageNameList) {
        List<String> appList = new ArrayList<String>();
        int policies = mode>0?1:0;

        StringBuffer strbuffer = new StringBuffer();

        SystemProperties.set(PolicyManagerUtils.GXX_POLICIES_RUN_APP, ""+policies);

        if( appPackageNameList == null || appPackageNameList.length ==0 ){
            SystemProperties.set(PolicyManagerUtils.GXX_POLICIES_RUN_APP_POLICIES, "");
        }else{
            for(int i=0;i<appPackageNameList.length;i++){
                appList.add(appPackageNameList[i]);
                strbuffer.append(appPackageNameList[i]);
                if(i!= appPackageNameList.length-1){
                    strbuffer.append(",");
                }
            }
            SystemProperties.set(PolicyManagerUtils.GXX_POLICIES_RUN_APP_POLICIES, strbuffer.toString());

            Log.d("policies","set runapp policies ="+strbuffer.toString());
        }

        final ActivityManager am = (ActivityManager) MainApp.mContext.getSystemService(
                Context.ACTIVITY_SERVICE);
        if(0 == mode){
            for(int i=0;i<appPackageNameList.length;i++){
                am.forceStopPackage(appPackageNameList[i]);
            }
        }else{
            try {
                List<String> thirdAppList = new ArrayList<String>();
                PackageManager pm = MainApp.mContext.getPackageManager();
                List<ApplicationInfo> listAppcations = pm
                        .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
                for (ApplicationInfo app : listAppcations) {
                    if (((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)
                            || (!appList.contains(app.packageName))) {
                        thirdAppList.add(app.packageName);
                    }
                }
                for (String pkgName : thirdAppList) {
                    am.forceStopPackage(pkgName);
                }
            }catch (Exception e){

            }

            for(int i=0;i<appPackageNameList.length;i++){
                PolicyManagerUtils.launchPackage(MainApp.mContext, appPackageNameList[i]);
                try {
                    Thread.sleep(600);
                } catch (Exception e) {
                }
            }
        }

        return true;
    }

    public String[] getRunAppPolicies() {

        String[] runAppPolicies = null;

        int mode = SystemProperties.getInt(PolicyManagerUtils.GXX_POLICIES_RUN_APP,0);

        String packages = SystemProperties.get(PolicyManagerUtils.GXX_POLICIES_RUN_APP_POLICIES,"");
        Log.d("policies","get runapp policies ="+packages);
        if( packages == null || packages.equals("")){
            runAppPolicies = new String[1];
            runAppPolicies[0] = ""+mode;
        }else {

            String[] arrPackages = packages.split(",");

            runAppPolicies = new String[arrPackages.length + 1];
            runAppPolicies[0] = "" + mode;
            for (int i = 0; i < arrPackages.length; i++) {
                runAppPolicies[i + 1] = arrPackages[i];
            }
        }
        return runAppPolicies;
    }

    public class Permission{
        String permission;
        int mode ;
    }


    public int getUid(String packageName){

        int uid = 0;

        PackageManager pm = MainApp.mContext. getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);  //获取
        for(PackageInfo info : packageInfos){
            if( info.packageName.equals( packageName)){
                uid = info.applicationInfo.uid;
            }
        }
        return uid;
    }
    public boolean setAppPermission(String appPackageName, String permissions) {

        if( permissions.isEmpty()||appPackageName.isEmpty() ){
            return false;
        }

        Log.d("permissions","setAppPermission string ="+ permissions);

        PackageManager pm = MainApp.mContext.getPackageManager();


        HashMap<String,Integer> perMap = new HashMap<>();
        try {
            JSONArray jsons = new JSONArray(permissions);
            if (jsons == null) {

                Log.d("permissions", "setAppPermission jsons == null");
                return false;
            }
            Log.d("permissions", "setAppPermission jsons.length == " + jsons.length());
            for (int i = 0; i < jsons.length(); i++) {
                JSONObject perJson = jsons.getJSONObject(i);
                String modeNames = perJson.getString("mode").toUpperCase();
                int index = SystemTools.getVendorPermissionIndex( perJson.getString("permission").toUpperCase() );
	if( index == -1){
	     continue;	
	}
                if( modeNames.equals("ALLOWED")){
                    perMap.put(SystemTools.getPlatePermission(index),0);
                }else{
                    perMap.put(SystemTools.getPlatePermission(index),-1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        if (perMap.size() > 0) {

            List<SystemTools.Permission> permissionsList = SystemTools.getNeedGrandPermissions(
                    MainApp.mContext,perMap,appPackageName);

            Log.d("permissions", "permissionsList size == "+permissionsList.size());

            for(SystemTools.Permission per : permissionsList){

                if( per.mode == 0 ){
                    pm.grantRuntimePermission(appPackageName, per.permission, UserHandle.OWNER);
                }else{
                    pm.revokeRuntimePermission(appPackageName, per.permission, UserHandle.OWNER);
                }
                    Log.d("permissions", "setAppPermission per.permission== "+per.permission+",per.mode= "+per.mode );
            }
        }

        return true;
    }

    public String getAppPermission(String appPackageName) {

        PackageManager packageManager = MainApp.mContext.getPackageManager();

        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(appPackageName, 0);

            List<SystemTools.Permission> permissions = SystemTools.getGrandedPermissions(MainApp.mContext,
                    packageInfo.requestedPermissions,appPackageName);

            JSONArray jsonArray = new JSONArray();

            for(SystemTools.Permission permission : permissions){
                int index =   SystemTools.getPlatePermissionIndex(permission.permission);
                if( index !=-1){
                    JSONObject item = new JSONObject();
                    try {
                        item.put("permission", SystemTools.getVendorPermission(index));
                        item.put("mode", permission.mode ==-1?"REMIND":"ALLOWED");
                        Log.d("permissions","get permissions mode ="+permission.mode);
                        jsonArray.put(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return jsonArray.toString();
        } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
        }
        return unknownString;
    }

    private Long[] getDataUsageByUid(int uid) throws RemoteException {
        Log.d(TAG, "getDataUsageByUid");
        long start = 0;
        long end = System.currentTimeMillis();
        NetworkStatsManager nsm = (NetworkStatsManager) MainApp.mContext.getSystemService(Context.NETWORK_STATS_SERVICE);

        android.app.usage.NetworkStats summaryStats;
        android.app.usage.NetworkStats.Bucket summaryBucket = new android.app.usage.NetworkStats.Bucket();
        Long[] result = new Long[]{0L, 0L};

        long totalTxBytes = 0;
        long totalRxBytes = 0;

        summaryStats = nsm.querySummary(ConnectivityManager.TYPE_WIFI, "0", start, end);
        do {
            summaryStats.getNextBucket(summaryBucket);
            int summaryUid = summaryBucket.getUid();
            long summaryRx = summaryBucket.getRxBytes();
            long summaryTx = summaryBucket.getTxBytes();
            Log.i(TAG, "getDataUsageByUid uid:" + summaryBucket.getUid() + " rx:" + summaryBucket.getRxBytes() +
                    " tx:" + summaryBucket.getTxBytes());
            if(uid == summaryUid) {
                totalTxBytes += summaryTx;
                totalRxBytes += summaryRx;
            }
        } while (summaryStats.hasNextBucket());

        result[0] = totalTxBytes;
        result[1] = totalRxBytes;
        Log.i(TAG, "getDataUsageByUid tx:" + result[0]+" rx:"+result[1]);
        return result;
    }

    public String[] getAppTrafficInfo(String appPackageName) {
        String[] traffic = new String[4];
        int uid = 0;
        PackageManager pm = MainApp.mContext.getPackageManager();
        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);  //获取
        for(PackageInfo info : packageInfos){
            if( info.packageName.equals( appPackageName)){
                uid = info.applicationInfo.uid;
            }
        }
        if( uid != 0){
            traffic[0] = String.valueOf(android.net.TrafficStats.getUidTxBytes(uid));
            traffic[1] = String.valueOf(android.net.TrafficStats.getUidRxBytes(uid));
        }

        Long[] wifiBytes = null;
        try {
            wifiBytes = getDataUsageByUid(uid);
        } catch (RemoteException re) {
            re.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "getAppTrafficInfo wifiBytes0:"+wifiBytes[0]+" wifiBytes1:"+wifiBytes[1]);
        if(wifiBytes != null && wifiBytes.length > 1){
            try {
                traffic[2] = String.valueOf(wifiBytes[0]);//WIFI怎么统计不知道
                traffic[3] = String.valueOf(wifiBytes[1]);
            }catch (Exception e){

            }
        }
        Log.d(TAG, "getAppTrafficInfo totalTxBytes:"+wifiBytes[0]+" totalRxBytes:"+wifiBytes[1]);

        return traffic;
    }

    public List getAppPowerUsage() {
        return getBatteryInfo( MainApp.mContext );
    }

    public List getAppRunInfo() {
        UsageStatsManager manager=(UsageStatsManager)MainApp.mContext.getApplicationContext()
                .getSystemService(Context.USAGE_STATS_SERVICE);
        ActivityManager am = (ActivityManager) MainApp.mContext.getSystemService(Context.ACTIVITY_SERVICE);

        PackageManager pm = MainApp.mContext.getPackageManager();

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);  //获取


        List<ActivityManager.RunningAppProcessInfo> runList = am.getRunningAppProcesses();
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new Date());
        long endt = calendar.getTimeInMillis();//结束时间
        calendar.add(Calendar.DAY_OF_YEAR, -1);//时间间隔为一年
        long statt = calendar.getTimeInMillis();//开始时间
        List<UsageStats> stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,statt,endt);
        if (stats == null || stats.isEmpty()) {
            return null;
        }

        ArrayList<String[]> list = new ArrayList<String[]>();

        for( UsageStats usageInfo : stats){
            String[] info = new String[4];
            info[0] = "";//默认为空, 通过packageinfo获取
            info[1] = "";//默认为空，通过runningProcess 获取
            info[2] = usageInfo.getPackageName();  //终端应用包名

            info[3] = String.valueOf( Math.abs(usageInfo.getLastTimeUsed()));  //终端应用已运行时间

            for(PackageInfo packageInfo : packageInfos ){
                if(packageInfo.applicationInfo.packageName.equals(usageInfo.getPackageName())){
                    info[1] = String.valueOf(packageInfo.applicationInfo.uid);
                }
            }

            for (ActivityManager.RunningAppProcessInfo runningProcess : runList) {
                if ((runningProcess.processName != null) && runningProcess.processName.equals(usageInfo.getPackageName()))
                {
                    info[0] = String.valueOf(runningProcess.pid);  //PID
                }
            }
            Log.d("getSoftwareInfo",","+info[0]+"\n"+info[1]+"\n"+info[2]+"\n"+info[3]+
                    "\n");
            list.add(info);
        }

        return list;
    }

    /**
     *
     * // REQ075, getAppRuntimeExceptionInfo, wmd, 2021.0630
     * @return
     */
    public List getAppRuntimeExceptionInfo() {
        ArrayList<String[]> list = new ArrayList<String[]>();
        String crashInfoStr = Settings.Global.getString(
                MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_APP_RUNTIME_EXCEPTION_INFO);
        if (DEBUG) {
            Log.d(TAG, "getAppRuntimeExceptionInfo crashInfoStr:" + crashInfoStr);
        }
        JSONObject crashInfo = null;
        try {
            if (!TextUtils.isEmpty(crashInfoStr)) {
                crashInfo = new JSONObject(crashInfoStr);
            } else {
                return list;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(crashInfo != null){
            try {
                Iterator iter = crashInfo.keys();
                while (iter.hasNext()) {
                    String[] info = new String[2];
                    info[0] = (String) iter.next();
                    info[1] = String.valueOf(crashInfo.get(info[0]));
                    if (DEBUG) {
                        Log.d(TAG, "getAppRuntimeExceptionInfo processName:" + info[0] + " count:" + info[1]);
                    }
                    list.add(info);
                }
            }catch (Exception e){
                if (DEBUG) {
                    Log.d(TAG, "getAppRuntimeExceptionInfo Exception:" + e.toString());
                }
            }
        }
        return list;
    }

    /**
     * 获取终端部分硬件信息和相关系统信息
     deviceInf[]：返回终端部分硬件信息和相关系统信息，具体如下：
     deviceInf[0]：IMEI/MEID（主卡对应的设备识别码）
     deviceInf[1]：MEID/MEID（副卡对应的设备识别码）（如无副卡，可为空）
     deviceInf[2]：运行内存容量（字符串，单位 GB，如 3GB）
     deviceInf[3]：内部存储容量（字符串，单位 GB，如 16GB）
     deviceInf[4]：屏幕分辨率（字符串，长*高，如 640*480）
     deviceInf[5]：终端生产厂商
     deviceInf[6]：终端型号
     deviceInf[7]：系统内核版本号
     deviceInf[8]：系统软件版本号
     deviceInf[9]：安全加固双操作系统版本（如无，可为空）
     deviceInf[10]：系统安全补丁程序级别（如无，可为空）
     deviceInf[11]：ICCID（SIM 卡 1 的 ICCID）
     deviceInf[12]：ICCID（SIM 卡 2 的 ICCID）（如无卡 2，可为空）
     deviceInf[13]：IMSI（SIM 卡 1 的 IMSI）
     deviceInf[14]：IMSI（SIM 卡 2 的 IMSI）（如无卡 2，可为空）
     deviceInf[15]：CPU 型号
     deviceInf[16]：支持的移动网络制式
     deviceInf[17]：无线网卡芯片型号
     deviceInf[18]：蓝牙芯片型号
     deviceInf[19]：NFC 芯片型号
     deviceInf[20]：定位芯片型号
     * @return
     */
    public String[] getDeviceInfo() {
        return PolicyManagerUtils.getDeviceInfo(MainApp.mContext);
    }

    public List getSoftwareInfo() {
        final int info_size = 7;

        PackageManager pm = MainApp.mContext.getPackageManager();

        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);  //获取所以已安装的包

        ArrayList<String[]> list = new ArrayList<String[]>();

        for( PackageInfo packageInfo : installedPackages){
            String[] info = new String[info_size];
            String packageName = packageInfo.packageName;  //包名
            String name = packageInfo.applicationInfo.loadLabel(pm).toString();  //应用名称
            info[0] = name;  //名称
            info[1] = packageInfo.packageName;  //包名
            info[2] = getTime(packageInfo.firstInstallTime);  //初次安装时间
            info[3] = getTime(packageInfo.lastUpdateTime);  //最后更新时间
            info[4] = packageInfo.versionName;  //用户友好版本号
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info[5] = String.valueOf(packageInfo.getLongVersionCode());  //内部版本号
            }else {
                info[5] = String.valueOf(packageInfo.versionCode);  //内部版本号
            }
            info[6] = "";  //开发者
            Log.d("getSoftwareInfo",","+info[0]+"\n"+info[1]+"\n"+info[2]+"\n"+info[3]+
                    "\n"+info[4]+"\n"+info[5]+"\n"+info[6]+"\n");
            list.add(info);
        }

        return list;
    }

    private String getTime(long timeMills){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeMills);
        return format.format(date);
    }

    /**
     * REQ078, getRootState, wmd, 2021.0611
     * @return
     */
    public boolean getRootState() {
        return PolicyManagerUtils.getRootState();
    }

    public boolean getSystemIntegrity() {
        return PolicyManagerUtils.getSystemIntegrity();
    }

    /**
     * 获取终端运行状态信息
     * string[0]：CPU 占用率
     * string[1]：内存占用率
     * string[2]：存储占用率
     * @return
     */
    public String[] getDeviceState() {
        return PolicyManagerUtils.getDeviceState(MainApp.mContext);
    }

    /**
     * 获取终端可信检测结果
     * @return
     */
    public String getTpmReport() {
        return MainApp.mContext.getResources().getString(R.string.gxx_tpm_report_has_verfied);
    }

    public boolean setContainerPolicies(int mode) {
        boolean result = Settings.Global.putInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_CONTAINER_POLICIES, mode);
        return result;
    }

    public int getContainerPolicies() {
        int result = Settings.Global.getInt(MainApp.mContext.getContentResolver(),
                PolicyManagerUtils.GXX_CONTAINER_POLICIES, 1);
        return result;
    }

    public int getContainerTotalNumber() {
        return 1;
    }

    public boolean setContainerNumber(int containerNumber) {
        return true;
    }

    public int getContainerNumber() {
        return 0;
    }

    public boolean isActived() {
        return true;
    }

    /**
     * REQ088, lockdevice, wmd, 2021.0611
     * @return
     */
    public boolean lockDevice() {
        Intent intent = new Intent(PolicyManagerUtils.GXX_ACTION_LOCK_DEVICE);
        MainApp.mContext.sendBroadcast(intent, PolicyManagerUtils.GXX_PERMISSION_DEVICE_LOCK_CONTROL);
        if(DEBUG) {
            Log.d(TAG, "lockDevice");
        }
        return true;
    }

    public boolean unlockDevice() {
        Intent intent = new Intent(PolicyManagerUtils.GXX_ACTION_UNLOCK_DEVICE);
        MainApp.mContext.sendBroadcast(intent, PolicyManagerUtils.GXX_PERMISSION_DEVICE_LOCK_CONTROL);
        if(DEBUG) {
            Log.d(TAG, "unlockDevice");
        }
        return true;
    }

    public boolean wipeDeviceData() {
        RemoteControl.requestFactoryReset(MainApp.mContext);
        if(DEBUG) {
            Log.d(TAG, "wipeDeviceData");
        }
        return true;
    }

    public boolean rebootDevice() {
        RemoteControl.requestReboot(MainApp.mContext);
        if(DEBUG) {
            Log.d(TAG, "rebootDevice");
        }
        return true;
    }

    public boolean shutdownDevice() {
        RemoteControl.requestShutdown(MainApp.mContext);
        if(DEBUG) {
            Log.d(TAG, "shutdownDevice");
        }
        return true;
    }

    public String getDevicePosition() {
        return PolicyManagerUtils.getDevicePosition(MainApp.mContext);
    }
    /**
     * 设置wifi
     * [{"ssid":"Tenda_2E5560","bssid":"c8:3a:35:2e:55:60","pwd":"12345678"},{"ssid":"cisco-60A8","bssid":"d8:24:bd:76:60:aa","pwd":"0123456789"}]
     * @param wlanConfig
     * @return
     */
    public boolean setWlanConfiguration(String wlanConfig) {
        boolean result = GosuncnSettings.Global.putString(
                MainApp.mContext.getContentResolver(),
                GosuncnSettings.Global.GXX_WLAN_CONFIGURATION,
                wlanConfig);

//        wlanConfig = "[{\"ssid\":\"Tenda_2E5560\",\"bssid\":\"c8:3a:35:2e:55:60\",\"pwd\":\"12345678\"},{\"ssid\":\"cisco-60A8\",\"bssid\":\"d8:24:bd:76:60:aa\",\"pwd\":\"0123456789\"}]";
//        wlanConfig = "[{\"ssid\":\"GOSUNCN\",\"bssid\":\"54:a7:03:ff:5c:59\",\"pwd\":\"yongmai123456\"}]";
        Gson gson = new Gson();
        WlanConfigurationInfo[] configs = gson.fromJson(wlanConfig, WlanConfigurationInfo[].class);
        for(WlanConfigurationInfo config: configs) {
            result = PolicyManagerUtils.setWlanConfiguration(MainApp.mContext, config);
        }

        if(DEBUG){
            Log.d(TAG, "setWlanConfiguration result:"+result+" wlanConfig:"+wlanConfig);
        }

        return result;
    }

    public String getWlanConfiguration() {
        String gxxWlanConfiguration = GosuncnSettings.Global.getString(
                MainApp.mContext.getContentResolver(),
                GosuncnSettings.Global.GXX_WLAN_CONFIGURATION,
                "");

        return PolicyManagerUtils.getWlanConfiguration(MainApp.mContext);
    }

    /**
     * {"name":"CTNET","APN":"ctnet","pwd":"12345678"}
     *         String apnInfo = "{\"name\":\"cmnetXXX\",\"apn\":\"cmnet\",\"numeric\":\"\",\"proxy\":\"\",\"port\":\"\",\"user\":\"\",\"password\":\"\",\"server\":\"\",\"mmsc\":\"\"," +
     *                 "\"mmsproxy\":\"\",\"mmsport\":\"\",\"mcc\":\"\",\"mnc\":\"\",\"authtype\":\"0\",\"type\":\"default,mms,supl,hipri,fota,ims,cbs\",\n" +
     *                 "\"protocol\":\"IPV4V6\",\"roaming_protocol\":\"IPV4V6\",\"bearer\":\"0\",\"bearer_bitmask\":\"0\",\"mvno_type\":\"imsi\",\"mvno_match_data\":\"46004x\"}";
     * @param apnInfo
     * @return
     */
    public int createApn(String apnInfo) {
        return PolicyManagerUtils.createApn(MainApp.mContext, apnInfo);
    }

    public boolean deleteApn(int apnId) {
        return PolicyManagerUtils.deleteApn(MainApp.mContext, apnId);
    }

    /**
     * REQ098, getApnList, wmd, 2021.0712
     * @return
     */
    public List getApnList() {
        List apnList = new ArrayList<Integer>();
        String apnListStr = Settings.Global.getString(
                MainApp.mContext.getContentResolver(),
                "gxx_get_apn_list"/*PolicyManagerUtils.GXX_GET_APN_LIST*/);
        if(DEBUG){
            Log.d(TAG, "getApnList apnListStr:"+apnListStr);
        }
        if(!TextUtils.isEmpty(apnListStr)) {
            String[] list = apnListStr.split(";");
            if(list != null && list.length > 0) {
                for (String apn : list) {
                    if(!TextUtils.isEmpty(apn)) {
                        apnList.add(Integer.valueOf(apn));
                    }
                }
            }
        }
        if(DEBUG){
            Log.d(TAG, "getApnList size:"+apnList.size());
        }
        return apnList;
//        return PolicyManagerUtils.getApnList(MainApp.mContext);
    }

    public String getApnInfo(int apnId) {
        return PolicyManagerUtils.getApnInfo(MainApp.mContext, apnId);
    }

    public int getCurrentApn() {
        return PolicyManagerUtils.getCurrentApn(MainApp.mContext);
    }

    public boolean setCurrentApn(int apnId) {
        return PolicyManagerUtils.setCurrentApn(MainApp.mContext, apnId);
    }

    /**
     * REQ102, setSysTime, wmd, 2021.0609
     * @param millis
     * @return
     */
    public boolean setSysTime(long millis) {
        return SystemClock.setCurrentTimeMillis(millis);
    }

    public boolean setLockPassword(String pwd) {
        return false;
    }

    public boolean isJwBuild() {
        return false;
    }

    public void addPersistentApp(String pkgName, boolean isPersist) {
        return;
    }

    public int setPassword(String pwd) {
        return 0;
    }

    public boolean resetPassword(String password) {
        return false;
    }

    /*
    *REQ121, NavigationBar  ,mode = 0 non_contrlol
    * */

    public void setNavigationBarDisabled(boolean disabled){

        int mode = disabled?1:0;
        Settings.Global.putInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_NAVIGATION,mode);
    }

    public boolean isNavigationBarDisabled(){

        int enable = Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_NAVIGATION,0);

        return  enable == 0 ? false:true;

    }

    /*
     *REQ121, StatusBar
     * */

    public void setStatusBarExpandPanelDisabled(boolean disabled){

        StatusBarManager mStatusBarManager = (StatusBarManager) MainApp.mContext.getSystemService(Context.STATUS_BAR_SERVICE);

        mStatusBarManager.disable( disabled ? StatusBarManager.DISABLE_EXPAND:StatusBarManager.DISABLE_NONE);

    }

    public boolean isStatusBarExpandPanelDisabled(){

        int enable = Settings.Global.getInt(MainApp.mContext.getContentResolver(),PolicyManagerUtils.GXX_ENABLE_STATUS_BAR_EXPAND,0);

        return  enable == 0 ? false:true;
    }
}
