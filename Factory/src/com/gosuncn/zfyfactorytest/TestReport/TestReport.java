package com.gosuncn.zfyfactorytest.TestReport;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.Framework.BaseActivity;
import com.gosuncn.zfyfactorytest.Framework.MainApp;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfw.service.GSFWManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestReport extends BaseActivity {

    private static final String TAG = TestReport.class.getSimpleName();
    boolean mAllTestPassed = true;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("NVflag", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        setContentView(loadDefaultConfirmText(""));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // loop mItemList
        List<String> sucList = new ArrayList<String>();
        List<String> failList = new ArrayList<String>();
        List<String> untestList = new ArrayList<String>();
        int sucLen = 0;
        int failLen = 0;

        List<Map<String, String>> list = (List<Map<String, String>>) MainApp
                .getInstance().mItemList;
//        Log.d(TAG, "onResume size:" + list.size());
//        Log.d(TAG, "onResume sucList:" + sucList.size());
//        Log.d(TAG, "onResume failList:" + failList.size());
//        Log.d(TAG, "onResume untestList:" + untestList.size());
        for (int i = 0; i < list.size(); i++) {
            Map<String, ?> item = list.get(i);

//            Log.d(TAG, "onResume packageName:" + item.get("packageName"));
            if("com.gosuncn.zfyfactorytest.TestReport".equals(item.get("packageName"))){
                continue;
            }

            String itemResult = Utils.getStringValueSaved(this.getApplicationContext(), (String)item.get("title"), "");
            if(Utils.RESULT_PASS.equals(itemResult)){
                sucList.add((String)item.get("title"));
            }else if(Utils.RESULT_FAIL.equals(itemResult)){
                failList.add((String)item.get("title"));
            }else{
                untestList.add((String)item.get("title"));
            }
        }
//        Log.d(TAG, "onResume sucList:" + sucList.size());
//        Log.d(TAG, "onResume failList:" + failList.size());
//        Log.d(TAG, "onResume untestList:" + untestList.size());
        if(failList.size() > 0 || untestList.size() > 0){
            mAllTestPassed = false;
//            GSFWManager.getInstance().setFactoryResultFlag("F");
        }else{
//            GSFWManager.getInstance().setFactoryResultFlag("P");
        }

        StringBuffer resultSB = new StringBuffer("");
        resultSB.append(getString(R.string.TestReport_title_pass));
        resultSB.append(" ");
        for (int i = 0; i < sucList.size(); i++) {
            resultSB.append(sucList.get(i));
            resultSB.append(";");
        }
        resultSB.append("\n");
        sucLen = resultSB.toString().length();

        resultSB.append(getString(R.string.TestReport_title_fail));
        resultSB.append(" ");
        for (int i = 0; i < failList.size(); i++) {
            resultSB.append(failList.get(i));
            resultSB.append(";");
        }
        resultSB.append("\n");
        failLen = resultSB.toString().length();

        resultSB.append(getString(R.string.TestReport_title_untest));
        resultSB.append(" ");
        for (int i = 0; i < untestList.size(); i++) {
            resultSB.append(untestList.get(i));
            resultSB.append(";");
        }
        resultSB.append("\n");

        SpannableString spannableString = new SpannableString(resultSB.toString());
        spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, sucLen - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), sucLen, failLen - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.YELLOW), failLen, resultSB.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        getConfirmText().setText(spannableString);

        hideNegativeButton();
        setPositiveButtonText(R.string.ok);

    }

    @Override
    protected void onPositiveCallback() {
        setResult(mAllTestPassed ? RESULT_OK : RESULT_CANCELED);
        Utils.writeCurMessage(this, TAG, mAllTestPassed ? "Pass" : "Failed");
        finish();
    }

    @Override
    protected void onNegativeCallback() {
        setResult(mAllTestPassed ? RESULT_OK : RESULT_CANCELED);
        Utils.writeCurMessage(this, TAG, mAllTestPassed ? "Pass" : "Failed");
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_factoryflag, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            if (sharedPreferences.getBoolean("constnvset", false)) {//if flag is true,setTitle first
                menu.getItem(0).setTitle(getResources().getString(R.string.factory_flag_u));
            }
        }catch (Exception e){e.printStackTrace();}
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nv_pass) {
            try {
                if (item.getTitle().equals(getResources().getString(R.string.factory_flag_p))) {//title = const set p
                    if (GSFWManager.getInstance().setFactoryResultFlag("P")) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (GSFWManager.getInstance().getFactoryResetFlag() != null &&
                                            GSFWManager.getInstance().getFactoryResetFlag().length() >= 4 &&
                                            GSFWManager.getInstance().getFactoryResetFlag().charAt(3) == 'P') {
                                        Log.e(TAG, "change to P success now commit preference");
                                        editor.putBoolean("constnvset", true);
                                        if (editor.commit()) {//commit success
                                            Log.e(TAG, "commit P_flag success now change UI");
                                            toast("P set OK");
                                            item.setTitle(R.string.factory_flag_u);
                                        } else {
                                            Log.e(TAG, "commit P_flag fail ");
                                        }
                                    } else {
                                        toast("P set fail ");
                                    }
                                }catch (Exception e){e.printStackTrace();}
                            }
                        });
                    } else {
                        toast("P set fail ");
                    }
                } else if (item.getTitle().equals(getResources().getString(R.string.factory_flag_u))) { //title = const set F
                    if (GSFWManager.getInstance().setFactoryResultFlag("F")) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    if (GSFWManager.getInstance().getFactoryResetFlag() != null &&
                                            GSFWManager.getInstance().getFactoryResetFlag().length() >= 4 &&
                                            GSFWManager.getInstance().getFactoryResetFlag().charAt(3) == 'F') {
                                        Log.e(TAG, "change to F success now commit preference");
                                        editor.putBoolean("constnvset", false);
                                        if (editor.commit()) {//commit success
                                            Log.e(TAG, "commit F_flag success now change UI");
                                            toast("F set OK");
                                            item.setTitle(R.string.factory_flag_p);
                                        } else {
                                            Log.e(TAG, "commit F_flag fail ");
                                        }
                                    } else {
                                        toast("F set fail ");
                                    }
                                }catch (Exception e){e.printStackTrace();}
                            }
                        });
                    } else {
                        toast("F set fail ");
                    }
                }
            }catch(Exception e){ e.printStackTrace();}
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toast(String msg){
        Toast.makeText(getBaseContext(),msg, Toast.LENGTH_SHORT).show();
    }
}

