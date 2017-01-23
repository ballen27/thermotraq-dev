package com.jogtek.alpha.jlog.api;

import java.util.List;

import com.jogtek.alpha.jlog.R.id;
import com.jogtek.alpha.jlog.R.layout;
import com.jogtek.alpha.jlog.api.list_data.data_s;
import com.jogtek.alpha.jlog.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class listDataAdapter extends ArrayAdapter<data_s> {
	private LayoutInflater mInflater;

	public listDataAdapter(Context context, int rid, List<data_s> list) {
		super(context, rid, list);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// ��X���
		data_s item = (data_s) getItem(position);
		try {
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.list_data, null);
			// �]�wtime
			TextView time;
			time = (TextView) convertView.findViewById(R.id.time);
			time.setText(item.time);
			// �]�wtemp
			TextView temp;
			temp = (TextView) convertView.findViewById(R.id.temp);
			temp.setText(item.temp);
			if (position == 0)// title
			{
				time.setTextColor(Color.WHITE);
				time.setTextSize(26);
				time.setBackgroundColor(Color.BLUE);
				temp.setTextColor(Color.WHITE);
				temp.setTextSize(26);
				temp.setBackgroundColor(Color.BLUE);
			} else {
				time.setTextColor(Color.BLACK);
				time.setTextSize(20);
				time.setBackgroundColor(Color.parseColor("#11FFFFFF"));
				double t = Double.parseDouble(item.temp);
				temp.setTextSize(20);
				if (t <= item.up && t >= item.dn) {
					temp.setBackgroundColor(Color.parseColor("#11FFFFFF"));
					temp.setTextColor(Color.BLACK);
				} else if (t < item.dn) {
					temp.setBackgroundColor(Color.BLUE);
					temp.setTextColor(Color.WHITE);
				} else {
					temp.setBackgroundColor(Color.RED);
					temp.setTextColor(Color.WHITE);
				}
			}
		} catch (Exception e) {
//			String ss= e.toString();
		}
		return convertView;
	}
}
