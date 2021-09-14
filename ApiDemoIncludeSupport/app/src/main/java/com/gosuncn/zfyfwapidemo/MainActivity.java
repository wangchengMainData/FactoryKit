package com.gosuncn.zfyhwapidemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;


import com.gosuncn.zfyfw.service.GSFWManager;
import com.gosuncn.zfyfw.service.ISettingsContentObserver;
import com.gosuncn.zfyfw.api.LedManager;



import android.os.RemoteException;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.gosuncn.zfyhwapidemo.MainApp.ApiItem;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private AbsListView mListView;
    private Map<String, ApiItem> mApiItemList = new LinkedHashMap<String, ApiItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        
        mListView = (AbsListView) findViewById(R.id.main_grid);
        mListView.setOnItemClickListener(mOnClickListener);
        mListView.setAdapter(mBaseAdapter);
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
            List<String> list = new ArrayList<String>();
            list.add("cn.wps.moffice_eng");
            GSFWManager.getInstance().setSpecificSystemAppsStatus(0, list);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "### onResume #S");
        //GSFWManager.getInstance().registerSettingsContentObserver(mISettingsContentObserver);
        Log.d(TAG, "### onResume #E");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "### onPause #S");
        //GSFWManager.getInstance().unregisterSettingsContentObserver(mISettingsContentObserver);
        Log.d(TAG, "### onPause #E");
    }

    private ISettingsContentObserver.Stub mISettingsContentObserver = new ISettingsContentObserver.Stub() {
        @Override
        public void onchanged(int type, int value, List<String> valueList) throws RemoteException {
            Log.d(TAG, "### onchanged type:" + type + " value:" + value + " valueList:" + valueList.toString());
        }
    };

    private void initData(){
        ApiItem item = null;
        XmlPullParser xmlPullParser = null;
        xmlPullParser = getResources().getXml(R.xml.api_item_list);

        try {
            int mEventType = xmlPullParser.getEventType();
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String name = xmlPullParser.getName();

                    if (name.equals("ApiItem")) {
                        item = null;
                        String enable = xmlPullParser.getAttributeValue(
                                null, "enable");
                        if (enable != null && enable.equals("true")) {
                            item = new ApiItem();
                            item.key = xmlPullParser
                                    .getAttributeValue(null, "key");
                            item.name = xmlPullParser
                                    .getAttributeValue(null, "name");
                            item.enable = xmlPullParser
                                    .getAttributeValue(null, "enable");
                            item.classname = xmlPullParser
                                    .getAttributeValue(null, "classname");
                        }
                    }
                } else if (mEventType == XmlPullParser.END_TAG) {
                    String tagName = xmlPullParser.getName();
                    if (item != null
                            && tagName.equals("ApiItem")) {
                        mApiItemList.put(item.classname,
                                item);
                    }
                }
                mEventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

        MainApp.getInstance().mItemList = getItemList(mApiItemList);
    }

    private List getItemList(Map<String, ApiItem> ApiItems) {
        List<Map> mList = new ArrayList<Map>();

        for (Map.Entry<String, ApiItem> entry : ApiItems.entrySet()) {
            ApiItem item = (ApiItem) entry.getValue();
            Map<String, Object> temp = new HashMap<String, Object>();
            Intent intent = new Intent();
            intent.setClassName(getApplicationContext().getPackageName(),
                    item.classname);
            temp.put("intent", intent);
            temp.put("key", item.key);
            temp.put("name", item.name);
            temp.put("classname", item.classname);

            mList.add(temp);
        }

        return mList;
    }

    private BaseAdapter mBaseAdapter = new BaseAdapter() {

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null)
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.list_item, null);

            Button text = (Button) convertView
                    .findViewById(R.id.text_center);
            text.setText((String) (MainApp.getInstance().mItemList
                    .get(position).get("name")));

            AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    64, MainActivity.this.getResources().getDisplayMetrics()));
            convertView.setLayoutParams(param);

            return convertView;
        }

        public int getCount() {

            return MainApp.getInstance().mItemList.size();
        }

        public Object getItem(int position) {

            return MainApp.getInstance().mItemList.get(position);
        }

        public long getItemId(int arg0) {

            return 0;
        }

    };

    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            onListItemClick((AbsListView)parent, v, position, id);
        }
    };

    protected void onListItemClick(AbsListView l, View v, int position, long id) {

        // Gets the data associated with the specified position in the list.
        Log.d(TAG, "click pos=" + position);
        Map map = (Map) l.getItemAtPosition(position);
        Intent intent = (Intent) map.get("intent");
        startActivityForResult(intent, position);
    }

}
