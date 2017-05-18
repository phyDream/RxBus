package com.like.rxbus.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 订阅RxBus消息的注解
 * <p>
 * 此注解只能注解方法，且方法必须public修饰，不能为static。方法的参数最多只能是1个。
 * 且此注解所在的宿主类只能被public修饰，并且宿主类的包名不能以`android`或者`java`开头。
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/2 10:13
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface RxBusSubscribe {
    String DEFAULT_TAG = "com.like.rxbus.annotations.RxBusSubscribe";

    /**
     * 标签数组
     */
    String[] value() default {DEFAULT_TAG};

    /**
     * 是否粘性事件
     */
    boolean isSticky() default false;

    /**
     * 注解的方法所在的线程
     */
    RxBusThread thread() default RxBusThread.MainThread;

}
