/*
 * Copyright (c) 2013-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.Framework;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gosuncn.zfyfactorytest.TestReport.TestReport;
import com.gosuncn.zfyfactorytest.R;
import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfactorytest.Values;
import com.gosuncn.zfyfactorytest.Framework.MainApp.FunctionItem;
import com.gosuncn.zfyfw.service.GSFWManager;



public class Framework extends Activity {

	private final static String TAG = "Framework";
	private boolean mExitFlag = false;
	private final static int MSG_REFRESH = 1000;
	SharedPreferences sharedPreferences;
	private Handler mHandler = new Handler(){ // wmd
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				// Upon receiving the fade pulse, we have the view perform a
				// fade and then enqueue a new message to pulse at the desired
				// next time.
				case MSG_REFRESH: {
					if(!isFinishing()) {
						mAutoTestBtn.setText(R.string.auto_test_name);
						mQuickTestBtn.setText(R.string.quick_test_name);
                        warningTips.setVisibility(View.GONE);
					}
					break;
				}
				default:
					super.handleMessage(msg);
			}
		}
	};
	private LayoutInflater mInflater;
	Context mContext;
	String TempFile;
	private long curBackButtonTime = 0;
	private long lastBackButtonTime = 0;
	private int positionClicked = 1;
	final static int[] flagList = new int[99];
	final static int[] resultCodeList = new int[99];
	private static final int MENU_CLEAN_STATE = Menu.FIRST;
	private static final int MENU_UNINSTALL = Menu.FIRST + 1;
	private Map<String, FunctionItem> mFunctionItems = new LinkedHashMap<String, FunctionItem>();
	private int itemcount;
	List<? extends Map<String, ?>> itemlist;
	private Bitmap passBitmap;
	private Bitmap failBitmap;
	private boolean toStartAutoTest = false;
	// Bug564, 工模-快速测试, wmd, 2020.0804
	public static boolean quickTestEnabled = false;
	private final int AUTO_TEST_TIME_INTERVAL = 900;
	private boolean originChargingStatus = true;

	// wmd
	private AbsListView mListView;
	private Button mAutoTestBtn;
	private Button mQuickTestBtn;
    private TextView warningTips;
	private int mItemWidth = 0;
	private static int SYSTEM_DEFAULT_SCREEN_TIME_OUT;

//	static {
//		logd("Loading libqcomfm_jni.so");
//		System.loadLibrary("qcomfm_jni");
//	}

	void init(Context context) {

		mContext = context;
		lastBackButtonTime = 0;
		curBackButtonTime = 0;

		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// �л�������
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mInflater = LayoutInflater.from(context);

		passBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.test_pass);
		failBitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.test_fail);

		/** nv factory_data_3 */
		if (Values.ENABLE_NV) {
		}
		// ��ʼдlog��Ϣ
		// Write TestLog Start message
		Utils.writeTestLog("\n=========Start=========", null);
		// ��ȡϵͳ����״̬
		originChargingStatus = getChargingStatus();
		logd("originChargingStatus=" + originChargingStatus);

		// To save test time, enable some devices first
		// Utilities.enableWifi(mContext, true);
		// Utilities.enableBluetooth(true);
		// Utilities.enableGps(mContext, true);
		// Utilities.configScreenTimeout(mContext, 1800000); // 1 min
		// Utilities.configMultiSim(mContext);
		// configSoundEffects(false);
		// createShortcut(context);// add shortcut

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		SYSTEM_DEFAULT_SCREEN_TIME_OUT = Utils.getScreenTimeout(this,60000);
		Log.e(TAG,"SYSTEM_DEFAULT_SCREEN_TIME_OUT:"+SYSTEM_DEFAULT_SCREEN_TIME_OUT);
		super.onCreate(savedInstanceState);
		// �жϵ�ǰ�Ƿ���Monkey���ԣ��еĻ����˳�
		if (ActivityManager.isUserAMonkey())
			finish();
		else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);//wmd
			init(getApplicationContext());

			String hwPlatform = Utils.getPlatform();
			System.out.println("hwPlatform="+hwPlatform);
//			setTitle(getString(R.string.app_name)/* + " " + hwPlatform*/); // wmd
			logd(hwPlatform);
			// �����Ƿ��������ļ�
			/** Get Test Items */
			FunctionItem functionItems = null;
			String configFile = null;
			for (String tmpConfigFile : Values.CONFIG_FILE_SEARCH_LIST) {
				File tmp = new File(tmpConfigFile);
				if (tmp.exists() && tmp.canRead()) {
					configFile = tmpConfigFile;
					logd("Found config file: " + tmpConfigFile);
					break;
				}
			}
			// ���������ļ���û���ڶ�ӦĿ¼�ҵ�����ʹ��Ĭ�ϵ������ļ�
			XmlPullParser xmlPullParser = null;
			if (configFile != null) {
				XmlPullParserFactory xmlPullParserFactory;
				try {
					xmlPullParserFactory = XmlPullParserFactory.newInstance();
					xmlPullParserFactory.setNamespaceAware(true);
					xmlPullParser = xmlPullParserFactory.newPullParser();
					// �������ļ�,���ڶ�ȡ
					FileInputStream fileInputStream = new FileInputStream(
							configFile);
					xmlPullParser.setInput(fileInputStream, "utf-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
			// ʹ��Ĭ�ϵ������ļ�
			} else {
				/*if (Values.PRODUCT_MSM8916_32.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8916_32);
					System.out.println("xmlPullParser: "+1);
				} else if (Values.PRODUCT_MSM8916_32_LMT.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8916_32);
					System.out.println("xmlPullParser: "+2);
				} else if (Values.PRODUCT_MSM8909_512.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8909_512);
					System.out.println("xmlPullParser: "+3);
				} else if (Values.PRODUCT_MSM8916.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8916);
					System.out.println("xmlPullParser: "+4);
				} else if (Values.PRODUCT_MSM7627A_SKU1.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_7627a_sku1);
					System.out.println("xmlPullParser: "+5);
				} else if (Values.PRODUCT_MSM7627A_SKU3.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_7627a_sku3);
					System.out.println("xmlPullParser: "+6);
				} else if (Values.PRODUCT_MSM8X25_SKU5.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8x25_sku5);
					System.out.println("xmlPullParser: "+7);
				} else if (Values.PRODUCT_MSM7X27_SKU5A.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_7x27_sku5a);
					System.out.println("xmlPullParser: "+8);
				} else if (Values.PRODUCT_MSM7627A_SKU7.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_7627a_sku7);
					System.out.println("xmlPullParser: "+9);
				} else if (Values.PRODUCT_MSM8X25Q_SKUD.equals(hwPlatform)) {
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8x25q_skud);
					System.out.println("xmlPullParser: "+10);
				} else if (Values.PRODUCT_MSM8226.equals(hwPlatform)){
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8x26);
					System.out.println("xmlPullParser: "+11);
				}
				else if (Values.PRODUCT_MSM8610.equals(hwPlatform)){
					xmlPullParser = getResources().getXml(
							R.xml.item_config_8610);
					System.out.println("xmlPullParser: "+12);
				}
				else*/ // wmd
					{
					xmlPullParser = getResources().getXml(
							R.xml.item_config_default);
					System.out.println("xmlPullParser: "+13);
				}
			}
			try {
				int mEventType = xmlPullParser.getEventType();
				/** Parse the xml */
				while (mEventType != XmlPullParser.END_DOCUMENT) {
					if (mEventType == XmlPullParser.START_TAG) {
						String name = xmlPullParser.getName();

						if (name.equals("FunctionItem")) {
							functionItems = null;
							String enable = xmlPullParser.getAttributeValue(
									null, "enable");

							if (enable != null && enable.equals("true")) {
								// �½�FunctionItem����
								functionItems = new FunctionItem();
								// �����������ƻ�ȡ��ǩ������ֵ
								functionItems.name = xmlPullParser
										.getAttributeValue(null, "name");
								System.out.println("functionItems.name= "+functionItems.name);
								functionItems.auto = xmlPullParser
										.getAttributeValue(null, "auto");
								functionItems.packageName = xmlPullParser
										.getAttributeValue(null, "packageName");
								// ��������parameter�����浽Map��
								Utils.parseParameter(xmlPullParser
										.getAttributeValue(null, "parameter"),
										functionItems.parameter);
							}
						}
					} else if (mEventType == XmlPullParser.END_TAG) {
						String tagName = xmlPullParser.getName();

						if (functionItems != null
								&& tagName.equals("FunctionItem")) {
							// add
							// ��packageName����key��functionItems����value������Map��
							mFunctionItems.put(functionItems.packageName,
									functionItems);
						}
					}
					mEventType = xmlPullParser.next();
				}
			} catch (Exception e) {
				loge(e);
			}
			// ����MainApp���󣬲����»�õ�Map���浽MainApp��mItemList��
			// put ItemList into MainApp.getInstance().mItemList
			MainApp.getInstance().mItemList = getItemList(mFunctionItems);
			if (Values.ENABLE_BACKGROUND_SERVICE)
				// ����AutoService����
				// ��AutoService�н�һЩ����ͷ����ȴ򿪣�Service���ں�̨���С��������Ե�ʱ��ȱȽϿ�
				startService(new Intent(mContext, AutoService.class));
			else {
				// To save test time, enable some devices first
				Utils.enableWifi(mContext, true);
				Utils.enableBluetooth(true);
				Utils.enableGps(mContext, true);
				Utils.enableNfc(mContext, true);
				Utils.configScreenTimeout(mContext, 1800000);
				Utils.configMultiSim(mContext);
				Utils.enableCharging(true);
			}
			setListAdapter(mBaseAdapter);
			itemlist = MainApp.getInstance().mItemList;
			itemcount = itemlist.size();
			TestReportResult();
			for(int i = 0 ; i < itemcount; i++) {
                Map<String, ?> item1 = itemlist.get(i);
                if(item1.get("result").equals("NULL"))
                {
                    warningTips.setVisibility(View.VISIBLE);
                    break;
                }
                else
                    warningTips.setVisibility(View.GONE);
            }
		}

	}

	// wmd
	private void setListAdapter(BaseAdapter baseAdapter){
		// gridview
		setContentView(R.layout.main_grid_layout);
		warningTips = findViewById(R.id.warning_tips);
		mListView = (AbsListView) findViewById(R.id.main_grid);
		mListView.setOnItemClickListener(mOnClickListener);
		mListView.setAdapter(baseAdapter);
		mItemWidth = (int)(getResources().getDisplayMetrics().widthPixels -  ((GridView)mListView).getNumColumns() * 10)  / ((GridView)mListView).getNumColumns();

		Button againgTestBtn = (Button)findViewById(R.id.againg_test);
		againgTestBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startAgaingTest();
			}
		});
		mAutoTestBtn = (Button)findViewById(R.id.auto_test);
		mAutoTestBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				switchAutoTest();
			}
		});
        Button resetBtn = (Button)findViewById(R.id.auto_test_reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanTestState();
            }
        });
        Button testReportBtn = (Button)findViewById(R.id.test_report);
		testReportBtn.setVisibility(View.GONE);
		testReportBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListView.getOnItemClickListener().onItemClick(mListView, null, mListView.getCount() - 1, 0);
			}
		});

		Button quickTestBtn = (Button)findViewById(R.id.quick_test);
		mQuickTestBtn = quickTestBtn;
		quickTestBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(toStartAutoTest){
					stopAutoTest();
				}else{
					quickTestEnabled = true;
					startAutoTest();
				}
			}
		});

		Button factoryResetBtn = (Button)findViewById(R.id.factory_reset);
		factoryResetBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				factoryResetHandler();
			}
		});

        TextView versionName = (TextView)findViewById(R.id.version_name);
        StringBuffer versionInfo = new StringBuffer("");
		PackageManager pm = getApplicationContext().getPackageManager();
		try {
			PackageInfo packageInfo = pm.getPackageInfo(getApplicationContext().getPackageName(), 0);
			Log.d(TAG, "setListAdapter versionName:" + packageInfo.versionName);
			versionInfo.append("V").append(packageInfo.versionCode).append(" ").append(packageInfo.versionName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		versionName.setText(versionInfo.toString());
	}

	public AbsListView getListView() {
		return mListView;
	}

	public void setSelection(int position) {
		mListView.setSelection(position);
	}

	@Override
	protected void onDestroy() {
		if (!ActivityManager.isUserAMonkey()) {

			Utils.enableWifi(mContext, false);
			Utils.enableBluetooth(false);
//			Utils.enableGps(mContext, false);
			try {
				Utils.enableNfc(mContext, false);
			}catch (Exception e){

			}
			// enableCharging(false);
			if (Values.ENABLE_BACKGROUND_SERVICE) {
				MainApp.getInstance().clearAllService();
				stopService(new Intent(mContext, AutoService.class));
			}
		}
		super.onDestroy();
		Utils.configScreenTimeout(mContext, SYSTEM_DEFAULT_SCREEN_TIME_OUT); // 1 min
		Log.e(TAG,"destroy default screen out:"+Utils.getScreenTimeout(this,60000));
	}

	private boolean getChargingStatus() {
		String value = Utils.getSystemProperties(Values.PROP_CHARGE_DISABLE,
				null);
		if ("1".equals(value))
			return false;
		else
			return true;

	}

	private void configSoundEffects(boolean enable) {
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (enable)
			mAudioManager.loadSoundEffects();
		else
			mAudioManager.unloadSoundEffects();
	}

	public void createShortcut(Context context) {
		Intent intent = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(R.string.app_name));
		intent.putExtra("duplicate", false);
		Intent appIntent = new Intent();
		appIntent.setAction(Intent.ACTION_MAIN);
		appIntent.setClass(context, getClass());
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, appIntent);
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
				context, R.drawable.icon);
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		context.sendBroadcast(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// wmd
/*		if (!ActivityManager.isUserAMonkey()) {
			int groupId = 0;
			// �����Ӳ˵�"Clean Test State", ������Ե���Ϣ
			SubMenu addMenu = menu.addSubMenu(groupId, MENU_CLEAN_STATE,
					Menu.NONE, R.string.clean_state);
			addMenu.setIcon(android.R.drawable.ic_menu_revert);
			// �����Ӳ˵�Uninstall, ����ж�����
			SubMenu resetMenu = menu.addSubMenu(groupId, MENU_UNINSTALL,
					Menu.NONE, R.string.uninstall);
			resetMenu.setIcon(android.R.drawable.ic_menu_delete);
			// ��ʾ��Actionbar
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.action_bar, menu);
		}*/

		return super.onCreateOptionsMenu(menu);
	}
	// ����˵���ѡ���Ӧ�Ĳ���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// wmd
/*
		logd(item.getItemId());
		switch (item.getItemId()) {
		case (MENU_CLEAN_STATE):
			cleanTestState();
			break;
		case (MENU_UNINSTALL):
			Uri uri = Uri.fromParts("package", "com.gosuncn.zfyfactorytest", null);
			startActivity(new Intent(Intent.ACTION_DELETE, uri));
			break;
		case R.id.run_auto_items:
			toStartAutoTest = true;
			positionClicked = getNextUntestedItem(MainApp.getInstance().mItemList);
			loge("pos=" + positionClicked);
			if (positionClicked < 0) {
				toStartAutoTest = false;
			} else {
				Intent intent = (Intent) MainApp.getInstance().mItemList.get(
						positionClicked).get("intent");
				intent.putExtra(Values.KEY_SERVICE_INDEX, positionClicked);
				startActivityForResult(intent, positionClicked);
			}
			break;
		case R.id.pause_auto_items:
			toStartAutoTest = false;
			break;
		}*/
		return super.onOptionsItemSelected(item);
	}

	// wmd
	private void startAutoTest(){
		toStartAutoTest = true;
		mAutoTestBtn.setText(R.string.cancel);
		mQuickTestBtn.setText(R.string.cancel);
		positionClicked = getNextUntestedItem(MainApp.getInstance().mItemList);
		loge("pos=" + positionClicked);
		if (positionClicked < 0) {
			toStartAutoTest = false;
			quickTestEnabled = false;
			mAutoTestBtn.setText(R.string.auto_test_name);
			mQuickTestBtn.setText(R.string.quick_test_name);
		} else {
			try {
				Intent intent = (Intent) MainApp.getInstance().mItemList.get(
						positionClicked).get("intent");
				intent.putExtra(Values.KEY_SERVICE_INDEX, positionClicked);
				startActivityForResult(intent, positionClicked);
			}catch (Exception e){
				toStartAutoTest = false;
				quickTestEnabled = false;
				mAutoTestBtn.setText(R.string.auto_test_name);
				mQuickTestBtn.setText(R.string.quick_test_name);
			}
		}
	}
	private void stopAutoTest() {
		toStartAutoTest = false;
		quickTestEnabled = false;
		mAutoTestBtn.setText(R.string.auto_test_name);
		mQuickTestBtn.setText(R.string.quick_test_name);
	}

	private void switchAutoTest(){
		if(toStartAutoTest){
			stopAutoTest();
		}else{
			startAutoTest();
		}
	}

	private void startAgaingTest(){
		Intent intent = new Intent(Framework.this,
				com.gosuncn.zfyfactorytest.agingtest.agingtest.class);
//		intent.setComponent(new ComponentName(getApplicationContext().getPackageName(),
//				"com.gosuncn.zfyfactorytest.agingtest.agingtest"));
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		if (!ActivityManager.isUserAMonkey()) {
			IntentFilter filter = new IntentFilter(
					Values.BROADCAST_UPDATE_MAINVIEW);
			registerReceiver(mViewBroadcastReceiver, filter);
		}
		GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (!ActivityManager.isUserAMonkey())
			unregisterReceiver(mViewBroadcastReceiver);
		super.onPause();
	}

	private List getItemList(Map<String, FunctionItem> functionItems) {
		boolean followConfigSequence = true;
		Map<String, Integer> classList = new HashMap<String, Integer>();
		List<Map> mList = new ArrayList<Map>();

		Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
		mIntent.addCategory("android.category.factory.kit");
		// ���һ��packageManager����
		PackageManager packageManager = getPackageManager();
		// ��ȡ���п��Դ���mIntent��Activity
		/** Retrieve all activities that can be performed for the given intent */
		List<ResolveInfo> list = packageManager.queryIntentActivities(mIntent,
				0);

		if (list == null)
			super.finish();

		int len = list.size();
		for (int i = 0; i < len; i++) {
			ResolveInfo resolveInfo = list.get(i);

			String className = resolveInfo.activityInfo.name.substring(0,
					resolveInfo.activityInfo.name.lastIndexOf('.'));
			// ��ȡ����
			System.out.println("className= "+className);

			if(!followConfigSequence) {
				// ����keyֵ����Map��ȡFunctionItem����
				FunctionItem functionItem = functionItems.get(className);
				if (functionItem == null) {
					continue;
				}
				Intent intent = new Intent();
				// ����Intent��ͼ���͸���һ��activity
				intent.setClassName(
						resolveInfo.activityInfo.applicationInfo.packageName,
						resolveInfo.activityInfo.name);
				addItem(mList, intent, functionItem);
			}else{
				classList.put(className, i);
			}
		}

		if(followConfigSequence) {
			for (Map.Entry<String, FunctionItem> entry : functionItems.entrySet()) {
//				System.out.println(entry.getKey() + "-" + entry.getValue());
				if(classList.containsKey(entry.getKey())) {
					FunctionItem functionItem = (FunctionItem) entry.getValue();
					Intent intent = new Intent();
					intent.setClassName(
							list.get(classList.get(entry.getKey())).activityInfo.applicationInfo.packageName,
							list.get(classList.get(entry.getKey())).activityInfo.name);
					addItem(mList, intent, functionItem);
				}
			}
		}

		return mList;
	}
	// ��Map��ӵ�List��, һ��Map���������ҪIntent�����FunctionTiem����
	private void addItem(List<Map> list, Intent intent,
			FunctionItem functionItem) {

		Map<String, Object> temp = new HashMap<String, Object>();
		temp.put("intent", intent);
		temp.put("title", functionItem.name);
		temp.put("packageName", functionItem.packageName);//wmd
		temp.put("auto", functionItem.auto);
		temp.put("parameter", functionItem.parameter);
		temp.put("result",
				Utils.getStringValueSaved(mContext, functionItem.name, "NULL"));
		list.add(temp);
	}

	// wmd
	private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id)
		{
			onListItemClick((AbsListView)parent, v, position, id);
		}
	};


	protected void onListItemClick(AbsListView l, View v, int position, long id) {

		// Gets the data associated with the specified position in the list.
		logd("click pos=" + position);
		if (!toStartAutoTest) {
			Map map = (Map) l.getItemAtPosition(position);
			positionClicked = position;
			Intent intent = (Intent) map.get("intent");
			intent.putExtra(Values.KEY_SERVICE_INDEX, position);
			startActivityForResult(intent, position);
		}
	}

	protected void updateView(int requestCode, int resultCode) {

		resultCodeList[requestCode] = resultCode;
		Map map = (Map) getListView().getItemAtPosition(requestCode);
		String name = (String) map.get("title");
		String result = (resultCode == RESULT_OK ? "OK" : "FAIL");
		map.put("result", result);
		// ��̬ˢ��ListView
		mBaseAdapter.notifyDataSetChanged();
	}

	protected void cleanTestState() {

		if (MainApp.getInstance().mItemList == null)
			return;

		int size = MainApp.getInstance().mItemList.size();

		for (int i = 0; i < size; i++) {

			Map map = (Map) this.getListView().getItemAtPosition(i);
			map.put("result", "NULL");
			Utils.saveStringValue(mContext, (String) map.get("title"), null);
		}
        warningTips.setVisibility(View.VISIBLE);
		mBaseAdapter.notifyDataSetChanged();
	}

	public void TestReportResult()
	{
		Map map_testreport = (Map) this.getListView().getItemAtPosition(itemcount - 1);
		for(int i = 0 ; i < itemcount - 1; i++) {
			Map<String, ?> item1 = itemlist.get(i);
			if(item1.get("result").equals("Failed") || item1.get("result").equals("NULL"))
			{
				map_testreport.put("result","Failed");
				break;
			}
			else
				map_testreport.put("result", "Pass");

		}
		try {
            sharedPreferences = getSharedPreferences("NVflag", MODE_PRIVATE);
            if (sharedPreferences.getBoolean("constnvset", false)) {
                //if flag is true do nothing
                Log.e(TAG, "constnvset:true");
            } else {
                Log.e(TAG, "constnvset:false");
                if (getNextUntestedItem(MainApp.getInstance().mItemList) == -1 && map_testreport.get("result").equals("Failed")) {
                    GSFWManager.getInstance().setFactoryResultFlag("F");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (GSFWManager.getInstance().getFactoryResetFlag() != null &&
                                        GSFWManager.getInstance().getFactoryResetFlag().length() >= 4 &&
                                        GSFWManager.getInstance().getFactoryResetFlag().charAt(3) == 'F')
                                    Log.e(TAG, "flag F set ok");
                                else
                                    Log.e(TAG, "flag F set fail");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 500);
                }
                if (getNextUntestedItem(MainApp.getInstance().mItemList) == -1 && map_testreport.get("result").equals("Pass")) {
                    GSFWManager.getInstance().setFactoryResultFlag("P");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (GSFWManager.getInstance().getFactoryResetFlag() != null &&
                                        GSFWManager.getInstance().getFactoryResetFlag().length() >= 4 &&
                                        GSFWManager.getInstance().getFactoryResetFlag().charAt(3) == 'P')
                                    Log.e(TAG, "flag P set ok");
                                else
                                    Log.e(TAG, "flag P set fail");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 500);
                }
            }
        }catch(Exception e){ e.printStackTrace();}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (positionClicked == requestCode) {
			flagList[requestCode] = 1;
			resultCodeList[requestCode] = resultCode;
			Map map = (Map) this.getListView().getItemAtPosition(requestCode);
			String name = (String) map.get("title");
			String result = (resultCode == RESULT_OK ? "Pass" : "Failed");
			map.put("result", result);
			logd("Test:" + name + "result=" + result);
			Utils.saveStringValue(mContext, name, result);
			TestReportResult();
			for(int i = 0 ; i < itemcount; i++) {
				Map<String, ?> item1 = itemlist.get(i);
				if(item1.get("result").equals("NULL"))
				{
					warningTips.setVisibility(View.VISIBLE);
					break;
				}
				else
					warningTips.setVisibility(View.GONE);
			}
			mBaseAdapter.notifyDataSetChanged();
			Utils.writeTestLog(name, result);
			// ������Զ�����
			//logd("onActivityResult toStartAutoTest:" + toStartAutoTest);
			if (toStartAutoTest) {
				mHandler.postDelayed(mRunnable, AUTO_TEST_TIME_INTERVAL);
				int nexPos = getNextUntestedItem(MainApp.getInstance().mItemList);
				if (nexPos > 4)
					setSelection(nexPos - 4);
			}
			// /** auto test */
			// if (toStartAutoTest) {
			// positionClicked =
			// getNextAutoItem(MainApp.getInstance().mItemList);
			// loge("pos=" + positionClicked);
			// if (positionClicked < 0) {
			// toStartAutoTest = false;
			// } else {
			// Intent intent = (Intent)
			// MainApp.getInstance().mItemList.get(positionClicked).get("intent");
			// startActivityForResult(intent, positionClicked);
			// }
			// }
		}

	}

	private int getNextUntestedItem(List<? extends Map<String, ?>> list) {
		int pos = -1;
		for (int i = 0; i < list.size(); i++) {
			Map<String, ?> item = list.get(i);
			if ("NULL".equals(item.get("result"))) {
				// if ("true".equals(item.get("auto")) &&
				// "NULL".equals(item.get("result"))) {
				pos = i;
				break;
			}
		}
		return pos;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			curBackButtonTime = System.currentTimeMillis();
			if (curBackButtonTime - lastBackButtonTime < 2000)
				mExitFlag = true;
			lastBackButtonTime = curBackButtonTime;
		}

		return super.onKeyUp(keyCode, event);
	}

	public void finish() {

		/*
		 * if (mExitFlag == false) { new
		 * AlertDialog.Builder(this).setTitle(getString
		 * (R.string.control_center_quit_confirm))
		 * .setPositiveButton(getString(R.string.yes), new
		 * DialogInterface.OnClickListener() {
		 * 
		 * public void onClick(DialogInterface dialog, int which) {
		 * 
		 * mExitFlag = true; finish(); }
		 * }).setNegativeButton(getString(R.string.no), new
		 * DialogInterface.OnClickListener() {
		 * 
		 * public void onClick(DialogInterface dialog, int which) {
		 * 
		 * } }).setCancelable(false).show(); return; }
		 */

		if (!mExitFlag) {
			toast(getString(R.string.back_prompt));
			return;
		}
		/** write NV_FACTORY_DATA_3_I result */
		if (Values.ENABLE_NV) {
		}
		super.finish();
	}

	private BaseAdapter mBaseAdapter = new BaseAdapter() {

		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null)
				convertView = mInflater.inflate(R.layout.list_item, null);

			ImageView image = (ImageView) convertView
					.findViewById(R.id.icon_center);
			Button text = (Button) convertView
					.findViewById(R.id.text_center);
			text.setText((String) (MainApp.getInstance().mItemList
					.get(position).get("title")));

			String result = (String) (String) (MainApp.getInstance().mItemList
					.get(position).get("result"));
			// ����result��ֵ����ListView��ͼƬ
			// wmd
			if (result.equals("NULL") == false){
				image.setBackground(result.equals("Pass") ? getDrawable(R.drawable.main_screen_btn_green_bg) : getDrawable(R.drawable.main_screen_btn_red_bg));
				text.setBackgroundColor(result.equals("Pass") ? Color.GREEN : Color.RED);
			}else{
				image.setBackgroundColor(Color.TRANSPARENT);
				text.setBackground(getDrawable(R.drawable.main_screen_btn_bg));
			}
//			if (result.equals("NULL") == false)
//				image.setImageBitmap(result.equals("Pass") ? passBitmap
//						: failBitmap);
//			else
//				image.setImageBitmap(null);
			AbsListView.LayoutParams param = new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT,
					getResources().getDimensionPixelSize(R.dimen.main_screen_gridview_item_height));
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

	private void autoTestItems(List<? extends Map<String, ?>> list) {
		int testNum = list.size();
		for (int pos = 0; pos < testNum; pos++) {
		}
	}

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			/** auto test */
			if (toStartAutoTest) {
				positionClicked = getNextUntestedItem(MainApp.getInstance().mItemList);
				//logd("toStartAutoTest mRunnable positionClicked:" + positionClicked);
				if (positionClicked < 0) {
					toStartAutoTest = false;
					quickTestEnabled = false;
					mHandler.sendEmptyMessage(MSG_REFRESH);
				} else {
					Intent intent = (Intent) MainApp.getInstance().mItemList
							.get(positionClicked).get("intent");
					intent.putExtra(Values.KEY_SERVICE_INDEX, positionClicked);
					startActivityForResult(intent, positionClicked);
					//logd("toStartAutoTest mRunnable startActivityForResult");
				}
			}
		}
	};

	BroadcastReceiver mViewBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			mBaseAdapter.notifyDataSetChanged();
		}
	};

	// Bug703, mount exfat failed in recovery mode - format sd to vfat when reset in factory test, wmd, 2021.0208
	private void sendFactoryResetBroadcast(){
		Intent resetIntent = new Intent("android.intent.action.FACTORY_RESET");
		resetIntent.setPackage("android");
		resetIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		resetIntent.putExtra(Intent.EXTRA_REASON, "FactoryReset");
		resetIntent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, true);
		Framework.this.sendBroadcast(resetIntent);
		finish();
	}

	private void formatSdcard(){
		Log.d(TAG, "[formatSdcard] start --> ");
		StorageManager sm = Framework.this.getSystemService(StorageManager.class);
		final List<VolumeInfo> volumes = sm.getVolumes();
		for (VolumeInfo vol : volumes) {
			DiskInfo diskInfo = vol.getDisk();
			Log.d(TAG, "formatSdcard vol.getDescription : " + vol.getDescription());
			Log.d(TAG, "formatSdcard vol.getPath : " + vol.getPath());
			Log.d(TAG, "formatSdcard vol.fsType : " + vol.fsType);
			if (diskInfo != null) {
				Log.d(TAG, "formatSdcard diskInfo.getDescription : " + diskInfo.getDescription());
			}
			if (diskInfo != null && diskInfo.isSd()) {
				Log.d(TAG, "formatSdcard SD disk id : " + diskInfo.getId());
				new PartitionTask().execute(diskInfo.getId());
				break;
			}
		}
	}

	class PartitionTask extends AsyncTask<String, Integer, Exception> {

		@Override
		protected Exception doInBackground(String... params) {
			StorageManager sm = Framework.this.getSystemService(StorageManager.class);
			try {
				Log.d(TAG, "PartitionTask [doInBackground] partitionPublic");
				sm.partitionPublic(params[0]);
				return null;
			} catch (Exception e) {
				Log.e(TAG, "PartitionTask [doInBackground] e : " + e);
				return e;
			}
		}

		@Override
		protected void onPostExecute(Exception e) {
			super.onPostExecute(e);
			boolean isResult = true;
			if (e != null) {
				//format sdcard fail
				isResult = false;
			}
			Log.d(TAG, "PartitionTask [onPostExecute] isResult : " + isResult);
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					sendFactoryResetBroadcast();
				}
			});
		}
	}

	private void factoryResetHandler(){
		if(0 == Settings.Global.getInt(this.getContentResolver(), "gxx_enable_factory_reset"/*PolicyManagerUtils.GXX_ENABLE_FACTORY_RESET */,1 )){
			Toast.makeText(this, R.string.gxx_enable_factory_reset, Toast.LENGTH_SHORT).show();
			return;
		}

		new AlertDialog.Builder(this)
				.setTitle(R.string.factory_reset_text)
				.setMessage(R.string.factory_reset_summary)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialoginterface, int i) {
					formatSdcard();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialoginterface, int i) {
					finish();
				}
			})
			.setCancelable(false).show();
	}

	/** Fixed */

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}

	private void loge(Object e) {

		if (e == null)
			return;
		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	private static void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}
}
