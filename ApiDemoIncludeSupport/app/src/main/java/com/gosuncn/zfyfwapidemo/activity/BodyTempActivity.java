package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;
import com.gosuncn.zfyhwapidemo.R;

import java.util.List;

public class BodyTempActivity extends Activity {
    private static final String TAG = BodyTempActivity.class.getSimpleName();

    TextView mTextView;
    Button mTextBtn;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String result = (String)msg.obj;
                mTextView.setText(result);
                Toast.makeText(BodyTempActivity.this, result, Toast.LENGTH_SHORT).show();
            }
        };
        setContentView(R.layout.body_temp_activity);

        mTextView = (TextView)findViewById(R.id.bodytemp_content);

        mTextBtn = (Button)findViewById(R.id.bodytemp_test_btn);
        mTextBtn.setText("REQUEST BODYTEMP");
        mTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "bodytemp click");
                GSFWManager.getInstance().requestBodytemp();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSFWManager.getInstance().registerBodyTempCallback(mISettingsContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSFWManager.getInstance().unresigterBodyTempCallback(mISettingsContentObserver);
    }

    private ISettingsContentObserver.Stub mISettingsContentObserver = new ISettingsContentObserver.Stub() {
        @Override
        public void onchanged(int type, int value, List<String> valueList) throws RemoteException {
            Log.d(TAG, "onchanged type:" + type + " value:" + value + " valueList:" + valueList.toString());
            if(valueList != null && valueList.size() > 0) {
                final String result = "BodyTemp: " + valueList.get(0);
                Message msg = mHandler.obtainMessage(1);
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        }
    };

}
