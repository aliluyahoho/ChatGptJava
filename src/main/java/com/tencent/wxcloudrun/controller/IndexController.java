package com.tencent.wxcloudrun.controller;

import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.service.AutoReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    public void chat(@RequestBody JSONObject request) throws Exception {
        autoReplyService.sendMsg(request);
    }

}
