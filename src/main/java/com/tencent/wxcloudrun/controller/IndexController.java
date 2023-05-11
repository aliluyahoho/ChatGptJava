package com.tencent.wxcloudrun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * index控制器
 */
@Controller

public class IndexController {

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

}
