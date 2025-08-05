package com.sobot.demo;

import android.app.Application;

import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.apiUtils.SobotBaseUrl;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZCSobotApi.setShowDebug(true);
//        SobotBaseUrl.setApi_Host("https://test.sobot.com");
        ZCSobotApi.initSobotSDK(this,"1c1da2c0aad047d7ba1d14ecd18ae4f6","");
    }
}
