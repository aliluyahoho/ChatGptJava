package com.tencent.wxcloudrun.aop;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class WebLogAspect {
    @Pointcut("execution(* com.tencent.wxcloudrun.controller..*(..))")
    public void controllerLog() {
    }

    @Before("controllerLog()")
    public void logBeforeController(JoinPoint joinPoint) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        String ip = getRemoteIP(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String params = JSON.toJSONString(request.getParameterMap());
        List<Object> arguments = Arrays.stream(joinPoint.getArgs())
                .filter(a -> !(a instanceof BeanPropertyBindingResult))
                .filter(a -> !(a instanceof HttpServletRequest))
                .filter(a -> !(a instanceof HttpServletResponse))
                .collect(Collectors.toList());
        try {
            log.info("method:{}, uri:{}, ip:{}, params:{}, arguments:{}", method, uri, ip, params, JSON.toJSONString(arguments));
        } catch (Exception e) {
            log.info("method:{}, uri:{}, ip:{}, params:{}", method, uri, ip, params);
        }
    }

    @Around("controllerLog()")
    public Object doAround(ProceedingJoinPoint proceeding) throws Throwable {
        //执行被拦截的方法 result是返回结果
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        try {
            Object result = proceeding.proceed();
            log.info("uri:{}, result:{}", request.getRequestURI(), JSON.toJSONString(result));
            return result;
        } catch (Exception e) {
            String msg = e.getMessage();
            if (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).getTargetException();
                msg = t.getMessage();
            }
            log.info("process error uri:{}, msg:{}", request.getRequestURI(), msg);
            throw e;
        }
    }

    private String getRemoteIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

    private Map<String, Object> getRequestHeaders(HttpServletRequest servletRequest){
        Map header = new HashMap(1);
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = servletRequest.getHeader(headerName);
            if(headerName.startsWith("x-")){
                header.put(headerName, headerValue);
            }
        }
        return header;
    }

    private Map<String, Object> getResponseHeaders(HttpServletRequest servletRequest){
        Map header = new HashMap(1);
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = servletRequest.getHeader(headerName);
            if(headerName.startsWith("x-")){
                header.put(headerName, headerValue);
            }
        }
        return header;
    }

}
