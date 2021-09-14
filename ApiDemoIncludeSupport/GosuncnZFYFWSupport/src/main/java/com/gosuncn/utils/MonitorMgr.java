package com.gosuncn.utils;

import com.gosuncn.zfyfw.service.GSFWManager;

public class MonitorMgr {

    /**
     * ROOT状态检测
     * @return 设备已ROOT返回true；设备未ROOT返回false
     */
    public static boolean getRootState() {
        return GSFWManager.getInstance().getRootState();
    }

    /**
     * 系统完整性检测
     * @return 统完整性未被破坏返回true；系统完整性被破坏返回false
     */
    public static boolean getSystemIntegrity() {
        return GSFWManager.getInstance().getSystemIntegrity();
    }
}
