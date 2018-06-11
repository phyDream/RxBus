package com.like.rxbus.compiler;

import com.like.rxbus.annotations.RxBusThread;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.type.TypeMirror;

final class MethodInfo {
    private String methodName;
    private Set<String> tags;
    private RxBusThread thread;
    private boolean isSticky;
    private String code;
    private TypeMirror paramType;

    public MethodInfo() {
        tags = new LinkedHashSet<>();
    }

    public String getMethodName() {
        return methodName;
    }

    public Set<String> getTags() {
        return tags;
    }

    public RxBusThread getThread() {
        return thread;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public String getCode() {
        return code;
    }

    public TypeMirror getParamType() {
        return paramType;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setTags(String[] tags) {
        this.tags.addAll(Arrays.asList(tags));
    }

    public void setThread(RxBusThread thread) {
        this.thread = thread;
    }

    public void setSticky(boolean sticky) {
        isSticky = sticky;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setParamType(TypeMirror paramType) {
        this.paramType = paramType;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "methodName='" + methodName + '\'' +
                ", tags=" + tags +
                ", thread=" + thread +
                ", isSticky=" + isSticky +
                ", code='" + code + '\'' +
                ", paramType=" + paramType +
                '}';
    }
}
