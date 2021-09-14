package com.gosuncn.zfyfw.nv;

import android.text.TextUtils;
import android.util.Log;

public class NvUtilExt {
    private static final String TAG = NvUtilExt.class.getSimpleName();

    private static NvUtilExt mInstance = null;

    public NvUtilExt() {
    }

    private native String nativeNvRead(int index);

    private native void nativeNvWrite(int index, String data);

    public static NvUtilExt getInstance() {
        if (mInstance == null) {
            mInstance = new NvUtilExt();
        }

        return mInstance;
    }

    public String readNv(int index) {
        return this.nativeNvRead(index);
    }

    public void writeNv(int index, String data) {
        this.nativeNvWrite(index, data);
    }

    public String getGNSSConfig(){
        return readNv(70326/*70326*/);
    }

    public void setGNSSConfig(String data){
        writeNv(70326, data);
    }

    public String getBTNv(){
        return readNv(447);
    }

    public void setBTNv(String data){
        writeNv(447, data);
    } // read only

    public String getWLANNv(){
        return readNv(4678);
    }

    public String getSNNv(){
        return readNv(0);
    }

    public String getIMEINv(){
        return readNv(550);//457);
    } // parameter bad

    public String getFactoryNv2497(){
        return readNv(2497);
    }
    public String getFactoryNv2498(){
        return readNv(2498);
    }
    public String getFactoryNv2499(){
        return readNv(2499);
    }
    public String getFactoryNv2500(){
        return readNv(2500);
    }

    public void clearFactoryNv2499(){
        writeNv(2499, "UUUUUUU");
    }

    /**
     * Bug326, set factory test result flag, wmd, 2020.0706
     * @param result U P F (untested, pass, fail)
     */
    public void setFactoryNv2499MMIBit(String result){
        String nv2499 = getFactoryNv2499();
        Log.d(TAG, "setFactoryNv2499MMIBit nv2499:"+nv2499);
        if(!TextUtils.isEmpty(nv2499) && nv2499.length() > 3) {
            StringBuilder sb = new StringBuilder(nv2499);
            sb.replace(3, 4, result);
            Log.d(TAG, "setFactoryNv2499MMIBit nv2499 e:" + sb.toString());
            writeNv(2499, sb.toString());
        }
    }

    static {
        System.loadLibrary("nvgxx_jni");
    }

}