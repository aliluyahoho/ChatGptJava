package com.tencent.wxcloudrun.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.wxcloudrun.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AutoReplyService {

    @Autowired
    public HttpClientUtils httpClient;


    public static final String send_msg_api = "http://api.weixin.qq.com/cgi-bin/message/custom/send";

    public static final String gpt_api = "https://api.openai.com/v1/chat/completions";

    public String sendMsg(Map header, JSONObject request) throws Exception {
        JSONObject response = new JSONObject();
        response.put("ToUserName", request.get("FromUserName"));
        response.put("FromUserName", request.get("ToUserName"));
        response.put("CreateTime", System.currentTimeMillis() / 1000);
        response.put("MsgType", request.get("MsgType"));
        String responseBody = callCharGpt(request.getString("Content"));
        response.put("Content", responseBody);

        return httpClient.doPostJson(send_msg_api, JSON.toJSONString(response), header);
    }

    private String callCharGpt(String content) throws Exception {
        Map<String, String> header = new HashMap<>(2);
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Bearer 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");
        String msgBody = String.format(getMsgTemlate("json/chatGptTeml.json"), content);

        return httpClient.doPostJson(gpt_api, msgBody, header);
    }

    private String getMsgTemlate(String resourcePath) throws IOException {
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("resource:{} generateShellBody failed.", resourcePath, e);
            throw e;
        }
    }
}
