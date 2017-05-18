package com.like.rxbus.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.like.rxbus.annotations.RxBusSubscribe;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * RxBus注解处理器。每一个注解处理器类都必须有一个空的构造函数，默认不写就行;
 */
@AutoService(Processor.class)
public class RxBusProcessor extends AbstractProcessor {
    private static final Map<TypeElement, ClassCodeGenerator> CODE_BUILDER_MAP = new HashMap<>();

    /**
     * init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
     * ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
     *
     * @param processingEnv 提供给 processor 用来访问工具框架的环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        ProcessUtils.typeUtils = processingEnv.getTypeUtils();
        ProcessUtils.elementUtils = processingEnv.getElementUtils();
        ProcessUtils.filer = processingEnv.getFiler();
        ProcessUtils.messager = processingEnv.getMessager();
    }

    /**
     * 这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
     * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
     *
     * @param annotations 请求处理的注解类型
     * @param roundEnv    有关当前和以前的信息环境
     * @return 如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们；
     * 如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 返回使用给定注解类型的元素
        Set<Element> elements = (Set<Element>) roundEnv.getElementsAnnotatedWith(RxBusSubscribe.class);
        for (Element element : elements) {
            try {
                // 验证有效性
                if (!SuperficialValidation.validateElement(element))
                    continue;
                if (!ProcessUtils.verifyEncloseingClass(element) || !ProcessUtils.verifyMethod(element))
                    continue;
                // 添加类(包含有被RxBusSubscribe注解的方法的类)
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();// 获取直接上级
                ClassCodeGenerator classCodeGenerator = CODE_BUILDER_MAP.get(enclosingElement);
                if (classCodeGenerator == null) {
                    classCodeGenerator = new ClassCodeGenerator();
                    CODE_BUILDER_MAP.put(enclosingElement, classCodeGenerator);
                }
                // 添加类下面的方法(被RxBusSubscribe注解的方法)
                classCodeGenerator.addElement(element);
            } catch (Exception e) {
                e.printStackTrace();
                ProcessUtils.error(element, e.getMessage());
            }
        }
        // 生成代码
        for (ClassCodeGenerator classCodeGenerator : CODE_BUILDER_MAP.values()) {
            if (classCodeGenerator != null)
                classCodeGenerator.create();
        }
        return true;
    }

    /**
     * 这里必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称
     *
     * @return 注解器所支持的注解类型集合，如果没有这样的类型，则返回一个空集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(RxBusSubscribe.class.getCanonicalName());
        return annotations;
    }

    /**
     * 指定使用的Java版本，通常这里返回SourceVersion.latestSupported()，默认返回SourceVersion.RELEASE_6
     *
     * @return 使用的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
