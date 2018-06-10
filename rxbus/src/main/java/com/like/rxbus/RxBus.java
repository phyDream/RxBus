package com.like.rxbus;

import android.support.annotation.NonNull;

import com.like.rxbus.annotations.RxBusSubscribe;

import io.reactivex.Scheduler;

/**
 * RxBus工具类，可以发送普通消息和Sticky消息<br/>
 * 自动防重复注册宿主、自动防重复注册标签（及同一个宿主下的标签不重复）。<br/>
 * 支持背压，采用的策略是BackpressureStrategy.DROP。<br/>
 * <p>
 * 使用方法：<br/>
 * 1、发送普通消息可以使用post()方法。
 * 2、发送Sticky消息使用postSticky()方法，注意Sticky消息在第一次接收后，就会销毁。
 * 和发送普通消息相比，发送Sticky消息，实际上就是延迟了接收消息的时间，用于替代startActivity传数据。<br/>
 * 2、接收消息使用{@link RxBusSubscribe}注解一个方法，其中可以设置Activity、Fragment、标签组、请求码、线程、Sticky标记。<br/>
 * 3、Activity、Fragment是用来控制RxBus的生命周期。<br/>
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/4 19:32
 */
public class RxBus {
    public static String TAG = "RxBus";

    /**
     * 注册宿主如果父类中已经注册，那么子类中可以不用注册了，注册了也无效。
     *
     * @param host 宿主
     */
    public static void register(@NonNull Object host) {
        if (!RxBusEventManager.getInstance().isRegisteredHost(host)) {// 避免重复注册某个宿主
            new RxBusProxy().init(host);
        }
    }

    /**
     * 取消注册某个宿主中的所有订阅者，通常在onDestroy()时调用
     *
     * @param host 宿主，通常用this
     */
    public static void unregister(@NonNull Object host) {
        RxBusEventManager.getInstance().remove(host);
    }

    /**
     * 订阅事件
     *
     * @param host             宿主，通常用this
     * @param scheduler        线程
     * @param tag              事件的标签
     * @param receivedListener 接收消息的监听器
     */
    static <T> void subscribe(@NonNull Object host, @NonNull String activityOrFragment, @NonNull String code, @NonNull String tag, Scheduler scheduler, boolean isSticky, OnReceivedListener<T> receivedListener) {
        RxBusEvent<T> event = new RxBusEvent<>(host, activityOrFragment, code, tag, scheduler, isSticky, receivedListener);
        RxBusEventManager.getInstance().subscribe(event);
    }

    public static <T> void post(@NonNull String tag) {
        RxBusEventManager.getInstance().post(tag);
    }

    public static <T> void post(@NonNull String tag, T content) {
        RxBusEventManager.getInstance().post(tag, content);
    }

    public static <T> void post(@NonNull String tag, @NonNull String code, T content) {
        RxBusEventManager.getInstance().post(tag, code, content);
    }

    /**
     * 发送Sticky消息
     */
    public static <T> void postSticky(@NonNull String tag, T content) {
        RxBusEventManager.getInstance().postSticky(tag, content);
    }

    /**
     * 发送Sticky消息
     */
    public static <T> void postSticky(@NonNull String tag, @NonNull String code, T content) {
        RxBusEventManager.getInstance().postSticky(tag, code, content);
    }

    /**
     * 接受发送来的消息
     *
     * @author like
     * @version 1.0
     * @created at 2017/3/29 22:57
     */
    public interface OnReceivedListener<T> {
        void onReceive(RxBusContent<T> rxBusContent) throws Exception;// 直接拋出异常，避免发送事件中断。
    }

}
