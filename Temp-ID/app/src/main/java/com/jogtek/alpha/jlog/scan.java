package com.jogtek.alpha.jlog;

import com.google.gson.Gson;
import com.jogtek.alpha.jlog.api.DataDevice;
import com.jogtek.alpha.jlog.api.NFCCommand;
import com.jogtek.alpha.jlog.api.OnTaskCompleted;
import com.jogtek.alpha.jlog.api.ReadWrite;
import com.jogtek.alpha.jlog.api.SharedPreference;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class scan extends Activity {
	DataDevice ma;
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	boolean go = true;

	Button btnCreatePW;
	Button btnEnterPW;

	EditText editTextPW;

	LinearLayout ll1;
	LinearLayout ll2;

	Gson gson = new Gson();

	ProgressBar progressBar;
	ProgressDialog dialog;
	ReadWrite rw = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ma = (DataDevice) getApplication();

		ma.dataFromRead[0] = new byte[4];
		ma.dataFromRead[1] = new byte[4];
		ma.dataFromRead[2] = new byte[4];
		ma.dataFromRead[3] = new byte[4];
		ma.dataFromRead[4] = new byte[4];
		ma.dataFromRead[5] = new byte[4];
		ma.dataFromRead[6] = new byte[4];
		initListener();
		rw = new ReadWrite(scan.this, progressBar, dialog);
		PackageManager pm = getPackageManager();
		if (pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
			mAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mAdapter.isEnabled()) {
				// mPendingIntent = PendingIntent.getActivity(this, 0, new
				// Intent(
				// this, getClass())
				// .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				// IntentFilter ndef = new IntentFilter(
				// NfcAdapter.ACTION_TECH_DISCOVERED);//
				// mFilters = new IntentFilter[] { ndef, };
				// mTechLists = new String[][] { new String[] {
				// android.nfc.tech.NfcV.class
				// .getName() } };
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		switch (item.getItemId()) {
		case 1:
			intent.setClass(scan.this, curve.class);
			break;
		default:
		}
		startActivity(intent);
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tagFromIntent == null)
			return;
		DataDevice dataDevice = (DataDevice) getApplication();
		dataDevice.setCurrentTag(tagFromIntent);
		rw.GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
				tagFromIntent, (DataDevice) getApplication());
		if (rw.DecodeGetSystemInfoResponse(rw.GetSystemInfoAnswer)) {
			rw.Read(new OnTaskCompleted() {
				@Override
				public void onTaskCompleted(String result) {
					if (result.equals("1")) {// success read
						Intent intent = new Intent();
						intent.setClass(scan.this, result.class);
						startActivity(intent);
					}
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
		// mTechLists);
		mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
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
		progressBar = (ProgressBar) findViewById(R.id.pbBar);
		dialog = new ProgressDialog(scan.this);
		btnCreatePW = (Button) findViewById(R.id.btnCreatePW);
		btnEnterPW = (Button) findViewById(R.id.btnEnterPW);
		editTextPW = (EditText) findViewById(R.id.etPW);
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll2 = (LinearLayout) findViewById(R.id.ll2);

		btnCreatePW.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ll1.setVisibility(View.INVISIBLE);
				ll2.setVisibility(View.VISIBLE);
			}
		});
		btnEnterPW.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (ma.PW == "") {
					ma.PW = editTextPW.getText().toString();
					SharedPreference.savePW(scan.this);
					go = true;
					Toast.makeText(getApplicationContext(),
							"Password has been created!", Toast.LENGTH_SHORT)
							.show();
					ll2.setVisibility(View.INVISIBLE);
				} else if (ma.PW.equals(editTextPW.getText().toString())) {
					go = true;
					Toast.makeText(getApplicationContext(),
							"Login successfully!", Toast.LENGTH_SHORT).show();
					ll2.setVisibility(View.INVISIBLE);
				} else {
					Toast.makeText(getApplicationContext(), "Wrong password!",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		SharedPreference.load(scan.this);
		if (ma.PW == "") {
			ll1.setVisibility(View.INVISIBLE);// VISIBLE);
			ll2.setVisibility(View.INVISIBLE);
		} else {
			go = false;
			ll1.setVisibility(View.INVISIBLE);
			btnEnterPW.setText("Login");
			ll2.setVisibility(View.VISIBLE);
		}
	}
}
