package com.like.rxbus.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.like.rxbus.RxBus;
import com.like.rxbus.annotations.RxBusSubscribe;
import com.like.rxbus.annotations.RxBusThread;

/**
 * Created by like on 2017/4/5.
 */

public class RxBusStickyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_rxbus1);
        RxBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.unregister(this);
    }

    public void clickAnnotation(View view) {
        RxBusMessageUtils.post();
    }

    @RxBusSubscribe(tags = "RxBusStickyActivity1", isSticky = true)
    public void test(int data) {
        RxBusMessageUtils.handleMessage(this, data);
    }

    @RxBusSubscribe(tags = "RxBusStickyActivity2", code = "3", isSticky = true)
    public void test(Double data) {
        RxBusMessageUtils.handleMessage(this, data);
    }

    @RxBusSubscribe(tags = "RxBusActivity1", code = "2")
    public void test(String data) {
        RxBusMessageUtils.handleMessage(this, data);
    }
}
