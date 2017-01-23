package com.jogtek.alpha.jlog;

import java.text.NumberFormat;

import com.jogtek.alpha.jlog.api.DataDevice;
import com.jogtek.alpha.jlog.api.NFCCommand;
import com.jogtek.alpha.jlog.api.OnTaskCompleted;
import com.jogtek.alpha.jlog.api.ReadWrite;
import com.jogtek.alpha.jlog.api.command;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class result extends Activity {
	DataDevice ma;
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	TextView tvUID;
	TextView tvMode;
	TextView tvMax;
	TextView tvMin;
	TextView tvRecordTime;
	TextView tvName;// 2
	LinearLayout llName;

	Button btnCurrent;
	Button btnList;
	Button btnCommand;
	Button btnLine;
	Button btnData;

	ProgressBar pbBar;
	ProgressDialog dialog;
	
	ReadWrite rw = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);

		initListener();
		rw = new ReadWrite(result.this,pbBar,dialog);
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
							showData();
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

	public int convertDIPtoPixel(int dp) {
		final float scale = getResources().getDisplayMetrics().density;
		int pixels = (int) (dp * scale + 0.5f);
		return pixels;
	}

	private void initListener() {
		ma = (DataDevice) getApplication();
		pbBar = (ProgressBar) findViewById(R.id.pbBar);
		dialog = new ProgressDialog(result.this);
		tvUID = (TextView) findViewById(R.id.tvUID);
		tvName = (TextView) findViewById(R.id.tvName);// 2
		llName = (LinearLayout) findViewById(R.id.llName);// 2
		tvMode = (TextView) findViewById(R.id.tvMode);
		tvMax = (TextView) findViewById(R.id.tvMax);
		tvMin = (TextView) findViewById(R.id.tvMin);
		tvRecordTime = (TextView) findViewById(R.id.tvRecordTime);

		btnCurrent = (Button) findViewById(R.id.btnCurrent);
		btnList = (Button) findViewById(R.id.btnList);
		btnCommand = (Button) findViewById(R.id.btnCommand);
		btnLine = (Button) findViewById(R.id.btnLine);
		btnData = (Button) findViewById(R.id.btnData);

		btnCurrent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
		btnList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(result.this, tag_list.class);
				startActivity(intent);
				finish();
			}
		});
		btnCommand.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(result.this, command.class);
				startActivity(intent);
				finish();
			}
		});
		btnLine.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(result.this, curve.class);
				startActivity(intent);
				finish();
			}
		});
		btnData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(result.this, data.class);
				startActivity(intent);
				finish();
			}
		});
		showData();
	}

	private void showData() {
		tvUID.setText(ma.curTag.uid);
		if (ma.curTag.name.length() > 0) {
			llName.setVisibility(View.VISIBLE);
			tvName.setText(ma.curTag.name);// 2
		} else {
			llName.setVisibility(View.GONE);
		}
		if (ma.curTag.is_using == 0)
			tvMode.setText("Allow Restart Mode");
		else if (ma.curTag.is_using == 0x80)
			tvMode.setText("Stop Mode");
		else
			tvMode.setText("Recording Mode");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2); // �p�ƫ���
		tvMax.setText(nf.format(ma.curTag.tmax));
		tvMin.setText(nf.format(ma.curTag.tmin));
		int sec = 0;
		if (ma.curTag.unit == 0)
			sec = ma.curTag.items * 2;// sec
		else
			sec = ma.curTag.items * 15 * ma.curTag.unit;
		int min = sec / 60;
		sec %= 60;
		int hour = min / 60;
		min %= 60;
		int day = hour / 24;
		hour %= 24;

		String time = "";

		if (day > 0) {
			time += (day + " Days " + hour + " Hours " + min + " Minutes "
					+ sec + " Seconds");
		} else {
			if (hour > 0)
				time += (hour + " Hours " + min + " Minutes " + sec + " Seconds");
			else {
				if (min > 0)
					time += (min + " Minutes " + sec + " Seconds");
				else
					time += (sec + " Seconds");
			}
		}
		tvRecordTime.setText(time);
	}
}
