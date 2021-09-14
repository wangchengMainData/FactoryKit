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
public class InstallResultReceiver extends BroadcastReceiver {
    private String TAG = InstallResultReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, " InstallResultReceiver onReceive");
        if (intent != null) {
            final int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_FAILURE);
            String packageName = intent.getStringExtra( PackageInstaller.EXTRA_PACKAGE_NAME);
            Log.e(TAG, "status="+status+" packageName="+packageName);
//            installResultCallBack.broadcastCallback(status);
            if (status == PackageInstaller.STATUS_SUCCESS) {
                // success
                installResultTransact(true);
//                Toast.makeText(MainApp.mContext,"install " + packageName + " successed!",Toast.LENGTH_SHORT).show();
                Log.e(TAG, packageName + " install success, msg: "+intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
            } else {
                installResultTransact(false);
//                Toast.makeText(MainApp.mContext,"install " + packageName + " successed!",Toast.LENGTH_SHORT).show();
                Log.e(TAG, packageName + " install filed, msg: "+intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
            }
        }
    }
    private void installResultTransact(Boolean result){
        IBinder binder = GSFWManager.getInstance().getCoreServiceIBinder();
        if(binder != null)
        {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("com.gosuncn.zfyfw.service.ICoreService");
            data.writeBoolean(result);
            try {
                binder.transact(GSFWManager.BINDER_CODE_INSTALL_RESULT, data, reply, 0);
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