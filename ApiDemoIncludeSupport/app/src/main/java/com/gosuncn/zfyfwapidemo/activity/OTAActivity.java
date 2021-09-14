package com.gosuncn.zfyhwapidemo.activity;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.AriaManager;
import com.arialyy.aria.core.common.HttpOption;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.gosuncn.zfyfw.service.GSFWManager;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gosuncn.zfyhwapidemo.R;

public class OTAActivity extends Activity {
    private static final String TAG = OTAActivity.class.getSimpleName();
    private Button mbutton_server1;
    private Button mbutton_server2;
    private Button mbutton_55_87;
    private Button mbutton_server1_full;
    private Button mbutton_server2_full;
    private Button mbutton_55_87_full;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT));
        mbutton_server1 = new Button(this);
        mbutton_server2 = new Button(this);
        mbutton_55_87 = new Button(this);
        mbutton_server1_full = new Button(this);
        mbutton_server2_full = new Button(this);
        mbutton_server1.setText("73_150 差分包");
        mbutton_server1.setGravity(Gravity.CENTER);
        mbutton_server1.setTag(1);
        mbutton_server2.setText("56_120 差分包");
        mbutton_server2.setGravity(Gravity.CENTER);
        mbutton_server2.setTag(2);
        mbutton_55_87.setText("55.87 差分包");
        mbutton_55_87.setGravity(Gravity.CENTER);
        mbutton_55_87.setTag(3);
        mbutton_server1_full.setText("73_150 整包");
        mbutton_server1_full.setGravity(Gravity.CENTER);
        mbutton_server1_full.setTag(4);
        mbutton_server2_full.setText("56_120 整包");
        mbutton_server2_full.setGravity(Gravity.CENTER);
        mbutton_server2_full.setTag(5);
        rootView.addView(mbutton_server1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, OTAActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_server2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, OTAActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_55_87, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, OTAActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_server1_full, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, OTAActivity.this.getResources().getDisplayMetrics())));
        rootView.addView(mbutton_server2_full, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, OTAActivity.this.getResources().getDisplayMetrics())));

        setContentView(rootView);
        mbutton_server1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStart("http://123.206.73.150:6224/gmvcs/uom/package/unauthorized/rest/download/updatePackage?file=161014_161003_user.zip&type=1&model=H6",
                                "73_150_ota.zip");
                    }
                }).start();
            }
        });
        mbutton_server2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStart("http://192.168.56.120:6222/gmvcs/uom/device/package/unauthorized/rest/download?file=PKG4401000020201009153154ffcffffff.zip&id=GM4401000020201009153154ffcfffff1",
                                "56_120_ota.zip");
                    }
                }).start();
            }
        });
        mbutton_55_87.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStart("http://wangmingdong.top/161014_161003_user.zip",
                                "55_87_ota.zip");
                    }
                }).start();
            }
        });
        mbutton_server1_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStart("http://123.206.73.150:6224/gmvcs/uom/package/unauthorized/rest/download/updatePackage?file=H6_GXX_L016_20201003_user_SKY_OTA.zip&type=1&model=H6",
                                "73_150_full.zip");
                    }
                }).start();
            }
        });
        mbutton_server2_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStart("http://123.206.73.150:6224/gmvcs/uom/package/unauthorized/rest/download/updatePackage?file=H6_GXX_L016_20201003_user_SKY_OTA.zip&type=1&model=H6",
                                "73_150_full.zip");
                    }
                }).start();
            }
        });

        mbutton_55_87_full = new Button(this);
        mbutton_55_87_full.setText("55.87 整包");
        mbutton_55_87_full.setGravity(Gravity.CENTER);
        mbutton_55_87_full.setTag(5);
        rootView.addView(mbutton_55_87_full, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        48, OTAActivity.this.getResources().getDisplayMetrics())));
        mbutton_55_87_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
			    new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadStart("http://192.168.55.87/H6_GXX_L016_20201009_user_SKY_OTA.zip",
                                "55_87_full.zip");
                    }
                }).start();
            }
        });
        AriaManager ariaManager = Aria.init(this);
        Aria.get(this).getDownloadConfig().setThreadNum(1);
        Aria.get(this).getDownloadConfig().setMaxSpeed(0);
        Aria.get(this).getDownloadConfig().setMaxTaskNum(1);
        Aria.get(this).getDownloadConfig().setUseBlock(false);
        Aria.download(this).register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Aria.download(this).unRegister();
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
//        GSFWManager.getInstance().setHomeKeyDispatched(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        GSFWManager.getInstance().setHomeKeyDispatched(false);
    }


    private DownloadManager downloadManager;
    private long downloadId;

    @Download.onTaskRunning protected void running(DownloadTask task) {
        String key = task.getKey();
        int percent = task.getPercent();	//任务进度百分比
        String speed = task.getConvertSpeed();	//转换单位后的下载速度，单位转换需要在配置文件中打开
        String speed1 = String.valueOf(task.getSpeed()); //原始byte长度速度
        Log.d(TAG, "running key:"+key+" percent:"+percent+" speed:"+speed+" speed1:"+speed1);
    }

    @Download.onTaskComplete void taskComplete(DownloadTask task) {
        String key = task.getKey();
        Log.d(TAG, "taskComplete key:"+key);
        //在这里处理任务完成的状态
    }

    private void downloadStart(String urlStr, String fileName){
        Log.d(TAG, "downloadStart S");

//        String urlStr="http://123.206.73.150:6224/gmvcs/uom/package/unauthorized/rest/download/updatePackage?file=161003_161009_user.zip&type=1&model=G5";
//        urlStr = "http://192.168.56.120:6222/gmvcs/uom/device/package/unauthorized/rest/download?file=PKG4401000020201009153154ffcffffff.zip&id=GM4401000020201009153154ffcfffff1";
//        urlStr = "http://123.206.73.150:6224/gmvcs/uom/package/unauthorized/rest/download/updatePackage?file=H6_GXX_L016_20201009_user_SKY_OTA.zip&type=1&model=H6";
        String path="file";
        //String fileName="update3.zip";
//        com.gosuncn.zfyhwapidemo.util.HttpDownloader httpDownloader = new com.gosuncn.zfyhwapidemo.util.HttpDownloader();
//        httpDownloader.downlaodFile(OTAActivity.this, urlStr,path,fileName);

        HttpOption httpOption = new HttpOption();
        httpOption.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36)");
        httpOption.addHeader("Accept-Encoding", "gzip, deflate");
        httpOption.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpOption.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpOption.setRequestType(RequestEnum.GET);

        long taskId = Aria.download(this)
                .load(urlStr)     //读取下载地址
                .setFilePath("/storage/9016-4EF8/11/" + fileName) //设置文件保存的完整路径
                .create();   //创建并启动下载

//        if (downloadManager == null)
//            downloadManager = (DownloadManager) USBActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
//
//        //创建request对象
//        DownloadManager.Request request=new DownloadManager.Request(Uri.parse("http://123.206.73.150:6224/gmvcs/uom/package/unauthorized/rest/download/updatePackage?file=161003_161009_user.zip&type=1&model=G5"));
//        //设置什么网络情况下可以下载
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        //设置通知栏的标题
//        request.setTitle("OTA下载");
//        //设置通知栏的message
//        request.setDescription("OTA正在下载.....");
//        //设置漫游状态下是否可以下载
//        request.setAllowedOverRoaming(false);
//        //设置文件存放目录
//        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,"update2.zip");
//        //获取系统服务
//        downloadManager= (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        //进行下载
//        downloadId = downloadManager.enqueue(request);

        Log.d(TAG, "downloadStart E");
    }
}
