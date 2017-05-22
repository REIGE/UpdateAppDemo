package com.reige.updatedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private UpdateAppEngine updateAppEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        updateAppEngine = new UpdateAppEngine(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //没有在没有下载中 开始下载
                if(!updateAppEngine.isDownLoading()) {
                    updateAppEngine.startDownLoad("https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk", "qq");
                }
            }
        });
    }

    private void initView() {
        button = (Button)findViewById(R.id.button);
    }
}
