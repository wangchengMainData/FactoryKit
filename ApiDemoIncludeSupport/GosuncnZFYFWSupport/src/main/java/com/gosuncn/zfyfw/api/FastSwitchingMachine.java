package com.gosuncn.zfyfw.api;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * OverTheWallManager类用APP层操作系统事件
 */
public class FastSwitchingMachine {

    private static final String TAG = FastSwitchingMachine.class.getSimpleName();


    private static FastSwitchingMachine mOverTheWallManager;

    private static final int BINDER_DEVICE_SLEEP = 5002;

    private FastSwitchingMachine() {

    }

    /**
     * 获取OverTheWallManager实例
     * @return
     */
    public static FastSwitchingMachine getInstance() {
        if ( mOverTheWallManager == null) {
            synchronized (FastSwitchingMachine.class) {
                if ( mOverTheWallManager == null ) {
                    mOverTheWallManager = new FastSwitchingMachine();
                }
            }
        }

        return mOverTheWallManager;
    }

    IBinder mNotificationIBinder = null;

    private IBinder getNotificationManager() {
        if(mNotificationIBinder == null) {
            // 获取服务IBinder方式
            // 1.源码编译
            // Android.mk:去掉LOCAL_SDK_VERSION，使用LOCAL_PRIVATE_PLATFORM_APIS，如：
            // //LOCAL_SDK_VERSION := current
            // LOCAL_PRIVATE_PLATFORM_APIS := true
            //notificationIBinder = ServiceManager.checkService("notification");

            // 2.IDE编译
            // 通过反射获取服务IBinder
            try {
                Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("checkService", new Class[]{String.class});
                mNotificationIBinder = (IBinder) getServiceMethod.invoke(null, new Object[]{"notification"});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mNotificationIBinder;
    }

    /*
    *   isSleep 0 亮  1 熄灭
    * */
    public boolean sleepSystemControl(int isSleep) {
        boolean result = false;

        IBinder notificationIBinder = getNotificationManager();

        if (notificationIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.INotificationManager");
            data.writeInt(isSleep);
            try {
                notificationIBinder.transact(BINDER_DEVICE_SLEEP, data, reply, 0);
                reply.readException();
                int intValue = reply.readInt();
                result = true;
            } catch (RemoteException e) {
                Log.d(TAG, "ledControl transact failed: remote exception happend ", e);
            } catch (Exception e) {
                Log.d(TAG, "ledControl transact failed: Someone wrote a bad service that doesn't like to be poked", e);
            } finally {
                reply.recycle();
                data.recycle();
            }
        }else{
            Log.d(TAG, "ledControl get service ibinder failed: null");
        }
        return result;
    }

}
