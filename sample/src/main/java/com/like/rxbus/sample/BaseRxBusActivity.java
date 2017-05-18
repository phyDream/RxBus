package com.like.rxbus.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.like.rxbus.RxBus;
import com.like.rxbus.annotations.RxBusSubscribe;

public class BaseRxBusActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.unregister(this);
    }

    @RxBusSubscribe()
    public void test() {
        RxBusMessageUtils.handleMessage(this, "");
    }

}
