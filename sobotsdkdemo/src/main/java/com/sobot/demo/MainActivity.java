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
                info.setPartnerid("jiaqian");
                info.setApp_key("d68b4e7db1f942bf9507f60a4d643eaa");
//                ZCSobotApi.setInternationalLanguage(MainActivity.this,"zh",true);
                ZCSobotApi.openZCChat(MainActivity.this,info);
            }
        });

    }
}