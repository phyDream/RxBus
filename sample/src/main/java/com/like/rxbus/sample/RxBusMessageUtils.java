package com.like.rxbus.sample;

import com.like.logger.Logger;
import com.like.rxbus.RxBus;

public class RxBusMessageUtils {
    public static void post() {
        RxBus.post();
        RxBus.post("123");
        RxBus.postByTag("RxBusActivity1");
        RxBus.post("RxBusActivity2", 123);
        RxBus.post("RxBusActivity3", null);
        RxBus.post("RxBusActivity_extra", "content-RxBusActivity_extra");
        RxBus.postSticky("RxBusStickyActivity", "123");
        RxBus.postSticky(1);
    }

    public static void handleMessage(Object object, Object content) {
        Logger.e(object + " 处理了消息，内容：" + content + " , 线程：" + Thread.currentThread().getName());
    }

}
