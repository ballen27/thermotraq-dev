package com.jogtek.alpha.jlog;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.jogtek.alpha.jlog.api.DataDevice;
import com.jogtek.alpha.jlog.api.NFCCommand;
import com.jogtek.alpha.jlog.api.OnTaskCompleted;
import com.jogtek.alpha.jlog.api.ReadWrite;
import com.jogtek.alpha.jlog.api.command;
import com.jogtek.alpha.jlog.api.listDataAdapter;
import com.jogtek.alpha.jlog.api.list_data;
import com.jogtek.alpha.jlog.api.net;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

//http://www.programering.com/a/MjM0QTMwATI.html
public class data extends Activity {
	Gson gson = new Gson();
	DataDevice ma;
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	EditText etIP;
	EditText etName;
	EditText etPw;

	LinearLayout llIP;
	LinearLayout llName;
	LinearLayout llPw;

	// List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
	List<list_data.data_s> list = new ArrayList<list_data.data_s>();
	List<list_data.data_s> list0 = new ArrayList<list_data.data_s>();

	listDataAdapter adapter;

	Button btnUpload;
	Button btnCurrent;
	Button btnList;
	Button btnCommand;
	Button btnLine;
	Button btnData;
	ListView lvData;

	RadioButton rbUp;
	RadioButton rbDn;
	RadioGroup rgGroup;

	ToggleButton tgSetting;

	ProgressBar pbBar;
	ProgressDialog dialog;

	boolean isDn = true;
	ReadWrite rw = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data);

		ma = (DataDevice) getApplication();
		initListener();
		rw = new ReadWrite(data.this, pbBar, dialog);
		// InitFTPServerSetting();
		initData();
		PackageManager pm = getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
			mAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mAdapter.isEnabled()) {
				mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
						this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				IntentFilter ndef = new IntentFilter(
						NfcAdapter.ACTION_TECH_DISCOVERED);
				mFilters = new IntentFilter[] { ndef, };
				mTechLists = new String[][] { new String[] { android.nfc.tech.NfcV.class
						.getName() } };
			}
		}
	}

	private void initListener() {
		pbBar = (ProgressBar) findViewById(R.id.pbBar);
		dialog = new ProgressDialog(data.this);
		btnUpload = (Button) findViewById(R.id.btnUpload);
		btnCurrent = (Button) findViewById(R.id.btnCurrent);
		btnList = (Button) findViewById(R.id.btnList);
		btnCommand = (Button) findViewById(R.id.btnCommand);
		btnLine = (Button) findViewById(R.id.btnLine);
		btnData = (Button) findViewById(R.id.btnData);
		lvData = (ListView) findViewById(R.id.lvData);
		etIP = (EditText) findViewById(R.id.etIP);
		etName = (EditText) findViewById(R.id.etName);
		etPw = (EditText) findViewById(R.id.etPw);

		rgGroup = (RadioGroup) findViewById(R.id.rgGroup);
		rbUp = (RadioButton) findViewById(R.id.rbUp);
		rbDn = (RadioButton) findViewById(R.id.rbDn);

		rgGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rbUp:
					adapter = new listDataAdapter(data.this, 0, list);
					lvData.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					break;
				case R.id.rbDn:
					adapter = new listDataAdapter(data.this, 0, list0);
					lvData.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					break;
				default:
				}
			}
		});
		llIP = (LinearLayout) findViewById(R.id.llIP);
		llName = (LinearLayout) findViewById(R.id.llName);
		llPw = (LinearLayout) findViewById(R.id.llPw);

		tgSetting = (ToggleButton) findViewById(R.id.tgSetting);
		tgSetting.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (tgSetting.isChecked()) {
					llIP.setVisibility(View.VISIBLE);
					llName.setVisibility(View.VISIBLE);
					llPw.setVisibility(View.VISIBLE);
				} else {
					llIP.setVisibility(View.GONE);
					llName.setVisibility(View.GONE);
					llPw.setVisibility(View.GONE);
				}
			}
		});
		btnCommand.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(data.this, command.class);
				startActivity(intent);
				finish();
			}
		});
		btnUpload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				net n = new net(data.this);
				n.uploadFTP(etIP.getText().toString(), etName.getText()
						.toString(), etPw.getText().toString());
			}
		});
		btnCurrent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(data.this, result.class);
				startActivity(intent);
				finish();
			}
		});
		btnList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(data.this, tag_list.class);
				startActivity(intent);
				finish();
			}
		});
		btnLine.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(data.this, curve.class);
				startActivity(intent);
				finish();
			}
		});
		btnData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
	}

	private void initData() {
		Calendar c = Calendar.getInstance();
		// if(ma.curTag.lave)
		c.set(ma.curTag.year + 2000, ma.curTag.month, ma.curTag.day,
				ma.curTag.hour, ma.curTag.min + ma.curTag.delay
						+ ma.curTag.delay0, ma.curTag.sec);
		Date[] d = new Date[ma.curTag.items];
		list.clear();
		list0.clear();
		list_data.data_s da = new list_data.data_s();
		da.time = getResources().getText(R.string.t_time).toString();
		da.temp = getResources().getText(R.string.t_temp).toString();
		list.add(da);
		list0.add(da);

		for (int i = 0; i < ma.curTag.items; i++) {
			Map<String, Object> data1 = new HashMap<String, Object>();
			if (ma.curTag.unit == 0)
				c.add(Calendar.SECOND, 2);
			else
				c.add(Calendar.SECOND, 15 * ma.curTag.unit);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String datetime = sdf.format(c.getTime());
			list_data.data_s ds = new list_data.data_s();
			ds.time = datetime;
			ds.up = ma.curTag.up_limit;
			ds.dn = ma.curTag.dn_limit;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2); // 小數後兩位
			ds.temp = nf.format(ma.curTag.datas.get(i));
			list0.add(ds);
		}
		for (int i = ma.curTag.items - 1; i >= 1; i--) {
			list_data.data_s ds = new list_data.data_s();
			try {
				ds.time = list0.get(i).time;
			} catch (Exception e) {
				String s = e.toString();
			}
			ds.up = list0.get(i).up;
			ds.dn = list0.get(i).dn;
			ds.temp = list0.get(i).temp;
			list.add(ds);
		}
		adapter = new listDataAdapter(data.this, 0, list);
		lvData.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	// NFC
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			DataDevice dataDevice = (DataDevice) getApplication();
			dataDevice.setCurrentTag(tagFromIntent);
			rw.GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
					tagFromIntent, (DataDevice) getApplication());
			if (rw.DecodeGetSystemInfoResponse(rw.GetSystemInfoAnswer)) {
				rw.Read(new OnTaskCompleted() {
					@Override
					public void onTaskCompleted(String result) {
						if (result.equals("1")) {// success read
							initData();
						}
					}
				});
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
				mTechLists);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mAdapter.disableForegroundDispatch(this);
		return;
	}
}