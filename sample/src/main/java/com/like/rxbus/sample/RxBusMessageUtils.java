package com.like.rxbus.sample;

import com.like.logger.Logger;
import com.like.rxbus.RxBus;

public class RxBusMessageUtils {
    public static void post() {
        RxBus.post("BaseRxBusActivity");
        RxBus.post("RxBusActivity1", "1", "1111111111111111");
        RxBus.post("RxBusActivity1", "2", "2222222222222");
        RxBus.post("RxBusActivity2", 2);
        RxBus.post("RxBusActivity3", null);
        RxBus.postSticky("RxBusStickyActivity1", 111);
        RxBus.postSticky("RxBusStickyActivity2", "3", 222.99);
    }

    public static void handleMessage(Object object, Object content) {
        Logger.e("RxBus", object + " 处理了消息，内容：" + content + " , 线程：" + Thread.currentThread().getName());
    }

}
