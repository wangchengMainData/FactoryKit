package com.gosuncn.bean;

public class CryptoModuleInfo {
    public String result;
    public String moduleType;
    public String manufacture;
    public String moduleId;

    public void setResult(String result){
        this.result = result;
    }
    public String getResult(){
        return this.result;
    }
    public void setModuleType(String moduleType){
        this.moduleType = moduleType;
    }
    public String getModuleType(){
        return this.moduleType;
    }
    public void setManufacture(String manufacture){
        this.manufacture = manufacture;
    }
    public String getManufacture(){
        return this.manufacture;
    }
    public void setModuleId(String moduleId){
        this.moduleId = moduleId;
    }
    public String getModuleId(){
        return this.moduleId;
    }

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("result:").append(result).append("\n")
                .append("moduleType:").append(moduleType).append("\n")
                .append("manufacture:").append(manufacture).append("\n")
                .append("moduleId:").append(moduleId).append("\n");
        return sb.toString();
    }



}
