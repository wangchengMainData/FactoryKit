package com.gosuncn.zfyfactorytest.SDCard;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;

import java.util.List;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class FormatService extends IntentService {
    private static final String TAG = FormatService.class.getSimpleName();

    private static final String ACTION_FORMAT = "android.intent.action.gosuncn.sdcardformat";

    public static void startTask(Context context){
        Log.d(TAG, "startTask");
        Intent formatSdcardIntent = new Intent(context, FormatService.class);
        formatSdcardIntent.setAction(ACTION_FORMAT);
        context.startService(formatSdcardIntent);
    }

    public FormatService(){
        super("FormatSDService");
    }

    public FormatService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent != null && ACTION_FORMAT.equals(intent.getAction())){
            Log.d(TAG, "onHandleIntent");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    formatSdcard(getApplicationContext());
                }
            }).start();
        }
    }

    private boolean formatSdcard(Context context){
        boolean result = false;
        try {
            Log.d(TAG, "[formatSdcard] start --> ");
            StorageManager sm = context.getSystemService(StorageManager.class);
            final List<VolumeInfo> volumes = sm.getVolumes();
            for (VolumeInfo vol : volumes) {
                DiskInfo diskInfo = vol.getDisk();
                Log.d(TAG, "formatSdcard vol.getDescription : " + vol.getDescription());
                Log.d(TAG, "formatSdcard vol.getPath : " + vol.getPath());
                Log.d(TAG, "formatSdcard vol.fsType : " + vol.fsType);
                if (diskInfo != null) {
                    Log.d(TAG, "formatSdcard diskInfo.getDescription : " + diskInfo.getDescription());
                }
                if (diskInfo != null && diskInfo.isSd() && !"vfat".equals(vol.fsType)) {
                    Log.d(TAG, "formatSdcard SD disk id : " + diskInfo.getId());
                    sm.partitionPublic(diskInfo.getId());
                    Log.d(TAG, "formatSdcard finished");
                    result = true;
                }
            }
        }catch (Exception e){
            result = false;
        }
        return result;
    }
}
