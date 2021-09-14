package com.gosuncn.zfyhwapidemo.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Spring on 2015/11/7.
 */
public class FileUtil {
    private Context mContext;
    private String SDPATH;

    public FileUtil(Context context) {
        mContext = context;
        SDPATH = "/storage/9016-4EF8/11/";//context.getFilesDir().getPath().toString();//Environment.getExternalStorageDirectory() + "/" ;
    }

    public String getSDPATH() {
        return SDPATH;
    }

    public FileUtil(Context context, String SDPATH){
        mContext = context;
        //得到外部存储设备的目录（/SDCARD）
        SDPATH = "/storage/9016-4EF8/11/";//context.getFilesDir().getPath().toString();//Environment.getExternalStorageDirectory() + "/" ;
    }

    /**
     * 在SD卡上创建文件
     * @param fileName
     * @return
     * @throws java.io.IOException
     */
    public File createSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        Log.d("FileUtil", "createSDFile "+file.getAbsolutePath());
        return file;
    }

    /**
     * 在SD卡上创建目录
     * @param dirName 目录名字
     * @return 文件目录
     */
    public File createDir(String dirName){
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 判断文件是否存在
     * @param fileName
     * @return
     */
    public boolean isFileExist(String fileName){
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    public File write2SDFromInput(String path,String fileName,InputStream input){
        File file = null;
        OutputStream output = null;
        int len = 0;
        try {
            createDir(path);
            file =createSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte [] buffer = new byte[100 * 1024];
            while((len = input.read(buffer)) != -1){
                Log.d("FileUtil", "len"+len);
                output.write(buffer, 0, len);
                //output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
