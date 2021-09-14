package com.gosuncn.zfyhwapidemo.util;
import com.gosuncn.zfyhwapidemo.util.FileUtil;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Spring on 2015/11/7.
 * 下载工具类
 */
public class HttpDownloader {
    private URL url = null;
    private final String TAG = "TAG";

    /**
     * 读取文本文件
     * @param urlStr url路径
     * @return 文本信息
     * 根据url下载文件，前提是这个文件中的内容是文本，
     * 1.创建一个URL对象
     * 2.通过URL对象，创建一个Http连接
     * 3.得到InputStream
     * 4.从InputStream中得到数据
     */
    public String download(String urlStr) {
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader bufferedReader = null;

        try {
            url = new URL(urlStr);
            //创建http连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            //使用IO流读取数据
            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("TAG","下载txt文件");
        Log.e("TAG",sb.toString());
        return sb.toString();
    }
    private static long getFileSize(File file) throws Exception
    {
        long size = 0;
        if (file.exists()){
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }
    /**
     * 读取任何文件
     * 返回-1 ，代表下载失败。返回0，代表成功。返回1代表文件已经存在
     *
     * @param urlStr
     * @param path
     * @param fileName
     * @return
     */
    public int downlaodFile(Context context, String urlStr, String path, String fileName) {
        InputStream input = null;
        try {
            FileUtil fileUtil = new FileUtil(context);
            if (fileUtil.isFileExist(path + fileName)) {
                File file = new File(path + fileName);
                if(file != null){
                    file.delete();
                }
            }
            Log.e("TAG","httpdownload start urlStr："+urlStr);
            long startTime = System.currentTimeMillis(); // 开始下载时获取开始时间
            {
                input = getInputStearmFormUrl(urlStr);
                File resultFile = fileUtil.write2SDFromInput(path,fileName,input);

                long curTime = System.currentTimeMillis();
                int usedTime = (int) ((curTime-startTime)/1000);

                if(usedTime==0)usedTime = 1;
                Log.e("TAG","httpdownload  usedTime:"+usedTime+ " fileName:"+fileName);

                long filesize = 0L;
                try {
                    filesize = getFileSize(new File(path + fileName));
                }catch (Exception e){

                }
                Log.e("TAG","httpdownload  filesize:"+filesize);
                int downloadSpeed = (int)((filesize /usedTime)/1024); // 下载速度
                Log.e("TAG","httpdownload  downloadSpeed:"+downloadSpeed);
                if (resultFile == null)
                    return -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        finally {
            try {
                if(input != null)
                    input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  0;
    }


    public InputStream getInputStearmFormUrl(String urlStr) throws IOException {
        url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36)");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        InputStream input = conn.getInputStream();
        return input;
    }
}
