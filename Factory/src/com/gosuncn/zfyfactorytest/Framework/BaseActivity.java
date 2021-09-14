package com.gosuncn.zfyfactorytest.Framework;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfw.service.GSFWManager;

public abstract class BaseActivity extends Activity implements View.OnClickListener {

    protected static final boolean SHOW_RESULT_DIALOG = false;
    View mButtonBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!SHOW_RESULT_DIALOG && ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == 0) ) {
            View contentView = findViewById(android.R.id.content);
            contentView.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.test_result_button_bar_height));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("BaseActivity","BaseActivity onresume");
        GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
        if(!SHOW_RESULT_DIALOG) {
            View v = LayoutInflater.from(this).inflate(
                    R.layout.test_result_button_bar_layout, null);
            mButtonBar = v;
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.BOTTOM;
            ((ViewGroup) getWindow().getDecorView()).addView(v, layoutParams);
            v.findViewById(R.id.test_result_pass).setOnClickListener(this);
            v.findViewById(R.id.test_result_fail).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        if (R.id.test_result_pass == v.getId()) {
            onPositiveCallback();
        } else if (R.id.test_result_fail == v.getId()) {
            onNegativeCallback();
        }
    }

    protected void hideNegativeButton(){
        mButtonBar.findViewById(R.id.test_result_fail).setVisibility(View.GONE);
    }
    protected void setPositiveButtonText(int id){
        setPositiveButtonText(getString(id));
    }
    protected void setPositiveButtonText(String default_confirm) {
        ((Button)mButtonBar.findViewById(R.id.test_result_pass)).setText(default_confirm);
    }

    TextView mConfirmText;
    protected View loadDefaultConfirmText(int id){
        return loadDefaultConfirmText(getResources().getString(id));
    }
    protected View loadDefaultConfirmText(String default_confirm){
        ScrollView rootView = new ScrollView(this);
        rootView.setScrollbarFadingEnabled(false);
        TextView tv = new TextView(this);
        mConfirmText = tv;
        tv.setSingleLine(false);
        tv.setText(default_confirm);
        tv.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Large);
        int paddingSize = getResources().getDimensionPixelSize(R.dimen.test_result_text_padding);
        tv.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
        rootView.addView(tv, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        return rootView;
    }
    protected TextView getConfirmText(){
        return mConfirmText;
    }
    protected void setConfirmText(int id){
        setConfirmText(getResources().getString(id));
    }
    protected void setConfirmText(String default_confirm){
        mConfirmText.setText(default_confirm);
    }

    protected void hideTestResultConfirmButtonBar(){
        mButtonBar.setVisibility(View.GONE);
    }
    protected void showTestResultConfirmButtonBar(){
        mButtonBar.setVisibility(View.VISIBLE);
    }

    protected abstract void onPositiveCallback();

    protected abstract void onNegativeCallback();
}
