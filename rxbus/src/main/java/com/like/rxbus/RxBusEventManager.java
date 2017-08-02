package com.like.rxbus;

import android.support.annotation.NonNull;

import com.like.logger.Logger;

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
     * sticky事件缓存，key为tag，value为内容
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
                return true;
            }
        }
        return false;
    }

    synchronized void subscribe(RxBusEvent event) {
        // 同一个host，避免重复订阅某个tag
        if (eventList.contains(event)) {
            Logger.e("RxBus", "宿主：" + event.getHost() + "，已经订阅过标签：" + event.getTag());
            return;
        }
        // 获取某个标签对应的Subject，因为这个Subject和tag是一一对应的。
        event.setSubject(getSubjectIfNullCreate(event.getTag()));
        eventList.add(event);

        if (event.isSticky()) {
            if (stickyMap.containsKey(event.getTag())) {
                if (event.invoke())
                    Logger.w("RxBus", "Sticky 订阅 宿主：" + event.getHost() + "，标签：" + event.getTag() + "，事件总数：" + getTagCount() + "，宿主总数：" + getHostCount());
                else
                    Logger.w("RxBus", "Sticky 订阅   宿主：" + event.getHost() + "，标签：" + event.getTag() + " 失败~~~~~~~~~~");

                postActual(event.getSubject(), event.getTag(), stickyMap.get(event.getTag()), true);
                stickyMap.remove(event.getTag());
            }
        } else {
            if (event.invoke())
                Logger.w("RxBus", "订阅 宿主：" + event.getHost() + "，标签：" + event.getTag() + "，事件总数：" + getTagCount() + "，宿主总数：" + getHostCount());
            else
                Logger.w("RxBus", "订阅   宿主：" + event.getHost() + "，标签：" + event.getTag() + " 失败~~~~~~~~~~");
        }
    }

    synchronized void post(@NonNull String tag) {
        postActual(getSubjectIfNullCreate(tag), tag, new RxBusContent<T>(), false);
    }

    synchronized void post(@NonNull String tag, T content) {
        postActual(getSubjectIfNullCreate(tag), tag, new RxBusContent<>(content), false);
    }

    synchronized void postSticky(@NonNull String tag, T content) {
        RxBusContent<T> rxBusContent = new RxBusContent<>(content);
        stickyMap.put(tag, rxBusContent);
        postActual(getSubjectIfNullCreate(tag), tag, rxBusContent, true);
    }

    private synchronized void postActual(Subject<RxBusContent<T>> subject, @NonNull String tag, RxBusContent<T> rxBusContent, boolean isSticky) {
        if (null != subject) {
            subject.onNext(rxBusContent);
            if (isSticky) {
                Logger.d("RxBus", "Sticky 发送了消息 --> tag：" + tag + "，内容：" + rxBusContent.getContent());
            } else {
                Logger.d("RxBus", "发送了消息 --> tag：" + tag + "，内容：" + rxBusContent.getContent());
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
                if (!event.getSubject().hasObservers()) {
                    stickyMap.remove(event.getTag());
                    Logger.w("RxBus", "取消   事件：" + event.getTag() + "，剩余事件总数：" + getTagCount());
                }
            }
        }
        Logger.w("RxBus", "取消 宿主：" + host + "，剩余宿主总数：" + getHostCount());
    }

    synchronized void clear() {
        for (RxBusEvent event : eventList) {
            event.dispose();
        }
        stickyMap.clear();
        eventList.clear();
    }

    /**
     * 因为一个tag对应一个subject，所以如果存在，就不需要创建了。
     *
     * @param tag
     * @return
     */
    private synchronized Subject<RxBusContent<T>> getSubjectIfNullCreate(@NonNull String tag) {
        Subject<RxBusContent<T>> subject = null;
        for (RxBusEvent event : eventList) {
            if (tag.equals(event.getTag())) {
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

    private synchronized int getHostCount() {
        HashSet<Object> set = new HashSet<>();
        for (RxBusEvent event : eventList) {
            set.add(event.getHost());
        }
        return set.size();
    }

    private synchronized int getTagCount() {
        HashSet<String> set = new HashSet<>();
        for (RxBusEvent event : eventList) {
            set.add(event.getTag());
        }
        return set.size();
    }

}
