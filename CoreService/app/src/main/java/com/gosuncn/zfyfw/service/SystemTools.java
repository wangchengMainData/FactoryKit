package com.gosuncn.zfyfw.service;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.location.LocationManager;
import android.os.UserHandle;
import android.provider.Settings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemTools {

    public static String TAG = "SystemTools";

    /**
     * @description  传入应用的包名即可kill掉应用
     * @param 
     * @return 
     * @author "wzn"
     * @time 2020/5/6 17:00
     */
    public static  void forceStopApp(String packageName,Context context) {

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @description 判断设备是否处于飞行模式下
     * @param 
     * @return 
     * @author "wzn"
     * @time 2020/5/19 15:08
     */

    public static boolean isAirplaneModeOn(Context context) {

        int modeIdx = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0);
        boolean isEnabled = (modeIdx == 1);

        return isEnabled;
    }
    
    /**
     * @description  设置飞行模式
     * @param 
     * @return 
     * @author "wzn"
     * @time 2020/5/19 15:10
     */
    
    public static void setAirplaneMode(boolean setAirPlane, Context context) {
        Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, setAirPlane ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", setAirPlane);
        context.sendBroadcast(intent);
    }


    /**
     * @description  设置GPS
     * @param 
     * @return 
     * @author "wzn"
     * @time 2020/5/19 15:16
     */

    public static boolean getGpsState( Context context ){
        ContentResolver resolver = context.getContentResolver();
        boolean gpsState = Settings.Secure.isLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER);
        return gpsState;
    }

    public static void setGpsState( Context context, boolean isGpsOn ){
        ContentResolver resolver = context.getContentResolver();
        boolean nowState = getGpsState(context);
        if (isGpsOn != nowState) {
            Settings.Secure.setLocationProviderEnabled(resolver,
                    LocationManager.GPS_PROVIDER, isGpsOn);
        }
    }

    /**
     * @description  发送广播关闭GPS
     * @param 
     * @return 
     * @author "wzn"
     * @time 2020/5/19 15:21
     */

    public static void closeGpsByBroadcast( Context context ){
        context.sendBroadcast(new Intent(
                "tchip.intent.action.ACTION_GPS_OFF"));
    }

    /**
     * @description  自定义关机事件 ，为上层服务
     * @param 
     * @return 
     * @author "wzn"
     * @time 2020/5/20 9:51
     */
    public static  void sendPowerOffBroadcast( Context context ){
        Intent i = new Intent("com.gosuncn.zfyfw.POWER_OFF");
        context.sendBroadcast(i);
    }


    /**
     * 进入关机后，关闭adb调试；退出关机后，根据情况启动adb调试;
     * @param status
     */
    public static  void adbController(Context context,int status){
        boolean adbEnabled = (Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) > 0);
        if(status == 1){
            try {
                Settings.Global.putInt(context.getContentResolver(),
                        "adb_enabled_last", adbEnabled ? 1 : 0);
            } catch (SecurityException e) {
            }
            if(adbEnabled){
                try {
                    Settings.Global.putInt(context.getContentResolver(),
                            Settings.Global.ADB_ENABLED, 0);
                } catch (SecurityException e) {
                }
            }
        }else if(status == 0){
            if(!adbEnabled) {
                boolean adbEnabledLast = (Settings.Global.getInt(context.getContentResolver(),
                        "adb_enabled_last", 0) > 0);
                if(adbEnabledLast){
                    try {
                        Settings.Global.putInt(context.getContentResolver(),
                                Settings.Global.ADB_ENABLED, 1);
                    } catch (SecurityException e) {
                    }
                }
            }
        }
    }


  
    public static List<Permission> getNeedGrandPermissions(Context context, HashMap<String,Integer> req_permissions,
                                                                       String packageName){

        if( req_permissions == null ){
                return new ArrayList<>();
        }

        PackageManager pm = context.getPackageManager();

        List<Permission> result = new ArrayList<>();

        for (Map.Entry<String,Integer> permission : req_permissions.entrySet()) {
            PermissionInfo pi = null;
            try {
                pi = pm.getPermissionInfo(permission.getKey(), PackageManager.GET_PERMISSIONS);
            } catch (PackageManager.NameNotFoundException nnfe) {
                // ignore
            }
            if (pi == null) {
                continue;
            }
            if (!isRuntime(pi)) {
                continue;
            }
            int flag = pm.checkPermission(permission.getKey(), packageName);

            if (flag == PackageManager.PERMISSION_DENIED && permission.getValue() !=-1) {
                Permission permission1 = new Permission();
                permission1.permission = permission.getKey();
                permission1.mode = 0;
                result.add( permission1 );
            }else if( flag == PackageManager.PERMISSION_GRANTED && permission.getValue() !=0){
                Permission permission1 = new Permission();
                permission1.permission = permission.getKey();
                permission1.mode = -1;
                result.add( permission1 );
            }
        }
        return  result;
    }


    public static List<Permission> getGrandedPermissions(Context context,String[] req_permissions,
                                                       String packageName){

        if( req_permissions == null ){
            return new ArrayList<>();
        }
        PackageManager pm = context.getPackageManager();
        List<Permission> result = new ArrayList<>();
        for (String permission : req_permissions) {
            PermissionInfo pi = null;
            try {
                pi = pm.getPermissionInfo(permission, 0);
            } catch (PackageManager.NameNotFoundException nnfe) {
                // ignore
            }
            if (pi == null) {
                continue;
            }
            if (!isRuntime(pi)) {
                continue;
            }
            int flag = pm.checkPermission(permission, packageName);
            Permission per = new Permission();
            per.permission = permission;
            per.mode = flag;
            result.add(per);
        }
        return  result;
    }


    private static boolean isRuntime(PermissionInfo pi) {
        return (pi.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                == PermissionInfo.PROTECTION_DANGEROUS;
    }



    public static String[] sVendorNames = new String[] {
            "READ_CONTACTS",
            "READ_CALENDAR",
            "SEND_SMS",
            "STORAGE",
            "LOCATION",
            "READ_CALLLOG",
            "READ_IMEI",
            "RECORD_AUDIO",
            "PHOTO",
            "MOTION_SENSORS",
    };


    public static String[] sPlatePermissions = new String[] {
            "android.permission.READ_CONTACTS",
            "android.permission.READ_CALENDAR",
            "android.permission.SEND_SMS",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CALL_LOG",
            "android.permission.READ_PHONE_STATE",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.BODY_SENSORS",
    };


    public static int getVendorPermissionIndex(String op) {

        for (int i=0; i<sVendorNames.length; i++) {
            if (sVendorNames[i].equals(op)) {
                return i;
            }
        }
        return -1;

    }

    public static int getPlatePermissionIndex(String op){
        for (int i=0; i<sPlatePermissions.length; i++) {
            if (sPlatePermissions[i].equals(op)) {
                return i;
            }
        }
        return -1;
    }

    public static String getPlatePermission(int index){

        return  sPlatePermissions[index];
    }

    public static String getVendorPermission(int index){

        return  sVendorNames[index];
    }

    public static class Permission{
        String permission;
        int mode ;
    }
}
