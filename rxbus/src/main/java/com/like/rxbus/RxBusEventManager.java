package com.like.rxbus;

import android.support.annotation.NonNull;

import com.like.logger.Logger;
import com.like.rxbus.annotations.RxBusSubscribe;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * {@link RxBusEvent}管理器
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/8 11:29
 */
class RxBusEventManager<T> {
    private List<RxBusEvent<T>> eventList;
    /**
     * sticky事件缓存，key为"tag;code"，value为内容
     */
    private ConcurrentMap<String, RxBusContent<T>> stickyMap;

    private volatile static RxBusEventManager sInstance = null;

    public static RxBusEventManager getInstance() {
        if (sInstance == null) {
            synchronized (RxBusEventManager.class) {
                if (sInstance == null) {
                    sInstance = new RxBusEventManager();
                }
            }
        }
        return sInstance;
    }

    private RxBusEventManager() {
        eventList = new ArrayList<>();
        stickyMap = new ConcurrentHashMap<>();
    }

    /**
     * 是否已经注册过指定宿主
     *
     * @param host
     * @return
     */
    synchronized boolean isRegisteredHost(@NonNull Object host) {
        for (RxBusEvent event : eventList) {
            if (host.equals(event.getHost())) {
                Logger.e(RxBus.TAG, "已经注册过宿主：" + event.getHost());
                return true;
            }
        }
        return false;
    }

    synchronized void subscribe(RxBusEvent event) {
        // 同一个host，避免重复订阅某个tag
        if (eventList.contains(event)) {
            Logger.e(RxBus.TAG, "已经订阅过事件：" + event.toString());
            return;
        }
        // 获取某个标签对应的Subject，因为这个Subject和tag是一一对应的。
        event.setSubject(getSubjectIfNullCreate(event.getTag(), event.getCode()));

        if (event.isSticky()) {
            String mapKey = event.getTag() + ";" + event.getCode();
            if (stickyMap.containsKey(mapKey)) {
                event.invoke();
                postActual(event.getSubject(), event.getTag(), event.getCode(), stickyMap.get(mapKey), true);
                stickyMap.remove(mapKey);
            }
        } else {
            if (event.invoke()) {
                eventList.add(event);
                Logger.i(RxBus.TAG, "订阅事件成功：" + event + "，事件总数：" + getEventCount() + " ，宿主总数：" + getHostCount());
            } else {
                Logger.e(RxBus.TAG, "订阅事件失败：" + event);
            }
        }
    }

    synchronized void post(@NonNull String tag) {
        postActual(getSubjectIfNullCreate(tag, RxBusSubscribe.DEFAULT_CODE), tag, RxBusSubscribe.DEFAULT_CODE, new RxBusContent<>(), false);
    }

    synchronized void post(@NonNull String tag, T content) {
        post(tag, RxBusSubscribe.DEFAULT_CODE, content);
    }

    synchronized void post(@NonNull String tag, @NonNull String code, T content) {
        postActual(getSubjectIfNullCreate(tag, code), tag, code, new RxBusContent<>(content), false);
    }

    synchronized void postSticky(@NonNull String tag, T content) {
        postSticky(tag, RxBusSubscribe.DEFAULT_CODE, content);
    }

    synchronized void postSticky(@NonNull String tag, @NonNull String code, T content) {
        RxBusContent<T> rxBusContent = new RxBusContent<>(content);
        stickyMap.put(tag + ";" + code, rxBusContent);
    }

    private synchronized void postActual(Subject<RxBusContent<T>> subject, @NonNull String tag, @NonNull String code, RxBusContent<T> rxBusContent, boolean isSticky) {
        if (null != subject) {
            subject.onNext(rxBusContent);
            if (isSticky) {
                Logger.v(RxBus.TAG, "发送了粘性消息 --> tag = " + tag + " code = " + code + " " + rxBusContent.getContent());
            } else {
                Logger.d(RxBus.TAG, "发送了消息 --> tag = " + tag + " code = " + code + " " + rxBusContent.getContent());
            }
        }
    }

    synchronized void remove(@NonNull Object host) {
        // 把host宿主对应的所有Subscription取消订阅，并把host从subscriptionManager中移除
        ListIterator<RxBusEvent<T>> iterator = eventList.listIterator();
        while (iterator.hasNext()) {
            RxBusEvent<T> event = iterator.next();
            if (event.getHost().equals(host)) {
                event.dispose();
                iterator.remove();
                Logger.i(RxBus.TAG, "取消事件：" + event + "，剩余事件总数：" + getEventCount());
            }
        }

        stickyMap.clear();// 每一次销毁，都清空粘性事件。

        Logger.i(RxBus.TAG, "取消宿主：" + host + "，剩余宿主总数：" + getHostCount());
    }

    /**
     * 因为一个event对应一个subject，所以如果存在，就不需要创建。如果不存在，就创建一个
     */
    private synchronized Subject<RxBusContent<T>> getSubjectIfNullCreate(@NonNull String tag, @NonNull String code) {
        Subject<RxBusContent<T>> subject = null;
        for (RxBusEvent event : eventList) {
            if (tag.equals(event.getTag()) && code.equals(event.getCode())) {
                subject = event.getSubject();
                break;
            }
        }
        if (null == subject) {
            // toSerialized method made bus thread safe
            Subject<RxBusContent<T>> subject1 = PublishSubject.create();
            subject = subject1.toSerialized();
        }
        return subject;
    }

    /**
     * 注册的宿主总数
     */
    private synchronized int getHostCount() {
        HashSet<Object> set = new HashSet<>();
        for (RxBusEvent event : eventList) {
            set.add(event.getHost());
        }
        return set.size();
    }

    /**
     * 注册的事件总数
     */
    private synchronized int getEventCount() {
        HashSet<RxBusEvent> set = new HashSet<>();
        for (RxBusEvent event : eventList) {
            set.add(event);
        }
        return set.size();
    }

//    /**
//     * 绑定onDestroy()方法
//     */
//    public LifecycleTransformer<T> bindDestroyEvent() {
//        if (host instanceof RxAppCompatActivity) {
//            return ((RxAppCompatActivity) host).bindUntilEvent(ActivityEvent.DESTROY);
//        } else if (host instanceof RxFragment) {
//            return ((RxFragment) host).bindUntilEvent(FragmentEvent.DESTROY);
//        }
//        return null;
//    }
}
