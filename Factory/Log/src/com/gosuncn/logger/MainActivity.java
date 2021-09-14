package com.gosuncn.logger;
// REQ241,system:Log Tool,wmd,2020.0416
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.R;



/**
 * 日志管理
 * 1.日志永久存储启动和停止
 * 2.日志永久存储缓冲区配置（暂不实现，默认全部）
 * 3.日志缓冲区大小配置（暂不实现，默认256K）
 * 4.日志条目个数限制（默认256）
 * 5.导出报告（界面上显示导出地址）
 * 6.清除旧日志
 * 7.清除旧报告
 * 8.查看报告（可以操作复制和移动报告文件到外置SD卡）
 * 参考：
 * BugreportProgressService.java	141 "com.android.internal.intent.action.BUGREPORT_STARTED";
 * dumpstate.cpp	2074 SendBroadcast("com.android.internal.intent.action.BUGREPORT_STARTED", am_args);
 */
public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MSG_REFRESH_LOG_SWITCH = 1000;
    private static final int MSG_CHECK_LOG_SWITCH = 1001;

    private boolean mIsCrashInfoUploadEnabled = false;
    private boolean isLogEnabled = false;
    private Switch mLogSwitch;
    private Spinner mLimitSizeSpinner;
    private Button mExportBtn;
    private Button mClearLogsBtn;
    private Button mClearReportsBtn;
    private Button mViewReportsBtn;
    private Button mMoveBtn;
    private TextView mReportSummaryTv;

    private Handler mHandler;

    private boolean isLogServiceEnalbed = LogManager.isLogServiceEnalbed;

    private void switchLogStatus(boolean isLogEnabled){
        if(isLogServiceEnalbed){
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.gosuncn.zfyfactorytest", "com.gosuncn.logger.LogService"));
            if(isLogEnabled){
                intent.setAction("android.intent.action.gosuncn.syslog.start");
            }else{
                intent.setAction("android.intent.action.gosuncn.syslog.stop");
            }
            MainActivity.this.startService(intent);
        }else{
            LogManager.getInstance().setLogPersistOff(isLogEnabled);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_main_activity);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(MSG_REFRESH_LOG_SWITCH == msg.what){
                    updateLogSwitch();
                }
            }
        };

        mLogSwitch = (Switch)findViewById(R.id.log_switch);
        mLogSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(mLogSwitch.isChecked() != isLogEnabled) {
                    isLogEnabled = mLogSwitch.isChecked();
                    Log.d(TAG, "onCheckedChanged isLogEnabled:"+isLogEnabled);

                    switchLogStatus(isLogEnabled);

                    Toast.makeText(MainActivity.this,
                            b ? getString(R.string.log_manager_switch_on) : getString(R.string.log_manager_switch_off),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mLimitSizeSpinner = (Spinner)findViewById(R.id.log_limit_size_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.log_limit_size_spinner_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        mLimitSizeSpinner.setAdapter(adapter);
        mLimitSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String[] arrays = MainActivity.this.getResources().getStringArray(R.array.log_limit_size_spinner_values);

                Log.d(TAG, "onitemselected logsize:"+LogManager.getInstance().getLogSize());
                Log.d(TAG, "onitemselected pos:"+pos);
                if( !(TextUtils.isEmpty(LogManager.getInstance().getLogSize()) && pos == 3/*256*/) ||
                        (!TextUtils.isEmpty(LogManager.getInstance().getLogSize()) && !LogManager.getInstance().getLogSize().equals(arrays[pos])) ) {
                    LogManager.getInstance().setLogSize(arrays[pos]);
                    Toast.makeText(MainActivity.this,
                            getString(R.string.log_manager_limit_size) + ": " + arrays[pos],
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mExportBtn = (Button)findViewById(R.id.log_bugreport);
        mExportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LogManager.getInstance().takeBugreport(ActivityManager.BUGREPORT_OPTION_FULL)){
                    Log.d(TAG, "takeBugreport");
                }
            }
        });

        mReportSummaryTv = (TextView)findViewById(R.id.log_manager_bugreport_summary);
        mReportSummaryTv.setVisibility(View.GONE);

        mClearLogsBtn = (Button)findViewById(R.id.log_manager_log_clear_btn);
        mClearLogsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchLogStatus(false);
                if(isLogServiceEnalbed){
                    deleteSysLog();
                }else {
                    LogManager.getInstance().clearLogs(MainActivity.this);
                }
                mHandler.sendEmptyMessageDelayed(MSG_REFRESH_LOG_SWITCH, 3000);
            }
        });
        mClearReportsBtn = (Button)findViewById(R.id.log_manager_bugreport_clear_btn);
        mClearReportsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManager.getInstance().clearReports(MainActivity.this);
            }
        });
        mViewReportsBtn = (Button)findViewById(R.id.log_manager_bugreport_view_btn);
        mViewReportsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManager.getInstance().viewReports(MainActivity.this);
            }
        });

        mMoveBtn = (Button)findViewById(R.id.log_manager_bugreport_move_to_sdcard);
        mMoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogManager.getInstance().moveReports(MainActivity.this);
            }
        });

        final Button saveUrlBtn = (Button)findViewById(R.id.log_manager_crash_info_save_url_btn);
        final EditText crashInfoeEditText = (EditText)findViewById(R.id.log_manager_crash_info_upload_edit);
        final Switch crashInfoUploadSwitch = (Switch)findViewById(R.id.log_manager_crash_info_upload_switch);
        String ddenabled = SystemProperties.get("persist.sys.ddenable", "false");
        mIsCrashInfoUploadEnabled = ("true".equals(ddenabled)) ? true : false;
        crashInfoUploadSwitch.setChecked(mIsCrashInfoUploadEnabled);
        crashInfoeEditText.setEnabled(mIsCrashInfoUploadEnabled);
        crashInfoeEditText.setClickable(mIsCrashInfoUploadEnabled);
        saveUrlBtn.setEnabled(mIsCrashInfoUploadEnabled);
        saveUrlBtn.setClickable(mIsCrashInfoUploadEnabled);
        crashInfoUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                boolean isCrashInfoUploadEnabled = crashInfoUploadSwitch.isChecked();
                if(isCrashInfoUploadEnabled != mIsCrashInfoUploadEnabled) {
                    Log.d(TAG, "onCheckedChanged isCrashInfoUploadEnabled:"+isCrashInfoUploadEnabled);
                    mIsCrashInfoUploadEnabled = isCrashInfoUploadEnabled;
                    SystemProperties.set("persist.sys.ddenable", mIsCrashInfoUploadEnabled ? "true" : "false");
                    crashInfoeEditText.setEnabled(mIsCrashInfoUploadEnabled);
                    crashInfoeEditText.setClickable(mIsCrashInfoUploadEnabled);
                    saveUrlBtn.setEnabled(mIsCrashInfoUploadEnabled);
                    saveUrlBtn.setClickable(mIsCrashInfoUploadEnabled);
                }
            }
        });
        saveUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editTextStr = null;
                try {
                    editTextStr = crashInfoeEditText.getText().toString();
                }catch (Exception e){

                }
                if(!TextUtils.isEmpty(editTextStr) && editTextStr.startsWith("http")){
                    if(editTextStr.length() < 90) {
                        SystemProperties.set("persist.sys.ddurl1", editTextStr);
                    }else{
                        SystemProperties.set("persist.sys.ddurl1", editTextStr.substring(0, 89));
                        SystemProperties.set("persist.sys.ddurl2", editTextStr.substring(89));
                    }
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.internal.intent.action.BUGREPORT_STARTED");
        intentFilter.addAction("com.android.internal.intent.action.BUGREPORT_FINISHED");
        intentFilter.addAction("com.android.internal.intent.action.REMOTE_BUGREPORT_STARTED");
        intentFilter.addAction("com.android.internal.intent.action.REMOTE_BUGREPORT_FINISHED");
        intentFilter.addAction("com.android.internal.intent.action.BUGREPORT_CLEAR_ALL_FINISHED");
        intentFilter.addAction("com.android.internal.intent.action.BUGREPORT_MOVE_TO_SDCARD_FINISHED");
        registerReceiver(mReportReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLogSwitch();

        mLimitSizeSpinner.setSelection(getLogSizePosistion(LogManager.getInstance().getLogSize()));

        mHandler.sendEmptyMessageDelayed(MSG_CHECK_LOG_SWITCH, 3000);

/*        if(!Build.IS_DEBUGGABLE){
            View v = findViewById(R.id.log_manager_switch);
            v.setVisibility(View.GONE);
            v = findViewById(R.id.log_manager_limit_size);
            v.setVisibility(View.GONE);
            v = findViewById(R.id.log_manager_log_clear);
            v.setVisibility(View.GONE);
        }
*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHandler.removeMessages(MSG_CHECK_LOG_SWITCH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReportReceiver);
    }

    BroadcastReceiver mReportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(isFinishing()){
                return;
            }
            if (("com.android.internal.intent.action.REMOTE_BUGREPORT_FINISHED".equals(action)) ||
                    ("com.android.internal.intent.action.BUGREPORT_FINISHED".equals(action))) {
                StringBuffer sb = new StringBuffer("");
                final String shareTitle = intent.getStringExtra("android.intent.extra.TITLE");
                if (!TextUtils.isEmpty(shareTitle)) {
                    sb.append(shareTitle);
                    final String shareDescription = intent.getStringExtra("android.intent.extra.DESCRIPTION");
                    sb.append("\n");
                    sb.append(shareDescription);
                }

                mReportSummaryTv.setVisibility(View.VISIBLE);
                mReportSummaryTv.setText(getString(R.string.log_manager_bugreport_stop)
                /*+ "\n"+getString(R.string.log_manager_bugreport_path)+
                        getString(R.string.log_manager_bugreport_u_disk_root)+sb.toString()*/);
                mExportBtn.setEnabled(true);
                mExportBtn.setClickable(true);

                mClearLogsBtn.setEnabled(true);
                mClearLogsBtn.setClickable(true);
                mClearReportsBtn.setEnabled(true);
                mClearReportsBtn.setClickable(true);
                mViewReportsBtn.setEnabled(true);
                mViewReportsBtn.setClickable(true);
                mMoveBtn.setEnabled(true);
                mMoveBtn.setClickable(true);
            }else if("com.android.internal.intent.action.BUGREPORT_STARTED".equals(action) ||
                    "com.android.internal.intent.action.REMOTE_BUGREPORT_STARTED".equals(action)){
                mReportSummaryTv.setVisibility(View.VISIBLE);
                mReportSummaryTv.setText(R.string.log_manager_bugreport_start);
                mExportBtn.setEnabled(false);
                mExportBtn.setClickable(false);

                mClearLogsBtn.setEnabled(false);
                mClearLogsBtn.setClickable(false);
                mClearReportsBtn.setEnabled(false);
                mClearReportsBtn.setClickable(false);
                mViewReportsBtn.setEnabled(false);
                mViewReportsBtn.setClickable(false);
                mMoveBtn.setEnabled(false);
                mMoveBtn.setClickable(false);
            }else if("com.android.internal.intent.action.BUGREPORT_CLEAR_ALL_FINISHED".equals(action)){
                mReportSummaryTv.setVisibility(View.VISIBLE);
                mReportSummaryTv.setText(R.string.log_manager_clear_all_finished);
            }else if("com.android.internal.intent.action.BUGREPORT_MOVE_TO_SDCARD_FINISHED".equals(action)){
                mReportSummaryTv.setVisibility(View.VISIBLE);
                mReportSummaryTv.setText(R.string.log_manager_move_to_sdcard_finished);
            }
        }
    };

    private void updateLogSwitch(){
        if(isLogServiceEnalbed) {
            boolean isLogServiceRunning = LogManager.getInstance().isLogServiceRunning(MainActivity.this);
            boolean isLogOpend = "1".equals(SystemProperties.get(LogManager.PERSIST_SYS_GSYSLOG, "0"));
            if(isLogServiceRunning){
                if(!isLogOpend){
                    SystemProperties.set(LogManager.PERSIST_SYS_GSYSLOG, "1");
                }
            }else{
                if(isLogOpend){
                    SystemProperties.set(LogManager.PERSIST_SYS_GSYSLOG, "0");
                }
            }
            isLogEnabled = isLogServiceRunning;
        }else {
            isLogEnabled = LogManager.getInstance().isLogPersistOff();
        }
        mLogSwitch.setChecked(isLogEnabled);
    }

    private int getLogSizePosistion(String logSize){
        if(TextUtils.isEmpty(logSize)){
            return 3;//256
        }
        String[] arrays = MainActivity.this.getResources().getStringArray(R.array.log_limit_size_spinner_values);
        for(int index = 0; index < arrays.length; index++){
            if(arrays[index].equals(logSize)){
                return index;
            }
        }
        return 3;//256
    }

    private void deleteSysLog(){
        LogManager.getInstance().deleteDirectory(LogService.LOG_PATH_SDCARD_DIR);
    }
}
