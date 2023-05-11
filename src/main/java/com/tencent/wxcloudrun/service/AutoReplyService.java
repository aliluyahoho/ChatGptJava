package com.tencent.wxcloudrun.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AutoReplyService {

    @Autowired
    public HttpClientUtils httpClient;

    public static final String send_msg_api = "http://api.weixin.qq.com/cgi-bin/message/custom/send";

    public void sendMsg(JSONObject request) throws Exception {
        httpClient.doPostJson(send_msg_api, JSON.toJSONString(request));
    }
}
