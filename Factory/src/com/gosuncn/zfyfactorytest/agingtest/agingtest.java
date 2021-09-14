package com.gosuncn.zfyfactorytest.agingtest;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.content.Intent;
import android.widget.TextView;
import com.gosuncn.zfyfactorytest.R;



public class agingtest extends Activity {
    TextView mlasttimeview;

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 3) {
            String result = data.getStringExtra("result");
            mlasttimeview.setText("上轮测试时长"+result);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences;
        preferences = getSharedPreferences("timefile", MODE_PRIVATE);
        final String lasttime = preferences.getString("lasttime", "还未测试");
        setContentView(R.layout.agingtest);
        final CheckBox agingtestLcdCb;
        final CheckBox agingtestSpkCb;
        final CheckBox agingtestVbrCb;
        final CheckBox agingtestInfCb;
        final CheckBox agingtestFlsCb;
        Button agingtestStartBt;
        agingtestLcdCb = findViewById(R.id.agingtest_lcd_cb);
        agingtestSpkCb = findViewById(R.id.agingtest_spk_cb);
        agingtestVbrCb = findViewById(R.id.agingtest_vbr_cb);
        agingtestInfCb = findViewById(R.id.agingtest_inf_cb);
        agingtestFlsCb = findViewById(R.id.agingtest_fls_cb);
        agingtestStartBt = findViewById(R.id.agingtest_start_bt);
        mlasttimeview = findViewById(R.id.lasttimer);
        mlasttimeview.setText("上轮测试时长" + lasttime);

        agingtestLcdCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
        agingtestSpkCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
        agingtestVbrCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
        agingtestInfCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
        agingtestFlsCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });


        agingtestStartBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean[] flag = {false, false, false, false, false};
                if (agingtestLcdCb.isChecked())
                    flag[0] = true;
                if (agingtestSpkCb.isChecked())
                    flag[1] = true;
                if (agingtestVbrCb.isChecked())
                    flag[2] = true;
                if (agingtestInfCb.isChecked())
                    flag[3] = true;
                if (agingtestFlsCb.isChecked())
                    flag[4] = true;
                Intent intent = new Intent(agingtest.this, Test.class);
                intent.putExtra("flag", flag);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onResume() {
        SystemProperties.set("persist.sys.gsfk.key", "1");
        super.onResume();
    }

    @Override
    protected void onPause() {
        SystemProperties.set("persist.sys.gsfk.key", "0");
        super.onPause();
    }
}
