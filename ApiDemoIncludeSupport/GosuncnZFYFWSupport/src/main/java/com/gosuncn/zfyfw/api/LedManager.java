package com.gosuncn.zfyfw.api;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * LedManager类用于控制LED灯
 * @author wangmingdong
 * @version 1.0.1
 */
public class LedManager {
    private static final String TAG = LedManager.class.getSimpleName();

    // led
    /**
     * LED ID定义：LIGHT_ID_BATTERY,LIGHT_ID_NOTIFICATIONS,LIGHT_ID_IRLED
     */
    /**
     * 电池LED
     */
    public static final int LIGHT_ID_BATTERY = 3; // same as id in LightsManager.java
    /**
     * 提示LED
     */
    public static final int LIGHT_ID_NOTIFICATIONS = 4; // same as id in LightsManager.java
    /**
     * 红外LED
     */
    public static final int LIGHT_ID_IRLED = 11;

    /**
     * LED Color定义：LIGHT_COLOR_RED,LIGHT_COLOR_GREEN,LIGHT_COLOR_YELLOW
     */
    /**
     * 红色
     */
    public static final int LIGHT_COLOR_RED = 0x00ff0000;
    /**
     * 绿色
     */
    public static final int LIGHT_COLOR_GREEN = 0x0000ff00;
    /**
     * 黄色
     */
    public static final int LIGHT_COLOR_YELLOW = 0x00ffff00;

    /**
     * LED Status定义：LED_STATUS_CLOSED,LED_STATUS_OPENED,LED_STATUS_FLASHING
     */
    /**
     * 关闭状态
     */
    public static final int LED_STATUS_CLOSED = 0;
    /**
     * 打开状态
     */
    public static final int LED_STATUS_OPENED = 1;
    /**
     * 闪烁状态
     */
    public static final int LED_STATUS_FLASHING = 2;

    private static LedManager mLedManager;

    private static final int BINDER_CODE_LED = 5001;

    private LedManager() {

    }

    /**
     * 获取LedManager实例
     * @return
     */
    public static LedManager getInstance() {
        if ( mLedManager == null) {
            synchronized (LedManager.class) {
                if ( mLedManager == null ) {
                    mLedManager = new LedManager();
                }
            }
        }

        return mLedManager;
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

    private boolean ledControl(int id, int color, int onMs, int offMs, int status) {
        boolean result = false;

        IBinder notificationIBinder = getNotificationManager();

        if (notificationIBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.INotificationManager");
            data.writeInt(id);
            data.writeInt(color);
            data.writeInt(onMs);
            data.writeInt(offMs);
            data.writeInt(status);
            Log.d(TAG, "ledControl get service ibinder ok");
            try {
                notificationIBinder.transact(BINDER_CODE_LED, data, reply, 0);
                Log.d(TAG, "ledControl transact ok");
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


    // led
    /**
     * 设置LED灯颜色并亮起
     * @param id 取值见“LED ID定义”
     * @param color 取值见“LED Color定义”
     * @return
     * true：表示设置成功
     * false：表示设置失败<br/>
     * <br/>
     * 代码示例： 打开电池灯，显示为绿色
     * <pre>
     * <code>LedManager.getInstance().setLedColor(LedManager.LIGHT_ID_BATTERY,LedManager.LIGHT_COLOR_GREEN);
     * </code>
     * </pre>
     */
    public boolean setLedColor(int id, int color){
        return LedManager.getInstance().ledControl(id, color, 0, 0, LED_STATUS_OPENED);
    }

    // PLM14161, ZFY SILENT MODE, wmd, 200622
    public boolean setLedColor(Context context, int id, int color){
        if(1 == Settings.Global.getInt(context.getContentResolver(), "persist.sys.zfy.silent",0)){
            return false;
        }

        return LedManager.getInstance().ledControl(id, color, 0, 0, LED_STATUS_OPENED);
    }

    /**
     * 设置LED灯颜色并闪烁
     * @param id 取值见“LED ID定义”
     * @param color 取值见“LED Color定义”
     * @param onMs 闪烁时亮的时间，单位毫秒
     * @param offMs 闪烁时灭的时间，单位毫秒
     * @return
     * true：表示设置成功
     * false：表示设置失败 <br/>
     * <br/>
     * 代码示例： 提示灯红色闪烁
     * <pre>
     * <code>LedManager.getInstance().setLedFlashing(
     *                         LedManager.LIGHT_ID_NOTIFICATIONS,
     *                         LedManager.LIGHT_COLOR_RED,
     *                         1000,
     *                         1000);
     * </code>
     * </pre>
     */
    public boolean setLedFlashing(int id, int color, int onMs, int offMs){
        return LedManager.getInstance().ledControl(id, color, onMs, offMs, LED_STATUS_FLASHING);
    }

    public boolean setLedFlashing(Context context, int id, int color, int onMs, int offMs){
        if(1 == Settings.Global.getInt(context.getContentResolver(), "persist.sys.zfy.silent",0)){
            return false;
        }

        return LedManager.getInstance().ledControl(id, color, onMs, offMs, LED_STATUS_FLASHING);
    }

    /**
     * 打开LED灯
     * @param id 取值见“LED ID定义”
     * @return
     * true：表示设置成功
     * false：表示设置失败<br/>
     * <br/>
     * 代码示例： 打开红外灯
     * <pre>
     * <code>LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_IRLED);
     * </code>
     * </pre>
     */
    public boolean turnOnLed(int id){
        return LedManager.getInstance().ledControl(id, 0x00ffffff, 0, 0, LED_STATUS_OPENED);
    }

    public boolean turnOnLed(Context context, int id){
        if(1 == Settings.Global.getInt(context.getContentResolver(), "persist.sys.zfy.silent",0)){
            return false;
        }

        return LedManager.getInstance().ledControl(id, 0x00ffffff, 0, 0, LED_STATUS_OPENED);
    }

    /**
     * 关闭LED灯
     * @param id 取值见“LED ID定义”
     * @return
     * true：表示设置成功
     * false：表示设置失败<br/>
     * <br/>
     * 代码示例： 关闭电池灯
     * <pre>
     * <code>LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
     * </code>
     * </pre>
     */
    public boolean turnOffLed(int id){
        return LedManager.getInstance().ledControl(id, 0x0, 0, 0, LED_STATUS_CLOSED);
    }
}
