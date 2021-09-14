package com.gosuncn.zfyfw.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.gosuncn.zfyfw.EmptyActivity;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class VpnConnectService extends VpnService {

    public static final String VPN_TAG = "vpn_tag";
    public static final int VPN_START = 1;
    public static final int VPN_STOP = -1;
    public static final int VPN_RELOAD = 0;
    private final static String TAG = VpnConnectService.class.getSimpleName();
    private Thread mThread;
    private int cmd;
    private ParcelFileDescriptor vpn;
    private Map<String,String> userConfig;
    //a. Configure a builder for the interface.

    // Services interface
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get command
        try {
            cmd = intent.getIntExtra(VPN_TAG, VPN_STOP);
            userConfig = (HashMap<String,String>)intent.getSerializableExtra("config");
        }catch (NullPointerException e){
            e.printStackTrace();
            cmd = VPN_STOP;
        }
        // Process command
        switch (cmd) {
            case VPN_START:
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        vpnStart(userConfig);
                    }
                },"MyVpnThread");
                mThread.start();
                break;
            case VPN_RELOAD:
                ParcelFileDescriptor prev = vpn;
                vpnStart(userConfig);
                if (prev != null)
                    vpnStop(prev);
                break;
            case VPN_STOP:
                if (vpn != null) {
                    vpnStop(vpn);
                    vpn = null;
                }
                stopSelf();
                break;
        }
        return START_STICKY;
    }

    private void vpnStart(Map<String,String> userConfig) {
        String address = userConfig.get("address");
        String dnsServer = userConfig.get("dnsServer");
        String route = userConfig.get("route");
        Log.d(TAG, "vpnStart  : config :" + address + "," + dnsServer + "," + route);
        if (getIpv4Address(getApplicationContext()) == null) {
            return;
        }
            final Builder builder = new Builder();
        try{
            builder.setSession("Myvpn");
//          builder.addAddress(getIpv4Address(getApplicationContext()), 24);
            builder.addAddress(address,24);
            builder.addDnsServer(dnsServer);
            builder.addRoute(route.substring(0,route.indexOf("/")), 0);
            vpn = builder.establish();
            Log.e(TAG, "启动完成");
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            Looper.prepare();
            Toast.makeText(getBaseContext(),"配置格式存在错误！",Toast.LENGTH_SHORT).show();
            Looper.loop();
                vpnStop(vpn);
            }
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(TAG, TAG, 3));
            startForeground(1, new Notification.Builder(this, TAG)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .build());
        }


    private void vpnStop(ParcelFileDescriptor prev) {
        if (prev != null) {
            try {
                prev.close();
            } catch (IOException ex) {
                Log.e(TAG, ex.toString() + "\n" + Log.getStackTraceString(ex));
            }
        }
    }

    private String getIpv4Address(Context context){
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            Log.e(TAG,"no internet");
        }
        return null;
    }

    public String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}
