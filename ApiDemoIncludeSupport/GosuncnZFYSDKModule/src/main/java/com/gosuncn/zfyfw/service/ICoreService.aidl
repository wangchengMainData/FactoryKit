package com.gosuncn.zfyfw.service;

import com.gosuncn.zfyfw.service.ISettingsContentObserver;

interface ICoreService {

    // settings
    boolean registerSettingsContentObserver(int type, ISettingsContentObserver observer);
    boolean unregisterSettingsContentObserver(ISettingsContentObserver observer);

    // led
    // com.gosuncn.zfyfw.api.LedManager.java

/*    String getWifiMacAddress();
    String getBluetoothMacAddress();
    String getICCIDNumber();
    String getIMEINumber();
    String getSoftwareCustomVersion();*/

    String[] listIccid();

    String[] listImei();

    String getCryptoModuleInfo();

    String[] listCertificates();

    int establishVpnConnection();

    int disestablishVpnConnection();

    int getVpnServiceState();

    boolean setSoundRecorderBtn(int mode);

    int getSoundRecorderBtn();

    boolean setVideoRecorderBtn(int mode);

    int getVideoRecorderBtn();

    boolean setPictureBtn(int mode);

    int getPictureBtn();

    boolean setSosBtn(int mode);

    int getSosBtn();

    boolean setPttBtn(int mode);

    int getPttBtn();

    String getMonitorInfo();

    boolean setWlanPolicies(int mode);

    int getWlanPolicies();

    boolean setDataConnectivityPolicies(int mode);

    int getDataConnectivityPolicies();

    boolean setBluetoothPolicies(int mode, in String[] bluetoothInfoList);

    String[] getBluetoothPolicies();

    boolean setNfcPolicies(int mode);

    int getNfcPolicies();

    boolean setIrPolicies(int mode);

    int getIrPolicies();

    boolean setBiometricRecognitionPolicies(int mode);

    int getBiometricRecognitionPolicies();

    boolean setGpsPolicies(int mode);

    int getGpsPolicies();

    boolean setUsbDataPolicies(int mode);

    int getUsbDataPolicies();

    boolean setExternalStoragePolicies(int mode);

    int getExternalStoragePolicies();

    boolean setMicrophonePolicies(int mode);

    int getMicrophonePolicies();

    boolean setSpeakerPolicies(int mode);

    int getSpeakerPolicies();

    boolean setCameraPolicies(int mode);

    int getCameraPolicies();

    boolean setFlashPolicies(int mode);

    int getFlashPolicies();

    boolean setPeripheralPolicies(int mode);

    int getPeripheralPolicies();

    boolean setVoicePolicies(int mode);

    int getVoicePolicies();

    boolean setSmsPolicies(int mode, String regExp);

    int getSmsPolicies();

    boolean setCaptureScreenPolicies(int mode);

    int getCaptureScreenPolicies();

    boolean setWlanApPolicies(int mode, in String[] macInfoList);

    String[] getWlanApPolicies();

    boolean setUserApnMgrPolicies(int mode);

    int getUserApnMgrPolicies();

    String executeShellToSetIptables(String commandline);

    boolean setUserPasswordPolicies(int mode);

    int getUserPasswordPolicies();

    boolean setUserTimeMgrPolicies(int mode);

    int getUserTimeMgrPolicies();

    boolean setFactoryResetPolicies(int mode);

    int getFactoryResetPolicies();

    boolean setDevelopmentModePolicies(int mode);

    int getDevelopmentModePolicies();

    boolean setSystemUpdatePolicies(int mode);

    int getSystemUpdatePolicies();

    boolean setInstallUninstallPolicies(int mode, in String[] appList);

    String[] getInstallUninstallPolicies();

    boolean setSilentInstallUninstallPolicies(int mode, in String[] appList);

    String[] getSilentInstallUninstallPolicies();

    boolean setAdbInstallUninstallPolicies(int mode);

    int getAdbInstallUninstallPolicies();

    boolean installPackage(String pathToAPK);

    boolean uninstallPackage(String appPackageName);

    boolean setAppInstallationPolicies(int mode, in String[] appPackageNames);

    String[] getAppInstallationPolicies();

    boolean setAppUninstallationPolicies(int mode, in String[] appPackageNames);

    String[] getAppUninstallationPolicies();

    boolean setRunAppPolicies(int mode, in String[] appPackageNameList);

    String[] getRunAppPolicies();

    boolean setAppPermission(String appPackageName, String permissions);

    String getAppPermission(String appPackageName);

    String[] getAppTrafficInfo(String appPackageName);

    List getAppPowerUsage();

    List getAppRunInfo();

    List getAppRuntimeExceptionInfo();

    String[] getDeviceInfo();

    List getSoftwareInfo();

    boolean getRootState();

    boolean getSystemIntegrity();

    String[] getDeviceState();

    String getTpmReport();

    boolean setContainerPolicies(int mode);

    int getContainerPolicies();

    int getContainerTotalNumber();

    boolean setContainerNumber(int containerNumber);

    int getContainerNumber();

    boolean isActived();

    boolean lockDevice();

    boolean unlockDevice();

    boolean wipeDeviceData();

    boolean rebootDevice();

    boolean shutdownDevice();

    String getDevicePosition();

    boolean setWlanConfiguration(String wlanConfig);

    String getWlanConfiguration();

    int createApn(String apnInfo);

    boolean deleteApn(int apnId);

    List getApnList();

    String getApnInfo(int apnId);

    int getCurrentApn();

    boolean setCurrentApn(int apnId);

    boolean setSysTime(long millis);

    boolean setLockPassword(String pwd);

    boolean isJwBuild();

    void addPersistentApp(String pkgName, boolean isPersist);

    int setPassword(String pwd);

    boolean resetPassword(String password);
    void setNavigationBarDisabled(boolean disabled);
    
    boolean isNavigationBarDisabled();

    void setStatusBarExpandPanelDisabled(boolean disabled);

    boolean isStatusBarExpandPanelDisabled();
        
}
