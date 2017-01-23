package com.jogtek.alpha.jlog.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jogtek.alpha.jlog.api.DataDevice.tag;

public class parser {
	public static int parserInt(String result) {
		JSONObject jsonObjRecv;
		int ret = 0;
		try {
			jsonObjRecv = new JSONObject(result);
			Iterator<?> iterator = jsonObjRecv.keys();
			String ex = "";
			String su = "";
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if ("Ex".equalsIgnoreCase(key)) {
					ex = jsonObjRecv.getString(key);
				} else if ("Su".equalsIgnoreCase(key)) {
					su = jsonObjRecv.getString(key);
				}
			}
			if ("".equals(ex)) {// success
				ret = Integer.valueOf(su);
			} else {
			}
		} catch (JSONException e) {
		}
		return ret;
	}
	public static int parserMessage(Context c,String result) {
		Gson gson = new Gson();
		DataDevice ma = (DataDevice) c.getApplicationContext();
		String Dresult = "";
		JSONObject jsonObjRecv;
		int ret = 0;
		try {
			jsonObjRecv = new JSONObject(result);
			Iterator<?> iterator = jsonObjRecv.keys();
			String ex = "";
			String su = "";
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if ("Ex".equalsIgnoreCase(key)) {
					ex = jsonObjRecv.getString(key);
				} else if ("Su".equalsIgnoreCase(key)) {
					su = jsonObjRecv.getString(key);
				} else if ("D".equalsIgnoreCase(key)) {
					Dresult = jsonObjRecv.getString(key);
				}
			}
			if ("".equals(ex)) {// success
				ret = Integer.valueOf(su);
				if (ret == 1) {
					List<tag> l1 = gson.fromJson(Dresult,
							new TypeToken<ArrayList<tag>>() {
							}.getType());
					for(tag tg:l1){
						ma.tags.add(0, tg);
					}
				}
			} else {
			}
		} catch (JSONException e) {
		}
		return ret;
	}
}
