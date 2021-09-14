package com.gosuncn.zfyhwapidemo.activity;

// Bug503, OTA - installpackage run failed, wmd, 2020.0624

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import com.gosuncn.zfyfw.api.LedManager;


import android.os.Handler;
import android.os.RecoverySystem;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;

import com.gosuncn.zfyfw.nv.NvUtilExt;
import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.R;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;

import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;
import android.widget.TextView;

public class NvActivity extends Activity {
    private static final String TAG = NvActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.nv_activity);

        final TextView resultTV = (TextView)findViewById(R.id.result);
        resultTV.setText(NvUtilExt.getInstance().getFactoryNv2499());

        Button clearBtn = (Button)findViewById(R.id.clear);
        clearBtn.setText("Clear");
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NvUtilExt.getInstance().clearFactoryNv2499();

                resultTV.setText(NvUtilExt.getInstance().getFactoryNv2499());
            }
        });
        Button setFlagBtn = (Button)findViewById(R.id.set_flag);
        setFlagBtn.setText("Set MMI Flag");
        setFlagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NvUtilExt.getInstance().setFactoryNv2499MMIBit("P");
                resultTV.setText(NvUtilExt.getInstance().getFactoryNv2499());
            }
        });
    }

}
