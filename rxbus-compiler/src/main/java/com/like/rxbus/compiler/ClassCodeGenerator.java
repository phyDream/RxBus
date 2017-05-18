package com.like.rxbus.compiler;

import com.like.rxbus.annotations.RxBusSubscribe;
import com.like.rxbus.annotations.RxBusThread;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * 一个类的代码生成工具
 *
 * @author like
 * @version 1.0
 * @created at 2017/4/2 11:27
 */
public class ClassCodeGenerator {
    private static final String CLASS_UNIFORM_MARK = "$$Proxy";
    // 因为java工程中没有下面这些类(Android中的类)，所以只能采用ClassName的方式。
    private static final ClassName SCHEDULER_MAIN = ClassName.get("io.reactivex.android.schedulers", "AndroidSchedulers", "mainThread");
    private static final ClassName SCHEDULER_IO = ClassName.get("io.reactivex.schedulers", "Schedulers", "io");
    private static final ClassName SCHEDULER_COMPUTATION = ClassName.get("io.reactivex.schedulers", "Schedulers", "computation");
    private static final ClassName SCHEDULER_NEWTHREAD = ClassName.get("io.reactivex.schedulers", "Schedulers", "newThread");
    private static final ClassName SCHEDULER_SINGLE = ClassName.get("io.reactivex.schedulers", "Schedulers", "single");
    private static final ClassName SCHEDULER_TRAMPOLINE = ClassName.get("io.reactivex.schedulers", "Schedulers", "trampoline");
    private static final ClassName RXBUSPROXY = ClassName.get("com.like.rxbus", "RxBusProxy");
    private static final ClassName LISTENER = ClassName.get("com.like.rxbus.RxBus", "OnReceivedListener");
    private static final ClassName RXBUSCONTENT = ClassName.get("com.like.rxbus", "RxBusContent");
    private static final ClassName RXBUSCONTENT_CONTENTTYPE = ClassName.get("com.like.rxbus.RxBusContent", "ContentType");

    private String mPackageName;
    private ClassName mTargetClassName;
    private Set<MethodInfo> mMethodInfoList;

    public ClassCodeGenerator() {
        mMethodInfoList = new LinkedHashSet<>();
    }

    public void create() {
        if (mMethodInfoList == null || mMethodInfoList.isEmpty()) {
            return;
        }
        // 生成 com.example.Xxx.java
        JavaFile javaFile = JavaFile.builder(mPackageName, createClass())
                .addFileComment(" This codes are generated automatically. Do not modify!")// 类的注释
                .build();
        try {
            javaFile.writeTo(ProcessUtils.filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建类
     */
    private TypeSpec createClass() {
        return TypeSpec.classBuilder(mTargetClassName.simpleName() + CLASS_UNIFORM_MARK)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(RXBUSPROXY, TypeVariableName.get(mTargetClassName.simpleName())))
                .addMethod(createMethod())
                .build();
    }

    /**
     * 创建方法
     */
    private MethodSpec createMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("autoGenerate")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mTargetClassName, "host")
                .addAnnotation(Override.class);
        for (MethodInfo binder : mMethodInfoList) {
            builder.addCode(createMethodCodeBlock(binder));
        }
        return builder.build();
    }

    private CodeBlock createMethodCodeBlock(MethodInfo methodInfo) {
        CodeBlock.Builder builder = CodeBlock.builder();
        Set<String> tags = methodInfo.getTags();
        for (String tag : tags) {
            ClassName thread = getRxThreadClassName(methodInfo.getThread());
            boolean isSticky = methodInfo.isSticky();

            CodeBlock.Builder b = CodeBlock.builder();
            b.addStatement("subscribe(host\n,$S\n,$T()\n,$L\n,$L)", tag, thread, isSticky, createListenerParam(methodInfo));
            builder.add(b.build());
        }
        return builder.build();
    }

    /**
     * 创建第四个Listener参数，是一个匿名内部类。
     */
    private TypeSpec createListenerParam(MethodInfo binder) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("onReceive")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        TypeMirror paramType = binder.getParamTypes();
        TypeName typeName;
        if (paramType == null) {
            typeName = ClassName.get("java.lang", "Object");
            methodBuilder.beginControlFlow("if (rxBusContent.getContentType() == $T.NO_DATA)", RXBUSCONTENT_CONTENTTYPE)
                    .addStatement("host." + binder.getMethodName() + "()")
                    .endControlFlow();
        } else {
            if (paramType.getKind().isPrimitive()) {
                typeName = TypeName.get(paramType);
                if (!typeName.isBoxedPrimitive())
                    typeName = typeName.box();
            } else
                typeName = ClassName.get(paramType);
            methodBuilder.beginControlFlow("if (rxBusContent.getContentType() == $T.HAS_DATA" + " && rxBusContent.getContent() instanceof $T)", RXBUSCONTENT_CONTENTTYPE, typeName)
                    .addStatement("host." + binder.getMethodName() + "(rxBusContent.getContent())")
                    .endControlFlow();
        }
        methodBuilder.addParameter(ParameterizedTypeName.get(RXBUSCONTENT, typeName), "rxBusContent");

        return TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(ParameterizedTypeName.get(LISTENER, typeName))
                .addMethod(methodBuilder.build())
                .build();
    }

    public void addElement(Element element) {
        if (mTargetClassName == null) {
            mTargetClassName = ClassName.get((TypeElement) element.getEnclosingElement());
            mPackageName = mTargetClassName.packageName();
        }

        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName(element.getSimpleName().toString());

        methodInfo.setTags(element.getAnnotation(RxBusSubscribe.class).value());
        methodInfo.setSticky(element.getAnnotation(RxBusSubscribe.class).isSticky());
        methodInfo.setThread(element.getAnnotation(RxBusSubscribe.class).thread());

        ExecutableElement executableElement = (ExecutableElement) element;
        if (executableElement.getParameters().size() == 1) {
            VariableElement ve = executableElement.getParameters().get(0);
            methodInfo.setParamType(ve.asType());
        }
        mMethodInfoList.add(methodInfo);
    }

    private ClassName getRxThreadClassName(RxBusThread threadType) {
        ClassName className = SCHEDULER_MAIN;
        switch (threadType) {
            case MainThread:
                className = SCHEDULER_MAIN;
                break;
            case IO:
                className = SCHEDULER_IO;
                break;
            case Computation:
                className = SCHEDULER_COMPUTATION;
                break;
            case Single:
                className = SCHEDULER_SINGLE;
                break;
            case NewThread:
                className = SCHEDULER_NEWTHREAD;
                break;
            case Trampoline:
                className = SCHEDULER_TRAMPOLINE;
                break;
        }
        return className;
    }
}
