package com.tencent.wxcloudrun.aop;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Aspect
@Component
public class HttpClientAop {

    public static Boolean isPrintReferenceCall = true;
    public Logger logger = LoggerFactory.getLogger(HttpClientAop.class);

    @Pointcut("execution(public * com.tencent.wxcloudrun.utils.HttpClientUtils.do*(..))")
    public void doOperation() {}

    @Before("doOperation()")
    public void before(JoinPoint joinPoint) throws Throwable {
        if(isPrintReferenceCall){
            this.printMethodParams(joinPoint);
        }
    }

    /**
     * 打印类method的名称以及参数
     *
     * @param point 切面
     */
    public void printMethodParams(JoinPoint point) {
        if (point == null) {
            return;
        }
        /** Signature 包含了方法名、申明类型以及地址等信息 */
        String class_name = point.getTarget().getClass().getName();
        String method_name = point.getSignature().getName();
        //重新定义日志
        logger = LoggerFactory.getLogger(point.getTarget().getClass());
        /** 获取方法的参数值数组。 */
        Object[] method_args = point.getArgs();

        try {
            /** 获取方法参数名称 */
            String[] paramNames = getFieldsName(class_name, method_name, method_args);

            /** 打印方法的参数名和参数值 */
            logParam(paramNames, method_args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 使用javassist来获取方法参数名称
     *
     * @param class_name  类名
     * @param method_name 方法名
     * @return
     * @throws Exception
     */
    private String[] getFieldsName(String class_name, String method_name, Object[] method_args) throws Exception {
        Class<?> clazz = Class.forName(class_name);
        String clazz_name = clazz.getName();
        ClassPool pool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(clazz);
        pool.insertClassPath(classPath);

        List<Method> methodList = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> {
            if (method_name.equals(m.getName()) && m.getParameterTypes().length == method_args.length) {
                Class[] paramsClazz = m.getParameterTypes();
                for (int i = 0; i < method_args.length; i++) {
                    if (null != method_args[i] && !paramsClazz[i].isAssignableFrom(method_args[i].getClass())) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        if (methodList.size() == 0) {
            return null;
        }

        CtClass[] params = Arrays.stream(methodList.get(0).getParameterTypes()).map(c -> {
            try {
                return pool.get(c.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).toArray(CtClass[]::new);

        CtClass ctClass = pool.get(clazz_name);


        CtMethod ctMethod = ctClass.getDeclaredMethod(method_name, params);
        MethodInfo methodInfo = ctMethod.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            return null;
        }
        String[] paramsArgsName = new String[ctMethod.getParameterTypes().length];
        int pos = Modifier.isStatic(ctMethod.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramsArgsName.length; i++) {
            paramsArgsName[i] = attr.variableName(i + pos);
        }
        return paramsArgsName;
    }


    /**
     * 判断是否为基本类型：包括String
     *
     * @param clazz clazz
     * @return true：是;     false：不是
     */
    private boolean isPrimite(Class<?> clazz) {
        if (clazz.isPrimitive() || clazz == String.class) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 打印方法参数值  基本类型直接打印，非基本类型需要重写toString方法
     *
     * @param paramsArgsName  方法参数名数组
     * @param paramsArgsValue 方法参数值数组
     */
    private void logParam(String[] paramsArgsName, Object[] paramsArgsValue) {
        if (ArrayUtils.isEmpty(paramsArgsName) || ArrayUtils.isEmpty(paramsArgsValue)) {
            logger.info("该方法没有参数");
            return;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < paramsArgsName.length; i++) {
            //参数名
            String name = paramsArgsName[i];
            //参数值
            Object value = paramsArgsValue[i];
            buffer.append(name + " = ");
            if (null == value || isPrimite(value.getClass())) {
                buffer.append(value + "  ,");
            } else {
                buffer.append(value.toString() + "  ,");
            }
        }
        logger.info(buffer.toString());
    }

}
