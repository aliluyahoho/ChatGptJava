package com.tencent.wxcloudrun.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.IOException;
import java.util.Map;

public interface HttpClientUtils {

    void setCookie(BasicClientCookie cookie);

    void clearCookie();

    void printCookieStore();
    /**
     * http get 方法
     *
     * @param url
     * @return 返回jsessionId
     * @throws
     */
    String doGet(String url) throws IOException;

    /**
     * http get 方法
     *
     * @param url
     * @return
     * @throws
     */
    CloseableHttpResponse doGetResponse(String url) throws IOException;

    /**
     * http get 方法 支持自定义head信息
     *
     * @param url
     * @param paramsMap
     * @param headMap
     * @return
     * @throws
     */
    String doGet(String url, Map<String, String> paramsMap, Map<String, String> headMap) throws IOException;

    /**
     * http get 方法 支持自定义head信息
     *
     * @param url
     * @param paramsMap
     * @param headMap
     * @return
     * @throws
     */
    CloseableHttpResponse doGetResponse(String url, Map<String, String> paramsMap, Map<String, String> headMap) throws IOException;

    /**
     * http get 方法，适用指定httpClientContext内容
     *
     * @param url
     * @return html
     * @throws
     */
    String doGet(String url, HttpClientContext context) throws IOException;


    /**
     * http get 方法，适用指定httpClientContext内容
     *
     * @param url
     * @return html
     * @throws
     */
    CloseableHttpResponse doGetResponse(String url, HttpClientContext context) throws IOException;


    /**
     * http post 方法，使用自定义head内容和body内容
     *
     * @param url
     * @param headerMap
     * @param bodyMap
     * @return html
     * @throws
     */
    String doPost(String url, Map<String, String> headerMap, Map<String, String> bodyMap) throws Exception;

    /**
     * http post 方法，使用自定义head内容和body内容
     *
     * @param url
     * @param headerMap
     * @param bodyMap
     * @return html
     * @throws
     */
    CloseableHttpResponse doPostResponse(String url, Map<String, String> headerMap,
                                                Map<String, String> bodyMap) throws Exception;

    /**
     * http post 方法，使用自定义head内容和body内容，外加HttpClientContext
     *
     * @param url
     * @param headerMap
     * @param bodyMap
     * @return html
     * @throws
     */
    String doPost(String url, Map<String, String> headerMap, Map<String, String> bodyMap,
                         HttpClientContext context) throws Exception;

    /**
     * http post 方法，使用自定义head内容和body内容，外加HttpClientContext
     *
     * @param url
     * @param headerMap
     * @param bodyMap
     * @return html
     * @throws
     */
    CloseableHttpResponse doPostResponse(String url, Map<String, String> headerMap,
                                                Map<String, String> bodyMap, HttpClientContext context) throws Exception;


    /**
     * http post 方法，提交内容为Json对象
     *
     * @param url
     * @param jsonObj
     * @return html
     * @throws
     */
    String doPostJson(String url, String jsonObj) throws Exception;

    /**
     * http post 方法
     *
     * @param url
     * @param jsonObj 提交内容为Json对象
     * @return
     * @throws Exception
     */
    CloseableHttpResponse doPostJsonResponse(String url, String jsonObj) throws Exception;

    /**
     * http post 方法
     *
     * @param url
     * @param jsonObj   提交内容为Json对象
     * @param headerMap
     * @return html
     * @throws
     */
    String doPostJson(String url, String jsonObj, Map<String, String> headerMap) throws Exception;


    /**
     * http post 方法，提交内容为Json对象
     *
     * @param url
     * @param jsonObj
     * @return html
     * @throws
     */
    CloseableHttpResponse doPostJsonResponse(String url, String jsonObj,
                                                    Map<String, String> headerMap) throws Exception;

    /**
     * 手工关闭连接
     *
     * @throws
     */
    void closeConnection() throws IOException;


    /**
     * 格式化response body
     *
     * @param
     * @throws
     */
    String printObject(Object response) throws IOException;

    /**
     * 下载文件到本地
     * @param url
     * @param filepath
     * @return
     */
    String download(String url, String filepath);

    /**
     * 获取随机文件名
     *
     * @return
     */
    String getRandomFileName();

}
