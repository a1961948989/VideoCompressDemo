package com.guo.videocompressdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by guoh on 2018/6/28.
 * 功能描述：选择功能
 * 需要的参数：
 */
public class MainActivity extends AppCompatActivity {

    private Button btnVideoCompress;
    private Button btnVideoJoin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_main);
        initViews();
        initData();
        addListeners();
    }

    private void initViews(){
        btnVideoCompress=findViewById(R.id.btn_compress);
        btnVideoJoin=findViewById(R.id.btn_join);
    }

    private void addListeners(){
        btnVideoCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,VideoCompressActivity.class));
            }
        });
        btnVideoJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,VideoJoinActivity.class));
            }
        });
    }

    private void initData(){

    }
}
