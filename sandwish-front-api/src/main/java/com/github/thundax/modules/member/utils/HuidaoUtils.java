package com.github.thundax.modules.member.utils;

import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * 一网通办相关
 * @author 51678
 */
@Component
public class HuidaoUtils {

	private final YwtbProperties properties;

	public HuidaoUtils(YwtbProperties properties){
		this.properties = properties;
	}

	public JSONObject getUserInfo(String accessToken) throws Exception {
		HttpClient httpclient = getHttpClient(properties.getBaseUrl().startsWith("https://"));
		HttpPost httpPost = new HttpPost(properties.getBaseUrl());
		String content = properties.getAppId() + properties.getApiId() + System.currentTimeMillis() / 1000;
		String signature = encrypt(content);
		httpPost.addHeader("appid", properties.getAppId());
		httpPost.addHeader("apiname", properties.getApiId());
		httpPost.addHeader("signature", signature);
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		formParams.add(new BasicNameValuePair("access_token", accessToken));
		UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
		httpPost.setEntity(urlEncodedFormEntity);
		HttpResponse httpResponse = httpclient.execute(httpPost);
		HttpEntity httpEntity = httpResponse.getEntity();
		InputStream is = httpEntity.getContent();
		byte[] bytes = toByteArray(is);
		is.close();
		String s = new String(bytes, "UTF-8");
		StatusLine statusLine = httpResponse.getStatusLine();
		httpPost.releaseConnection();
		if (statusLine.getStatusCode() == 200) {
			System.out.println("getUserInfo成功");
			System.out.println(s);
			return JSONObject.fromObject(s);
		} else {
			System.out.println("getUserInfo失败" + statusLine.getStatusCode() + "," + statusLine.getReasonPhrase());
			throw new RuntimeException(s);
		}
	}

	private  SSLContext createIgnoreVerifySSL() {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			X509TrustManager trustManager = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates, String paramString) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] x509Certificates, String paramString) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sc.init(null, new TrustManager[] { trustManager }, null);
			return sc;
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private HttpClient getHttpClient(boolean isHttps) {
		if (isHttps) {
			SSLContext sslContext = createIgnoreVerifySSL();
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", new SSLConnectionSocketFactory(sslContext)).build();
			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			return HttpClients.custom().setConnectionManager(connManager).build();
		} else {
			return HttpClients.createDefault();
		}
	}

	private String encrypt(String str) throws Exception {
		SecretKeySpec secretKeySpec = new SecretKeySpec(properties.getAppKey().replace("-","").getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] bytes = cipher.doFinal(str.getBytes("UTF-8"));
		return new Base64().encodeToString(bytes);
	}

	private byte[] encryptByRSA(byte[] pubKeyInByte, byte[] data) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyInByte);
		PublicKey pubKey = keyFactory.generatePublic(keySpec);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, pubKey);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		int maxEncryptBlock = 117;
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > maxEncryptBlock) {
				cache = cipher.doFinal(data, offSet, maxEncryptBlock);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * maxEncryptBlock;
		}
		return out.toByteArray();
	}

	private byte[] toByteArray(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		toOutputStream(is, baos);
		return baos.toByteArray();
	}

	private void toOutputStream(InputStream is, OutputStream os) throws Exception {
		byte[] buffer = new byte[2048];
		int bytesRead;
		while ((bytesRead = is.read(buffer, 0, 1024)) != -1) {
			os.write(buffer, 0, bytesRead);
		}
	}

}
