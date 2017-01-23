package com.jogtek.alpha.jlog.api;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jogtek.alpha.jlog.api.DataDevice.tag;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
	private static SharedPreferences settings;
	private static final String data = "DATA";

	public static String readData(Context c, String name) {
		settings = c.getSharedPreferences(data, 0);
		return settings.getString(name, "");
	}

	public static void saveData(Context c, String name, String value) {
		settings = c.getSharedPreferences(data, 0);
		settings.edit().putString(name, value).commit();
	}

	public static void load(Context c) {
		DataDevice ma = ((DataDevice) c.getApplicationContext());
		Gson gson = new Gson();
		String js = readData(c, "log-id");
		if (js.length() > 2) {
			ma.tags=gson.fromJson(js,
					new TypeToken<ArrayList<tag>>() {
					}.getType());
		}
		ma.PW=readData(c, "PW");
	}
	public static void save(Context c) {
		DataDevice ma = ((DataDevice) c.getApplicationContext());
		Gson gson = new Gson();
		saveData(c, "log-id",gson.toJson(ma.tags));
	}
	public static void savePW(Context c) {
		DataDevice ma = ((DataDevice) c.getApplicationContext());
		saveData(c, "PW",ma.PW);
	}
	public static void loadNetSetting(Context c) {
		DataDevice ma = ((DataDevice) c.getApplicationContext());
		ma.m_IP=readData(c, "IP");
		ma.m_name=readData(c, "NAME");
		ma.m_pw=readData(c, "NETPW");
	}
	public static void saveNetSetting(Context c) {
		DataDevice ma = ((DataDevice) c.getApplicationContext());
		saveData(c, "IP",ma.m_IP);
		saveData(c, "NAME",ma.m_name);
		saveData(c, "NETPW",ma.m_pw);
	}
}
