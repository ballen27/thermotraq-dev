package com.jogtek.alpha.jlog.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.client.ClientProtocolException;
import android.content.Context;

public class myJsonPost {
	public static String send(String url, String myData,Context c) {
		String reply = "";
		BufferedReader reader = null;
		String line = "";
		try {
			URL url2 = new URL(url);
			HttpURLConnection httppost = (HttpURLConnection) url2
					.openConnection();// connection;
			httppost.setDoInput(true);
			httppost.setDoOutput(true);

			httppost.setInstanceFollowRedirects(false);
			httppost.setRequestMethod("POST");
			httppost.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			httppost.setRequestProperty("charset", "utf-8");
			httppost.setUseCaches(false);
			httppost.setRequestProperty("Connection", "Keep-Alive");
			httppost.connect();
			DataOutputStream dos = new DataOutputStream(
					httppost.getOutputStream());
			dos.write(myData.getBytes()); // bytes[] b of post data
			dos.flush();
			dos.close();

			reader = new BufferedReader(new InputStreamReader(
					httppost.getInputStream(), "utf-8"));
			while ((line = reader.readLine()) != null) {
				reply = reply + line;
			}
		} catch (ClientProtocolException e) {
//			my.e( "send error! ClientProtocolException=" + e);
//			my.AddErrLog(0, "myJsonPost: " + "Send ClientProtocolException="
//					+ e);
//			my.alertbox(c, "myJsonPost ERROR!","ClientProtocolException EX="+ e);
		} catch (IOException e) {
//			my.e( "send error! IOException=" + e);
//			my.AddErrLog(0, "myJsonPost: " + "Send IOException=" + e);
//			my.alertbox(c, "myJsonPost ERROR!","IOException EX="+ e);
		}
		return reply;
	}
	public static String send(String url, String myData) {
		String reply = "";
		BufferedReader reader = null;
		String line = "";
		try {
			URL url2 = new URL(url);
			HttpURLConnection httppost = (HttpURLConnection) url2
					.openConnection();// connection;
			httppost.setDoInput(true);
			httppost.setDoOutput(true);

			httppost.setInstanceFollowRedirects(false);
			httppost.setRequestMethod("POST");
			httppost.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			httppost.setRequestProperty("charset", "utf-8");
			httppost.setUseCaches(false);
			httppost.setRequestProperty("Connection", "Keep-Alive");
			httppost.connect();
			DataOutputStream dos = new DataOutputStream(
					httppost.getOutputStream());
			dos.write(myData.getBytes()); // bytes[] b of post data
			dos.flush();
			dos.close();

			reader = new BufferedReader(new InputStreamReader(
					httppost.getInputStream(), "utf-8"));
			while ((line = reader.readLine()) != null) {
				reply = reply + line;
			}
		} catch (ClientProtocolException e) {
//			my.e( "send error! ClientProtocolException=" + e);
//			my.AddErrLog(0, "myJsonPost: " + "Send ClientProtocolException="
//					+ e);
		} catch (IOException e) {
//			my.e( "send error! IOException=" + e);
//			my.AddErrLog(0, "myJsonPost: " + "Send IOException=" + e);
		}
		return reply;
	}
}
