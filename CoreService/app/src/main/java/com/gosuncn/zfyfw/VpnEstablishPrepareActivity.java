package com.gosuncn.zfyfw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import ga.mdm.PolicyManagerUtils;

public class VpnEstablishPrepareActivity extends Activity {

    private static final String TAG = VpnEstablishPrepareActivity.class.getSimpleName();
    private int mResult = 1;
    private Map<String, String> mVpn = new HashMap<>();
    private Button button;
    private Context mContext;
    @Override
    protected void onResume() {
        super.onResume();
        mContext = this;
        Log.e(TAG,"empty activity1 onresume");
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view();
    }
    private String getIpV4Address(Context context){
        NetworkInfo info = ((ConnectivityManager) this
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.d("wc1",mVpn.toString());
            PolicyManagerUtils.getInstance().establishVpnConnection(mVpn,mContext);
        }
    }

    private void view(){
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT));

        scrollView.addView(rootView);
        button = new Button(this);
        button.setText("test");
        rootView.addView(button, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48,VpnEstablishPrepareActivity.this.getResources().getDisplayMetrics())));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click();
            }
        });
        setContentView(scrollView);
    }

    private void click(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView tv1 = new TextView(this);tv1.setText("服务器:");
        TextView tv2 = new TextView(this);tv2.setText("DNS:");
        TextView tv3 = new TextView(this);tv3.setText("转发地址:");

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText address = new EditText(this);address.setLayoutParams(lp);
        EditText dns = new EditText(this);dns.setLayoutParams(lp);
        EditText route = new EditText(this);route.setLayoutParams(lp);
        String ip = getIpV4Address(this);
        address.setText(ip == null ? "no internet" : ip);
        dns.setText("8.8.8.8");
        route.setText("0.0.0.0/0");

        builder.setTitle("Vpn配置");
        LinearLayout layout = new LinearLayout(this);
        LinearLayout layout1 = new LinearLayout(this);
        layout1.setOrientation(LinearLayout.HORIZONTAL);
        layout1.addView(tv1);
        layout1.addView(address);
        LinearLayout layout2 = new LinearLayout(this);
        layout2.setOrientation(LinearLayout.HORIZONTAL);
        layout2.addView(tv2);
        layout2.addView(dns);
        LinearLayout layout3 = new LinearLayout(this);
        layout3.setOrientation(LinearLayout.HORIZONTAL);
        layout3.addView(tv3);
        layout3.addView(route);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(layout1);
        layout.addView(layout2);
        layout.addView(layout3);
        builder.setView(layout);

        builder.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = VpnService.prepare(mContext);
                Intent intent1 = VpnService.prepare(mContext);

                Intent intent3 = VpnService.prepare(getBaseContext());
                Intent intent4 = VpnService.prepare(getApplicationContext());
                Log.e(TAG,"intent1 = " + intent + "\n" + intent1+ "\n" + intent3+ "\n" + intent4);
                if (intent != null) {
                    Log.e(TAG,"intent != null");
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0,RESULT_OK,null);
                    Log.e(TAG,"intent = " + intent);
                    Map<String, String> vpnconfig = new HashMap<>();
                    vpnconfig.put("address", address.getText().toString());
                    vpnconfig.put("dnsServer", dns.getText().toString());
                    vpnconfig.put("route", route.getText().toString());
                    mVpn = vpnconfig;
                    int result = PolicyManagerUtils.getInstance().establishVpnConnection(mVpn,mContext);
                    if(result == -1){
                        Toast.makeText(mContext,"no internet", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext,"取消", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();
    }
}



