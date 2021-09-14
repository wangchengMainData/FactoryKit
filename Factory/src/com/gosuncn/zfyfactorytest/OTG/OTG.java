package com.gosuncn.zfyfactorytest.OTG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.InputDevice;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.Framework;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.R;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class OTG extends BaseActivity {

    private static final String TAG = OTG.class.getSimpleName();

    private StorageManager mStorageManager;
    private boolean isOTGStorageAvi = false;
    private boolean isOTGInputDeviceAvi = false;
    private Context mContext;
    private InputManager mInputManager;
    private InputManager.InputDeviceListener mInputDeviceListener;

    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (OTG.isInteresting(vol)) {
                getConfirmText().setText(R.string.OTG_init);
                OTG.this.isudiskExists();
                String str = OTG.this.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onVolumeStateChanged isOTGStorageAvi=");
                stringBuilder.append(OTG.this.isOTGStorageAvi);
                Log.i(str, stringBuilder.toString());
                if (OTG.this.isOTGStorageAvi || OTG.this.isOTGInputDeviceAvi) {
                    OTG.this.refresh();

                    if(Framework.quickTestEnabled){
                        onPositiveCallback();
                    }
                }else{
                    getConfirmText().setText(R.string.OTG_fail);
                }
            }
        }

        public void onDiskDestroyed(DiskInfo disk) {
        }
    };

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ((UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) ||
                (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))) {
                Log.d(TAG, "onUSBDeviceChanged");
                final int[] devices = InputDevice.getDeviceIds();
                isOTGInputDeviceAvi = false;
                try {
                    for (int i = 0; i < devices.length; i++) {
                        InputDevice device = InputDevice.getDevice(devices[i]);
                        if (device != null && !device.isVirtual()/* && device.isExternal()*/) {
                            Log.d(TAG, "onUSBDeviceChanged device.getName()=" + device.getName() + " device.getId() " + device.getId() + " getDescriptor " + device.getDescriptor());
                            if (device.getName().toUpperCase().contains("MOUSE") || device.getName().toUpperCase().contains("KEYBOARD")) {
                                Log.d(TAG, "onUSBDeviceChanged Mouse OR Keyboard");
                                isOTGInputDeviceAvi = true;
                            }
                        }
                    }
                }catch (Exception e){e.printStackTrace();}
                if (OTG.this.isOTGStorageAvi || OTG.this.isOTGInputDeviceAvi) {
                    OTG.this.refresh();

                    if(Framework.quickTestEnabled){
                        onPositiveCallback();
                    }
                }else{
                    getConfirmText().setText(R.string.OTG_fail);
                }
            }
        }
    };

    private void inputDeviceChanged(String log_tag) {
        Log.d(TAG, log_tag);
        final int[] devices = InputDevice.getDeviceIds();
        isOTGInputDeviceAvi = false;
        try {
            for (int i = 0; i < devices.length; i++) {
                InputDevice device = InputDevice.getDevice(devices[i]);
                if (device != null && !device.isVirtual()/* && device.isExternal()*/) {
                    Log.d(TAG, log_tag + " device.getName()=" + device.getName() + " device.getId() " + device.getId() + " getDescriptor " + device.getDescriptor());
                    if (device.getName().toUpperCase().contains("MOUSE") || device.getName().toUpperCase().contains("KEYBOARD")) {
                        Log.d(TAG, log_tag + " Mouse OR Keyboard");
                        isOTGInputDeviceAvi = true;
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();}

        if (OTG.this.isOTGStorageAvi || OTG.this.isOTGInputDeviceAvi) {
            OTG.this.refresh();

            if(Framework.quickTestEnabled){
                onPositiveCallback();
            }
        }else{
            getConfirmText().setText(R.string.OTG_fail);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(loadDefaultConfirmText(getString(R.string.OTG_init)));

        mContext = getApplicationContext();
        mStorageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);

        mInputManager = (InputManager)getSystemService(Context.INPUT_SERVICE);

        mInputDeviceListener = new InputManager.InputDeviceListener() {
            @Override
            public void onInputDeviceAdded(int deviceId) {
                inputDeviceChanged("onInputDeviceAdded");
            }

            @Override
            public void onInputDeviceRemoved(int deviceId) {
                inputDeviceChanged("onInputDeviceRemoved");
            }

            @Override
            public void onInputDeviceChanged(int deviceId) {
                Log.d(TAG, "onInputDeviceChanged");
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        mStorageManager.registerListener(this.mStorageListener);
        mInputManager.registerInputDeviceListener(mInputDeviceListener, null);
/*        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);*/

        getConfirmText().setText(R.string.OTG_init);

        final int[] devices = InputDevice.getDeviceIds();
        try {
            for (int i = 0; i < devices.length; i++) {
                InputDevice device = InputDevice.getDevice(devices[i]);
                if (device != null && !device.isVirtual()/* && device.isExternal()*/) {
                    Log.d(TAG, "device.getName()=" + device.getName() + " device.getId() " + device.getId() + " getDescriptor " + device.getDescriptor());
                    if (device.getName().toUpperCase().contains("MOUSE") || device.getName().toUpperCase().contains("KEYBOARD")) {
                        Log.d(TAG, "onInputDeviceFind Mouse OR Keyboard");
                        isOTGInputDeviceAvi = true;
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();}

        isudiskExists();
        String str = this.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onResume isOTGStorageAvi=");
        stringBuilder.append(this.isOTGStorageAvi);
        Log.i(str, stringBuilder.toString());
        if (this.isOTGStorageAvi || isOTGInputDeviceAvi) {
            refresh();

            if(Framework.quickTestEnabled){
                onPositiveCallback();
            }
        }else{
            getConfirmText().setText(R.string.OTG_fail);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStorageManager.unregisterListener(this.mStorageListener);
//        unregisterReceiver(mUsbReceiver);
        mInputManager.unregisterInputDeviceListener(mInputDeviceListener);
    }

    @Override
    protected void onPositiveCallback() {
        setResult(RESULT_OK);
        Utils.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    @Override
    protected void onNegativeCallback() {
        setResult(RESULT_CANCELED);
        Utils.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    public void isudiskExists() {
        int num = 0;
        for (VolumeInfo volInfo : this.mStorageManager.getVolumes()) {
            DiskInfo diskInfo = volInfo.getDisk();
            if (diskInfo != null && diskInfo.isUsb()) {
                String sdcardState = VolumeInfo.getEnvironmentForState(volInfo.getState());
                String str = this.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("isudiskExists sdcardState=");
                stringBuilder.append(sdcardState);
                Log.i(str, stringBuilder.toString());
                // unmountable
                // checking
                //if ("mounted".equals(sdcardState)) {
                    num++;
                //}
            }
        }
        String str2 = this.TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("isudiskExists num=");
        stringBuilder2.append(num);
        Log.i(str2, stringBuilder2.toString());
        if (num > 0) {
            this.isOTGStorageAvi = true;
        } else {
            this.isOTGStorageAvi = false;
        }
    }

    private static boolean isInteresting(VolumeInfo vol) {
        if (vol.getType() != 0) {
            return false;
        }
        return true;
    }

    private void refresh() {

        if(isOTGStorageAvi) {
            List<VolumeInfo> volumes = this.mStorageManager.getVolumes();
            Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
            String str = this.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("refresh volumes.size=");
            stringBuilder.append(volumes.size());
            Log.i(str, stringBuilder.toString());
            boolean hasExisted = false;
            for (VolumeInfo volume : volumes) {
                DiskInfo diskInfo = volume.getDisk();
                if (diskInfo != null && diskInfo.isUsb()) {
                    if ("mounted".equals(VolumeInfo.getEnvironmentForState(volume.getState()))) {
                        hasExisted = updateDisplay(volume);
                    }
                }
            }
            if (!hasExisted) {
                getConfirmText().setText(getString(R.string.OTG_ok));
            }
        }
        else if(isOTGInputDeviceAvi){
            getConfirmText().setText(getString(R.string.OTG_ok));
        }
    }

    private boolean updateDisplay(VolumeInfo volume) {
        if (volume.isMountedReadable()) {
            File path = volume.getPath();
            long totalBytes = path.getTotalSpace();
            String used = Formatter.formatFileSize(this.mContext, totalBytes - path.getFreeSpace());
            String total = Formatter.formatFileSize(this.mContext, totalBytes);
            String str = this.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("updateDisplay used=");
            stringBuilder.append(used);
            stringBuilder.append(";total=");
            stringBuilder.append(total);
            Log.i(str, stringBuilder.toString());
            getConfirmText().setText(getString(R.string.storage_volume_summary, new Object[]{used, total}));
            return true;
        }
        String stateDesc = volume.getDescription();
        getConfirmText().setText(!TextUtils.isEmpty(stateDesc) ? (getString(R.string.OTG_ok) + ":" + stateDesc) : getString(R.string.OTG_ok));
        return true;
    }
}

