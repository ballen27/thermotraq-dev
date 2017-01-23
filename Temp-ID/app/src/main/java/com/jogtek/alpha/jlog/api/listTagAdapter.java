package com.jogtek.alpha.jlog.api;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jogtek.alpha.jlog.R.id;
import com.jogtek.alpha.jlog.R.layout;
import com.jogtek.alpha.jlog.api.DataDevice.tag;
import com.jogtek.alpha.jlog.R;

public class listTagAdapter  extends ArrayAdapter<tag>{
	private LayoutInflater mInflater;
	DataDevice ma;
	public listTagAdapter(Context context, int rid, List<tag> list) {
		super(context, rid, list);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		ma = (DataDevice) context.getApplication();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// ��X���
		tag item = (tag) getItem(position);

		if(convertView==null)
			convertView = mInflater.inflate(R.layout.list_data, null);
		// �]�wtime
		TextView time;
		time = (TextView) convertView.findViewById(R.id.time);
		time.setText(item.SERIAL);
		// �]�wtemp
		TextView temp;
		temp = (TextView) convertView.findViewById(R.id.temp);
		temp.setText(item.TIME);
		
		time.setTextColor(Color.BLACK);
		time.setTextSize(20);
		time.setBackgroundColor(Color.parseColor("#e0FFFFFF"));
		temp.setTextColor(Color.BLACK);
		temp.setTextSize(20);
		temp.setBackgroundColor(Color.parseColor("#90FFFFFF"));
		
//		if(ma.)
/*		if(position==0)//title
		{
			time.setTextColor(Color.WHITE);
			time.setTextSize(26);
			time.setBackgroundColor(Color.BLUE);
			temp.setTextColor(Color.WHITE);
			temp.setTextSize(26);
			temp.setBackgroundColor(Color.BLUE);
		}
		else
		{
			time.setTextColor(Color.BLACK);
			time.setTextSize(20);
			time.setBackgroundColor(Color.parseColor("#11FFFFFF"));
			temp.setTextColor(Color.BLACK);
			temp.setTextSize(20);
			temp.setBackgroundColor(Color.parseColor("#11FFFFFF"));
		}*/
		return convertView;
	}
}
