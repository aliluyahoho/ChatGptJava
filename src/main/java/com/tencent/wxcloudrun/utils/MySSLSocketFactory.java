package com.tencent.wxcloudrun.utils;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class MySSLSocketFactory extends SSLSocketFactory {

	public String[] cipherSuites = null;
	public SSLContext sslContext;
	public List<SNIServerName> serverNames = null;

	public MySSLSocketFactory(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public void setServerNames(List<SNIServerName> serverNames) {
		this.serverNames = serverNames;
	}

	public void setCipherSuites(String[] cipherSuites) {
		this.cipherSuites = cipherSuites;
	}

	/**
	 * 创建SSLContext对象，使用默认信任管理器初始化
	 */
	private SSLContext getSslContext() {
		if (sslContext == null) {
			try {
				sslContext = SSLContext.getInstance("TLSv1.2");
				sslContext.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				e.printStackTrace();
			}
			SSLContext.setDefault(sslContext);
		}
		return sslContext;
	}

	// 设置算法套
	private void setSSLParams(SSLSocket sslSocket) {
		sslSocket.setUseClientMode(true);
		sslSocket.setEnabledCipherSuites(cipherSuites);

		if (serverNames != null) {
			SSLParameters params = sslSocket.getSSLParameters();
			params.setServerNames(serverNames);
			sslSocket.setSSLParameters(params);
		}
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return cipherSuites;
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return cipherSuites;
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port, boolean b) throws IOException {
		SSLSocket sslSocket = (SSLSocket) getSslContext().getSocketFactory().createSocket(socket, host, port, b);
		setSSLParams(sslSocket);
		return sslSocket;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		SSLSocket sslSocket = (SSLSocket) getSslContext().getSocketFactory().createSocket(host, port);
		setSSLParams(sslSocket);
		return sslSocket;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		SSLSocket sslSocket = (SSLSocket) getSslContext().getSocketFactory().createSocket(host, port, localHost, localPort);
		setSSLParams(sslSocket);
		return sslSocket;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		SSLSocket sslSocket = (SSLSocket) getSslContext().getSocketFactory().createSocket(host, port);
		setSSLParams(sslSocket);
		return sslSocket;
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		SSLSocket sslSocket = (SSLSocket) getSslContext().getSocketFactory().createSocket(address, port, localAddress, localPort);
		setSSLParams(sslSocket);
		return sslSocket;
	}


	/**
	 * SSL信任管理类
	 */
	private static class DefaultTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}