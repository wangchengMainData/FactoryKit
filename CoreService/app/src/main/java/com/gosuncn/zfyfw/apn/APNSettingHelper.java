package com.gosuncn.zfyfw.apn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gosuncn.zfyfw.MainApp;

import java.util.List;

/**
 * @author Linzy
 * @date 2020-05-25
 * Copyright © 2019 Gosuncn. All rights reserved.
 */
public class APNSettingHelper {


    private static APNSettingHelper instance;

    private APNSettingHelper(){}

    public static synchronized APNSettingHelper getInstance(){

        if (instance == null) {
            instance = new APNSettingHelper();
        }
        return instance;
    }

// 更新或删除某个卡的某一apn
    private Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");

    private Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    /**
     * 设置APNS
     * 把所有APNS更新到数据库
     * @param apnModels
     */
    public GSResult<APNModel> setAPNs(List<APNModel> apnModels){
        String SPName = getSIMSPName();

        String errorMessage = "";

        APNModel selectAPNModel = null;

        for (APNModel apnModel : apnModels) {
            int apnId = insertOrUpdate(apnModel);
            if (apnId > 0){
                apnModel.setApnId(apnId);

                if (apnModel.getSp().equalsIgnoreCase(SPName)){
                    selectAPNModel = apnModel;
                    setPreferredAPN(apnId);
                }

            }else{
                errorMessage = errorMessage + (apnModel.getSp() + "设置失败\n");
            }
        }


        if (selectAPNModel == null){
            for (APNModel apnModel : apnModels) {
                if (apnModel.getApnId() > 0){
                    selectAPNModel = apnModel;
                    setPreferredAPN(apnModel.getApnId());
                    break;
                }
            }
        }


        if (selectAPNModel != null){
            return new GSResult<>(selectAPNModel);
        }

        return new GSResult<>(-1,errorMessage);

    }

    public String getSIMSPName() {
        TelephonyManager iPhoneManager = (TelephonyManager) getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (iPhoneManager == null) {
            return  null;
        }
        return iPhoneManager.getNetworkOperatorName();
    }

    /**
     * 如果不存在,则插入
     * 如果存在 则更新
     * @param apnModel
     * @return
     */
    public int insertOrUpdate(APNModel apnModel){
        int id = -1;
        boolean existing = false;
        Cursor parser = getContext().getContentResolver().query(APN_TABLE_URI, null, null, null, null);
        if (parser == null){
            return -1;
        }
        parser.moveToLast();
        while (!parser.isBeforeFirst()){
            int index = parser.getColumnIndex("name");
            String name = parser.getString(index);
            if (name.equals(apnModel.getSp())){
                existing = true;
                index = parser.getColumnIndex("_id");
                id = parser.getInt(index);
                Log.i("Linzy", "APN id" + id + "已设置");
            }
            parser.moveToPrevious();
        }
        ContentValues values = new ContentValues();
        String[] t =  apnModel.getHost().split(":");
        String host = t[0];
        int port = -1;
        if (t.length == 2){
            try {
                port = Integer.parseInt(t[1]);
            }catch (Exception e){

            }

        }

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

        if (!existing){
            id = insert(values);
        }else{
            id = update(values,id);
        }
        return id;
    }


    private int insert(ContentValues values){
        ContentResolver resolver =    getContext().getContentResolver();
        int id = -1;
        Cursor c = null;
        try{
            Uri newRow = resolver.insert(APN_TABLE_URI, values);

            if(newRow != null){
                c = resolver.query(newRow, null, null, null, null);
                int idindex = c.getColumnIndex("_id");
                c.moveToFirst();
                id = c.getShort(idindex);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        if(c !=null ) {
            c.close();
        }
        Log.i("Linzy","insert id " + id);
        return id;
    }

    private int update(ContentValues values,int id){
        ContentResolver resolver =    getContext().getContentResolver();
        int resulut = 0;
        try{
            resulut =  resolver.update(APN_TABLE_URI,values,"_id = "+ id, null);
        } catch(SQLException e){
            e.printStackTrace();
        }
        Log.i("Linzy","update id " + id  + "resulut " + resulut);
        if (resulut > 0){
            return id;
        }else{
            return -1;
        }
    }

    public void showAllColumne(){
        Cursor apu = getContext().getContentResolver().query(CURRENT_APN_URI, null, null, null, null);
        apu.moveToFirst();
        int index;
        Log.i("Linzy","getColumnCount " + apu.getColumnCount());
        for (String columnName : apu.getColumnNames()) {
            Log.i("Linzy","ColumnIndex: " + apu.getColumnIndex(columnName) + " getColumnName: " + columnName );
        }
    }

    public void  readAll(List<APNModel> apnmodes ){
        Cursor parser = getContext().getContentResolver().query(APN_TABLE_URI, null, null, null, null);
        if (parser == null){
            return ;
        }

        parser.moveToLast();
        while (!parser.isBeforeFirst()){

            APNModel apnModel = new APNModel();

            int index = parser.getColumnIndex("name");

            String name = parser.getString(index);
            Log.i("Linzy","==========" + name + "==========");
            for (String columnName : parser.getColumnNames()) {
/*                if (columnName.equals("_id")&&parser.getString(parser.getColumnIndex(columnName)).equals("3193")) {
                    Log.i("Linzy","columnName: "+ columnName + " value: " + parser.getString(parser.getColumnIndex(columnName)));
                }
                if (columnName.equals("bearer")) {
                    Log.i("Linzy","columnName: "+ columnName + " value: " + parser.getString(parser.getColumnIndex(columnName)));
                }*/
                if( columnName.equals("_id") ){
                    apnModel.setApnId( parser.getInt(parser.getColumnIndex(columnName) ));
                }

                if( columnName.equals("name") ){
                    apnModel.setSp( parser.getString(parser.getColumnIndex(columnName) ));
                }

                if( columnName.equals("apn") ){
                    apnModel.setHost( parser.getString(parser.getColumnIndex(columnName) ));
                }
            }
            parser.moveToPrevious();

            apnmodes.add( apnModel );
        }
    }


    public boolean setPreferredAPN(int id){
        if (id == -1){
            return false;
        }
        boolean res = false;
        ContentResolver resolver = getContext().getContentResolver();
        ContentValues values = new ContentValues();

        values.put("apn_id", id);
        try{
            resolver.update(CURRENT_APN_URI, values, null, null);
            Cursor c = resolver.query(CURRENT_APN_URI, new String[]{"name", "apn"}, "_id="+id, null, null);
            if(c != null){
                res = true;
                c.close();
            }
        }
        catch (SQLException e){

            return false;
        }
        return res;
    }

    public String getPreferredAPNName(){
        String name = null;
        ContentResolver resolver = getContext().getContentResolver();
        try {
            Cursor cursor = resolver.query(CURRENT_APN_URI, null, null, null, null);
            if(cursor != null){
                if (cursor.moveToFirst()){
                   name =  cursor.getString(cursor.getColumnIndex("name"));
                }
                cursor.close();
            }
        } catch (SQLException e){}
        return name;
    }


    public String getAPNInfo(){
        String info = null;
        ContentResolver resolver = getContext().getContentResolver();
        try {
            Cursor cursor = resolver.query(CURRENT_APN_URI, null, null, null, null);
            if(cursor != null){
                if (cursor.moveToFirst()){
                    info = cursor.getString(cursor.getColumnIndex("_id"))+","+ cursor.getString(cursor.getColumnIndex("name"))
                            + ","+ cursor.getString(cursor.getColumnIndex("apn")) ;
                }
                cursor.close();
            }
        } catch (SQLException e){}
        return info;
    }




    protected String getSysProtocolVal(String protocol) {
        String result = "";
        if (protocol!=null) {
            String ucRoam = protocol.toUpperCase();
            if ("IPV4".equals(ucRoam)) {
                result = "IP";
            }else if("IPV6".equals(ucRoam)){
                result = ucRoam;
            }else {
                result = "IPV4V6";
            }
        }
        return result;
    }

    protected int getSysBearerVal(String bearerStr) {
        int bearer = 0;
        if (bearerStr != null){
            if (bearerStr.equalsIgnoreCase("LTE")){
                bearer = 14;
            }else if (bearerStr.equalsIgnoreCase("HSPAP")){
                bearer = 15;
            }else if (bearerStr.equalsIgnoreCase("HSPA")){
                bearer = 11;
            }else if (bearerStr.equalsIgnoreCase("HSUPA")){
                bearer = 10;
            }else if (bearerStr.equalsIgnoreCase("HSDPA")){
                bearer = 9;
            }else if (bearerStr.equalsIgnoreCase("UMTS")){
                bearer = 3;
            }else if (bearerStr.equalsIgnoreCase("EDGE")){
                bearer = 2;
            }else if (bearerStr.equalsIgnoreCase("GPRS")){//1
                bearer = 1;
            }else if (bearerStr.equalsIgnoreCase("eHRPD")){
                bearer = 13;
            }else if (bearerStr.equalsIgnoreCase("EVDO_B")){
                bearer = 12;
            }else if (bearerStr.equalsIgnoreCase("EVDO_A")){
                bearer = 8;
            }else if (bearerStr.equalsIgnoreCase("EVDO_0")){
                bearer = 7;
            }else if (bearerStr.equalsIgnoreCase("1xRTT")){
                bearer = 6;
            }else if (bearerStr.equalsIgnoreCase("IS95B")){
                bearer = 5;
            }else if (bearerStr.equalsIgnoreCase("IS95A")){
                bearer = 4;
            }
        }
        return bearer;
    }
    private Context getContext(){
        return MainApp.mContext;
    }









}
