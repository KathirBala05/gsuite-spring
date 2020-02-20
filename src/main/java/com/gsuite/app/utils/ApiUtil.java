package com.gsuite.app.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ApiUtil {

	static String respCallBack;
	static InputStream is = null;

	static HttpClient httpClient;

	public static JSONObject request(String url, String method, String body, List<NameValuePair> params, Map<String, String> headers) {


		try {
			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}
						public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

						public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
					}
			};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSslcontext(sc).build();

		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpResponse response=null;
		StringEntity s = null;

		try {

			if(method.equals("POST")){
				HttpPost httpPost = new HttpPost(url);
				if(body != null && !body.equals(""))
					s = new StringEntity(body, HTTP.UTF_8);

				else if(params != null && !params.isEmpty())
					s = new UrlEncodedFormEntity(params, "UTF-8");

				if(s != null)
					httpPost.setEntity(s);

				if(headers != null && !headers.isEmpty()){
					for(Map.Entry<String, String> entry: headers.entrySet()){
						httpPost.setHeader(entry.getKey(), entry.getValue());
					}
				}
				response = httpClient.execute(httpPost);
			}else if(method.equals("PUT")){
				HttpPut httpPut = new HttpPut(url);
				s = new StringEntity(body, HTTP.UTF_8);
				httpPut.setEntity(s);
				if(headers != null && !headers.isEmpty()){
					for(Map.Entry<String, String> entry: headers.entrySet()){
						httpPut.setHeader(entry.getKey(), entry.getValue());
					}
				}
				response = httpClient.execute(httpPut);
			}else {
				HttpGet httpGet = new HttpGet(url);
				if(headers != null && !headers.isEmpty()){
					for(Map.Entry<String, String> entry: headers.entrySet()){
						httpGet.setHeader(entry.getKey(), entry.getValue());
					}
				}
				response = httpClient.execute(httpGet);
			}

			HttpEntity httpEntity = response.getEntity();
			is = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			respCallBack = sb.toString();

		} catch (UnsupportedEncodingException e) {
			// writing error to Log
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// writing exception to log
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			respCallBack = null;
		}

		JSONObject result = null;
		if(null != respCallBack){
			result = convertToJson(respCallBack);
		}
		return result;
	}
	
	public static JSONObject convertToJson(String respStr) {
		JSONParser parser = new JSONParser();
		JSONObject resjson = null;
		try {
			resjson = (JSONObject) parser.parse(respStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return resjson;
	}

}
