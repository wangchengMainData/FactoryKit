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
     * @return ??????mPolicyManager??????
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
     * ?????????????????????????????????????????????????????????????????????????????????
     * @param settingsContentObserver ??????ISettingsContentObserver???????????????ISettingsContentObserver.Stub??????onchanged??????????????????????????????
     * @return  ??????-true ??????-false
     */
    public boolean registerInstallorUnInstallAppSilentCallBack(ISettingsContentObserver settingsContentObserver){
        return GSFWManager.getInstance().registerSettingsContentObserver(REMOTE_CALL_BACK_TYPE_SILENT_INSTALL_UNINSTALL,settingsContentObserver);
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     * @param settingsContentObserver?????????ISettingsContentObserver??????
     * @return   ??????-true ??????-false
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
     * ????????????????????????IMEI???
     * @param context ???????????????
     * @return  ??????String[]???????????????????????????imei???
     */
    public String[] listImei(/*@Nullable*/ Context context) {
        return AppService.getImeiNumber(context);
    }

    /**
     * ????????????????????????iccid???
     * @param context??????????????????
     * @return  ??????String[]???????????????????????????iccid???
     */
    public String[] listIccid(/*@Nullable */Context context) {
        return AppService.getIccidNumber(context);
    }

    /**
     * ??????vpn???????????????
     * @param context??????????????????
     * @return  false???????????????true?????????
     */
    public Boolean getVpnConnectionState(Context context) {
        return AppService.getVpnConnectionState(context);
    }

    /**
     * ??????Vpn??????
     * @param vpnconfig Map<String,String></>?????????vpn????????????????????????(<key,value> :  "address", (value)  ?????????????????????xx.xx.xx.xx
     * @param context   ???????????????                                                   "dnsServer",(value)???dns????????????8.8.8.8
     * @return   0??????    ????????????                                                    "route",(value)     ??????????????????0.0.0./0
     */
    public int establishVpnConnection(Map<String,String> vpnconfig,Context context){
            return AppService.establishVpnConnection(vpnconfig,context);
    }

    /**
     * ??????vpn??????
     * @return  ????????????????????????
     */
    public  int disestablishVpnConnection(){
        return AppService.disestablishVpnConnection();
    }

    /**
     * ??????????????????
     * ???????????????????????????????????????????????????apk???????????????????????????
     * @param pathToAPK   app????????????????????????
     */
    public void installPackage(String pathToAPK) {
       AppService.installAppSilent(pathToAPK);
    }


    /**
     * ??????????????????
     * ???????????????????????????????????????????????????apk???????????????????????????
     * @param context??????????????????
     * @param appPackageName????????????????????????
     * @return ????????????????????????????????????????????????false
     */
    public boolean uninstallPackage(/*@Nullable*/ Context context, String appPackageName) {
        return AppService.uninstallAppSilent(context,appPackageName);
    }

    /**
     * WLAN??????
     * @param mode 0 ???????????????????????????????????????????????????????????????????????????
     * @return  true???????????????false??????
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
     * ????????????????????????
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
     * ??????????????????????????????
     *
     * @param context
     * @param settingsmenu
     */
    public void openSingleSettingsActivity(/*@Nullable*/ Context context, /*@SettingsMenu */String settingsmenu) {
        Intent intent = new Intent(settingsmenu);
        context.startActivity(intent);
    }

    /**
     * ??????????????????
     * false ??????
     * true ??????
     * @param datastate
     */
    public void setMobileDataEnabled(Boolean datastate) {
        GSFWManager.getInstance().setMobileDataEnabled(datastate);
    }

    /**
     *
     * @param context
     * @return
     *  FUNCTION MTP??????4
     * FUNCTION ADB??????1
     * FUNCTION MIDI??????8
     * FUNCTION PTP??????16
     * FUNCTION ACCESSORY??????2
     * FUNCTION AUDIOSOURCE??????64
     * FUNCTION NONE??????0
     * FUNCTION RNDIS??????32
     */
    public Long getCurrentUsbFunction(Context context){
        return GSFWManager.getInstance().getCurrentUsbFunction();
    }

    public void setCurrentUsbFunction(Long function){
        GSFWManager.getInstance().setCurrentUsbFunction(function);
    }

    /**
     * ????????????????????????????????????????????????????????????
     * @return CryptoModuleInfo
     * ?????????
     * result String ?????? 0???????????????
     *                   1?????????????????????????????????
     *                   2?????????????????????????????????
     *                   3???????????????????????????????????????
     *                   ????????????0??????????????????????????????????????????????????????
     *  moduleType String ?????? ?????????????????????????????????????????????tfcard???usbkey
     *  manufacture String ?????? ??????????????????????????????
     *  moduleId String ?????? ??????????????????????????????
     */
    public CryptoModuleInfo getCryptoModuleInfo() {
        return AppService.getCryptoModuleInfo();
    }

    /**
     * ??????????????????
     * ????????????????????????????????????????????????????????????????????????????????????????????????IP??????????????????????????????????????????????????????????????????????????????
     * @param commandline ??? ?????? iptables -A OUTPUT -m owner --uid-owner=10032 -j DROP
     *                           ip6tables -A OUTPUT -m owner --uid-owner=10032 -j DROP
     * @return ?????????????????????????????????????????????????????????????????????????????????????????????????????????true-?????????false-??????
     */
    public String executeShellToSetIptables(String commandline){
        return AppService.executeShellToSetIptables(commandline);
    }

    /**
     * ????????????adb????????????/????????????????????????
     * @param mode ????????????
     * 0???????????????adb????????????/?????????????????????
     * 1??????????????????adb????????????/?????????????????????
     * @return ????????????true???????????????false???
     */
    public static boolean setAdbInstallUninstallPolicies(int mode) {
        return DeviceBaseFuncMgr.setAdbInstallUninstallPolicies(mode);
    }

    /**
     * ????????????adb????????????/????????????????????????
     * @return ??????????????????adb????????????/?????????????????????????????????setAdbInstallUnistallPolicies???????????????mode
     */
    public static int getAdbInstallUninstallPolicies() {
        return DeviceBaseFuncMgr.getAdbInstallUninstallPolicies();
    }

    /**
     * ROOT????????????
     * @return ?????????ROOT??????true????????????ROOT??????false
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
            //   /system/xbin/which ??????  /system/bin/which
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
     * ?????????????????????
     * @return ??????????????????????????????true?????????????????????????????????false
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
     * ???Base64????????????????????????????????????????????????????????????
     * @return ???????????????????????????????????????????????????????????????????????????null
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
            sb.append(days).append("???");
        }
        if(hours > 0){
            sb.append(hours).append("??????");
        }
        if(minutes > 0){
            sb.append(minutes).append("??????");
        }
        if(seconds > 0){
            sb.append(seconds).append("???");
        }
        return sb.toString();
    }

    /**
     * ????????????
     * {"longitude"="?????????????????","latitude"="?????????????????",
     * "height"="?????????"}
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

        deviceInf[0] =  (imeis != null && imeis.length > 0 && !TextUtils.isEmpty(imeis[0])) ? imeis[0] : ""; // ???IMEI/MEID????????????????????????????????????
        deviceInf[1] =  (imeis != null && imeis.length > 1 && !TextUtils.isEmpty(imeis[1])) ? imeis[1] : ""; // ???MEID/MEID??????????????????????????????????????????????????????????????????
        deviceInf[2] =  "2GB"; // ????????????????????????????????????????????GB????????3GB???
        deviceInf[3] =  "16GB"; // ????????????????????????????????????????????GB????????16GB???
        deviceInf[4] =  "320x240"; // ????????????????????????????????????*???????????640*480???
        deviceInf[5] =  SystemProperties.get("ro.product.manufacturer", ""); // ?????????????????????
        deviceInf[6] =  SystemProperties.get("ro.product.model", ""); // ???????????????
        deviceInf[7] =  "4.9.112+"; // ????????????????????????
        deviceInf[8] =  "Android " + SystemProperties.get("ro.build.version.release", ""); // ????????????????????????
        deviceInf[9] =  ""; // ????????????????????????????????????????????????????????????
        deviceInf[10] =  SystemProperties.get("ro.build.version.security_patch", ""); // ?????????????????????????????????????????????????????????
        deviceInf[11] =  (iccids != null && iccids.length > 0 && !TextUtils.isEmpty(iccids[0])) ? iccids[0] : ""; // ???ICCID???SIM???????1???????ICCID???
        deviceInf[12] =  (iccids != null && iccids.length > 1 && !TextUtils.isEmpty(iccids[1])) ? iccids[1] : ""; // ???ICCID???SIM???????2???????ICCID?????????????????2???????????????
        deviceInf[13] =  (imsis != null && imsis.length > 0 && !TextUtils.isEmpty(imsis[0])) ? imsis[0] : ""; // ???IMSI???SIM???????1???????IMSI???
        deviceInf[14] =  (imsis != null && imsis.length > 1 && !TextUtils.isEmpty(imsis[1])) ? imsis[1] : ""; // ???IMSI???SIM???????2???????IMSI?????????????????2???????????????
        deviceInf[15] =  "Qualcomm Technologies, Inc MSM8953"; // ???CPU????????
        deviceInf[16] =  "TD-SCDMA/LTE/CDMA/EVDO/GSM/WCDMA"; // ??????????????????????????????
        deviceInf[17] =  "WCN3680B"; // ???????????????????????????
        deviceInf[18] =  "WCN3680B"; // ?????????????????????
        deviceInf[19] =  "WCN3680B"; // ???NFC??????????????
        deviceInf[20] =  "WTR2965"; // ?????????????????????

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
            String path = "/proc/stat";// ??????CPU????????????
            long totalJiffies[] = new long[2];
            long totalIdle[] = new long[2];
            int firstCPUNum = 0;//???????????????????????????????????????????????????????????????CPU???????????????????????????????????????????????????????????????CPU???????????????
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
                                    if (index == 3) {//????????????????????????4?????????
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
                            try {//??????50????????????????????????????????????
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
