package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.service.AutoReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * index控制器
 */
@Controller
@RequestMapping
public class IndexController {

    @Autowired
    public AutoReplyService autoReplyService;

    /**
     * 主页页面
     *
     * @return API response html
     */
    @GetMapping
    public String index() {
        return "index";
    }

    @ResponseBody
    @GetMapping("/MP_verify_P97WLFQjVODXzc3j.txt")
    public String getData() throws IOException {
        return "P97WLFQjVODXzc3j";
    }


    @ResponseBody
    @PostMapping("/chat")
    public String chat(HttpServletRequest servletRequest, @RequestBody JSONObject request) throws Exception {
        Map header = new HashMap(1);
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = servletRequest.getHeader(headerName);
            header.put(headerName, headerValue);
        }
        return autoReplyService.sendMsg(header, request);
    }

}
