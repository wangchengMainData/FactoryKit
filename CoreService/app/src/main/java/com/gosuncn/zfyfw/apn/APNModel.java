package com.gosuncn.zfyfw.apn;

/**
 * @author: Administrator
 * @date: 2020/7/24
 */
public class APNModel {


    /*
    *
        values.put("name", apnModel.getSp());
        values.put("apn", apnModel.getApn());
        if (port > 0){
            values.put("port", port);
        }

        values.put("user", apnModel.getUser());
        values.put("password", apnModel.getPwd());
        values.put("server", host);
        values.put("proxy",apnModel.getProxy());

        int authtype = 0;
        if (apnModel.getAuth() != null){
            if ("CHAP".equalsIgnoreCase(apnModel.getAuth())){
                authtype = 2;
            }else if ("PAP".equalsIgnoreCase(apnModel.getAuth())){
                authtype = 1;
            }else if ("CHAPPAP".equalsIgnoreCase(apnModel.getAuth())){
                authtype = 3;
            }else{
                try {
                    authtype = Integer.parseInt(apnModel.getAuth());
                }catch (Exception e){
                }
            }
        }
        values.put("authtype",authtype);

        String mcc = apnModel.getMcc();
        String mnc = apnModel.getMnc();
        if (mcc == null || mcc.isEmpty()){
            mcc = "460";
        }
        if ( mnc == null || mnc.isEmpty()){
            switch (apnModel.getSp()){
                case "CMCC":
                    mnc = "00";
                    break;
                case "CTCC":
                    mnc = "01";
                    break;
                case "CUCC":
                    mnc = "03";
                    break;
                default:
                    mnc = "20";
                    break;
            }
        }

        values.put("mcc",mcc);
        values.put("mnc",mnc);

        values.put("type",apnModel.getType());

        values.put("protocol", getSysProtocolVal(apnModel.getProt()));
        values.put("roaming_protocol", getSysProtocolVal(apnModel.getRoam()));
        values.put("bearer",getSysBearerVal(apnModel.getBearer()));
    *
    * */
    private int apnId;
    private String sp;
    private String user;
    private String pwd;
    private String proxy;
    private String auth;
    private String mcc;
    private String mnc;
    private String port;
    private String roam;
    private String bearer;
    private String host;
    private String prot;
    private String type;
    private String apn;

    public int getApnId() {
        return apnId;
    }

    public void setApnId(int apnId) {
        this.apnId = apnId;
    }

    public String getSp() {
        return sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRoam() {
        return roam;
    }

    public void setRoam(String roam) {
        this.roam = roam;
    }

    public String getBearer() {
        return bearer;
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProt() {
        return prot;
    }

    public void setProt(String prot) {
        this.prot = prot;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }
}
