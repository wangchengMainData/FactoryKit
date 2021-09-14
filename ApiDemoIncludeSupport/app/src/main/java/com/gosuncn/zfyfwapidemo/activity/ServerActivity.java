package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import com.gosuncn.zfyfw.service.GSFWManager;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gosuncn.zfyhwapidemo.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class ServerActivity extends Activity {
    private static final String TAG = ServerActivity.class.getSimpleName();
    private Button mbutton_dial;
    private Button mbutton_setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT));
        mbutton_dial = new Button(this);
        mbutton_setting = new Button(this);

        mbutton_dial.setText("SCAN");
        mbutton_dial.setGravity(Gravity.CENTER);
        mbutton_dial.setTag(1);
        mbutton_setting.setText("SEND");
        mbutton_setting.setGravity(Gravity.CENTER);
        mbutton_setting.setTag(2);

        rootView.addView(mbutton_dial, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, ServerActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_setting, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, ServerActivity.this.getResources().getDisplayMetrics())));


        setContentView(rootView);
        mbutton_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        testUDP();
                    }
                }).start();
            }
        });
        mbutton_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        testSend();
                    }
                }).start();
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        GSFWManager.getInstance().setHomeKeyDispatched(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        GSFWManager.getInstance().setHomeKeyDispatched(false);
    }
    private WifiManager.MulticastLock multicastLock;
    private DatagramSocket datagramSocket = null;
    private InetAddress serverAddress = null;
    private int localProt = 5060;
    private void testUDP(){

        if(datagramSocket == null) {
            try {
                datagramSocket = new DatagramSocket(null);
                datagramSocket.setReuseAddress(true);
                datagramSocket.bind(new InetSocketAddress(localProt));
//                datagramSocket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            serverAddress = InetAddress.getByName("183.62.9.180");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        Log.i("APPServer","DatagramPacket create success");

        while (datagramSocket != null) {
            byte data[] = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                datagramSocket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String result = new String(packet.getData(), packet.getOffset(), packet.getLength());
            if(!TextUtils.isEmpty(result)) {
                Log.i("APPServer","==============RECV START ==============");
                Log.i("APPServer", "" + result);
                Log.i("APPServer","==============RECV END ==============");
                Log.i("APPServer","========================================");
            }
        }
    }

    private void testSend(){
        String IpStr = getIP(ServerActivity.this.getApplicationContext());

        Log.i("APPServer","IpStr = " + IpStr);

        String d = "REGISTER sip:34020000002000000001@3402000000 SIP/2.0\n" +
                "Via: SIP/2.0/UDP "+IpStr+":"+ localProt+";rport;branch=z9hG4bK1706077698\n" +
                "Route: <sip:34020000002000000001@183.62.9.180:5060;lr>\n" +
                "From: <sip:11122345901521739152@1112234590>;tag=1850340815\n" +
                "To: <sip:11122345901521739152@1112234590>\n" +
                "Call-ID: 1948742443@183.62.9.180\n" +
                "CSeq: 5 REGISTER\n" +
                "Contact: <sip:11122345901521739152@"+IpStr+":"+ localProt+">\n" +
                "Max-Forwards: 70\n" +
                "User-Agent: GoSunCN\n" +
                "Expires: 3000\n" +
                "Content-Length: 0\n";


        byte[] data = d.getBytes();
        DatagramPacket  packet = new DatagramPacket (data ,0, data.length , serverAddress , localProt);
        try {
            datagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("APPServer","==============SEND START ==============");
        Log.i("APPServer","serverAddress:" + serverAddress.toString());
        Log.i("APPServer","" + d);
        Log.i("APPServer","==============SEND END ==============");
        Log.i("APPServer","========================================");
    }

    public static String getIP(Context context) {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
