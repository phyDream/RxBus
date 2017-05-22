package com.like.rxbus;

import android.text.TextUtils;

import com.like.logger.Logger;

import io.reactivex.Scheduler;

/**
 * 连接RxBus和自动生成的代码的工具类
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/4 9:53
 */
public class RxBusProxy<T> {

    /**
     * 由自动生成的代码里面调用。因为参数都是从注解中传递来的。
     *
     * @param tag
     * @param scheduler
     * @param listener
     * @param <V>
     */
    public <V> void subscribe(T host, String tag, Scheduler scheduler, boolean isSticky, RxBus.OnReceivedListener<V> listener) {
        if (host == null || TextUtils.isEmpty(tag)) {
            return;
        }
        RxBus.subscribe(host, tag, scheduler, isSticky, listener);
    }

    public void init(T host) {
        if (host == null) {
            return;
        }
        try {
            initAllHierarchyFromHost(host, host.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化父类，及调用所有父类的autoGenerate(T host)方法，避免父类中的tag未注册
     *
     * @param clazz
     * @throws Exception
     */
    private void initAllHierarchyFromHost(T host, Class clazz) throws Exception {
        if (clazz != null) {
            Logger.v("RxBus", "initAllHierarchyFromHost --> " + clazz);
            // 查找代理类
            Class<?> proxyClass = null;
            try {
                proxyClass = Class.forName(clazz.getName() + "$$Proxy");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 初始化
            if (proxyClass != null) {
                RxBusProxy rxBusProxy = (RxBusProxy) proxyClass.newInstance();
                rxBusProxy.autoGenerate(host);
            }
            // 继续查找并初始化父类。这里过滤开始的字符，及过滤android和java系统自带的类。
            Class superclass = clazz.getSuperclass();
            if (superclass != null
                    && !superclass.getName().startsWith("android.")
                    && !superclass.getName().startsWith("java.")) {
                initAllHierarchyFromHost(host, superclass);
            }
        }
    }

    /**
     * 自动生成代码时重写此方法，方法体是对entity中所有注册的tag进行subcribe()方法的调用
     */
    protected void autoGenerate(T host) {
    }

}
