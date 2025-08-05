package com.sobot.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.api.model.Information;

public class MainActivity extends AppCompatActivity {
    private Button qidong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qidong=findViewById(R.id.qidong);
//        ZCSobotApi.setSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN,true);
        qidong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Information info=new Information();
                info.setApp_key("1c1da2c0aad047d7ba1d14ecd18ae4f6");
                ZCSobotApi.openZCServiceCenter(MainActivity.this,info);
            }
        });

    }
}