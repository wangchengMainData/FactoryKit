package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.os.Bundle;


import com.gosuncn.zfyfw.api.LedManager;



import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Button;
import android.util.TypedValue;
import android.widget.ScrollView;

import com.gosuncn.zfyhwapidemo.R;

/**
 * LED灯Demo
 */
public class LedActivity extends Activity {
    private static final String TAG = LedActivity.class.getSimpleName();

    private Button mOpenBtn;
    private Button mCloseBtn;
    private Button mFlashingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        scrollView.addView(rootView);

        mOpenBtn = new Button(this);
        mCloseBtn = new Button(this);
        mFlashingBtn = new Button(this);
        mOpenBtn.setTag(991);
        mOpenBtn.setText("IRLED Open");
        mOpenBtn.setGravity(Gravity.CENTER);
        mCloseBtn.setTag(992);
        mCloseBtn.setText("IRLED Close");
        mCloseBtn.setGravity(Gravity.CENTER);
        rootView.addView(mOpenBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mCloseBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        mOpenBtn.setOnClickListener(mOnClickListener);
        mCloseBtn.setOnClickListener(mOnClickListener);

        mOpenBtn = new Button(this);
        mCloseBtn = new Button(this);
        mFlashingBtn = new Button(this);
        mOpenBtn.setTag(1);
        mOpenBtn.setText("RED Open");
        mOpenBtn.setGravity(Gravity.CENTER);
        mCloseBtn.setTag(2);
        mCloseBtn.setText("RED Close");
        mCloseBtn.setGravity(Gravity.CENTER);
        mFlashingBtn.setTag(3);
        mFlashingBtn.setText("RED Flashing");
        mFlashingBtn.setGravity(Gravity.CENTER);
        rootView.addView(mOpenBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mCloseBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mFlashingBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        mOpenBtn.setOnClickListener(mOnClickListener);
        mCloseBtn.setOnClickListener(mOnClickListener);
        mFlashingBtn.setOnClickListener(mOnClickListener);

        mOpenBtn = new Button(this);
        mCloseBtn = new Button(this);
        mFlashingBtn = new Button(this);
        mOpenBtn.setTag(11);
        mOpenBtn.setText("YELLOW Open");
        mOpenBtn.setGravity(Gravity.CENTER);
        mCloseBtn.setTag(12);
        mCloseBtn.setText("YELLOW Close");
        mCloseBtn.setGravity(Gravity.CENTER);
        mFlashingBtn.setTag(13);
        mFlashingBtn.setText("YELLOW Flashing");
        mFlashingBtn.setGravity(Gravity.CENTER);
        rootView.addView(mOpenBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mCloseBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mFlashingBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        mOpenBtn.setOnClickListener(mOnClickListener);
        mCloseBtn.setOnClickListener(mOnClickListener);
        mFlashingBtn.setOnClickListener(mOnClickListener);

        mOpenBtn = new Button(this);
        mCloseBtn = new Button(this);
        mFlashingBtn = new Button(this);
        mOpenBtn.setTag(111);
        mOpenBtn.setText("GREEN Open");
        mOpenBtn.setGravity(Gravity.CENTER);
        mCloseBtn.setTag(112);
        mCloseBtn.setText("GREEN Close");
        mCloseBtn.setGravity(Gravity.CENTER);
        mFlashingBtn.setTag(113);
        mFlashingBtn.setText("GREEN Flashing");
        mFlashingBtn.setGravity(Gravity.CENTER);
        rootView.addView(mOpenBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mCloseBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mFlashingBtn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, LedActivity.this.getResources().getDisplayMetrics())));
        mOpenBtn.setOnClickListener(mOnClickListener);
        mCloseBtn.setOnClickListener(mOnClickListener);
        mFlashingBtn.setOnClickListener(mOnClickListener);

        setContentView(scrollView);
    }

	// REQ116,lights,wmd,2020.0407
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if((int)v.getTag() == 1){
                // 打开电池灯
                LedManager.getInstance().setLedColor(
                        LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_RED);
            }else if((int)v.getTag() == 3){
                // 指示灯红色闪烁
                LedManager.getInstance().setLedFlashing(
                        LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_RED,
                        1000,
                        1000);
            }else if((int)v.getTag() == 2){
                // 关闭指示灯
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
            }else if((int)v.getTag() == 11){
                // 打开电池灯
                LedManager.getInstance().setLedColor(
                        LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_YELLOW);
            }else if((int)v.getTag() == 13){
                // 指示灯黄色闪烁
                LedManager.getInstance().setLedFlashing(
                        LedManager.LIGHT_ID_NOTIFICATIONS,
                        LedManager.LIGHT_COLOR_YELLOW,
                        1000,
                        1000);
            }else if((int)v.getTag() == 12){
                // 关闭指示灯
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
            }else if((int)v.getTag() == 111){
                // 打开电池灯
                LedManager.getInstance().setLedColor(
                        LedManager.LIGHT_ID_BATTERY,
                        LedManager.LIGHT_COLOR_GREEN);
            }else if((int)v.getTag() == 113){
                // 指示灯绿色闪烁
                LedManager.getInstance().setLedFlashing(
                        LedManager.LIGHT_ID_BATTERY,
                        LedManager.LIGHT_COLOR_GREEN,
                        1000,
                        1000);
            }else if((int)v.getTag() == 112){
                // 关闭指示灯
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
            }else if((int)v.getTag() == 991){
                // 打开电池灯
                LedManager.getInstance().turnOnLed(LedManager.LIGHT_ID_IRLED);
            }else if((int)v.getTag() == 992){
                // 指示灯绿色闪烁
                LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_IRLED);
            }


        }
    };

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

    }

    @Override
    protected void onPause() {
        super.onPause();

        LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_BATTERY);
        LedManager.getInstance().turnOffLed(LedManager.LIGHT_ID_NOTIFICATIONS);
    }

}
