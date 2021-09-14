package com.gosuncn.zfyfwapidemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gosuncn.zfyfw.apn.APNModel;
import com.gosuncn.zfyfw.apn.APNSettingHelper;
import com.gosuncn.zfyfw.R;

/**
 * @author: Administrator
 * @date: 2020/7/24
 */
public class ApnAddActivity extends Activity {

    EditText mApnEt,mHostEt,mPwdEt,mSpEt,mUserEt,mAuthEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_apn);
        findViews();
    }

    private void findViews(){

        mApnEt = (EditText) findViewById(R.id.apn_et);
        mHostEt = (EditText) findViewById(R.id.host_et);
        mPwdEt = (EditText) findViewById(R.id.pwd_et);
        mSpEt = (EditText) findViewById(R.id.sp_et);
        mUserEt = (EditText) findViewById(R.id.user_et);
        mAuthEt = (EditText) findViewById(R.id.auth_et);
    }

    public void back(View view){

        this.finish();

    }


    public void add(View view){

        APNModel apnModel = new APNModel();

        if(true) {
            if (mApnEt.getText().toString().isEmpty() || mHostEt.getText().toString().isEmpty()||
                    mSpEt.getText().toString().isEmpty()) {
                Toast.makeText(this, "存在空值" +
                        "Add default", Toast.LENGTH_LONG).show();
                apnModel.setHost("114.114.114.114");
                apnModel.setSp("CMCC");
                apnModel.setApn("cmnet");
                apnModel.setMcc("460");
                apnModel.setMnc("2");

            }else {
                apnModel.setApn(mApnEt.getText().toString());
                apnModel.setHost(mHostEt.getText().toString());
                apnModel.setPwd(""+mPwdEt.getText().toString());
                apnModel.setSp(""+mSpEt.getText().toString());
                apnModel.setUser(""+mUserEt.getText().toString());
                apnModel.setAuth(""+mAuthEt.getText().toString());
            }
        }else {
            apnModel.setHost("192.168.55.12:6222");
            apnModel.setPwd("admin");
            apnModel.setSp("CMCC");
            apnModel.setUser("admin");
            apnModel.setAuth("0");
            apnModel.setApn("admin");
        }

        APNSettingHelper.getInstance().insertOrUpdate( apnModel );
    }


}
