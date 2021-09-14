package ga.mdm;

import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.gosuncn.zfyfw.service.ICoreService;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PolicyManager {
    private static final String TAG = PolicyManager.class.getSimpleName();
    private static final boolean DEBUG = !"user".equals(Build.TYPE);
    public static final String SERVICE_NAME = "zfyfw";
    private static final String[] unknownStringArray = new String[]{"unknown"};
    private static final String unknownString = "unknown";
    private static List unknownList = new ArrayList<String>();

    private ICoreService mICoreService;
    private PolicyManager() {
        mICoreService = getServiceInterface();
        unknownList.add("unknown");
    }
    private ICoreService getICoreService(){
        if (mICoreService == null) {
            mICoreService = getServiceInterface();
        }
        return  mICoreService;
    }

    public static PolicyManager getInstance() {
        return PolicyManager.HOLDER.instance;
    }

    public String[] listIccid() {
        boolean result = false;
        String[] listIccid = unknownStringArray;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            listIccid = mICoreService.listIccid();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "listIccid result:"+result+" listIccid:"+listIccid);
        }
        return listIccid;
    }

    public String[] listImei() {
        boolean result = false;
        String[] listImei = unknownStringArray;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            listImei = mICoreService.listImei();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "listImei result:"+result+" listImei:"+listImei);
        }
        return listImei;
    }

    public String getCryptoModuleInfo(){
        String result = unknownString;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getCryptoModuleInfo();
        } catch (RemoteException re){
            re.printStackTrace();
            result = unknownString;
        } catch (Exception e){
            e.printStackTrace();
            result = unknownString;
        }
        if(DEBUG) {
            Log.d(TAG, "getCryptoModuleInfo result:"+result);
        }
        return result;
    }
	
	 public String[] listCertificates() {
        boolean result = false;
        String[] listCertification = unknownStringArray;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            listCertification = mICoreService.listCertificates();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "listCertification result:"+result+" listCertification:"+listCertification);
        }
        return listCertification;
    }
	
	    public int establishVpnConnection() {
        int result = 1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.establishVpnConnection();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 1;
        } catch (Exception e){
            e.printStackTrace();
            result = 1;
        }
        return result;
    }

    public int disestablishVpnConnection() {
        int result = 1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.disestablishVpnConnection();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 1;
        } catch (Exception e){
            e.printStackTrace();
            result = 1;
        }
        return result;
    }
    public boolean setSoundRecorderBtn(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setSoundRecorderBtn(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setSoundRecorderBtn result:"+result);
        }
        return false;
    }

    public int getSoundRecorderBtn() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getSoundRecorderBtn();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getSoundRecorderBtn result:"+result);
        }
        return result;
    }

    public boolean setVideoRecorderBtn(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setVideoRecorderBtn(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setVideoRecorderBtn result:"+result);
        }
        return false;
    }

    public int getVideoRecorderBtn() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getVideoRecorderBtn();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getVideoRecorderBtn result:"+result);
        }
        return result;
    }
	
	public int getVpnServiceState() {
        int result = 1;//0：未启动；1：连接中；2：重试中；3：已建立；4：发生错误；5：已断开
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getVpnServiceState();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 1;
        } catch (Exception e){
            e.printStackTrace();
            result = 1;
        }
        return result;
    }

    public boolean setPictureBtn(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setPictureBtn(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setPictureBtn result:"+result);
        }
        return false;
    }

    public int getPictureBtn() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getPictureBtn();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getPictureBtn result:"+result);
        }
        return result;
    }

    public boolean setSosBtn(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setSosBtn(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setSosBtn result:"+result);
        }
        return false;
    }

    public int getSosBtn() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getSosBtn();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getSosBtn result:"+result);
        }
        return result;
    }

    public boolean setPttBtn(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setPttBtn(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setPttBtn result:"+result);
        }
        return false;
    }

    public int getPttBtn() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getPttBtn();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getPttBtn result:"+result);
        }
        return result;
    }

    public String getMonitorInfo() {
        String result = unknownString;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getMonitorInfo();
        } catch (RemoteException re){
            re.printStackTrace();
            result = unknownString;
        } catch (Exception e){
            e.printStackTrace();
            result = unknownString;
        }
        if(DEBUG) {
            Log.d(TAG, "getMonitorInfo result:"+result);
        }
        return result;
    }

    public boolean setWlanPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setWlanPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setwlanpolicies result:"+result);
        }
        return result;
    }

    public int getWlanPolicies() {
        int result = -1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getWlanPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getwlanpolicies result:"+result);
        }
        return result;
    }

    public boolean setDataConnectivityPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setDataConnectivityPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setDataConnectivityPolicies:"+result);
        }
        return result;
    }

    public int getDataConnectivityPolicies() {
        int result = -1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getDataConnectivityPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getDataConnectivityPolicies:"+result);
        }
        return result;
    }

    public boolean setBluetoothPolicies(int mode, String[] bluetoothInfoList) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setBluetoothPolicies(mode,bluetoothInfoList);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setBluetoothPolicies:"+result);
        }
        return result;
    }

    public String[] getBluetoothPolicies() {
        boolean result = false;
        String[] policies = unknownStringArray;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getBluetoothPolicies();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "getBluetoothPolicies:"+result);
        }
        return policies;
    }

    public boolean setNfcPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setNfcPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setNfcPolicies:"+result);
        }
        return result;
    }

    public int getNfcPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getNfcPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "setNfcPolicies:"+policies);
        }
        return policies;
    }

    public boolean setIrPolicies(int mode) {
        Log.e("wc","setIrPolicies PoliciyManager :" + mode );
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setIrPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setNfcPolicies:"+result);
        }
        return result;
    }

    public int getIrPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getIrPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getIrPolicies:"+policies);
        }
        return policies;
    }

    public boolean setBiometricRecognitionPolicies(int mode) {
        Log.e("wc","setBiometricRecognitionPolicies PoliciyManager :" + mode );
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setBiometricRecognitionPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setBiometricRecognitionPolicies:"+result);
        }
        return result;
    }

    public int getBiometricRecognitionPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getBiometricRecognitionPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getBiometricRecognitionPolicies:"+policies);
        }
        return policies;
    }

    public boolean setGpsPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setGpsPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setGpsPolicies:"+result);
        }
        return result;
    }

    public int getGpsPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getGpsPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getGpsPolicies:"+policies);
        }
        return policies;
    }

    public boolean setUsbDataPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setUsbDataPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setUsbDataPolicies:"+result);
        }
        return result;
    }

    public int getUsbDataPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getUsbDataPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getUsbDataPolicies:"+policies);
        }
        return policies;
    }

    public boolean setExternalStoragePolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setExternalStoragePolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setExternalStoragePolicies:"+result);
        }
        return result;
    }

    public int getExternalStoragePolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getExternalStoragePolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getExternalStoragePolicies:"+policies);
        }
        return policies;
    }

    public boolean setMicrophonePolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setMicrophonePolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setMicrophonePolicies:"+result);
        }
        return result;
    }

    public int getMicrophonePolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getMicrophonePolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getMicrophonePolicies:"+policies);
        }
        return policies;
    }

    public boolean setSpeakerPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setSpeakerPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setSpeakerPolicies:"+result);
        }
        return result;
    }

    public int getSpeakerPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getSpeakerPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getSpeakerPolicies:"+policies);
        }
        return policies;
    }

    public boolean setCameraPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setCameraPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setCameraPolicies:"+result);
        }
        return result;
    }

    public int getCameraPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getCameraPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getCameraPolicies:"+policies);
        }
        return policies;
    }

    public boolean setFlashPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setFlashPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setFlashPolicies:"+result);
        }
        return result;
    }

    public int getFlashPolicies() {
        int policies = -1 ;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            policies = mICoreService.getFlashPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            policies = -1;
        } catch (Exception e){
            e.printStackTrace();
            policies = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getFlashPolicies:"+policies);
        }
        return policies;
    }

    public boolean setPeripheralPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setPeripheralPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setPeripheralPolicies:"+result);
        }
        return result;
    }

    public int getPeripheralPolicies() {
        int result = -1;
        try {
            result = getICoreService().getPeripheralPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getPeripheralPolicies result:"+result);
        }
        return result;
    }

    public boolean setVoicePolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setVoicePolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setVoicePolicies:"+result);
        }
        return result;
    }

    public int getVoicePolicies() {
        int result = -1;
        try {
            result = getICoreService().getVoicePolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getVoicePolicies result:"+result);
        }
        return result;
    }

    public boolean setSmsPolicies(int mode, String regExp) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setSmsPolicies(mode, regExp);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setVoicePolicies:"+result);
        }
        return result;
    }

    public int getSmsPolicies() {
        int result = -1;
        try {
            result = getICoreService().getSmsPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getSmsPolicies result:"+result);
        }
        return result;
    }

    public boolean setCaptureScreenPolicies(int mode) {
        boolean result = false;
        try {
            result = getICoreService().setCaptureScreenPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setCaptureScreenPolicies result:"+result);
        }
        return false;    
    }

    public int getCaptureScreenPolicies() {
        int result = -1;
        try {
            result = getICoreService().getCaptureScreenPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getCaptureScreenPolicies result:"+result);
        }
        return result;
    }

    public boolean setWlanApPolicies(int mode, String[] macInfoList) {
        boolean result = false;
        try {
            result = getICoreService().setWlanApPolicies(mode,macInfoList);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setWlanApPolicies result:"+result);
        }
        return result;
    }

    public String[] getWlanApPolicies() {
        boolean result = false;
        String[] apPolicies = unknownStringArray;
        try {
            apPolicies = getICoreService().getWlanApPolicies();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "getWlanApPolicies result:"+result);
        }
        return apPolicies;
    }

    public boolean setUserApnMgrPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setUserApnMgrPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setUserApnMgrPolicies result:"+result);
        }
        return result;
    }

    public int getUserApnMgrPolicies() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getUserApnMgrPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getUserApnMgrPolicies result:"+result);
        }
        return result;
    }

    public String executeShellToSetIptables(String commandline) {
        String result = null;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.executeShellToSetIptables(commandline);
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "executeShellToSetIptables result:"+result);
        }
        return result;
    }

    /**
     * 设置允许用户设定的密码策略
     * 0：要求设置字母数字混合密码，并对密码合规性进行检查；
     * 1：要求设置简单数字密码，并对密码合规性进行检查；
     * 2：要求启用生物识别技术；
     * 3：允许用户自行设定密码策略，不进行统一管控
     * @param mode
     * @return
     */
    public boolean setUserPasswordPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setUserPasswordPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setUserPasswordPolicies result:"+result);
        }
        return result;
    }

    /**
     * 获取允许用户设定的密码策略
     * 0：要求设置字母数字混合密码，并对密码合规性进行检查；
     * 1：要求设置简单数字密码，并对密码合规性进行检查；
     * 2：要求启用生物识别技术；
     * 3：允许用户自行设定密码策略，不进行统一管控
     * @return
     */
    public int getUserPasswordPolicies() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getUserPasswordPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getUserPasswordPolicies result:"+result);
        }
        return result;
    }

    public boolean setUserTimeMgrPolicies(int mode) {
        boolean result = false;
        try {
            result = getICoreService().setUserTimeMgrPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setUserTimeMgrPolicies result:"+result);
        }
        return result;
    }

    public int getUserTimeMgrPolicies() {
        int result = -1;
        try {
            result = getICoreService().getUserTimeMgrPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getUserTimeMgrPolicies result:"+result);
        }
        return result;
    }

  public boolean setFactoryResetPolicies(int mode) {
        boolean result = false;
        try {
            result = getICoreService().setFactoryResetPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setFactoryResetPolicies result:"+result);
        }
        return result;
    }

    public int getFactoryResetPolicies() {
        int result = -1;
        try {
            result = getICoreService().getFactoryResetPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getFactoryResetPolicies result:"+result);
        }
        return result;
    }


 public boolean setDevelopmentModePolicies(int mode) {
        boolean result = false;
        try {
            result = getICoreService().setDevelopmentModePolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setDevelopmentModePolicies result:"+result);
        }
        return result;
    }


 public int getDevelopmentModePolicies() {
        int result = -1;
        try {
            result = getICoreService().getDevelopmentModePolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getDevelopmentModePolicies result:"+result);
        }
        return result;
    }

  public boolean setSystemUpdatePolicies(int mode) {
        boolean result = false;
        try {
            result = getICoreService().setSystemUpdatePolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setSystemUpdatePolicies result:"+result);
        }
        return result;
    }

    public int getSystemUpdatePolicies() {
        int result = -1;
        try {
            result = getICoreService().getSystemUpdatePolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getSystemUpdatePolicies result:"+result);
        }
        return result;
    }

    /**
     * 禁用终端应用调用应用交互式安装/卸载接口
     * @param mode
     * 0：不允许任何终端应用调用应用交互式安装/卸载接口；
     * 1：仅允许接口授权列表指定的终端应用调用应用交互式安装/卸载接口，接口授权列表包含应用包名和应用签名证书指纹值；
     * 2：允许所有应用调用应用交互式安装/卸载接口
     * @param appList
     * 仅 当   mode=1   时 有 效 ， 数 组 中 每 一 项 为 一 个   JSON   格 式 字 符 串 ， 
     * 格 式 如 下 ：{"AppPackageName":"应用包名","CertificateHash":"证书 Sha1 哈希值 16 进制字符串"}
     * @return
     */
    public boolean setInstallUninstallPolicies(int mode, String[] appList) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setInstallUninstallPolicies(mode, appList);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setInstallUninstallPolicies result:"+result);
        }
        return result;
    }

    /**
     * 是否禁用终端应用调用应用交互式安装/卸载接口
     * string[0]：功能模式，参见 setInstallUninstallPolicies 方法的 mode 参数。
     * string[1]至 string[n-1]：仅当 mode=1 时返回允许交互式安装/卸载的特定 APP 信息，参见
     * setInstallUninstallPolicies 方法的 appList 参
     * @return
     */
    public String[] getInstallUninstallPolicies() {
        String[] result = null;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getInstallUninstallPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getInstallUninstallPolicies result:"+result);
        }
        return result;
    }

    /**
     * 禁用应用调用应用静默安装/卸载接口
     * 0：不允许所有应用调用应用静默安装/卸载接口；
     * 1：仅允许接口授权列表内的终端应用调用应用静默安装/卸载接口，接口授权列表包含应用包名和应用签名证书指纹值；
     * 2：允许所有应用调用应用静默安装/卸载接口
     * @param mode
     * @param appList
     * @return
     */
    public boolean setSilentInstallUninstallPolicies(int mode, String[] appList) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setSilentInstallUninstallPolicies(mode, appList);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setSilentInstallUninstallPolicies result:"+result);
        }
        return result;
    }

    /**
     * 是否禁用应用调用应用静默安装/卸载接口
     * string[0]：功能模式，参见 setInstallUninstallPolicies 方法的 mode 参数。
     * string[1]至 string[n-1]：仅当 mode=1 时返回允许交互式安装/卸载的特定 APP 信息，参见
     * setInstallUninstallPolicies 方法的 appList 参
     * @return
     */
    public String[] getSilentInstallUninstallPolicies() {
        String[] result = null;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getSilentInstallUninstallPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getSilentInstallUninstallPolicies result:"+result);
        }
        return result;
    }

    /**
     * 禁用ADB方式安装/卸载应用
     * 0：允许使用 adb 方式安装/卸载终端应用；
     * 1：不允许使用 adb 方式安装/卸载终端应用
     * @param mode
     * @return
     */
    public boolean setAdbInstallUninstallPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setAdbInstallUninstallPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setAdbInstallUninstallPolicies result:"+result);
        }
        return result;
    }

    public int getAdbInstallUninstallPolicies() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getAdbInstallUninstallPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getAdbInstallUninstallPolicies result:"+result);
        }
        return result;
    }

    public boolean installPackage(String pathToAPK) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.installPackage(pathToAPK);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "install silent result:"+result);
        }
        return result;
    }

    public boolean uninstallPackage(String appPackageName) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.uninstallPackage(appPackageName);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "uninstall silent result:"+result);
        }
        return result;
    }

 
    public boolean setAppInstallationPolicies(int mode, String[] appPackageNames) {
        boolean result = false;
        try {
            result = getICoreService().setAppInstallationPolicies(mode,appPackageNames);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setAppInstallationPolicies result:"+result);
        }
        return result;
    }

    public String[] getAppInstallationPolicies() {
        boolean result = false;
        String[] polices = unknownStringArray;
        try {
            polices = getICoreService().getAppInstallationPolicies();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "getAppInstallationPolicies result:"+result);
        }
        return polices;
    }

    public boolean setAppUninstallationPolicies(int mode, String[] appPackageNames) {
        boolean result = false;
        try {
            result = getICoreService().setAppUninstallationPolicies(mode,appPackageNames);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setAppUninstallationPolicies result:"+result);
        }
        return result;
    }

    public String[] getAppUninstallationPolicies() {
        boolean result = false;
        String[] polices = unknownStringArray;
        try {
            polices = getICoreService().getAppUninstallationPolicies();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "getAppUninstallationPolicies result:"+result);
        }
        return polices;
    }


    public boolean setRunAppPolicies(int mode, String[] appPackageNameList) {
        boolean result = false;
        try {
            result = getICoreService().setRunAppPolicies(mode,appPackageNameList);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setRunAppPolicies result:"+result);
        }
        return result;
    }

    public String[] getRunAppPolicies() {
        boolean result = false;
        String[] polices = unknownStringArray;

        try {
            polices = getICoreService().getRunAppPolicies();
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
           // for( int i=0;i < polices.length;i++){
           //     Log.d(TAG, "string["+i+"]"+polices[i]+"\n");
           // }
            Log.d(TAG, "getRunAppPolicies result:"+result+"polices.length ="+polices.length);
        }
        return polices;
    }

    public boolean setAppPermission(String appPackageName, String permissions) {
        boolean result = false;
        try {
            result = getICoreService().setAppPermission(appPackageName,permissions);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setAppPermission result:"+result);
        }
        return result;
    }

    public String getAppPermission(String appPackageName) {
        String result = null;
        try {
            result = getICoreService().getAppPermission(appPackageName);
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getAppPermission result:"+result);
        }
        return result;
    }

    public String[] getAppTrafficInfo(String appPackageName) {
        boolean result = false;
        String[] polices = unknownStringArray;
        try {
            polices = getICoreService().getAppTrafficInfo( appPackageName );
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
           // for( int i=0;i < polices.length;i++){
           //     Log.d(TAG, "string["+i+"]"+polices[i]+"\n");
           // }
            Log.d(TAG, "getAppTrafficInfo = "+result+",polices.length="+polices.length);
        }
        return polices;
    }

    public List getAppPowerUsage() {
        List result = null;
        try {
            result = getICoreService().getAppPowerUsage();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getAppPowerUsage result:"+result);
        }
        return result;
    }

    public List getAppRunInfo() {
        List result = null;
        try {
            result = getICoreService().getAppRunInfo();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
           // for(int j =0;j<result.size();j++){
           //     String[] item = (String[])result.get(j);
           //     for( int i=0;i < item.length;i++){
           //         Log.d(TAG, "string["+i+"]"+item[i]+"\n");
           //     }
           // }
            Log.d(TAG, "getAppRunInfo result:"+result);
        }
        return result;
    }

    public List getAppRuntimeExceptionInfo() {
        List result = null;
        try {
            result = getICoreService().getAppRuntimeExceptionInfo();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getAppRuntimeExceptionInfo result:"+result);
        }
        return result;
    }

    public String[] getDeviceInfo() {
        String[] result = null;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getDeviceInfo();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getDeviceInfo");
        }
        return result;
    }

    public String[][] getSoftwareInfo() {

        String[][] softInfo = null;

        List<String[]> infolist = null;
        try {
            infolist = getICoreService().getSoftwareInfo();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if( infolist!=null && infolist.size()>0 ){
            softInfo = new String[infolist.size()][7];
            for(int i=0;i<infolist.size();i++){
                String[] info = infolist.get(i);
                for(int j =0;j<7;j++){
                    softInfo[i][j] = info[j];
                }
            }
        }
        return softInfo;
    }


    /**
     * REQ078, getRootState, wmd, 2021.0611
     * @return
     */
    public boolean getRootState() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getRootState();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "getRootState result:"+result);
        }
        return result;
    }

    public boolean getSystemIntegrity() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getSystemIntegrity();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "getSystemIntegrity result:"+result);
        }
        return result;
    }

    /**
     * 获取终端运行状态信息
     * input:
     * 无
     * output:
     * String[]：返回终端运行状态信息，以下为每个 index 所包含的值：
     * string[0]：CPU 占用率
     * string[1]：内存占用率
     * string[2]：存储占用率
     * @return
     */
    public String[] getDeviceState() {
        String[] result = null;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getDeviceState();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getDeviceState result:"+result);
        }
        return result;
    }

    /**
     * 获取终端可信检测结果
     * @return
     */
    public String getTpmReport() {
        String result = unknownString;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getTpmReport();
        } catch (RemoteException re){
            re.printStackTrace();
            result = unknownString;
        } catch (Exception e){
            e.printStackTrace();
            result = unknownString;
        }
        if(DEBUG) {
            Log.d(TAG, "getTpmReport result:"+result);
        }
        return result;
    }

    /**
     * 设置系统使用模式切换权限
     * @param mode
     * 系统使用模式切换管控开关
     * 0：不允许用户切换系统使用模式；
     * 1：允许用户切换系统使用模式
     * @return
     */
    public boolean setContainerPolicies(int mode) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setContainerPolicies(mode);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setContainerPolicies result:"+result);
        }
        return result;
    }

    public int getContainerPolicies() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getContainerPolicies();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getContainerPolicies result:"+result);
        }
        return result;
    }

    public int getContainerTotalNumber() {
        int result = 1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getContainerTotalNumber();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 1;
        } catch (Exception e){
            e.printStackTrace();
            result = 1;
        }
        if(DEBUG) {
            Log.d(TAG, "getContainerTotalNumber result:"+result);
        }
        return result;
    }

    public boolean setContainerNumber(int containerNumber) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setContainerNumber(containerNumber);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setContainerNumber result:"+result);
        }
        return result;
    }

    public int getContainerNumber() {
        int result = 0;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getContainerNumber();
        } catch (RemoteException re){
            re.printStackTrace();
            result = 0;
        } catch (Exception e){
            e.printStackTrace();
            result = 0;
        }
        if(DEBUG) {
            Log.d(TAG, "getContainerNumber result:"+result);
        }
        return result;
    }

    public boolean isActived() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.isActived();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "isActived result:"+result);
        }
        return result;
    }

    public boolean lockDevice() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.lockDevice();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "lockDevice result:"+result);
        }
        return result;
    }

    public boolean unlockDevice() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.unlockDevice();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "unlockDevice result:"+result);
        }
        return result;
    }

    public boolean wipeDeviceData() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.wipeDeviceData();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "wipeDeviceData result:"+result);
        }
        return result;
    }

    public boolean rebootDevice() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.rebootDevice();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "rebootDevice result:"+result);
        }
        return result;
    }

    public boolean shutdownDevice() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.shutdownDevice();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "shutdownDevice result:"+result);
        }
        return result;
    }

    public String getDevicePosition() {
        String result = unknownString;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getDevicePosition();
        } catch (RemoteException re){
            re.printStackTrace();
            result = unknownString;
        } catch (Exception e){
            e.printStackTrace();
            result = unknownString;
        }
        if(DEBUG) {
            Log.d(TAG, "getDevicePosition result:"+result);
        }
        return result;
    }

    public boolean setWlanConfiguration(String wlanConfig) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setWlanConfiguration(wlanConfig);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setWlanConfiguration result:"+result);
        }
        return result;
    }

    public String getWlanConfiguration() {
        String result = unknownString;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getWlanConfiguration();
        } catch (RemoteException re){
            re.printStackTrace();
            result = unknownString;
        } catch (Exception e){
            e.printStackTrace();
            result = unknownString;
        }
        if(DEBUG) {
            Log.d(TAG, "getWlanConfiguration result:"+result);
        }
        return result;
    }

    public int createApn(String apnInfo) {
        int result = -1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.createApn(apnInfo);
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "createApn result:"+result);
        }
        return result;
    }

    public boolean deleteApn(int apnId) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.deleteApn(apnId);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "deleteApn result:"+result+" apnId:"+apnId);
        }
        return result;
    }

    public List getApnList() {
        List result = null;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getApnList();
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getApnList result:"+result);
        }
        return result;
    }

    public String getApnInfo(int apnId) {
        String result = unknownString;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getApnInfo(apnId);
        } catch (RemoteException re){
            re.printStackTrace();
            result = null;
        } catch (Exception e){
            e.printStackTrace();
            result = null;
        }
        if(DEBUG) {
            Log.d(TAG, "getApnInfo result:"+result+" apnId:"+apnId);
        }
        return result;
    }

    public int getCurrentApn() {
        int result = -1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.getCurrentApn();
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "getCurrentApn result:"+result);
        }
        return result;
    }

    public boolean setCurrentApn(int apnId) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setCurrentApn(apnId);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setCurrentApn result:"+result+" apnId:"+apnId);
        }
        return result;
    }

    public boolean setSysTime(long millis) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setSysTime(millis);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setSysTime result:"+result);
        }
        return result;
    }

    public boolean setLockPassword(String pwd) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setLockPassword(pwd);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "setLockPassword result:"+result);
        }
        return result;
    }

    public boolean isJwBuild() {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.isJwBuild();
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "isJwBuild result:"+result);
        }
        return result;
    }

    public void addPersistentApp(String pkgName, boolean isPersist) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            mICoreService.addPersistentApp(pkgName, isPersist);
            result = true;
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "addPersistentApp result:"+result);
        }
        return;
    }

    public int setPassword(String pwd) {
        int result = -1;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.setPassword(pwd);
        } catch (RemoteException re){
            re.printStackTrace();
            result = -1;
        } catch (Exception e){
            e.printStackTrace();
            result = -1;
        }
        if(DEBUG) {
            Log.d(TAG, "setPassword result:"+result);
        }
        return result;
    }

    public boolean resetPassword(String password) {
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            result = mICoreService.resetPassword(password);
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        if(DEBUG) {
            Log.d(TAG, "resetPassword result:"+result);
        }
        return result;
    }

    private static class HOLDER {
        private static PolicyManager instance = new PolicyManager();

        private HOLDER() {
        }
    }
	

    /* ############################################################################################# */
    /*                                           Common                                              */
    /* ############################################################################################# */

    private static ICoreService getServiceInterface() {
        /* get a handle to NFC service */
        IBinder b = null;

        //b = ServiceManager.getService(SERVICE_NAME);

        try {
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", new Class[]{String.class});
            b = (IBinder) getServiceMethod.invoke(null, new Object[]{SERVICE_NAME});
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
        if(DEBUG) {
            Log.d(TAG, "getServiceInterface b:" + b);
        }

        if (b == null) {
            return null;
        }
        return ICoreService.Stub.asInterface(b);
    }

    public boolean registerSettingsContentObserver(int type, ISettingsContentObserver observer){
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            if(DEBUG) {
                Log.d(TAG, "registerSettingsContentObserver #S mICoreService:" + mICoreService);
            }
            result = mICoreService.registerSettingsContentObserver(type, observer);
            if(DEBUG) {
                Log.d(TAG, "registerSettingsContentObserver #E mICoreService:" + mICoreService);
            }
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean unregisterSettingsContentObserver(ISettingsContentObserver observer){
        boolean result = false;
        try {
            if (mICoreService == null) {
                mICoreService = getServiceInterface();
            }
            if(DEBUG) {
                Log.d(TAG, "unregisterSettingsContentObserver #S mICoreService:" + mICoreService);
            }
            result = mICoreService.unregisterSettingsContentObserver(observer);
            if(DEBUG) {
                Log.d(TAG, "unregisterSettingsContentObserver #E mICoreService:" + mICoreService);
            }
        } catch (RemoteException re){
            re.printStackTrace();
            result = false;
        } catch (Exception e){
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /* ############################################################################################# */
    /*                                           Common                                              */
    /* ############################################################################################# */

    public static final int REMOTE_CALLBACK_TYPE_TEMP = 9001;	
}