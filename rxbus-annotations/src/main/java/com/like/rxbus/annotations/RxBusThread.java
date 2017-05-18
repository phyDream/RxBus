package com.like.rxbus.annotations;

/**
 * RxBus线程类型，对应了RxJava和RxAndroid的线程类型
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/3 17:06
 */
public enum RxBusThread {
    MainThread,
    IO,
    Computation,
    Single,
    NewThread,
    Trampoline
}
