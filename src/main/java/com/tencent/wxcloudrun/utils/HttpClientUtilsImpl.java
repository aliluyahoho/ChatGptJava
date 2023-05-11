package com.tencent.wxcloudrun.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HttpClientUtilsImpl implements HttpClientUtils {

	private static CloseableHttpClient closeableHttpClient;
	private static CloseableHttpResponse closeableHttpResponse;
	private static HttpClientContext context;
	private static Registry<CookieSpecProvider> registry;
	private CookieStore cookieStore;

	public String[] cipherSuites = null;
	public String sniHostName = null;

	public HttpClientUtilsImpl() {
		cookieStore = new BasicCookieStore();
		closeableHttpClient = createSSLClientDefault2();
		// 通过context上下文信息来保持会话
		context = new HttpClientContext();
		registry = RegistryBuilder.<CookieSpecProvider>create().register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider()).build();
		context.setCookieSpecRegistry(registry);
	}

	/**
	 * 处理特殊字符
	 */
	private static String formatURL(String url) {
		url = url.replaceAll("\\{", "%7B");
		url = url.replaceAll("\\}", "%7D");
		url = url.replaceAll("\\|", "%7c");
		url = url.replaceAll("\\[", "%5B");
		url = url.replaceAll("\\]", "%5D");
		url = url.replaceAll("\"", "%22");
		url = url.replaceAll(" ", "%20");
		return url;
	}

	/**
	 * 创建http连接的可关闭的客户端
	 */
	private CloseableHttpClient createClientDefault() {
		RegistryBuilder<ConnectionSocketFactory> rb = RegistryBuilder.<ConnectionSocketFactory>create();
		ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
		rb.register("http", plainSF);
		Registry<ConnectionSocketFactory> registry = rb.build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Charsets.UTF_8).build();
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(50000)
				.setConnectionRequestTimeout(10000)
				.setSocketTimeout(50000)
				.setRedirectsEnabled(false)
				.build();
		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(100000).build();
		connManager.setDefaultConnectionConfig(connectionConfig);
		connManager.setDefaultSocketConfig(socketConfig);
		return HttpClientBuilder.create().setConnectionManager(connManager).setDefaultRequestConfig(requestConfig).build();
	}

	/**
	 * 创建带https连接的可关闭的客户端
	 */
	private CloseableHttpClient createSSLClientDefault2() {
		RegistryBuilder<ConnectionSocketFactory> rb = RegistryBuilder.<ConnectionSocketFactory>create();
		ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
		rb.register("http", plainSF);
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			SSLContext sslContext = SSLContexts.custom().useProtocol("TLS").loadTrustMaterial(trustStore, new AnyTrustStrategy()).build();

			MySSLSocketFactory sslSocketFactory = generateSocketFactory(sslContext);

			// SSL套接字连接工厂,NoopHostnameVerifier为信任所有服务器
			SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslSocketFactory, NoopHostnameVerifier.INSTANCE);
			rb.register("https", socketFactory);
		} catch (Exception e) {
			log.error("实例化SSL时出现异常：", e);
		}
		Registry<ConnectionSocketFactory> registry = rb.build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(registry);
		ConnectionConfig connectionConfig = ConnectionConfig.custom().setCharset(Charsets.UTF_8).build();
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setConnectionRequestTimeout(10000).setSocketTimeout(50000)
						.setRedirectsEnabled(false).build();
		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(100000).build();
		connManager.setDefaultConnectionConfig(connectionConfig);
		connManager.setDefaultSocketConfig(socketConfig);
		connManager.setMaxTotal(32);
		connManager.setDefaultMaxPerRoute(32);
		return HttpClientBuilder.create().setConnectionManager(connManager)
				.setDefaultRequestConfig(requestConfig)
				.setDefaultCookieStore(cookieStore)
				.build();
	}

	@Override
	public void setCookie(BasicClientCookie cookie){
		cookieStore.addCookie(cookie);
	}

	@Override
	public void clearCookie(){
		cookieStore.clear();
	}

	@Override
	public void printCookieStore(){
		List<Cookie> cookies = cookieStore.getCookies();
		cookies.stream().forEach(cookie -> {
			System.out.println(cookie.getValue());
			System.out.println(cookie.getPath());
			System.out.println(cookie.getDomain());
			System.out.println(cookie.getExpiryDate());
		});
	}
	public MySSLSocketFactory generateSocketFactory(SSLContext sslContext) {

		SSLSocketFactory defaultSslSocketFactory = sslContext.getSocketFactory();
		MySSLSocketFactory sslSocketFactory = new MySSLSocketFactory(sslContext);

		if (sniHostName != null) {
			SNIHostName serverName = new SNIHostName(sniHostName);
			List<SNIServerName> serverNames = new ArrayList<>(1);
			serverNames.add(serverName);
			sslSocketFactory.setServerNames(serverNames);
		}
		if (cipherSuites != null) {
			sslSocketFactory.setCipherSuites(cipherSuites);
		} else {
			sslSocketFactory.setCipherSuites(defaultSslSocketFactory.getDefaultCipherSuites());
		}
		return sslSocketFactory;
	}

	@Override
	public String doGet(String url) throws IOException {

		String htmlBody = null;
		HttpGet httpGet = generateGet(url, null, null);
		closeableHttpResponse = closeableHttpClient.execute(httpGet);
		if (closeableHttpResponse == null) {
			log.info("reponse is null!");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		htmlBody = EntityUtils.toString(httpEntity);
		httpGet.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doGetResponse(String url) throws IOException {

		HttpGet httpGet = generateGet(url, null, null);
		closeableHttpResponse = closeableHttpClient.execute(httpGet);
		if (closeableHttpResponse == null) {
			log.info("repsone is null!");
			return null;
		}
		httpGet.abort();
		return closeableHttpResponse;
	}

	@Override
	public String doGet(String url, Map<String, String> paramsMap, Map<String, String> headMap) throws IOException {

		String htmlBody = null;
		HttpGet httpGet = generateGet(url, paramsMap, headMap);
		closeableHttpResponse = closeableHttpClient.execute(httpGet);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		htmlBody = EntityUtils.toString(httpEntity);
		httpGet.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doGetResponse(String url, Map<String, String> paramsMap, Map<String, String> headMap)
					throws IOException {

		HttpGet httpGet = generateGet(url, paramsMap, headMap);
		closeableHttpResponse = closeableHttpClient.execute(httpGet);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		httpGet.abort();
		return closeableHttpResponse;
	}

	@Override
	public String doGet(String url, HttpClientContext context) throws IOException {
		String htmlBody = null;
		if (StringUtils.isEmpty(url)) {
			log.info("URL can't be empty!");
			return null;
		}
		url = formatURL(url);
		HttpGet httpGet = new HttpGet(url);
		closeableHttpResponse = closeableHttpClient.execute(httpGet, context);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		htmlBody = EntityUtils.toString(httpEntity);
		httpGet.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doGetResponse(String url, HttpClientContext context)
					throws IOException {
		if (StringUtils.isEmpty(url)) {
			log.info("URL can't be empty!");
			return null;
		}
		url = formatURL(url);
		HttpGet httpGet = new HttpGet(url);
		closeableHttpResponse = closeableHttpClient.execute(httpGet, context);
		if (closeableHttpResponse == null) {
			log.info("response is null");
			return null;
		}
		httpGet.abort();
		return closeableHttpResponse;
	}

	@Override
	public String doPost(String url, Map<String, String> headerMap, Map<String, String> bodyMap)
					throws Exception {

		String htmlBody = null;
		HttpPost httpPost = generatePost(url, headerMap, bodyMap);
		closeableHttpResponse = closeableHttpClient.execute(httpPost);
		if (closeableHttpResponse == null) {
			log.info("response is null");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		htmlBody = EntityUtils.toString(httpEntity);
		httpPost.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doPostResponse(String url, Map<String, String> headerMap,
	                                            Map<String, String> bodyMap) throws Exception {

		HttpPost httpPost = generatePost(url, headerMap, bodyMap);
		closeableHttpResponse = closeableHttpClient.execute(httpPost);

		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}

		httpPost.abort();
		return closeableHttpResponse;
	}

	@Override
	public String doPost(String url, Map<String, String> headerMap, Map<String, String> bodyMap,
	                     HttpClientContext context) throws Exception {

		String htmlBody = null;
		HttpPost httpPost = generatePost(url, headerMap, bodyMap);
		closeableHttpResponse = closeableHttpClient.execute(httpPost, context);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		htmlBody = EntityUtils.toString(httpEntity);
		httpPost.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doPostResponse(String url, Map<String, String> headerMap,
	                                            Map<String, String> bodyMap, HttpClientContext context) throws Exception {

		HttpPost httpPost = generatePost(url, headerMap, bodyMap);
		closeableHttpResponse = closeableHttpClient.execute(httpPost, context);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		httpPost.abort();
		return closeableHttpResponse;
	}

	@Override
	public String doPostJson(String url, String jsonObj) throws Exception {

		String htmlBody = null;

		HttpPost httpPost = generatePost(url, jsonObj, null);
		closeableHttpResponse = closeableHttpClient.execute(httpPost);
		if (closeableHttpResponse == null) {
			log.info("response is null");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		if (httpEntity == null) {
			log.info("response is null!");
			return null;
		}
		htmlBody = EntityUtils.toString(httpEntity);
		httpPost.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doPostJsonResponse(String url, String jsonObj) throws Exception {

		HttpPost httpPost = generatePost(url, jsonObj, null);
		closeableHttpResponse = closeableHttpClient.execute(httpPost);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		httpPost.abort();
		return closeableHttpResponse;
	}

	@Override
	public String doPostJson(String url, String jsonObj, Map<String, String> headerMap)
					throws Exception {
		String htmlBody = null;

		HttpPost httpPost = generatePost(url, jsonObj, headerMap);

		closeableHttpResponse = closeableHttpClient.execute(httpPost);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		HttpEntity httpEntity = closeableHttpResponse.getEntity();
		htmlBody = EntityUtils.toString(httpEntity);
		httpPost.abort();
		return htmlBody;
	}

	@Override
	public CloseableHttpResponse doPostJsonResponse(String url, String jsonObj,
	                                                Map<String, String> headerMap) throws Exception {

		HttpPost httpPost = generatePost(url, jsonObj, headerMap);

		closeableHttpResponse = closeableHttpClient.execute(httpPost);
		if (closeableHttpResponse == null) {
			log.info("response is null!");
			return null;
		}
		httpPost.abort();
		return closeableHttpResponse;
	}

	@Override
	public void closeConnection() throws IOException {
		closeableHttpClient.close();
	}

	@Override
	public String printObject(Object response) throws IOException {
		String pretty = JSON.toJSONString(response,
						SerializerFeature.PrettyFormat,
						SerializerFeature.WriteMapNullValue,
						SerializerFeature.WriteDateUseDateFormat);
		return pretty;
	}

	public static final int cache = 10 * 1024;

	@Override
	public String download(String url, String filepath){
		try {
			HttpClient client = HttpClients.createDefault();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = client.execute(httpget);

			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			if (filepath == null){
				throw new RuntimeException("file path is null.");
			}
			filepath += getFilePath(response);
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			FileOutputStream fileout = new FileOutputStream(file);
			/**
			 * 根据实际运行效果 设置缓冲区大小
			 */
			byte[] buffer = new byte[cache];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fileout.write(buffer, 0, ch);
			}
			is.close();
			fileout.flush();
			fileout.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取response要下载的文件的默认路径
	 *
	 * @param response
	 * @return
	 */
	public String getFilePath(HttpResponse response) {
		String filename = getFileName(response);

		if (filename != null) {
			return filename;
		} else {
			return getRandomFileName();
		}
	}

	/**
	 * 获取response header中Content-Disposition中的filename值
	 *
	 * @param response
	 * @return
	 */
	public static String getFileName(HttpResponse response) {
		Header contentHeader = response.getFirstHeader("Content-Disposition");
		String filename = null;
		if (contentHeader != null) {
			HeaderElement[] values = contentHeader.getElements();
			if (values.length == 1) {
				NameValuePair param = values[0].getParameterByName("filename");
				if (param != null) {
					try {
						filename = param.getValue();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return filename;
	}

	@Override
	public String getRandomFileName() {
		return String.valueOf(System.currentTimeMillis());
	}

	private HttpGet generateGet(String url, Map<String, String> paramsMap, Map<String, String> headMap) {

		if (StringUtils.isEmpty(url)) {
			log.info("URL can't be empty!");
			return null;
		}
		if (paramsMap != null && paramsMap.size() != 0) {
			StringBuffer sb = new StringBuffer(url);
			sb.append("?");
			for (String key : paramsMap.keySet()) {
				sb.append(key);
				sb.append("=");
				sb.append(paramsMap.get(key));
				sb.append("&");
			}
			url = formatURL(sb.toString());
		} else {
			url = formatURL(url);
		}
		HttpGet httpGet = new HttpGet(url);
		// 设置默认的header信息
		httpGet.setHeader("Referer", url);
		httpGet.setHeader("Connection", "keep-alive");
		// 自定义header信息
		if (headMap != null && headMap.size() != 0) {
			for (String key : headMap.keySet()) {
				httpGet.setHeader(key, headMap.get(key));
			}
		}

		return httpGet;
	}

	private HttpPost generatePost(String url, String jsonObj, Map<String, String> headerMap) throws
					URISyntaxException {

		if (StringUtils.isEmpty(url)) {
			log.info("URL can't be empty");
			return null;
		}
		url = formatURL(url);

		HttpPost httpPost = new HttpPost();
		httpPost.setURI(new URI(url));
		httpPost.setHeader("Referer", url);
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Content-type", "application/json");

		if (StringUtils.isNotBlank(jsonObj)) {
			StringEntity stringEntity = new StringEntity(jsonObj, "utf-8");
			httpPost.setEntity(stringEntity);
		}

		// 设置个性化的header信息
		if (headerMap != null && headerMap.size() != 0) {
			for (String headerKey : headerMap.keySet()) {
				httpPost.setHeader(headerKey, headerMap.get(headerKey));
			}
		}

		return httpPost;
	}

	private HttpPost generatePost(String url, Map<String, String> headerMap, Map<String, String> bodyMap) throws
					URISyntaxException, UnsupportedEncodingException {

		if (StringUtils.isEmpty(url)) {
			log.info("URL can't be empty!");
		}
		url = formatURL(url);
		HttpPost httpPost = new HttpPost();
		httpPost.setURI(new URI(url));
		// 设置默认的header信息
		httpPost.setHeader("Referer", url);
		httpPost.setHeader("Connection", "keep-alive");
		// 设置个性化的header信息
		if (headerMap != null && headerMap.size() != 0) {
			for (String headerKey : headerMap.keySet()) {
				httpPost.setHeader(headerKey, headerMap.get(headerKey));
			}
		}

		// 设置个性化的body信息
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		if (bodyMap != null && bodyMap.size() != 0) {
			for (String key : bodyMap.keySet()) {
				nvps.add(new BasicNameValuePair(key, bodyMap.get(key)));
			}
		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

		return httpPost;
	}
}



