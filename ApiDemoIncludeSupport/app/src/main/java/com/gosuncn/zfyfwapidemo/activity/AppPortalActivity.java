package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.os.Bundle;
import com.gosuncn.zfyfw.service.GSFWManager;

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

public class AppPortalActivity extends Activity {
    private static final String TAG = AppPortalActivity.class.getSimpleName();
    private Button mbutton_dial;
    private Button mbutton_setting;
    private Button mbutton_engineeringcam;
    private Button mbutton_syscam;
    private Button mWifiDisplayBtn;
    private Button mBLEBtn;
    private Button mNetworkPermissionBtn;

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
        mbutton_engineeringcam = new Button(this);
        mbutton_syscam = new Button(this);
        mWifiDisplayBtn = new Button(this);
        mbutton_dial.setText(R.string.dial);
        mbutton_dial.setGravity(Gravity.CENTER);
        mbutton_dial.setTag(1);
        mbutton_setting.setText(R.string.settings);
        mbutton_setting.setGravity(Gravity.CENTER);
        mbutton_setting.setTag(2);
        mbutton_engineeringcam.setText(R.string.engineeringcam);
        mbutton_engineeringcam.setGravity(Gravity.CENTER);
        mbutton_engineeringcam.setTag(3);
        mbutton_syscam.setText(R.string.syscam);
        mbutton_syscam.setGravity(Gravity.CENTER);
        mbutton_syscam.setTag(4);
        mWifiDisplayBtn.setText(R.string.wifi_display);
        mWifiDisplayBtn.setGravity(Gravity.CENTER);
        mWifiDisplayBtn.setTag(5);
        rootView.addView(mbutton_dial, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_setting, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_engineeringcam, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_syscam, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mWifiDisplayBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));

        setContentView(rootView);
        mbutton_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSFWManager.startDialerActivity(AppPortalActivity.this);
            }
        });
        mbutton_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSFWManager.startSystemSettingsActivity(AppPortalActivity.this);
            }
        });
        mbutton_engineeringcam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSFWManager.startEngineeringCameraActivity(AppPortalActivity.this);
            }
        });
        mbutton_syscam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSFWManager.startSystemCameraActivity(AppPortalActivity.this);
            }
        });
        mWifiDisplayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSFWManager.startWifiDisplaySettingsActivity(AppPortalActivity.this, true);
            }
        });

        mBLEBtn = new Button(this);
        mBLEBtn.setText("Bluetooth Settings");
        mBLEBtn.setGravity(Gravity.CENTER);
        mBLEBtn.setTag(5);
        rootView.addView(mBLEBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));
        mBLEBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GSFWManager.startBluetoothSettingsActivity(AppPortalActivity.this);
            }
        });

        mNetworkPermissionBtn = new Button(this);
        mNetworkPermissionBtn.setText("Network Permission");
        mNetworkPermissionBtn.setGravity(Gravity.CENTER);
        mNetworkPermissionBtn.setTag(5);
        rootView.addView(mNetworkPermissionBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, AppPortalActivity.this.getResources().getDisplayMetrics())));
        mNetworkPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GSFWManager.startNetworkPermissionActivity(AppPortalActivity.this);
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
//        GSFWManager.getInstance().setHomeKeyDispatched(false);
    }

}
