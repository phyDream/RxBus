package com.like.rxbus.sample;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

import com.like.rxbus.annotations.RxBusSubscribe;
import com.like.rxbus.annotations.RxBusThread;

public class RxBusActivity extends BaseRxBusActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_rxbus);
    }

    public void clickAnnotation(View view) {
        RxBusMessageUtils.post();
    }

    public void clickSticky(View view) {
        startActivity(new Intent(this, RxBusStickyActivity.class));
    }

    @RxBusSubscribe("RxBusActivity1")
    public void test() {
        RxBusMessageUtils.handleMessage(this, "");
    }

    @RxBusSubscribe("RxBusActivity2")
    public void test(int data) {
        RxBusMessageUtils.handleMessage(this, data);
    }

    @RxBusSubscribe(value = {"RxBusActivity3", "RxBusActivity_extra"}, thread = RxBusThread.IO)
    public void test(String data) {
        RxBusMessageUtils.handleMessage(this, data);
    }
}
