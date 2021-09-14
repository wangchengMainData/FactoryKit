package com.gosuncn.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gosuncn.bean.CryptoModuleInfo;
import com.gosuncn.zfyfw.service.GSFWManager;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.TELEPHONY_SERVICE;

public class AppService {

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
    public static CryptoModuleInfo getCryptoModuleInfo() {
        CryptoModuleInfo cryptoModuleInfo = new CryptoModuleInfo();
        cryptoModuleInfo.result = "0";
        cryptoModuleInfo.moduleType = "QSEE";
        cryptoModuleInfo.manufacture = "QUALCOMM";
        cryptoModuleInfo.moduleId = "4.0";
        return cryptoModuleInfo;
    }

    /**
     * 网络规则管控
     * 网络访问规则控制应支持对目标网络、主机进行访问控制，规则需对目标IP地址、网络掩码、端口、协议、是否允许访问等进行描述。
     * @param commandline ， 如： iptables -A OUTPUT -m owner --uid-owner=10032 -j DROP
     *                           ip6tables -A OUTPUT -m owner --uid-owner=10032 -j DROP
     * @return 返回值为命令执行的标准输出或标准错误输出。目前只能返回指令执行的结果，true-成功，false-失败
     */
    public static String executeShellToSetIptables(String commandline){
//        Log.d(TAG, "executeShellToSetIptables "+commandline);
        boolean result = GSFWManager.getInstance().dispatchIptablesCmd(commandline);
//        Log.d(TAG, "executeShellToSetIptables result:"+result);
        return result ? "true" : "false";
    }

    /**
     * 应用静默安装
     * 不在用户界面显示任何提示，无打扰，apk在后台静默安装完毕
     * @param apkfilepath   app安裝包的路径全拼
     */
    public static void installAppSilent(String apkfilepath){
        GSFWManager.getInstance().installAppSilent(apkfilepath);
    }

    /**
     * 应用静默卸载
     * 不在用户界面显示任何提示，无打扰，apk在后台静默卸载完毕
     * @param context　上下文对象
     * @param pkgname　应用的包名全拼
     * @return 可卸载返回成功，安装包不存在返回false
     */
    public static boolean uninstallAppSilent(Context context, String pkgname){
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(pkgname, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "pkgname dosen't exist!");
            return false;
        }
        GSFWManager.getInstance().uninstallAppSilent(pkgname);
        return true;
    }

    /**
     * 获取设备中存在的IMEI值
     * @param context 上下文对象
     * @return  返回String[]类型，列出设备中的imei表
     */
    public static String[] getImeiNumber(Context context){
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String[] imei_list;
//        if (ContextCompat.checkSelfPermission(context,
//                Manifest.permission.READ_PHONE_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//            return null;
//        }
        int phoneCount = mTelephonyManager.getPhoneCount();//sim卡槽数量
        Log.d(TAG,"phoneCount = " + phoneCount);
        if(phoneCount == 0) //无卡槽
            return null;
        else if(phoneCount == 1){//单卡手机
            imei_list = new String[1];
            imei_list[0] = mTelephonyManager.getDeviceId();
            return imei_list;
        }
        else if(phoneCount == 2) {//双卡手机
            imei_list = new String[2];
            imei_list[0] = mTelephonyManager.getDeviceId(0);
            imei_list[1] = mTelephonyManager.getDeviceId(1);
            return imei_list;
        }else
            return null;
    }

    /**
     * 获取设备中存在的iccid值，该方法中通过getSimSerialNumber(Context,subId)方法，查询对应sim卡的iccid号码
     * @param context　上下文对象
     * @return  返回String[]类型，列出设备中的iccid表
     */
    public static String[] getIccidNumber(Context context){
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String[] iccid_list;
//        if (ContextCompat.checkSelfPermission(context,
//                Manifest.permission.READ_PHONE_STATE)
//                != PackageManager.PERMISSION_GRANTED) {
//            return null;//无权限，返回null
//        }
        int activeSubscriptionInfoCount = SubscriptionManager.from(context).getActiveSubscriptionInfoCount();//current acitve sim counts
        Log.d(TAG,"current activated sim counts : " + activeSubscriptionInfoCount);
        if(activeSubscriptionInfoCount == 0) {//no sim active
            return null;
        }else if(activeSubscriptionInfoCount == 1) {//1 sim active
            iccid_list = new String[1];
            iccid_list[0] = mTelephonyManager.getSimSerialNumber();
            return iccid_list;
        }else if(activeSubscriptionInfoCount == 2) {//2 sim active
            iccid_list = new String[2];
            iccid_list[0] = getSimSerialNumber(context,0);
            iccid_list[1] = getSimSerialNumber(context,1);
            return iccid_list;
        }else{
            return null;
        }
    }

    /**
     * 通过反射获取方法getSimSerialNumber查询对应已装载的sim卡的iccid号码
     * @param context　上下文对象
     * @param subId　卡号，卡一：０　　卡二：１
     * @return  返回String类型的iccid号码
     */
    public static String getSimSerialNumber(Context context,int subId) {//get iccid according to cardNo.
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);// 取得相关系统服务
        Class<?> telephonyManagerClass = null;
        String iccid = null;
        try {
            telephonyManagerClass = Class.forName("android.telephony.TelephonyManager");
            Method method = telephonyManagerClass.getMethod("getSimSerialNumber",int.class);
            iccid = (String) method.invoke(mTelephonyManager, subId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return iccid;
    }

    /**
     * 获取vpn的连接状态
     * @param context　上下文对象
     * @return  false－未连接　true已连接
     */
    public static boolean getVpnConnectionState(Context context){
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network mNetwork = mConnectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(mNetwork);
            if (networkCapabilities != null) {
                if (networkCapabilities.toString().substring(0, networkCapabilities.toString().indexOf("Capabilities")).contains("VPN")) {
                    //vpn连接
                    return true;
                } else {
                    //vpn未连接
                    return false;
                }
            }
        }
        return false;
        /* below method 2  */
//        try {
//            List<NetworkInterface> list = Collections.list(NetworkInterface.getNetworkInterfaces());
//            for(NetworkInterface networkInterface : list) {
//                String name = networkInterface.getName();
//                Log.d(TAG + ",name", name);
//                if (name.equals("ppp0") || name.equals("tun0"))
//                    return true;
//            }
//        }catch (SocketException e){e.printStackTrace();}
//        return false;
    }

    /**
     * 建立Vpn连接
     * @param vpnconfig Map<String,String></>类型的vpn配置，需要有参数(<key,value> :  "address", (value)  服务器地址，如xx.xx.xx.xx
     * @param context   上下文对象                                                   "dnsServer",(value)　dns地址，如8.8.8.8
     * @return   0成功    -1失败(无网络)                                             "route",(value)     转发地址，如0.0.0./0
     */
    public static int establishVpnConnection(Map<String,String> vpnconfig,Context context){
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            GSFWManager.getInstance().establishVpnConnection(vpnconfig);
            return 0;
        }else{//no internet
            return -1;
        }
    }

    /**
     * 断开vpn连接
     * @return  ０成功　其他失败
     */
    public static int disestablishVpnConnection(){
        GSFWManager.getInstance().disEstablishVpnConnection();
        return 0;
    }

    /**
     * 以Base64编码列举终端密码模块内存储的所有数字证书
     * @return 成功：返回包含所有数字证书的列表；失败：返回空指针null
     */
    public static String[] listCertificates() {
        return GSFWManager.getInstance().listCertificates();
    }
}
