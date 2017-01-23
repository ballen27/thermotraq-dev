package com.jogtek.alpha.jlog;

import java.io.File;
import org.apache.commons.net.ftp.FTPClient;

import com.google.gson.Gson;
import com.jogtek.alpha.jlog.api.DataDevice;
import com.jogtek.alpha.jlog.api.NFCCommand;
import com.jogtek.alpha.jlog.api.OnTaskCompleted;
import com.jogtek.alpha.jlog.api.ReadWrite;
import com.jogtek.alpha.jlog.api.SharedPreference;
import com.jogtek.alpha.jlog.api.command;
import com.jogtek.alpha.jlog.api.listTagAdapter;
import com.jogtek.alpha.jlog.api.my;
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
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class tag_list extends Activity {
	DataDevice ma;
	Gson gson = new Gson();
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	TextView tvUID;
	TextView tvMode;
	TextView tvMax;
	TextView tvMin;
	TextView tvRecordTime;

	Button btnClearAll;
	Button btnDnload;
	Button btnCurrent;
	Button btnList;
	Button btnCommand;
	Button btnLine;
	Button btnData;

	EditText etIP;
	EditText etName;
	EditText etPw;

	LinearLayout llIP;
	LinearLayout llName;
	LinearLayout llPw;
	ToggleButton tgSetting;

	ListView lvTags;
	listTagAdapter adapter;

	ProgressBar pbBar;
	ProgressDialog dialog;

	FTPClient ftpClient;
	ReadWrite rw = null;
	net n = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_list);

		initListener();
		rw = new ReadWrite(tag_list.this, pbBar, dialog);
		n = new net(tag_list.this);
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
		dialog = new ProgressDialog(tag_list.this);
		tvUID = (TextView) findViewById(R.id.tvUID);
		tvMode = (TextView) findViewById(R.id.tvMode);
		tvMax = (TextView) findViewById(R.id.tvMax);
		tvMin = (TextView) findViewById(R.id.tvMin);
		tvRecordTime = (TextView) findViewById(R.id.tvRecordTime);

		btnClearAll = (Button) findViewById(R.id.btnClearAll);
		btnDnload = (Button) findViewById(R.id.btnDnload);
		btnCurrent = (Button) findViewById(R.id.btnCurrent);
		btnList = (Button) findViewById(R.id.btnList);
		btnCommand = (Button) findViewById(R.id.btnCommand);
		btnLine = (Button) findViewById(R.id.btnLine);
		btnData = (Button) findViewById(R.id.btnData);
		etIP = (EditText) findViewById(R.id.etIP);
		etName = (EditText) findViewById(R.id.etName);
		etPw = (EditText) findViewById(R.id.etPw);
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
				intent.setClass(tag_list.this, command.class);
				startActivity(intent);
				finish();
			}
		});
		lvTags = (ListView) findViewById(R.id.lvTags);
		lvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ma.curTag = ma.tags.get(position);
				try {
					my.comfirmbox(
							tag_list.this,
							"",// "Delete",
							"",// "do you want to delete this item?",
							getResources().getString(R.string.text_show_data),
							getResources().getString(R.string.text_delete_this),
							new OnTaskCompleted() {
								@Override
								public void onTaskCompleted(String result) {
									if (result.equals("1")) {// text_show_data
										Intent intent = new Intent();
										intent.setClass(tag_list.this,
												curve.class);
										startActivity(intent);
										finish();
									} else {// text_delete_this
										if (ma.isFTP) {
											ma.tags.remove(ma.curTag);
											SharedPreference
													.save(tag_list.this);
											showData();
										} else {
				//							n.deleteOne(new OnTaskCompleted() {
				//								@Override
				//								public void onTaskCompleted(
				//										String result) {
				//									if (result.equals("1")) {// success
				//																// read
				//										showData();
				//									}
				//								}
				//							});
										}
									}
								}
							});
				} catch (Exception e) {
				}
			}
		});
		btnClearAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (ma.isFTP) {
					ma.tags.clear();
					SharedPreference.save(tag_list.this);
					showData();
				} else {
		//			n.deleteAll(new OnTaskCompleted() {
		//				@Override
		//				public void onTaskCompleted(String result) {
		//					if (result.equals("1")) {// success read
		//						showData();
		//					}
		//				}
		//			});
				}
			}
		});
		btnDnload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (ma.isFTP) {
		//			clearSD();
		//			n.downloadFTP(new OnTaskCompleted() {
		//				@Override
		//				public void onTaskCompleted(String result) {
		//					if (result.equals("1")) {// success read
		//						showData();
		//					}
		//				}
		//			});
				} else {
		//			n.download(new OnTaskCompleted() {
		//				@Override
		//				public void onTaskCompleted(String result) {
		//					if (result.equals("1")) {// success read
		//						showData();
		//					}
		//				}
		//			});
				}
			}
		});
		btnCurrent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(tag_list.this, result.class);
				startActivity(intent);
				finish();
			}
		});
		btnList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
		btnLine.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(tag_list.this, curve.class);
				startActivity(intent);
				finish();
			}
		});
		btnData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(tag_list.this, data.class);
				startActivity(intent);
				finish();
			}
		});
		if (ma.isFTP) {
			tgSetting.setVisibility(View.GONE);
			btnDnload.setVisibility(View.GONE);
		}
		showData();
	}

	private void clearSD() {
		File sdCardRoot = Environment.getExternalStorageDirectory();
		File yourDir = new File(sdCardRoot, "jogtek_dn");
		if (yourDir.exists()) {
			// if (yourDir.listFiles().length > 0) {
			for (File f : yourDir.listFiles()) {
				if (f.isFile()) {
					String name = f.getName();
					File file = new File(name);
					file.delete();
				}
			}
		}
	}

	private void showData() {
		adapter = new listTagAdapter(tag_list.this, 0, ma.tags);
		lvTags.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
}
