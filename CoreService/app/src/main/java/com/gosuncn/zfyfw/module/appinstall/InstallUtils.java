package com.gosuncn.zfyfw.module.appinstall;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

//import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// PLM14773, install app quietly, wmd, 2020.0720
public class InstallUtils {
    private static final String TAG = InstallUtils.class.getSimpleName();


    // 适配android9的安装方法。
    public static void install(Context context, String apkFilePath, Class<InstallResultReceiver> receiver) {
        Log.d(TAG, "install path=" + apkFilePath);
        File apkFile = new File(apkFilePath);
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams sessionParams
                = new PackageInstaller.SessionParams(PackageInstaller
                .SessionParams.MODE_FULL_INSTALL);
        sessionParams.setSize(apkFile.length());

        int sessionId = createSession(packageInstaller, sessionParams);
        Log.d(TAG, "install  sessionId=" + sessionId);
        if (sessionId != -1) {
            boolean copySuccess = copyInstallFile(packageInstaller, sessionId, apkFilePath);
            Log.d(TAG, "install  copySuccess=" + copySuccess);
            if (copySuccess) {
                execInstallCommand(context, packageInstaller, sessionId, receiver);
            }
        }
    }

    public static void uninstall(Context context, String packageName) {
        Intent broadcastIntent = new Intent(context, UninstallResultReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,
                broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(packageName, pendingIntent.getIntentSender());
    }

    private static int createSession(PackageInstaller packageInstaller,
                                     PackageInstaller.SessionParams sessionParams) {
        int sessionId = -1;
        try {
            sessionId = packageInstaller.createSession(sessionParams);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sessionId;
    }

    private static boolean copyInstallFile(PackageInstaller packageInstaller,
                                           int sessionId, String apkFilePath) {
        InputStream in = null;
        OutputStream out = null;
        PackageInstaller.Session session = null;
        boolean success = false;
        try {
            File apkFile = new File(apkFilePath);
            session = packageInstaller.openSession(sessionId);
            out = session.openWrite("base.apk", 0, apkFile.length());
            in = new FileInputStream(apkFile);
            int total = 0, c;
            byte[] buffer = new byte[65536];
            while ((c = in.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            Log.i(TAG, "streamed " + total + " bytes");
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException IOE){

            }
            if (session != null) {
                session.close();
            }
        }
        return success;
    }

    private static void execInstallCommand(Context context, PackageInstaller packageInstaller, int sessionId,
                                           Class<InstallResultReceiver> receiver) {
        PackageInstaller.Session session = null;
        try {
            session = packageInstaller.openSession(sessionId);
            Intent intent = new Intent(context, receiver);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
            Log.i(TAG, "begin session");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
