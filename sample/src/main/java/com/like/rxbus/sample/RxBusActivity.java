package com.like.rxbus.sample;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.like.rxbus.RxBus;
import com.like.rxbus.annotations.RxBusSubscribe;
import com.like.rxbus.annotations.RxBusThread;

public class RxBusActivity extends BaseRxBusActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_rxbus);
        RxBus.register(this);
    }

    public void clickAnnotation(View view) {
        RxBusMessageUtils.post();
    }

    public void clickSticky(View view) {
        RxBus.postSticky("RxBusStickyActivity1", 111);
        RxBus.postSticky("RxBusStickyActivity2", "3", 222.99);
        startActivity(new Intent(this, RxBusStickyActivity.class));
    }

    public void clickSticky111(View view) {
//        RxBus.postSticky("stick", 111);
//        startActivity(new Intent(this, StickyActivity.class));
    }

    @RxBusSubscribe(tags = {"RxBusActivity1", "RxBusActivity2"}, code = "1", thread = RxBusThread.IO)
    public void test(String data) {
        RxBusMessageUtils.handleMessage(this, data);
    }

    @RxBusSubscribe(tags = "RxBusActivity3")
    public void test(Integer data) {
        RxBusMessageUtils.handleMessage(this, data);
    }

}
