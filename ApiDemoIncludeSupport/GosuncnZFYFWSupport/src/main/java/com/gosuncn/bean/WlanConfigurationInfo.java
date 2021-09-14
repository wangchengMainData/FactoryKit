package com.gosuncn.bean;

public class WlanConfigurationInfo {
    
    private String ssid;
    private String bssid;
    private String pwd;
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
    public String getSsid() {
        return ssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }
    public String getBssid() {
        return bssid;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    public String getPwd() {
        return pwd;
    }

}
