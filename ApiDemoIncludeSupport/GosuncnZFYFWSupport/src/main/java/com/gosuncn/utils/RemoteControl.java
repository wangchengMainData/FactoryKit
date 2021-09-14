package com.gosuncn.utils;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.UserHandle;


public class RemoteControl {

    public static void requestFactoryReset(Context context){
        Intent resetIntent = new Intent("android.intent.action.FACTORY_RESET");
        resetIntent.setPackage("android");
        resetIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        resetIntent.putExtra(Intent.EXTRA_REASON, "FactoryReset");
        resetIntent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, true);
        context.sendBroadcast(resetIntent);
    }

    public static void requestReboot(Context context){
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    public static void requestShutdown(Context context){
        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
        intent.putExtra(Intent.EXTRA_REASON,
                PowerManager.SHUTDOWN_USER_REQUESTED);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityAsUser(intent, UserHandle.CURRENT);
    }


}
