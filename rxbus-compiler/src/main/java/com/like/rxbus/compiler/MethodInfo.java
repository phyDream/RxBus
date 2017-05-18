package com.like.rxbus.compiler;

import com.like.rxbus.annotations.RxBusThread;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.type.TypeMirror;

final class MethodInfo {
    private String mMethodName;
    private Set<String> mTags;
    private RxBusThread mThread;
    private boolean isSticky;
    private TypeMirror mParamType;

    public MethodInfo() {
        mTags = new LinkedHashSet<>();
    }

    public void setMethodName(String name) {
        mMethodName = name;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public void setThread(RxBusThread thread) {
        mThread = thread;
    }

    public RxBusThread getThread() {
        return mThread;
    }

    public void setTags(String[] tags) {
        mTags.addAll(Arrays.asList(tags));
    }

    public Set<String> getTags() {
        return mTags;
    }

    public void setParamType(TypeMirror typeMirror) {
        mParamType = typeMirror;
    }

    public TypeMirror getParamTypes() {
        return mParamType;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean sticky) {
        isSticky = sticky;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "mMethodName='" + mMethodName + '\'' +
                ", mTags=" + mTags +
                ", mThread=" + mThread +
                ", mParamType=" + mParamType +
                ", isSticky=" + isSticky +
                '}';
    }
}
