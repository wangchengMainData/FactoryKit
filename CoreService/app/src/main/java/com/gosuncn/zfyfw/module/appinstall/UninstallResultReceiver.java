package com.gosuncn.zfyfw.module.appinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.gosuncn.zfyfw.MainApp;
import com.gosuncn.zfyfw.service.GSFWManager;

// PLM14773, install app quietly, wmd, 2020.0720
public class UninstallResultReceiver extends BroadcastReceiver {
    private String TAG = UninstallResultReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "UnInstallResultReceiver onReceive");
        if (intent != null) {
            final int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE);
            String packageName = intent.getStringExtra( PackageInstaller.EXTRA_PACKAGE_NAME);
//            uninstallResultCallback.broadcastCallback(status);
            Log.e(TAG, "status="+status+" packageName="+packageName);
            if (status == PackageInstaller.STATUS_SUCCESS) {
                // success
                unInstallResultTransact(true);
//                Toast.makeText(MainApp.mContext,"uninstall " + packageName + " successed!",Toast.LENGTH_SHORT).show();
                Log.e(TAG, packageName + " uninstall success, msg: "+intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
            } else {
                unInstallResultTransact(false);
//                Toast.makeText(MainApp.mContext,"uninstall " + packageName + " failed!",Toast.LENGTH_SHORT).show();
                Log.e(TAG, packageName + " uninstall filed, msg: "+intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
            }
        }
    }

    private void unInstallResultTransact(Boolean result){
        IBinder binder = GSFWManager.getInstance().getCoreServiceIBinder();
        if(binder != null)
        {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeBoolean(result);
            try {
                binder.transact(GSFWManager.BINDER_CODE_UNINSTALL_RESULT, data, reply, 0);
                reply.readException();
            }catch (RemoteException e){
                e.printStackTrace();
            }finally {
                data.recycle();
                reply.recycle();
            }
        }
    }
}