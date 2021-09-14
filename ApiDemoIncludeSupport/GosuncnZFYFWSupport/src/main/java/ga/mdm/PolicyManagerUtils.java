package ga.mdm;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.VpnService;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

//import androidx.annotation.Nullable;
//import androidx.annotation.StringDef;
//import androidx.core.content.ContextCompat;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.gosuncn.bean.ApnItem;
import com.gosuncn.bean.CryptoModuleInfo;
import com.gosuncn.bean.InstallAppInfo;
import com.gosuncn.bean.MediaInfo;
import com.gosuncn.bean.WlanConfigurationInfo;
import com.gosuncn.utils.AppService;
import com.gosuncn.utils.DeviceBaseFuncMgr;
import com.gosuncn.utils.MonitorMgr;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.DoubleUtils;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Context.USB_SERVICE;
import static android.content.Context.WIFI_SERVICE;
import static android.content.pm.PackageManager.INSTALL_FAILED_ABORTED;


public class PolicyManagerUtils {

    private static final String TAG = PolicyManagerUtils.class.getSimpleName();
    static final boolean DEBUG = !"user".equals(Build.TYPE);
    private static PolicyManagerUtils mPolicyManager;

    public static final Long USB_FUNCTIONS_NONE = 0L;
    public static final Long USB_FUNCTIONS_MTP = 4L;
    public static final int WLAN_MODE_FOBID = 0;
    public static final int WLAN_MODE_SCANNING = 1;
    public static final int REMOTE_CALL_BACK_TYPE_SILENT_INSTALL_UNINSTALL = 9002;
    public static final int REMOTE_VPN_CONNECTED_OR_DISCONNECTED = 9003;

    /**
     * @return 获取mPolicyManager实例
     */
    public static PolicyManagerUtils getInstance() {
        if (mPolicyManager == null) {
            synchronized (PolicyManagerUtils.class) {
                if (mPolicyManager == null) {
                    mPolicyManager = new PolicyManagerUtils();
                }
            }
        }
        return mPolicyManager;
    }

    /**
     * 注册静默安装卸载应用的回调函数，用来监听安装卸载的结果
     * @param settingsContentObserver 接口ISettingsContentObserver对象，重写ISettingsContentObserver.Stub中的onchanged方法，对结果进行处理
     * @return  成功-true 失败-false
     */
    public boolean registerInstallorUnInstallAppSilentCallBack(ISettingsContentObserver settingsContentObserver){
        return GSFWManager.getInstance().registerSettingsContentObserver(REMOTE_CALL_BACK_TYPE_SILENT_INSTALL_UNINSTALL,settingsContentObserver);
    }

    /**
     * 注销静默安装卸载应用的回调函数，用来取消监听
     * @param settingsContentObserver　接口ISettingsContentObserver对象
     * @return   成功-true 失败-false
     */
    public boolean unregisterInstallorUnInstallAppSilentCallBack(ISettingsContentObserver settingsContentObserver){
        return GSFWManager.getInstance().unregisterSettingsContentObserver(settingsContentObserver);
    }

    public boolean registerVpnStateCallBack(ISettingsContentObserver settingsContentObserver){
        return GSFWManager.getInstance().registerSettingsContentObserver(REMOTE_VPN_CONNECTED_OR_DISCONNECTED,settingsContentObserver);
    }

    public boolean unregisterVpnStateCallBack(ISettingsContentObserver settingsContentObserver){
        return GSFWManager.getInstance().unregisterSettingsContentObserver(settingsContentObserver);
    }


    /**
     * 获取设备中存在的IMEI值
     * @param context 上下文对象
     * @return  返回String[]类型，列出设备中的imei表
     */
    public String[] listImei(/*@Nullable*/ Context context) {
        return AppService.getImeiNumber(context);
    }

    /**
     * 获取设备中存在的iccid值
     * @param context　上下文对象
     * @return  返回String[]类型，列出设备中的iccid表
     */
    public String[] listIccid(/*@Nullable */Context context) {
        return AppService.getIccidNumber(context);
    }

    /**
     * 获取vpn的连接状态
     * @param context　上下文对象
     * @return  false－未连接　true已连接
     */
    public Boolean getVpnConnectionState(Context context) {
        return AppService.getVpnConnectionState(context);
    }

    /**
     * 建立Vpn连接
     * @param vpnconfig Map<String,String></>类型的vpn配置，需要有参数(<key,value> :  "address", (value)  服务器地址，如xx.xx.xx.xx
     * @param context   上下文对象                                                   "dnsServer",(value)　dns地址，如8.8.8.8
     * @return   0成功    其他失败                                                    "route",(value)     转发地址，如0.0.0./0
     */
    public int establishVpnConnection(Map<String,String> vpnconfig,Context context){
            return AppService.establishVpnConnection(vpnconfig,context);
    }

    /**
     * 断开vpn连接
     * @return  ０成功　其他失败
     */
    public  int disestablishVpnConnection(){
        return AppService.disestablishVpnConnection();
    }

    /**
     * 应用静默安装
     * 不在用户界面显示任何提示，无打扰，apk在后台静默安装完毕
     * @param pathToAPK   app安裝包的路径全拼
     */
    public void installPackage(String pathToAPK) {
       AppService.installAppSilent(pathToAPK);
    }


    /**
     * 应用静默卸载
     * 不在用户界面显示任何提示，无打扰，apk在后台静默卸载完毕
     * @param context　上下文对象
     * @param appPackageName　应用的包名全拼
     * @return 可卸载返回成功，安装包不存在返回false
     */
    public boolean uninstallPackage(/*@Nullable*/ Context context, String appPackageName) {
        return AppService.uninstallAppSilent(context,appPackageName);
    }

    /**
     * WLAN管控
     * @param mode 0 禁止终端使用无线　１只允许终端进行无线网络指纹扫描
     * @return  true　成功　　false失败
     */
    public boolean setWlanPolicies(int mode){
        GSFWManager.getInstance().setWlanMode(mode);
//        WifiManager wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
//        if(mode == WLAN_MODE_FOBID) {
//            wifiManager.setWifiEnabled(false);
//            return !wifiManager.isWifiEnabled();
//        }if(mode == WLAN_MODE_SCANNING){
//            wifiManager.setWifiEnabled(true);
//            return wifiManager.isWifiEnabled();
//        }else
            return false;
    }

    /**
     * 限定跳转取值范围
     */
//    @Retention(RetentionPolicy.SOURCE)
//    @Target({ElementType.PARAMETER})
//    @StringDef(value = {SettingsMenu.ACTION_VPN_SETTINGS, SettingsMenu.ACTION_WIFI_SETTINGS, SettingsMenu.ACTION_BLUETOOTH_SETTINGS})
//    public @interface SettingsMenu {
    public static final String ACTION_VPN_SETTINGS = Settings.ACTION_VPN_SETTINGS;
    public static final String ACTION_WIFI_SETTINGS = Settings.ACTION_WIFI_SETTINGS;
    public static final String ACTION_BLUETOOTH_SETTINGS = Settings.ACTION_BLUETOOTH_SETTINGS;
//    }

    /**
     * 打开设置中的相关界面
     *
     * @param context
     * @param settingsmenu
     */
    public void openSingleSettingsActivity(/*@Nullable*/ Context context, /*@SettingsMenu */String settingsmenu) {
        Intent intent = new Intent(settingsmenu);
        context.startActivity(intent);
    }

    /**
     * 数据流量开关
     * false 关闭
     * true 打开
     * @param datastate
     */
    public void setMobileDataEnabled(Boolean datastate) {
        GSFWManager.getInstance().setMobileDataEnabled(datastate);
    }

    /**
     *
     * @param context
     * @return
     *  FUNCTION MTP　：4
     * FUNCTION ADB　：1
     * FUNCTION MIDI　：8
     * FUNCTION PTP　：16
     * FUNCTION ACCESSORY　：2
     * FUNCTION AUDIOSOURCE　：64
     * FUNCTION NONE　：0
     * FUNCTION RNDIS　：32
     */
    public Long getCurrentUsbFunction(Context context){
        return GSFWManager.getInstance().getCurrentUsbFunction();
    }

    public void setCurrentUsbFunction(Long function){
        GSFWManager.getInstance().setCurrentUsbFunction(function);
    }

    /**
     * 获取终端密码模块的硬件信息及当前连接情况
     * @return CryptoModuleInfo
     * 字段：
     * result String 必须 0：查询成功
     *                   1：终端密码模块连接异常
     *                   2：终端密码模块状态异常
     *                   3：终端密码模块信息读取错误
     *                   仅当为“0”时，后续字段有效，否则其余字段无效
     *  moduleType String 可选 终端密码模块类型，包括但不限于tfcard、usbkey
     *  manufacture String 可选 终端密码模块生产厂商
     *  moduleId String 可选 终端密码模块硬件编号
     */
    public CryptoModuleInfo getCryptoModuleInfo() {
        return AppService.getCryptoModuleInfo();
    }

    /**
     * 网络规则管控
     * 网络访问规则控制应支持对目标网络、主机进行访问控制，规则需对目标IP地址、网络掩码、端口、协议、是否允许访问等进行描述。
     * @param commandline ， 如： iptables -A OUTPUT -m owner --uid-owner=10032 -j DROP
     *                           ip6tables -A OUTPUT -m owner --uid-owner=10032 -j DROP
     * @return 返回值为命令执行的标准输出或标准错误输出。目前只能返回指令执行的结果，true-成功，false-失败
     */
    public String executeShellToSetIptables(String commandline){
        return AppService.executeShellToSetIptables(commandline);
    }

    /**
     * 设置应用adb方式安装/卸载功能控制状态
     * @param mode 功能模式
     * 0：允许使用adb方式安装/卸载终端应用；
     * 1：不允许使用adb方式安装/卸载终端应用。
     * @return 成功返回true；失败返回false。
     */
    public static boolean setAdbInstallUninstallPolicies(int mode) {
        return DeviceBaseFuncMgr.setAdbInstallUninstallPolicies(mode);
    }

    /**
     * 获取当前adb方式安装/卸载功能管控状态
     * @return 返回值为当前adb方式安装/卸载功能管控状态，参见setAdbInstallUnistallPolicies方法的参数mode
     */
    public static int getAdbInstallUninstallPolicies() {
        return DeviceBaseFuncMgr.getAdbInstallUninstallPolicies();
    }

    /**
     * ROOT状态检测
     * @return 设备已ROOT返回true；设备未ROOT返回false
     */
    public static boolean getRootState() {
        if(DEBUG)
            Log.d(TAG, "getRootState A");
        File file = null;
        try {
            String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                    "/system/bin/failsafe/su", "/data/local/su"};
            for (String path : paths) {
                file = new File(path);
                if (file != null && file.exists()) {
                    Log.d(TAG, "getRootState file exists");
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(DEBUG)
            Log.d(TAG, "getRootState S");
        Process process = null;
        try {
            //   /system/xbin/which 或者  /system/bin/which
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) {
                Log.d(TAG, "getRootState "+in.readLine());
                return true;
            }
            if(DEBUG)
                Log.d(TAG, "getRootState EA");
            return false;
        } catch (Throwable t) {
            if(DEBUG)
                Log.d(TAG, "getRootState EB");
            return false;
        } finally {
            if (process != null) process.destroy();
        }
//        return MonitorMgr.getRootState();
    }

    /**
     * 系统完整性检测
     * @return 统完整性未被破坏返回true；系统完整性被破坏返回false
     */
    public static boolean getSystemIntegrity() {
        String verifiedbootstate = SystemProperties.get("ro.boot.verifiedbootstate", "");
        String veritymode = SystemProperties.get("ro.boot.veritymode", "");
        if(DEBUG){
            Log.d(TAG, "getSystemIntegrity verifiedbootstate:"+verifiedbootstate+" veritymode:"+veritymode);
        }
        boolean result = false;
        result = !(("red".equals(verifiedbootstate)) || "disabled".equals(veritymode));
        if(DEBUG){
            Log.d(TAG, "getSystemIntegrity result:"+result);
        }
        return result;
//        return MonitorMgr.getSystemIntegrity();
    }

    /**
     * 以Base64编码列举终端密码模块内存储的所有数字证书
     * @return 成功：返回包含所有数字证书的列表；失败：返回空指针null
     */
    public static String[] listCertificates() {
        return AppService.listCertificates();
    }

    //REQ103, key control, wmd, 2021.0608
//        GSFWManager.java
//        public static final int KEYCODE_VIDEO = 24; // 0x80
//        public static final int KEYCODE_AUDIO = 25; // 0x40
//        public static final int KEYCODE_CAMERA = 66; // 0x20
//        public static final int KEYCODE_SOS = 87; // 0x10
//        public static final int KEYCODE_PTT = 88; // 0x08
    public static final int GXX_KEY_CONTROL_VIDEO = 0x80;
    public static final int GXX_KEY_CONTROL_AUDIO = 0x40;
    public static final int GXX_KEY_CONTROL_CAMERA = 0x20;
    public static final int GXX_KEY_CONTROL_SOS = 0x10;
    public static final int GXX_KEY_CONTROL_PTT = 0x08;

    public static final String GXX_KEY_CONTROL = "gxx_key_control";
    public static final String GXX_CONTAINER_POLICIES = "gxx_container_policies";
	
	public static final String GXX_WLAN_POLICIES = "gxx_wlan_policies";
    public static final String GXX_DATA_CONNECTIVITY_POLICIES = "gxx_data_connectivity_policies";
    public static final String GXX_BLUETOOTH_POLICIES = "gxx_bluetooth_policies";
    public static final String GXX_BLUETOOTH_INFO_LIST = "gxx_bluetooth_info_list";

    public static final String GXX_NFC_POLICIES = "gxx_nfc_policies";
    public static final String GXX_IR_POLICIES = "gxx_ir_policies";
    public static final String GXX_BIOMETRIC_RECOGNITION_POLICIES = "gxx_biometric_recognition_policies";
    public static final String GXX_GPS_POLICIES = "gxx_gps_policies";
    public static final String GXX_USBDATA_POLICIES = "gxx_usbdata_policies";
    public static final String GXX_EXTERNAL_STORAGE_POLICIES = "gxx_external_storage_policies";
    public static final String GXX_MICROPHONE_POLICIES = "gxx_microphone_policies";
    public static final String GXX_SPEAKER_POLICIES = "gxx_speaker_policies";
    public static final String GXX_CAMERA_POLICIES = "gxx_camera_policies";
    public static final String GXX_FLASH_POLICIES = "gxx_flash_policies";

    public static final String GXX_PERIPHERAL_POLICIES = "gxx_peripheral_policies";
    public static final String GXX_VOICE_POLICIES = "gxx_voice_policies";
    public static final String GXX_SMS_POLICIES = "gxx_sms_policies";

    public static final String GXX_USER_APN_MGR_POLICIES = "gxx_user_apn_mgr_policies";
    public static final String GXX_USER_PASSWORD_POLICIES = "gxx_user_password_policies";

    public static final String GXX_ENABLE_SCREEN_CAPTURE = "gxx_enable_screen_capture";
	//REQ050 ,wzn,20210618
    public static final String GXX_ENABLE_USER_TIME_MGR = "gxx_enable_user_time_mgr";
	public static final String GXX_ENABLE_USER_TIME_MGR_SYS = "persist.sys.gxxTIMEMGR";
	
    public static final String GXX_ENABLE_FACTORY_RESET = "gxx_enable_factory_reset";
    public static final String GXX_DEVELOPMENT_MODE_POLICIES = "gxx_development_mode_policies";

	//REQ 56
	public static final String GXX_ENABLE_SYS_UPDATE = "gxx_enable_sys_update";
    public static final String GXX_POLICIES_APP_INSTALLATION = "gxx_policies_app_installation";
    public static final String GXX_POLICIES_APP_INSTALL_PACKAGES = "gxx_policies_app_installation_packages";
	
    public static final String GXX_ENABLE_WLAN_AP = "gxx_enable_wlan_ap";
    public static final String GXX_ENABLE_WLAN_AP_MACLIST = "gxx_enable_wlan_ap_maclist";
	
	public static final String GXX_POLICIES_APP_UNINSTALLATION = "gxx_policies_app_uninstallation";
    public static final String GXX_POLICIES_APP_UNINSTALL_PACKAGES = "gxx_policies_app_uninstallation_packages";

    public static final String GXX_POLICIES_RUN_APP = "persist.sys.gxxRUNAPP";
    public static final String GXX_POLICIES_RUN_APP_POLICIES = "persist.sys.RUNAPPPACKAGES";
    public static final String GXX_POLICIES_APP_RUN_INFO = "gxx_policies_app_run_info";

    // REQ058, setInstallUninstallPolicies, wmd, 2021.0629
    public static final String GXX_POLICIES_INSTALL_UNINSTALL_MODE = "gxx_policies_install_uninstall_mode";
    public static final String GXX_POLICIES_INSTALL_UNINSTALL_APPLIST = "gxx_policies_install_uninstall_applist";
    public static final String GXX_POLICIES_SILENT_INSTALL_UNINSTALL_MODE = "gxx_policies_silent_install_uninstall_mode";
    public static final String GXX_POLICIES_SILENT_INSTALL_UNINSTALL_APPLIST = "gxx_policies_silent_install_uninstall_applist";

    // REQ075, getAppRuntimeExceptionInfo, wmd, 2021.0630
    public static final String GXX_APP_RUNTIME_EXCEPTION_INFO = "gxx_app_runtime_exception_info";
    public static final String GXX_GET_APN_LIST = "gxx_get_apn_list";

    // REQ088, lockdevice, wmd, 2021.0611
    public static final String GXX_ACTION_LOCK_DEVICE = "android.intent.action.gxx_lock_device";
    public static final String GXX_ACTION_UNLOCK_DEVICE = "android.intent.action.gxx_unlock_device";

    public static final String GXX_PERMISSION_DEVICE_LOCK_CONTROL = "android.permission.GXX_DEVICE_LOCK_CONTROL";
	
	// REQ 120 121 ,statusBar Navigation
	public static final String GXX_ENABLE_STATUS_BAR_EXPAND = "gxx_enable_status_bar_expand";
	public static final String GXX_ENABLE_NAVIGATION = "gxx_enable_navigation";	

    // REQ113, getMonitorInfo, wmd, 2021-0608
    public static String getExternalStorageFilePath(Context context){
        String path = null;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume[] volumes = storageManager.getVolumeList();
            for (int index = 0; index < volumes.length; index++) {
                path = volumes[index].getPath();
                if(DEBUG) {
                    Log.d(TAG, "getExternalStorageFilePath path:" + path);
                }
                /*File file = new File(path);*/
                if (volumes[index].isRemovable()/* && file.exists() && file.isDirectory()*/) {
                    return path;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return path;
    }

    public static String getInternalStorageFilePath(Context context){
        String path = null;
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume[] volumes = storageManager.getVolumeList();
            for (int index = 0; index < volumes.length; index++) {
                path = volumes[index].getPath();
                if(DEBUG) {
                    Log.d(TAG, "getInternalStorageFilePath path:" + path);
                }
                /*File file = new File(path);*/
                if (!volumes[index].isRemovable()/* && file.exists() && file.isDirectory()*/) {
                    return path;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return path;
    }

    public static String getExternalStorageAvailableSize(String filePath) {
        if(!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if(file != null && file.exists() && file.isDirectory()) {
                StatFs mStatFs = new StatFs(filePath);
                long blockSize = mStatFs.getBlockSize();
                long availableBlocks = mStatFs.getAvailableBlocks();
                return byteToString(availableBlocks * blockSize);
            }
        }
        return "";
    }

    public static String byteToString(long size) {
        long GB = 1024 * 1024 * 1024;
        long MB = 1024 * 1024;
        long KB = 1024;
        DecimalFormat df = new DecimalFormat("0.00");
        String resultSize = "";
        if (size / GB >= 1) {
            resultSize = df.format(size / (float) GB) + " GB";
        } else if (size / MB >= 1) {
            resultSize = df.format(size / (float) MB) + " MB";
        } else if (size / KB >= 1) {
            resultSize = df.format(size / (float) KB) + " KB";
        } else {
            resultSize = size + " B";
        }
        return resultSize;
    }

    public static MediaInfo getMediaInfo(Context context){
        MediaInfo mediaInfo = new MediaInfo();

        Cursor mCursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Video.Media.DURATION},
                MediaStore.Video.Media.MIME_TYPE + "=?",
                new String[]{"video/mp4"}, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
        if(mCursor!=null && mCursor.moveToFirst()){
            mediaInfo.videoCount = mCursor.getCount();
            do {
                mediaInfo.videoDuration += (long)mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
            }while (mCursor.moveToNext());
            mCursor.close();
        }

        mCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Audio.Media.DURATION},
                MediaStore.Audio.Media.MIME_TYPE + "=?",
                new String[]{"audio/x-wav"}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(mCursor!=null && mCursor.moveToFirst()){
            mediaInfo.audioCount = mCursor.getCount();
            do{
                mediaInfo.audioDuration += (long)mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            }while (mCursor.moveToNext());
            mCursor.close();
        }

        mCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Images.Media._ID},
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/jpg"},MediaStore.Images.Media.DEFAULT_SORT_ORDER);
        if(mCursor!=null){
            mediaInfo.imageCount = mCursor.getCount();
            mCursor.close();
        }

        return mediaInfo;
    }

    public static String formatDuration(long mss) {
        StringBuffer sb = new StringBuffer();
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        if(days > 0){
            sb.append(days).append("天");
        }
        if(hours > 0){
            sb.append(hours).append("小时");
        }
        if(minutes > 0){
            sb.append(minutes).append("分钟");
        }
        if(seconds > 0){
            sb.append(seconds).append("秒");
        }
        return sb.toString();
    }

    /**
     * 获取位置
     * {"longitude"=" 经 度 值 ","latitude"=" 纬 度 值 ",
     * "height"="高度值"}
     * "{\"longitude\"=\"121.400792\",\"latitude\"=\"31.166922\",\n" +
     *                 "\"height\"=\"0\"}";
     * @param context
     * @return
     */
    public static String getDevicePosition(Context context){
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("longitude", String.valueOf(((location != null) ? location.getLongitude() : 0)));
        jsonObject.addProperty("latitude", String.valueOf(((location != null) ? location.getLatitude() : 0)));
        jsonObject.addProperty("height", String.valueOf(((location != null) ? location.getAltitude() : 0)));

        return jsonObject.toString();
    }

    public static String getSSID(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "ssid empty";
    }

    public static String getWifiBSSID(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            return  winfo.getBSSID();
        }
        return "bssid empty";
    }

    public static String getWlanConfiguration(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configurationInfos = wifiManager.getConfiguredNetworks();

        if(configurationInfos == null || configurationInfos.size() < 1){
            return "";
        }

        List<WlanConfigurationInfo> infos = new ArrayList<WlanConfigurationInfo>();
        for(WifiConfiguration configuration : configurationInfos){
            WlanConfigurationInfo info = new WlanConfigurationInfo();
            info.setSsid((!TextUtils.isEmpty(configuration.SSID)) ? configuration.SSID.replaceAll("\"", "") : "");
            info.setBssid((!TextUtils.isEmpty(configuration.BSSID)) ? configuration.BSSID.replaceAll("\"", "") : "");
            info.setPwd((!TextUtils.isEmpty(configuration.preSharedKey)) ? configuration.preSharedKey.replaceAll("\"", "") : "");
            if(DEBUG){
                Log.d(TAG, "getWlanConfiguration SSID:" + configuration.SSID+" BSSID:"+configuration.BSSID+" preSharedKey:"+configuration.preSharedKey);
            }
            infos.add(info);
        }

        WlanConfigurationInfo[] infoArray = (WlanConfigurationInfo[])infos.toArray(new WlanConfigurationInfo[0]);
        Gson gson = new Gson();
        String jsonArray = gson.toJson(infoArray, WlanConfigurationInfo[].class);
        if(DEBUG){
            Log.d(TAG, "getWlanConfiguration result:" + jsonArray);
        }
        return jsonArray;
    }

    public static boolean setWlanConfiguration(Context context, WlanConfigurationInfo info) {

        if(info == null)
            return false;

        String ssid = info.getSsid();
        String bssid = info.getBssid();
        String password = info.getPwd();
        if(DEBUG){
            Log.d(TAG, "setWlanConfiguration ssid:"+ssid + " bssid:"+bssid + " password:"+password);
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configurationInfos = wifiManager.getConfiguredNetworks();

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
//        wifiConfig.BSSID = String.format("\"%s\"", bssid);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

        if(TextUtils.isEmpty(password)) {
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedAuthAlgorithms.clear();
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }else {
            wifiConfig.hiddenSSID = true;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfig.preSharedKey = String.format("\"%s\"", password);
        }

        int netId = wifiManager.addNetwork(wifiConfig);
        if (netId == -1) {
            for(WifiConfiguration wifiConfiguration : configurationInfos){
                if(wifiConfig.SSID.equals(wifiConfiguration.SSID)){
                    netId = wifiConfiguration.networkId;
                }
            }
        }
        if(DEBUG){
            Log.d(TAG, "setWlanConfiguration netId:"+netId);
        }
        if (netId == -1) {
            return false;
        }else{
            wifiManager.enableNetwork(netId, true);
            return true;
        }
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9A-F]+$");
    public static boolean isHexString(String password) {
        return HEX_PATTERN.matcher(password).matches();
    }

    public static int createApn(Context context, String apnInfo) {
        if(DEBUG) {
            Log.d(TAG, "createApn apnInfo:" + apnInfo);
        }
        Gson gson = new Gson();
        ApnItem apnItem = gson.fromJson(apnInfo, ApnItem.class);

        int id = -1;
//        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//        String numeric = telephonyManager.getSimOperator();
//        if (TextUtils.isEmpty(numeric)) {
//            Log.d(TAG, "createApn exit: numeric emtpry");
//            return -1;
//        }
        if(TextUtils.isEmpty(apnItem.getMcc()) || TextUtils.isEmpty(apnItem.getMnc())){
            Log.d(TAG, "createApn exit: no mcc or mnc");
            return -1;
        }

        ContentResolver resolver = context.getContentResolver();
        // query by name and apn, ?
        ContentValues values = new ContentValues();
        try {
            values.put(Telephony.Carriers.NAME, apnItem.getName());
            values.put(Telephony.Carriers.APN, apnItem.getApn());
            values.put(Telephony.Carriers.TYPE, apnItem.getType());
            values.put(Telephony.Carriers.NUMERIC, apnItem.getMcc() + apnItem.getMnc());
            values.put(Telephony.Carriers.MCC, apnItem.getMcc());
            values.put(Telephony.Carriers.MNC, apnItem.getMnc());
            values.put(Telephony.Carriers.PROXY, apnItem.getProxy());
            values.put(Telephony.Carriers.PORT, apnItem.getPort());
            values.put(Telephony.Carriers.MMSPROXY, apnItem.getMmsproxy());
            values.put(Telephony.Carriers.MMSPORT, apnItem.getMmsport());
            values.put(Telephony.Carriers.USER, apnItem.getUser());
            values.put(Telephony.Carriers.SERVER, apnItem.getServer());
            values.put(Telephony.Carriers.PASSWORD, apnItem.getPassword());
            values.put(Telephony.Carriers.MMSC, apnItem.getMmsc());
            values.put(Telephony.Carriers.PROTOCOL, apnItem.getProtocol());
            values.put(Telephony.Carriers.ROAMING_PROTOCOL, apnItem.getRoaming_protocol());
            try {
                values.put(Telephony.Carriers.BEARER, Integer.valueOf(apnItem.getBearer()));
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                values.put(Telephony.Carriers.BEARER_BITMASK, Integer.valueOf(apnItem.getBearer_bitmask()));
            }catch (Exception e){
                e.printStackTrace();
            }
            values.put(Telephony.Carriers.MVNO_TYPE, apnItem.getMvno_type());
            values.put(Telephony.Carriers.MVNO_MATCH_DATA, apnItem.getMvno_match_data());
            try {
                values.put(Telephony.Carriers.AUTH_TYPE, Integer.valueOf(apnItem.getAuthtype()));
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                values.put(Telephony.Carriers.NETWORK_TYPE_BITMASK, Integer.valueOf(apnItem.getNetwork_type_bitmask()));
            }catch (Exception e){
                e.printStackTrace();
            }
            values.put(Telephony.Carriers.EDITED, Telephony.Carriers.USER_EDITED);
            values.put(Telephony.Carriers.USER_EDITABLE, Telephony.Carriers.USER_EDITED);
            values.put(Telephony.Carriers.CARRIER_ENABLED, 1);
        }catch (Exception e){
            e.printStackTrace();
        }

        Cursor c = null;
        try {
            Uri newRow = resolver.insert(Telephony.Carriers.CONTENT_URI, values);
            if (newRow != null) {
                c = resolver.query(newRow, null, null, null, null);
                int idIndex = c.getColumnIndex("_id");
                c.moveToFirst();
                id = c.getShort(idIndex);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (c != null)
            c.close();
        Log.d(TAG, "createApn id:"+id);

        return id;
    }

    public static boolean deleteApn(Context context, int apnId) {
        ContentResolver resolver = context.getContentResolver();
        int id = resolver.delete(Telephony.Carriers.CONTENT_URI, Telephony.Carriers._ID + "=?",
                new String[]{String.valueOf(apnId)});
        Log.d(TAG, "deleteApn id:"+id);
        return (id != -1);
    }

    public static List getApnList(Context context) {
        if(DEBUG){
            Log.d(TAG, "getApnList S");
        }
        List<String> apnList = new ArrayList<String>();
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String numeric = telephonyManager.getSimOperator();
        if (TextUtils.isEmpty(numeric)) {
            Log.d(TAG, "getApnList exit: numeric emtpry");
            return apnList;
        }
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Telephony.Carriers.CONTENT_URI, null, Telephony.Carriers.NUMERIC + "=?",
                new String[]{numeric}, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if(cursor != null && cursor.moveToFirst()){
            do {
                String apnString = null;
                try{
                    apnString = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers._ID)));
                }catch (Exception e){

                }
//                ApnItem apnItem = new ApnItem();
//
//                apnItem.setName(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.NAME)));
//                apnItem.setApn(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.APN)));
//                apnItem.setType(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.TYPE)));
//                apnItem.setNumeric(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.NUMERIC)));
//                apnItem.setMcc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MCC)));
//
//                apnItem.setMnc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MNC)));
//                apnItem.setProxy(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PROXY)));
//                apnItem.setPort(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PORT)));
//                apnItem.setMmsproxy(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPROXY)));
//                apnItem.setMmsport(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPORT)));
//
//                apnItem.setUser(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.USER)));
//                apnItem.setServer(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.SERVER)));
//                apnItem.setPassword(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PASSWORD)));
//                apnItem.setMmsc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSC)));
//                apnItem.setProtocol(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PROTOCOL)));
//
//                apnItem.setRoaming_protocol(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.ROAMING_PROTOCOL)));
//                apnItem.setBearer(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.BEARER))));
//                apnItem.setBearer_bitmask(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.BEARER_BITMASK))));
//                apnItem.setMvno_type(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MVNO_TYPE)));
//                apnItem.setMvno_match_data(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MVNO_MATCH_DATA)));
//
//                apnItem.setAuthtype(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.AUTH_TYPE))));
//                apnItem.setNetwork_type_bitmask(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.NETWORK_TYPE_BITMASK))));
//
//                Gson gson = new Gson();
//                String apnString = gson.toJson(apnItem, ApnItem.class);
//                if(DEBUG){
//                    Log.d(TAG, "getApnList apnString:" + apnString);
//                }
                if(!TextUtils.isEmpty(apnString)) {
                    apnList.add(apnString);
                }
            }while (cursor.moveToNext());
        }
        if(DEBUG){
            Log.d(TAG, "getApnList apnList size:" + apnList.size()+" list:"+apnList.toString());
        }
        return apnList;
    }

    public static List getApnList2(Context context) {
        if(DEBUG){
            Log.d(TAG, "getApnList S");
        }
        List<String> apnList = new ArrayList<String>();
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String numeric = telephonyManager.getSimOperator();
        if (TextUtils.isEmpty(numeric)) {
            Log.d(TAG, "getApnList exit: numeric emtpry");
            return apnList;
        }
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Telephony.Carriers.CONTENT_URI, null, Telephony.Carriers.NUMERIC + "=?",
                new String[]{numeric}, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if(cursor != null && cursor.moveToFirst()){
            do {
                ApnItem apnItem = new ApnItem();

                apnItem.setName(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.NAME)));
                apnItem.setApn(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.APN)));
                apnItem.setType(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.TYPE)));
                apnItem.setNumeric(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.NUMERIC)));
                apnItem.setMcc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MCC)));

                apnItem.setMnc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MNC)));
                apnItem.setProxy(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PROXY)));
                apnItem.setPort(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PORT)));
                apnItem.setMmsproxy(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPROXY)));
                apnItem.setMmsport(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPORT)));

                apnItem.setUser(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.USER)));
                apnItem.setServer(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.SERVER)));
                apnItem.setPassword(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PASSWORD)));
                apnItem.setMmsc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSC)));
                apnItem.setProtocol(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PROTOCOL)));

                apnItem.setRoaming_protocol(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.ROAMING_PROTOCOL)));
                apnItem.setBearer(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.BEARER))));
                apnItem.setBearer_bitmask(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.BEARER_BITMASK))));
                apnItem.setMvno_type(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MVNO_TYPE)));
                apnItem.setMvno_match_data(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MVNO_MATCH_DATA)));

                apnItem.setAuthtype(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.AUTH_TYPE))));
                apnItem.setNetwork_type_bitmask(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.NETWORK_TYPE_BITMASK))));

                Gson gson = new Gson();
                String apnString = gson.toJson(apnItem, ApnItem.class);
                if(DEBUG){
                    Log.d(TAG, "getApnList apnString:" + apnString);
                }
                apnList.add(apnString);
            }while (cursor.moveToNext());
        }
        if(DEBUG){
            Log.d(TAG, "getApnList apnList size:" + apnList.size());
        }
        return apnList;
    }
    public static String getApnInfo(Context context, int apnId) {
        if(DEBUG){
            Log.d(TAG, "getApnInfo S");
        }
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Telephony.Carriers.CONTENT_URI, null, Telephony.Carriers._ID + "=?",
                new String[]{String.valueOf(apnId)}, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()){
            ApnItem apnItem = new ApnItem();

            apnItem.setName(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.NAME)));
            apnItem.setApn(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.APN)));
            apnItem.setType(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.TYPE)));
            apnItem.setNumeric(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.NUMERIC)));
            apnItem.setMcc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MCC)));

            apnItem.setMnc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MNC)));
            apnItem.setProxy(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PROXY)));
            apnItem.setPort(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PORT)));
            apnItem.setMmsproxy(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPROXY)));
            apnItem.setMmsport(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSPORT)));

            apnItem.setUser(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.USER)));
            apnItem.setServer(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.SERVER)));
            apnItem.setPassword(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PASSWORD)));
            apnItem.setMmsc(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MMSC)));
            apnItem.setProtocol(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.PROTOCOL)));

            apnItem.setRoaming_protocol(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.ROAMING_PROTOCOL)));
            apnItem.setBearer(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.BEARER))));
            apnItem.setBearer_bitmask(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.BEARER_BITMASK))));
            apnItem.setMvno_type(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MVNO_TYPE)));
            apnItem.setMvno_match_data(cursor.getString(cursor.getColumnIndex(Telephony.Carriers.MVNO_MATCH_DATA)));

            apnItem.setAuthtype(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.AUTH_TYPE))));
            apnItem.setNetwork_type_bitmask(String.valueOf(cursor.getInt(cursor.getColumnIndex(Telephony.Carriers.NETWORK_TYPE_BITMASK))));

            Gson gson = new Gson();
            String apnString = gson.toJson(apnItem, ApnItem.class);
            if(DEBUG){
                Log.d(TAG, "getApnInfo apnString:" + apnString);
            }
            return apnString;
        }
        return "";
    }

    public static int getCurrentApn(Context context) {
        String apnIdStr = null;
        Cursor cursor = context.getContentResolver().query(getApnPreferUriForCurrSubId(context, Uri.parse("content://telephony/carriers/preferapn")),
                new String[] {"_id"}, null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            apnIdStr = cursor.getString(cursor.getColumnIndex("_id"));
        }
        if(cursor != null) {
            cursor.close();
        }
        if(DEBUG){
            Log.d(TAG, "getCurrentApn apnId:" + apnIdStr);
        }
        int apnId =  -1;
        try {
            apnId = Integer.valueOf(apnIdStr);
        }catch (Exception e){
            e.printStackTrace();
        }
        return apnId;
    }

    public static boolean setCurrentApn(Context context, int apnId) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", apnId);
        int id = resolver.update(getApnPreferUriForCurrSubId(context, Uri.parse("content://telephony/carriers/preferapn")), values, null, null);
        return (id != -1);
    }

    public static int getSubIdBySlotId(Context context, int slotId){
        int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        SubscriptionInfo subscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfoForSimSlotIndex(slotId);
        if(null != subscriptionInfo){
            subId = subscriptionInfo.getSubscriptionId();
        }
        if(DEBUG){
            Log.d(TAG, "getSubIdBySlotId subId:" + subId+" slotId:"+slotId);
        }
        return subId;
    }

    public static Uri getApnPreferUriForCurrSubId(Context context, Uri uri) {
        int slotId = 0; // ?
        int subId = getSubIdBySlotId(context, slotId);
        Uri result = null;
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            result = Uri.withAppendedPath(uri, "subId/" + String.valueOf(subId));
        } else {
            result = uri;
        }
        if(DEBUG){
            Log.d(TAG, "getApnPreferUriForCurrSubId result:" + result.toString());
        }
        return result;
    }

    public static String[] listIMSI(/*@Nullable */Context context) {
        String[] imsis = new String[] {"", ""};
        int slotId = 0; // ?
        int subId = getSubIdBySlotId(context, slotId);
        String imsi = null;

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if(subId > 0) {
            imsi = telephonyManager.getSubscriberId(subId);
            imsis[0] = imsi;
        }

        slotId = 1; // ?
        subId = getSubIdBySlotId(context, slotId);
        if(subId > 0) {
            imsi = telephonyManager.getSubscriberId(subId);
            imsis[1] = imsi;
        }

        return  imsis;
    }

    public static String[] getDeviceInfo(Context context){
        String[] deviceInf = new String[21];

        if(DEBUG) {
            Log.d(TAG, "getDeviceInfo s");
        }

        String[] imeis = PolicyManagerUtils.getInstance().listImei(context);
        String[] iccids = PolicyManagerUtils.getInstance().listIccid(context);
        String[] imsis = PolicyManagerUtils.listIMSI(context);

        deviceInf[0] =  (imeis != null && imeis.length > 0 && !TextUtils.isEmpty(imeis[0])) ? imeis[0] : ""; // ：IMEI/MEID（主卡对应的设备识别码）
        deviceInf[1] =  (imeis != null && imeis.length > 1 && !TextUtils.isEmpty(imeis[1])) ? imeis[1] : ""; // ：MEID/MEID（副卡对应的设备识别码）（如无副卡，可为空）
        deviceInf[2] =  "2GB"; // ：运行内存容量（字符串，单位 GB，如 3GB）
        deviceInf[3] =  "16GB"; // ：内部存储容量（字符串，单位 GB，如 16GB）
        deviceInf[4] =  "320x240"; // ：屏幕分辨率（字符串，长*高，如 640*480）
        deviceInf[5] =  SystemProperties.get("ro.product.manufacturer", ""); // ：终端生产厂商
        deviceInf[6] =  SystemProperties.get("ro.product.model", ""); // ：终端型号
        deviceInf[7] =  "4.9.112+"; // ：系统内核版本号
        deviceInf[8] =  "Android " + SystemProperties.get("ro.build.version.release", ""); // ：系统软件版本号
        deviceInf[9] =  ""; // ：安全加固双操作系统版本（如无，可为空）
        deviceInf[10] =  SystemProperties.get("ro.build.version.security_patch", ""); // ：系统安全补丁程序级别（如无，可为空）
        deviceInf[11] =  (iccids != null && iccids.length > 0 && !TextUtils.isEmpty(iccids[0])) ? iccids[0] : ""; // ：ICCID（SIM 卡 1 的 ICCID）
        deviceInf[12] =  (iccids != null && iccids.length > 1 && !TextUtils.isEmpty(iccids[1])) ? iccids[1] : ""; // ：ICCID（SIM 卡 2 的 ICCID）（如无卡 2，可为空）
        deviceInf[13] =  (imsis != null && imsis.length > 0 && !TextUtils.isEmpty(imsis[0])) ? imsis[0] : ""; // ：IMSI（SIM 卡 1 的 IMSI）
        deviceInf[14] =  (imsis != null && imsis.length > 1 && !TextUtils.isEmpty(imsis[1])) ? imsis[1] : ""; // ：IMSI（SIM 卡 2 的 IMSI）（如无卡 2，可为空）
        deviceInf[15] =  "Qualcomm Technologies, Inc MSM8953"; // ：CPU 型号
        deviceInf[16] =  "TD-SCDMA/LTE/CDMA/EVDO/GSM/WCDMA"; // ：支持的移动网络制式
        deviceInf[17] =  "WCN3680B"; // ：无线网卡芯片型号
        deviceInf[18] =  "WCN3680B"; // ：蓝牙芯片型号
        deviceInf[19] =  "WCN3680B"; // ：NFC 芯片型号
        deviceInf[20] =  "WTR2965"; // ：定位芯片型号

        if(DEBUG) {
            Log.d(TAG, "getDeviceInfo e");
        }

        return deviceInf;
    }

    public static String[] getDeviceState(Context context){
        String[] deviceState = new String[]{"", "", ""};

        if(DEBUG) {
            Log.d(TAG, "getDeviceState s");
        }

        deviceState[0] = getCpuUsage();
        deviceState[1] = getMemoryUsage(context);
        deviceState[2] = getStorageUsage(context);

        if(DEBUG) {
            Log.d(TAG, "getDeviceState e");
        }

        return deviceState;
    }

    public static String getCpuUsage(){
        try {
            String path = "/proc/stat";// 系统CPU信息文件
            long totalJiffies[] = new long[2];
            long totalIdle[] = new long[2];
            int firstCPUNum = 0;//设置这个参数，这要是防止两次读取文件获知的CPU数量不同，导致不能计算。这里统一以第一次的CPU数量为基准
            FileReader fileReader = null;
            BufferedReader bufferedReader = null;
            Pattern pattern = Pattern.compile(" [0-9]+");
            for (int i = 0; i < 2; i++) {
                totalJiffies[i] = 0;
                totalIdle[i] = 0;
                try {
                    fileReader = new FileReader(path);
                    bufferedReader = new BufferedReader(fileReader, 8192);
                    int currentCPUNum = 0;
                    String str;
                    while ((str = bufferedReader.readLine()) != null && (i == 0 || currentCPUNum < firstCPUNum)) {
                        if (str.toLowerCase().startsWith("cpu")) {
                            currentCPUNum++;
                            int index = 0;
                            Matcher matcher = pattern.matcher(str);
                            while (matcher.find()) {
                                try {
                                    long tempJiffies = Long.parseLong(matcher.group(0).trim());
                                    totalJiffies[i] += tempJiffies;
                                    if (index == 3) {//空闲时间为该行第4条栏目
                                        totalIdle[i] += tempJiffies;
                                    }
                                    index++;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (i == 0) {
                            firstCPUNum = currentCPUNum;
                            try {//暂停50毫秒，等待系统更新信息。
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            int percent = 0;
            if (totalJiffies[0] > 0 && totalJiffies[1] > 0 && totalJiffies[0] != totalJiffies[1]) {
                percent = (int) (100.0 * ((totalJiffies[1] - totalIdle[1]) - (totalJiffies[0] - totalIdle[0])) / (totalJiffies[1] - totalJiffies[0]));
            }

            if (DEBUG) {
                Log.d(TAG, "getCpuUsage percent:" + percent);
            }
            return String.valueOf(percent) + "%";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String getCpuUsage2()  {
        double usage = 0.0;
        boolean initCpu = true;
        double o_idle = 0.0;
        double o_cpu = 0.0;

        try {
            if (initCpu) {
                initCpu = false;
                RandomAccessFile reader = null;
                reader = new RandomAccessFile("/proc/stat", "r");
                String load = reader.readLine();
                String[] toks = load.split(" ");
                o_idle = Double.parseDouble(toks[5]);
                o_cpu = Double.parseDouble(toks[2])
                        + Double.parseDouble(toks[3])
                        + Double.parseDouble(toks[4])
                        + Double.parseDouble(toks[6])
                        + Double.parseDouble(toks[7])
                        + Double.parseDouble(toks[8])
                        + Double.parseDouble(toks[9]);
                if (reader != null) {
                    reader.close();
                }
            } else {
                RandomAccessFile reader = null;
                reader = new RandomAccessFile("/proc/stat", "r");
                String load;
                load = reader.readLine();
                String[] toks = load.split(" ");
                double c_idle = Double.parseDouble(toks[5]);
                double c_cpu = Double.parseDouble(toks[2])
                        + Double.parseDouble(toks[3])
                        + Double.parseDouble(toks[4])
                        + Double.parseDouble(toks[6])
                        + Double.parseDouble(toks[7])
                        + Double.parseDouble(toks[8])
                        + Double.parseDouble(toks[9]);
                if (0 != ((c_cpu + c_idle) - (o_cpu + o_idle))) {
                    usage = DoubleUtils.div((100.00 * ((c_cpu - o_cpu))),
                            ((c_cpu + c_idle) - (o_cpu + o_idle)), 2);
                }
                o_cpu = c_cpu;
                o_idle = c_idle;
                if (reader != null) {
                    reader.close();
                }
            }
            if(DEBUG) {
                Log.d(TAG, "getCpuUsage usage:"+usage);
            }
            return String.valueOf(usage) + "%";

        }catch (Exception e){
            e.printStackTrace();
        }

        return "";
    }

    private static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(mi);
        return mi.availMem;
    }

    public static String getMemoryUsage(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();

            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
            if(DEBUG) {
                Log.d(TAG, "getMemoryUsage totalMemorySize:"+totalMemorySize+" availableSize:"+availableSize+" percent:"+percent);
            }
            return percent + "%";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String getStorageUsage(Context context) {
        String filePath = PolicyManagerUtils.getExternalStorageFilePath(context);
        if(!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if(file != null && file.exists() && file.isDirectory()) {
                StatFs mStatFs = new StatFs(filePath);
//                long availableBytes = mStatFs.getAvailableBlocks() * mStatFs.getBlockSize();
                long availableBytes = mStatFs.getAvailableBytes();
                long totalBytes = mStatFs.getTotalBytes();
                int percent = (int) (100 * (totalBytes - availableBytes) / (float) totalBytes);
                if(DEBUG) {
                    Log.d(TAG, "getStorageUsage availableBytes:"+availableBytes+" totalBytes:"+totalBytes+" percent:"+percent);
                }
                return percent + "%";
            }
        }
        return "";
    }

    public static boolean setInstallUninstallPolicies(Context context, int mode, String[] appList) {
//        if(DEBUG){
//            if(appList != null && appList.length > 0){
//                for(int index = 0; index < appList.length; index++){
//                    Log.d(TAG, "setInstallUninstallPolicies index:"+index+" value:"+appList[index]);
//                }
//            }
//        }
        Gson gson = new Gson();

        boolean modeResult = Settings.Global.putInt(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_INSTALL_UNINSTALL_MODE, mode);
        boolean appListResult = false;

        String jsonArray = null;
        if(appList != null && appList.length > 0){
            List<InstallAppInfo> infos = new ArrayList<InstallAppInfo>();
            for(int index = 0; index < appList.length; index++){
                InstallAppInfo info = gson.fromJson(appList[index], InstallAppInfo.class);
                infos.add(info);
            }
            InstallAppInfo[] infoArray = (InstallAppInfo[])infos.toArray(new InstallAppInfo[0]);
            jsonArray = gson.toJson(infoArray, InstallAppInfo[].class);
        }
        if(!TextUtils.isEmpty(jsonArray)){
            appListResult = Settings.Global.putString(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_INSTALL_UNINSTALL_APPLIST, jsonArray);
        }else{
            appListResult = Settings.Global.putString(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_INSTALL_UNINSTALL_APPLIST, "");
        }

        return modeResult & appListResult;
    }

    public static String[] getInstallUninstallPolicies(Context context) {
        String[] result = null;
        Gson gson = new Gson();

        int mode = Settings.Global.getInt(context.getContentResolver(),PolicyManagerUtils.GXX_POLICIES_INSTALL_UNINSTALL_MODE ,2 );

        String jsonArray = Settings.Global.getString(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_INSTALL_UNINSTALL_APPLIST);
        if(!TextUtils.isEmpty(jsonArray)) {
            InstallAppInfo[] infoArray = gson.fromJson(jsonArray, InstallAppInfo[].class);
            if(infoArray != null && infoArray.length > 0){
                result = new String[ 1 + infoArray.length];
                try {
                    result[0] = String.valueOf(mode);
                }catch (Exception e){
                    result[0] = "0";
                }
                for(int index =0; index < infoArray.length; index++){
                    String infoString = gson.toJson(infoArray[index], InstallAppInfo.class);
                    result[index + 1] = infoString;
                }
                return result;
            }
        }
        result = new String[1];
        try {
            result[0] = String.valueOf(mode);
        }catch (Exception e){
            result[0] = "0";
        }
        return result;
    }

    public static boolean setSilentInstallUninstallPolicies(Context context, int mode, String[] appList) {
        Gson gson = new Gson();

        boolean modeResult = Settings.Global.putInt(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_MODE, mode);
        boolean appListResult = false;

        String jsonArray = null;
        if(appList != null && appList.length > 0){
            List<InstallAppInfo> infos = new ArrayList<InstallAppInfo>();
            for(int index = 0; index < appList.length; index++){
                InstallAppInfo info = gson.fromJson(appList[index], InstallAppInfo.class);
                infos.add(info);
            }
            InstallAppInfo[] infoArray = (InstallAppInfo[])infos.toArray(new InstallAppInfo[0]);
            jsonArray = gson.toJson(infoArray, InstallAppInfo[].class);
        }
        if(!TextUtils.isEmpty(jsonArray)){
            appListResult = Settings.Global.putString(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_APPLIST, jsonArray);
        }else{
            appListResult = Settings.Global.putString(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_APPLIST, "");
        }

        return modeResult & appListResult;
    }

    public static String[] getSilentInstallUninstallPolicies(Context context) {
        String[] result = null;
        Gson gson = new Gson();

        int mode = Settings.Global.getInt(context.getContentResolver(),PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_MODE ,2 );

        String jsonArray = Settings.Global.getString(context.getContentResolver(), PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_APPLIST);
        if(!TextUtils.isEmpty(jsonArray)) {
            InstallAppInfo[] infoArray = gson.fromJson(jsonArray, InstallAppInfo[].class);
            if(infoArray != null && infoArray.length > 0){
                result = new String[ 1 + infoArray.length];
                try {
                    result[0] = String.valueOf(mode);
                }catch (Exception e){
                    result[0] = "0";
                }
                for(int index =0; index < infoArray.length; index++){
                    String infoString = gson.toJson(infoArray[index], InstallAppInfo.class);
                    result[index + 1] = infoString;
                }
                return result;
            }
        }
        result = new String[1];
        try {
            result[0] = String.valueOf(mode);
        }catch (Exception e){
            result[0] = "0";
        }
        return result;
    }


    public static String getPackageName(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            ApplicationInfo appInfo = null;
            if (info != null) {
                appInfo = info.applicationInfo;
                String packageName = appInfo.packageName;
                return packageName;
            }
        }catch (Exception e){

        }
        return null;
    }

    public static boolean isInInstallUninstallWhiteList(Context context, boolean isSilent, String packageName){
        if(TextUtils.isEmpty(packageName)){
            return false;
        }
        String applistString = null;
        if(isSilent) {
            applistString = Settings.Global.getString(context.getContentResolver(),
                    "gxx_policies_silent_install_uninstall_applist"/*PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_APPLIST*/);
        }else{
            applistString = Settings.Global.getString(context.getContentResolver(),
                    "gxx_policies_install_uninstall_applist"/*PolicyManagerUtils.GXX_POLICIES_INSTALL_UNINSTALL_APPLIST*/);
        }
        if(!TextUtils.isEmpty(applistString)) {
            try {
                JSONArray array = new JSONArray(applistString);
                if(array != null) {
                    Log.d(TAG, "isInInstallUninstallWhiteList array.length :" + array.length()+" isSilent:"+isSilent);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String pkgName = obj.getString("AppPackageName");
                        Log.d(TAG, "isInInstallUninstallWhiteList AppPackageName :" + obj.getString("AppPackageName")
                                + " :" + obj.getString("CertificateHash"));
                        if (packageName.equals(pkgName)) {
                            return true;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isBlockedBySilentInstallUninstallPolicies(Context context, String filepath, String packageName){
        String pkgName = packageName;
        if(TextUtils.isEmpty(pkgName) && !TextUtils.isEmpty(filepath)){
            pkgName = getPackageName(context, filepath);
        }
        Log.d(TAG, "isBlockedBySilentInstallUninstallPolicies pkgName:"+pkgName);
        if(TextUtils.isEmpty(pkgName)){
            return false;
        }
        int mode = 0;
        try {
            mode = Settings.Global.getInt(context.getContentResolver(),
                    "gxx_policies_silent_install_uninstall_mode"/*PolicyManagerUtils.GXX_POLICIES_SILENT_INSTALL_UNINSTALL_MODE*/, 2);
        }catch (Exception e){

        }
        if (0 == mode) {
            Log.d(TAG, "isBlockedBySilentInstallUninstallPolicies mode 0");
            return true;
        } else if (1 == mode) {
            if (!isInInstallUninstallWhiteList(context, true, pkgName)) {
                Log.d(TAG, "isBlockedBySilentInstallUninstallPolicies mode 1");
                return true;
            }else{
                Log.d(TAG, "sisBlockedBySilentInstallUninstallPolicies mode 1 whitelist");
                return false;
            }
        }else {
            Log.d(TAG, "isBlockedBySilentInstallUninstallPolicies mode 2");
            return false;
        }
    }

    public static void sendCommandToHostapd(String cmd) {
        Log.d(TAG, "sendCommandToHostapd s");
        Runtime mRuntime = Runtime.getRuntime();
        try {
            Process mProcess = mRuntime.exec(cmd);
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
            Log.d(TAG, "sendCommandToHostapd response: "+ mRespBuff.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "sendCommandToHostapd e");
    }

    public static void syncApWhiteListToServer(Context context){
        context.sendBroadcast(new Intent("android.intent.action.gxx_sync_white_list_2_hostapd"));
//        sendCommandToHostapd("hostapd_cli accept_acl CLEAR ");
//        Log.d(TAG, "syncApWhiteListToServer");
//        int mode = Settings.Global.getInt(context.getContentResolver(),
//                "gxx_enable_wlan_ap"/*PolicyManagerUtils.GXX_ENABLE_WLAN_AP*/, 1);
//        if(mode == 0){
//            return;
//        }
//        String whiteListStr = Settings.Global.getString(context.getContentResolver(),
//                "gxx_enable_wlan_ap_maclist"/*PolicyManagerUtils.GXX_ENABLE_WLAN_AP_MACLIST*/);
//        Log.d(TAG, "syncApWhiteListToServer mode:"+mode+" white list:"+whiteListStr);
//        if(mode == 1 && !TextUtils.isEmpty(whiteListStr)){
//            String[] whiteList = whiteListStr.split(",");
//            if(whiteList != null && whiteList.length > 0){
//                String[] list = new String[whiteList.length];
//                for(int index = 0; index < list.length; index++){
//                    list[index] = whiteList[index].replaceAll("-", ":");
//                    Log.d(TAG, "syncApWhiteListToServer index:"+index+" list value:"+list[index]);
//                    sendCommandToHostapd("hostapd_cli accept_acl ADD_MAC "+ list[index]);
//                }
//            }
//        }else if(mode == 2){
//            sendCommandToHostapd("hostapd_cli accept_acl ADD_MAC ff:ff:ff:ff:ff:ff");
//        }
    }

    public static void launchPackage(Context context, String pkgName) {
        try {
            Intent pkgIntent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (pkgIntent != null) {
                pkgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(pkgIntent);
            }
        }catch (Exception e){

        }
    }
}
