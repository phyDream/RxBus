package com.like.rxbus;

import android.support.annotation.NonNull;

import com.like.logger.LogLevel;
import com.like.logger.Logger;
import com.like.rxbus.annotations.RxBusSubscribe;

import io.reactivex.Scheduler;

/**
 * RxBus工具类，可以发送普通消息和Sticky消息<br/>
 * 自动防重复注册宿主、自动防重复注册标签（及同一个宿主下的标签不重复）。<br/>
 * 支持背压，采用的策略是BackpressureStrategy.DROP。<br/>
 * <p>
 * 使用方法：<br/>
 * 1、在创建某个类的实例时调用register(this)方法进行注册宿主（通常在Activity的onCreate()方法中调用）。
 * 当在父类调用register(this)方法后，在子类无需再调用了，调用了也行，会自动防重复注册宿主。<br/>
 * 2、在销毁某个类的实例时调用unregister(this)方法进行取消注册宿主（通常在Activity的onDestroy()方法中调用）。<br/>
 * 3、发送普通消息可以使用post()、postByTag()方法。
 * 发送Sticky消息使用postSticky()方法，注意Sticky消息在第一次接收后，就会销毁，以后就和普通消息一样了。
 * 和发送普通消息相比，发送Sticky消息，实际上就是延迟了第一次接收消息的时间。<br/>
 * 4、接收消息使用{@link RxBusSubscribe}注解一个方法，其中可以设置标签组、线程、Sticky标记。<br/>
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/4 19:32
 */
public class RxBus {
    static {
        Logger.setTag("RxBus");
        RxBus.setLogLevel(LogLevel.SIMPLE);
    }

    public static void setLogLevel(LogLevel logLevel) {
        Logger.setLogLevel(logLevel);
    }

    /**
     * 注册宿主如果父类中已经注册，那么子类中可以不用注册了
     *
     * @param host 宿主
     */
    public static void register(@NonNull Object host) {
        if (!RxBusEventManager.getInstance().isRegisteredHost(host)) {// 避免重复注册某个宿主
            new RxBusProxy().init(host);
        }
    }

    /**
     * 订阅事件
     *
     * @param host             宿主，通常用this
     * @param scheduler        线程
     * @param tag              事件的标签
     * @param receivedListener 接收消息的监听器
     */
    static <T> void subscribe(@NonNull Object host, @NonNull String tag, Scheduler scheduler, boolean isSticky, OnReceivedListener<T> receivedListener) {
        RxBusEvent<T> event = new RxBusEvent<>(host, tag, scheduler, isSticky, receivedListener);
        RxBusEventManager.getInstance().subscribe(event);
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
     * 在主界面退出时调用
     */
    public static void clear() {
        RxBusEventManager.getInstance().clear();
    }

    /**
     * 发送消息，采用{@link RxBusSubscribe#DEFAULT_TAG}
     */
    public static void post() {
        RxBusEventManager.getInstance().post(RxBusSubscribe.DEFAULT_TAG);
    }

    /**
     * 发送消息，采用{@link RxBusSubscribe#DEFAULT_TAG}
     *
     * @param content
     */
    public static <T> void post(T content) {
        RxBusEventManager.getInstance().post(RxBusSubscribe.DEFAULT_TAG, content);
    }

    /**
     * 发送消息
     *
     * @param tag
     * @param content
     */
    public static <T> void post(@NonNull String tag, T content) {
        RxBusEventManager.getInstance().post(tag, content);
    }

    /**
     * 发送消息
     *
     * @param tag
     */
    public static void postByTag(@NonNull String tag) {
        RxBusEventManager.getInstance().post(tag);
    }

    /**
     * 发送Sticky消息，采用{@link RxBusSubscribe#DEFAULT_TAG}
     *
     * @param content
     */
    public static <T> void postSticky(T content) {
        RxBusEventManager.getInstance().postSticky(RxBusSubscribe.DEFAULT_TAG, content);
    }

    /**
     * 发送Sticky消息
     *
     * @param tag
     * @param content
     */
    public static <T> void postSticky(@NonNull String tag, T content) {
        RxBusEventManager.getInstance().postSticky(tag, content);
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
