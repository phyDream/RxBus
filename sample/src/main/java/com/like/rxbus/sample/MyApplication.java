package com.like.rxbus.sample;

import android.app.Application;

import com.like.logger.LogLevel;
import com.like.rxbus.RxBus;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxBus.setLogLevel(LogLevel.SIMPLE);
    }
}
