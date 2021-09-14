package com.gosuncn.utils;

import com.gosuncn.zfyfw.service.GSFWManager;

public class DeviceBaseFuncMgr {

    /**
     * 设置应用adb方式安装/卸载功能控制状态
     * @param mode 功能模式
     * 0：允许使用adb方式安装/卸载终端应用；
     * 1：不允许使用adb方式安装/卸载终端应用。
     * @return 成功返回true；失败返回false。
     */
    public static boolean setAdbInstallUninstallPolicies(int mode) {
        return GSFWManager.getInstance().setAdbInstallUninstallPolicies(mode);
    }

    /**
     * 获取当前adb方式安装/卸载功能管控状态
     * @return 返回值为当前adb方式安装/卸载功能管控状态，参见setAdbInstallUnistallPolicies方法的参数mode
     */
    public static int getAdbInstallUninstallPolicies() {
        return GSFWManager.getInstance().getAdbInstallUninstallPolicies();
    }

}
