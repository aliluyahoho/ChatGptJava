package com.tencent.wxcloudrun.advice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tencent.wxcloudrun.config.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class BizControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ApiResponse handler(Exception e) {
        log.error("error", e);
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse argumentErrorHandler(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        String requestBody = JSON.toJSONString(result.getTarget(), SerializerFeature.PrettyFormat);
        log.error("requestBody => \n{}", requestBody, ex);
        return ApiResponse.error(result.hasErrors() ? result.getAllErrors().get(0).getDefaultMessage() : "参数错误");
    }

    @ExceptionHandler(value = BindException.class)
    public ApiResponse bindExceptionHandler(BindException ex) {
        BindingResult result = ex.getBindingResult();
        log.error("error", ex);
        return ApiResponse.error(result.hasErrors() ? result.getAllErrors().get(0).getDefaultMessage() : "参数错误");
    }
}