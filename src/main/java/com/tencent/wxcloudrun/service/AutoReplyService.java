package com.tencent.wxcloudrun.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.utils.HttpClientUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AutoReplyService {

    @Autowired
    public HttpClientUtils httpClient;

    public static final String send_msg_api = "https://api.weixin.qq.com/cgi-bin/message/custom/send?from_appid=wxb73a97e6793331b4";

    public String sendMsg(Map header, JSONObject request) throws Exception {
        JSONObject response = new JSONObject();
        response.put("ToUserName", request.get("FromUserName"));
        response.put("FromUserName", request.get("ToUserName"));
        response.put("CreateTime", System.currentTimeMillis() / 1000);
        response.put("MsgType", request.get("MsgType"));
        response.put("Content", request.get("Content"));
        return httpClient.doPostJson(send_msg_api, JSON.toJSONString(response), header);
    }
}
