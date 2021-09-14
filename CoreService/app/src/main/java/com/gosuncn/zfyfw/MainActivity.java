package com.gosuncn.zfyfw;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.widget.LockPatternUtils;

import com.gosuncn.zfyfw.nv.NvUtilExt;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;
import com.gosuncn.zfyfw.R;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends Activity {
    static final String TAG = MainActivity.class.getSimpleName();

    private LockPatternUtils mLockPatternUtils;
    private UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLockPatternUtils = new LockPatternUtils(getApplicationContext());
        mUserManager = (UserManager)this.getSystemService(Context.USER_SERVICE);

        TextView tv = (TextView)findViewById(R.id.set_lock_passwd);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLockPasswd();
            }
        });
        tv = (TextView)findViewById(R.id.rm_lock_passwd);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rmLockPasswd();
            }
        });
        tv = (TextView)findViewById(R.id.nv_read);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = NvUtilExt.getInstance().getBTNv();
                Log.d(TAG, "getBTNv value:" + value);
                final TextView tv = (TextView)findViewById(R.id.nv_read);
                tv.setText(getString(R.string.nv_read) + ":" + value);

                value = NvUtilExt.getInstance().getWLANNv();
                Log.d(TAG, "getWLANNv value:" + value);

                value = NvUtilExt.getInstance().getSNNv();
                Log.d(TAG, "getSNNv value:" + value);

                value = NvUtilExt.getInstance().getIMEINv();
                Log.d(TAG, "getIMEINv value:" + value);

                value = NvUtilExt.getInstance().getFactoryNv2497();
                Log.d(TAG, "getFactoryNv2497 value:" + value);

                value = NvUtilExt.getInstance().getFactoryNv2498();
                Log.d(TAG, "getFactoryNv2498 value:" + value);

                value = NvUtilExt.getInstance().getFactoryNv2499();
                Log.d(TAG, "getFactoryNv2499 value:" + value);

                value = NvUtilExt.getInstance().getFactoryNv2500();
                Log.d(TAG, "getFactoryNv2500 value:" + value);
            }
        });
        tv = (TextView)findViewById(R.id.nv_write);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NvUtilExt.getInstance().setBTNv("121212121212");
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private static String getStoragePath(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void setLockPasswd(){
        Log.d(TAG, " setLockPasswd");
        List<UserHandle> users = mUserManager.getUserProfiles();
        UserHandle userHandle = null;
        if(!users.isEmpty()){
            userHandle = users.get(0);
        }
        boolean isOpen = mLockPatternUtils.isLockPasswordEnabled(userHandle.describeContents());
        mLockPatternUtils.saveLockPassword("888888", null, 0, userHandle.describeContents());
    }

    private void rmLockPasswd(){
        Log.d(TAG, " rmLockPasswd");
        mLockPatternUtils.clearLock("888888", UserHandle.myUserId());
        mLockPatternUtils.setLockScreenDisabled(true, UserHandle.myUserId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

/*
            String path = getStoragePath(MainActivity.this, false);
            Log.d(TAG, "onOptionsItemSelected internal path:"+path);
            path = getStoragePath(MainActivity.this, true);
            Log.d(TAG, "onOptionsItemSelected external path:"+path);

            File file = new File(path+ File.separator + "sdsd.txt");
            if(!file.exists()){
                try{
                    file.createNewFile();
                }catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }
*/

            return true;
        }else if(id == R.id.nv_write){
            Log.d(TAG, "NvUtilExtTest setGNSSConfig");
            return true;

        }else if(id == R.id.nv_read){
            Log.d(TAG, "NvUtilExtTest getGNSSConfig");//:"+GNSSConfig);
            return true;
        }else if(id == R.id.set_lock_passwd){
            Log.d(TAG, " set_lock_passwd");
            setLockPasswd();
            return true;
        }else if(id == R.id.rm_lock_passwd){
            Log.d(TAG, " rm_lock_passwd");
            rmLockPasswd();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSFWManager.getInstance().registerBodyTempCallback(mISettingsContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();

        GSFWManager.getInstance().unresigterBodyTempCallback(mISettingsContentObserver);
    }

    private ISettingsContentObserver.Stub mISettingsContentObserver = new ISettingsContentObserver.Stub() {
        @Override
        public void onchanged(int type, int value, List<String> valueList) throws RemoteException {
            Log.d(TAG, "onchanged type:" + type + " value:" + value + " valueList:" + valueList.toString());
            if(valueList != null && valueList.size() > 0) {
                Toast.makeText(MainActivity.this, String.valueOf(type) + "-" + valueList.get(0), Toast.LENGTH_SHORT).show();
            }
        }

    };
}
