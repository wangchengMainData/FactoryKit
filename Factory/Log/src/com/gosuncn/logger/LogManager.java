package com.gosuncn.logger;
// REQ241,system:Log Tool,wmd,2020.0416
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import com.android.settingslib.development.SystemPropPoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * persist.vendor.radio.adb_log_on
 */
public class LogManager {
    private static final String TAG = LogManager.class.getSimpleName();

    private static final String SELECT_LOGPERSIST_KEY = "select_logpersist";
    private static final String SELECT_LOGPERSIST_PROPERTY = "persist.logd.logpersistd";
    static final String ACTUAL_LOGPERSIST_PROPERTY = "logd.logpersistd";
    static final String SELECT_LOGPERSIST_PROPERTY_SERVICE = "logcatd";
    private static final String SELECT_LOGPERSIST_PROPERTY_CLEAR = "clear";
    private static final String SELECT_LOGPERSIST_PROPERTY_STOP = "stop";
    private static final String SELECT_LOGPERSIST_PROPERTY_BUFFER =
            "persist.logd.logpersistd.buffer";
    static final String ACTUAL_LOGPERSIST_PROPERTY_BUFFER = "logd.logpersistd.buffer";
    private static final String ACTUAL_LOGPERSIST_PROPERTY_ENABLE = "logd.logpersistd.enable";

    public static boolean isLogServiceEnalbed = Build.IS_USER;
    public static final String PERSIST_SYS_GSYSLOG = "persist.sys.gsyslog";
    public static final String PERSIST_SYS_SLOGSIZE = "persist.sys.slogsize";

    private static LogManager mLogManager;

    private LogManager() {

    }

    /**
     * 获取LogManager实例
     * @return
     */
    public static LogManager getInstance() {
        if ( mLogManager == null) {
            synchronized (LogManager.class) {
                if ( mLogManager == null ) {
                    mLogManager = new LogManager();
                }
            }
        }

        return mLogManager;
    }

    /**
     * @param bugreportType
     * ActivityManager.BUGREPORT_OPTION_FULL
     * ActivityManager.BUGREPORT_OPTION_INTERACTIVE
     */
    public boolean takeBugreport(int bugreportType) {
        try {
            ActivityManager.getService().requestBugReport(bugreportType);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "error taking bugreport (bugreportType=" + bugreportType + ")", e);
        }
        return false;
    }

    public void setLogPersistOff(boolean isPersistOff) {
        SystemProperties.set(SELECT_LOGPERSIST_PROPERTY_BUFFER, "");
        SystemProperties.set(ACTUAL_LOGPERSIST_PROPERTY_BUFFER, "");
        SystemProperties.set(SELECT_LOGPERSIST_PROPERTY, "");
        SystemProperties.set(ACTUAL_LOGPERSIST_PROPERTY, isPersistOff ? "" : SELECT_LOGPERSIST_PROPERTY_STOP);
        SystemPropPoker.getInstance().poke();
        if(!isPersistOff){
            return;
        }

//        SystemProperties.set(SELECT_LOGPERSIST_PROPERTY_BUFFER, "default,security,kernel,radio");
        SystemProperties.set(SELECT_LOGPERSIST_PROPERTY, SELECT_LOGPERSIST_PROPERTY_SERVICE);
        SystemPropPoker.getInstance().poke();
    }

    public boolean isLogPersistOff(){
        return SELECT_LOGPERSIST_PROPERTY_SERVICE.equals(
                SystemProperties.get(ACTUAL_LOGPERSIST_PROPERTY));
    }

    public void setLogSize(String size) {
        if(isLogServiceEnalbed){
            SystemProperties.set(PERSIST_SYS_SLOGSIZE, size);
        }else {
            SystemProperties.set("persist.logd.logpersistd.size", size);
            SystemPropPoker.getInstance().poke();
        }
    }

    public String getLogSize(){
        if(isLogServiceEnalbed){
            return SystemProperties.get(PERSIST_SYS_SLOGSIZE, "256");
        }else {
            return SystemProperties.get("logd.logpersistd.size", "256");
        }
    }

    /**
     * /data/misc/logd/*
     * logcatd.rc: on property:logd.logpersistd.enable=true && property:logd.logpersistd=clear
     * adb shell setprop logd.logpersistd clear
     */
    public void clearLogs(Context context){
        SystemProperties.set(ACTUAL_LOGPERSIST_PROPERTY, "clear");
        SystemPropPoker.getInstance().poke();
    }

    /**
     * /bugreports/* --> /data/user_de/0/com.android.shell/files/bugreports/*
     * receiver: com.android.shell.BugreportReceiver
     * action:"com.android.internal.intent.action.BUGREPORT_CLEAR_ALL"
     */
    public void clearReports(Context context){
        Intent intent = new Intent("com.android.internal.intent.action.BUGREPORT_CLEAR_ALL");
        intent.setClassName("com.android.shell",
                "com.android.shell.BugreportReceiver");
        context.sendBroadcast(intent);
    }

    public void moveReports(Context context){
        Intent intent = new Intent("com.android.internal.intent.action.BUGREPORT_MOVE_TO_SDCARD");
        intent.setClassName("com.android.shell",
                "com.android.shell.BugreportReceiver");
        context.sendBroadcast(intent);
    }

    /**
     * Uri: content://com.android.shell.documents/document/bugreport
     * Document id: bugreport
     * @param context
     */
    public void viewReports(Context context){
        Intent intent = new Intent("android.intent.action.VIEW_DOWNLOADS");
        intent.setClassName("com.android.documentsui",
                "com.android.documentsui.ViewDownloadsActivity"/*files.FilesActivity*/);
        context.startActivity(intent);
    }


    private static final String UNICODE = "utf-8";

    class StreamConsumer extends Thread {
        InputStream is;
        List<String> list;

        StreamConsumer(InputStream is) {
            this.is = is;
        }

        StreamConsumer(InputStream is, List<String> list) {
            this.is = is;
            this.list = list;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is,UNICODE);
                BufferedReader br = new BufferedReader(isr);
                String line ;
                while ((line = br.readLine()) != null) {
                    if (list != null) {
                        list.add(line);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    class ProcessInfo {
        private String user;
        private String pid;
        private String ppid;
        private String name;

        @Override
        public String toString() {
            return "ProcessInfo{" +
                    "user='" + user + '\'' +
                    ", pid='" + pid + '\'' +
                    ", ppid='" + ppid + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    private List<String> getAllProcess() {
        List<String> orgProcList = new ArrayList<>();
        Process pro = null;
        try {
            pro = Runtime.getRuntime().exec("ps");
            StreamConsumer errorConsumer = new StreamConsumer(pro.getErrorStream());
            StreamConsumer outputConsumer = new StreamConsumer(pro.getInputStream(), orgProcList);
            errorConsumer.start();
            outputConsumer.start();
            if (pro.waitFor() != 0) {
                Log.e(TAG, "getAllProcess pro.waitFor() != 0");
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllProcess failed", e);
        } finally {
            try {
                if (pro != null) {
                    pro.destroy();
                }
            } catch (Exception e) {
                Log.e(TAG, "getAllProcess failed", e);
            }
        }
        return orgProcList;
    }

    private List<ProcessInfo> getProcessInfoList(List<String> orgProcessList) {
        List<ProcessInfo> proInfoList = new ArrayList<>();
        for (int i = 1; i < orgProcessList.size(); i++) {
            String processInfo = orgProcessList.get(i);
            String[] proStr = processInfo.split(" ");
            // USER PID PPID VSIZE RSS WCHAN PC NAME
            // root 1 0 416 300 c00d4b28 0000cd5c S /init
            List<String> orgInfo = new ArrayList<>();
            for (String str : proStr) {
                if (!"".equals(str)) {
                    orgInfo.add(str);
                }
            }
            if (orgInfo.size() == 9) {
                ProcessInfo pInfo = new ProcessInfo();
                pInfo.user = orgInfo.get(0);
                pInfo.pid = orgInfo.get(1);
                pInfo.ppid = orgInfo.get(2);
                pInfo.name = orgInfo.get(8);
                proInfoList.add(pInfo);
            }
        }
        return proInfoList;
    }

    private String getAppUser(String packName, List<ProcessInfo> allProList) {
        for (ProcessInfo processInfo : allProList) {
            if (processInfo.name.equals(packName)) {
                return processInfo.user;
            }
        }
        return null;
    }

    public boolean isLogServiceRunning(Context context){
        List<String> orgProcessList = getAllProcess();
        List<ProcessInfo> processInfoList = getProcessInfoList(orgProcessList);

        String packName = context.getPackageName();
        String myUser = getAppUser(packName, processInfoList);
        String pid = String.valueOf(android.os.Process.myPid());
        Log.d(TAG, "isLogServiceRunning myUser:"+myUser+" current pid:"+pid);

        /*
        recordLogServiceLog("app user is:"+myUser);
        recordLogServiceLog("========================");
        for (ProcessInfo processInfo : allProcList) {
            recordLogServiceLog(processInfo.toString());
        }
        recordLogServiceLog("========================");
        */
        for (ProcessInfo processInfo : processInfoList) {
            Log.d(TAG, "isLogServiceRunning ppid:"+processInfo.ppid+"  pid:"+processInfo.pid+" name:"+processInfo.name);
            if (processInfo.name.toLowerCase().equals("logcat") && pid.equals(processInfo.ppid)) {
                Log.d(TAG, "isLogServiceRunning pid:"+processInfo.pid+" name:"+processInfo.name.toLowerCase());
                return true;
            }
        }

        return false;
    }

    public void deleteDirectory(final String dirPath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(dirPath);
                if (file.isDirectory()) {
                    File[] allFiles = file.listFiles();
                    for (File logFile : allFiles) {
                        logFile.delete();
                    }
                }
            }
        }).start();
    }
}
