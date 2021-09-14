package com.gosuncn.zfyfwapidemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.gosuncn.zfyfw.apn.APNModel;
import com.gosuncn.zfyfw.apn.APNSettingHelper;
import com.gosuncn.zfyfw.apn.ApnAdapter;
import com.gosuncn.zfyfw.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Administrator
 * @date: 2020/7/24
 */
public class ApnTestActivity extends Activity {

    TextView mCurrentApnTv,mSelectTv,mApnIdTv,mCApnTv;
    ListView mListView;
    ApnAdapter mApnAdapter;
    List<APNModel> mDatas= new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apn);
        findViews();
    }

    private void findViews(){
        mCurrentApnTv = (TextView)findViewById(R.id.apn_index_tv);
        mSelectTv = (TextView)findViewById(R.id.current_apn);
        mApnIdTv = (TextView)findViewById(R.id.apn_index_tv);
        mCApnTv = (TextView)findViewById(R.id.current_apn);

        mListView = (ListView) findViewById(R.id.apn_list);
        mApnAdapter = new ApnAdapter(mDatas,this);
        mListView.setAdapter( mApnAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mApnIdTv.setText(""+ mDatas.get(i).getApnId() );
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        readDatasfronDb();
    }

    private void readDatasfronDb(){

        mCApnTv.setText("Current_APN:"+ APNSettingHelper.getInstance().getAPNInfo());

        mDatas.clear();

        APNSettingHelper.getInstance().readAll( mDatas );

        if(mApnAdapter!= null ){
            mApnAdapter.notifyDataSetChanged();
        }

    }


    public void addnew(View view){
        gotoActivity( ApnAddActivity.class );
    }

    public void select( View view){

        if( !mApnIdTv.getText().toString().isEmpty() ){
            int id = Integer.valueOf( mApnIdTv.getText().toString());
            APNSettingHelper.getInstance().setPreferredAPN( id );
        }
    }


    public void gotoActivity(Class<?> classname) {
        Intent intent = new Intent();
        intent.setClass( this, classname);
        startActivity( intent );
    }
}
