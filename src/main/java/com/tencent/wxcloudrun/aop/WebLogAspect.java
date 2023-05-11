package com.tencent.wxcloudrun.aop;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
public class WebLogAspect {
    final Logger logger = LoggerFactory.getLogger(WebLogAspect.class);

    @Pointcut("execution(* com.nnuo.hunt.controller..*(..))")
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
            logger.info("method:{}, uri:{}, ip:{}, params:{}, arguments:{}", method, uri, ip, params, JSON.toJSONString(arguments));
        } catch (Exception e) {
            logger.info("method:{}, uri:{}, ip:{}, params:{}", method, uri, ip, params);
        }
    }

    private String getRemoteIP(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }

}
