package com.tencent.wxcloudrun.utils;

import org.apache.http.conn.ssl.TrustStrategy;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

class AnyTrustStrategy implements TrustStrategy {

	@Override
	public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		return true;
	}

}

