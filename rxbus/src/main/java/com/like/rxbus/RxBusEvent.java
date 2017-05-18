package com.like.rxbus;

import android.support.annotation.NonNull;

import com.like.logger.Logger;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.Subject;

/*
 * Subject表示一个同时是Observable和Observer的对象，所以Subject即使订阅者也是观察者。
 * 利用这个特性，我们就可以利用它发送和接收消息。Subject的onNext(Object)方法会向所有订阅者发送事件
 * SerializedSubject<T,R>包装后的Subject是线程安全
 * PublishSubject<T>，使用onNext() 发射消息时，已经订阅此Subject 的观察者会收到消息，后来才订阅的新观察者将无法接收已经发布过了的消息
 * ReplaySubject<T>，订阅者订阅时，按发射顺序向订阅者发射此主题发射过的每一条数据
 * BehaviorSubject<T>，向订阅者发射在订阅者订阅此Subject 之前的最近的一条数据以及之后的每一条数据
 * AsyncSubject<T>，一次只能向它的订阅者发射一条消息（发射多条消息时，只有最后一条是有效的），这里还有一点特殊的是，它必须调用 onCompleted 来完成一次新消息的发射
 */

/**
 * RxBus事件信息
 *
 * @param <T>
 */
class RxBusEvent<T> {
    private String tag;
    private Object host;
    private Scheduler scheduler;
    private Disposable disposable;
    private RxBus.OnReceivedListener<T> receivedListener;
    private Subject<RxBusContent<T>> subject;
    private boolean isSticky;

    public RxBusEvent(@NonNull Object host, @NonNull String tag, @NonNull Scheduler scheduler, boolean isSticky, RxBus.OnReceivedListener<T> receivedListener) {
        this.host = host;
        this.tag = tag;
        this.scheduler = scheduler;
        this.isSticky = isSticky;
        this.receivedListener = receivedListener;
    }

    /**
     * 执行订阅
     *
     * @return 是否订阅成功
     */
    public boolean invoke() {
        if (subject == null) {
            disposable = null;
            return false;
        }
        // 发送的数据不为null时
        disposable = subject.observeOn(scheduler).toFlowable(BackpressureStrategy.DROP).subscribe(this::handleResult);
        return true;
    }

    /**
     * 处理接收到的数据
     */
    private void handleResult(RxBusContent<T> rxBusContent) {
        if (isSticky) {
            Logger.i("Sticky RxBus 收到了消息 --> 宿主：" + host + "，标签：" + tag + "，内容：" + rxBusContent.getContent());
        } else {
            if (rxBusContent.getContentType() == RxBusContent.ContentType.NO_DATA) {
                Logger.i("RxBus 收到了消息 --> 宿主：" + host + "，标签：" + tag + "，没有内容");
            } else if (rxBusContent.getContentType() == RxBusContent.ContentType.HAS_DATA) {
                Logger.i("RxBus 收到了消息 --> 宿主：" + host + "，标签：" + tag + "，内容：" + rxBusContent.getContent());
            }
        }
        if (null != receivedListener) {
            try {
                receivedListener.onReceive(rxBusContent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        RxBusEvent<?> that = (RxBusEvent<?>) object;

        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        return host != null ? host.equals(that.host) : that.host == null;

    }

    @Override
    public int hashCode() {
        int result = tag != null ? tag.hashCode() : 0;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        return result;
    }

    public String getTag() {
        return tag;
    }

    public Object getHost() {
        return host;
    }

    public Subject<RxBusContent<T>> getSubject() {
        return subject;
    }

    public void setSubject(Subject<RxBusContent<T>> subject) {
        this.subject = subject;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void dispose() {
        if (null != disposable && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}
