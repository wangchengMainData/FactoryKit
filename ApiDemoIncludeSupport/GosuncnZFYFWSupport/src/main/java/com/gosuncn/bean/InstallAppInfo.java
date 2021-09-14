package com.gosuncn.bean;

public class InstallAppInfo {

    private String AppPackageName;
    private String CertificateHash;
    public void setAppPackageName(String AppPackageName) {
        this.AppPackageName = AppPackageName;
    }
    public String getAppPackageName() {
        return AppPackageName;
    }

    public void setCertificateHash(String CertificateHash) {
        this.CertificateHash = CertificateHash;
    }
    public String getCertificateHash() {
        return CertificateHash;
    }

}