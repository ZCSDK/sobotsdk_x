package com.sobot.chat.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.application.MyApplication;

import java.io.File;
import java.security.MessageDigest;

/**
 * @author Created by jinxl on 2018/12/3.
 */
public class SobotPathManager {
    private Context mContext;

    private static String mRootPath;

    private static final String ROOT_DIR = "download";
    private static final String VIDEO_DIR = "video";
    private static final String VOICE_DIR = "voice";
    private static final String PIC_DIR = "pic";
    private static final String CACHE_DIR = "cache";

    private SobotPathManager(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
        } else {
            mContext = MyApplication.getInstance().getLastActivity();
        }
    }

    private static SobotPathManager instance;

    public static SobotPathManager getInstance() {
        if (instance == null) {
            synchronized (SobotPathManager.class) {
                if (instance == null) {
                    instance = new SobotPathManager(SobotApp.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public String getRootDir() {
        if (mRootPath == null) {
            String packageName = mContext != null ? mContext.getPackageName() : "";
            // 改用应用专属外部存储目录（Android 10+ 兼容）
            File externalFilesDir = mContext.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                mRootPath = externalFilesDir.getPath() + File.separator + ROOT_DIR + File.separator + encode(packageName + "cache_sobot");
            } else {
                // 回退到内部存储
                mRootPath = mContext.getFilesDir().getPath() + File.separator + ROOT_DIR + File.separator + encode(packageName + "cache_sobot");
            }
        }
        LogUtils.d("SobotPathManager getRootDir() = " + mRootPath);
        return mRootPath;
    }

    //sdcard/download/xxxx/video
    public String getVideoDir() {
        String videoDir = mContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath() + File.separator;
        LogUtils.d("SobotPathManager getVideoDir() = " + videoDir);
        return videoDir;
    }

    //sdcard/download/xxxx/voice
    public String getVoiceDir() {
        String voiceDir = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getPath() + File.separator;
        LogUtils.d("SobotPathManager getVoiceDir() = " + voiceDir);
        return voiceDir;
    }

    //sdcard/download/xxxx/pic
    public String getPicDir() {
        String picDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + File.separator;
        LogUtils.d("SobotPathManager getPicDir() = " + picDir);
        return picDir;
    }

    //sdcard/download/xxxx/cache
    public String getCacheDir() {
        String cacheDir = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator + CACHE_DIR + File.separator;
        LogUtils.d("SobotPathManager getCacheDir() = " + cacheDir);
        return cacheDir;
    }

    private String encode(String str) {
        StringBuilder sb = new StringBuilder();

        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            byte[] digest = instance.digest(str.getBytes());
            for (byte b : digest) {
                int num = b & 0xff;
                String hex = Integer.toHexString(num);
                if (hex.length() < 2) {
                    sb.append("0");
                }
                sb.append(hex);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
