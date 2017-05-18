package com.like.rxbus.compiler;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class ProcessUtils {
    public static Types typeUtils;// 用来处理TypeMirror的工具
    public static Elements elementUtils;// 用来处理Element的工具
    public static Filer filer;// 用来生成我们需要的.java文件的工具
    public static Messager messager;// 提供给注解处理器一个报告错误、警告以及提示信息的途径

    /**
     * 判断该元素的上层元素是否符合目标元素的上层元素
     * <p>
     * 判断宿主类是否为类，而且是否为public修饰
     * 然后判断包名，android.和java.开头的不行
     *
     * @param element
     * @return
     */
    public static final boolean verifyEncloseingClass(Element element) {
        TypeElement encloseingElement = (TypeElement) element.getEnclosingElement();
        if (encloseingElement.getKind() != CLASS) {
            error(element, "%s 不属于CLASS类型", element.getSimpleName().toString());
            return false;
        }
        if (!encloseingElement.getModifiers().contains(PUBLIC)) {
            error(element, "%s 类必须被public修饰", element.getSimpleName().toString());
            return false;
        }
        String qualifiedName = encloseingElement.getQualifiedName().toString();
        if (qualifiedName.startsWith("android.") || qualifiedName.startsWith("java.")) {
            error(element, "%s 类的名称不能以`android.`或者`java.`开头", element.getSimpleName().toString());
            return false;
        }
        return true;
    }

    /**
     * 判断是否为目标方法
     * <p>
     * 元素类型必须为method，必须public修饰，不能为static。方法的参数最多只能是1个
     *
     * @param element
     * @return
     */
    public static final boolean verifyMethod(Element element) {
        if (element.getKind() != METHOD) {
            error(element, "%s 不属于METHOD类型", element.getSimpleName().toString());
            return false;
        }

        if (!element.getModifiers().contains(PUBLIC) || element.getModifiers().contains(STATIC)) {
            error(element, "%s 方法必须被public修饰，且不能为static", element.getSimpleName().toString());
            return false;
        }

        ExecutableElement executableElement = (ExecutableElement) element;
        int size = executableElement.getParameters().size();
        if (size > 1) {
            error(executableElement, "%s 方法的参数最多只能有1个", executableElement.getSimpleName().toString());
            return false;
        }

        return true;
    }

    public static void error(Element element, String format, Object... args) {
        if (args.length > 0)
            format = String.format(format, args);
        ProcessUtils.messager.printMessage(Diagnostic.Kind.ERROR, format, element);
    }

}
