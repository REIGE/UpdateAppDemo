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
        UpdateAppEngine.init(this);
        updateAppEngine = UpdateAppEngine.getInstance();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAppEngine.downLoad(MainActivity.this,"qq","https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk");
            }
        });
    }

    private void initView() {
        button = (Button)findViewById(R.id.button);

    }
}
