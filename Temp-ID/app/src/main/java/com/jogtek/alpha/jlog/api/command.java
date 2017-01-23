package com.jogtek.alpha.jlog.api;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import com.jogtek.alpha.jlog.api.DataDevice.tag;
import com.jogtek.alpha.jlog.R;
import com.jogtek.alpha.jlog.curve;
import com.jogtek.alpha.jlog.result;
import com.jogtek.alpha.jlog.data;
import com.jogtek.alpha.jlog.tag_list;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class command extends Activity {
	private static final String TAG = "jlog";
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	Button btnStart;
	Button btnDelay;
	Button btnInterval;
	Button btnUpDown;
	Button btnExpire;
	Button btnAll;
	Button btnWhat;

	Button btnCurrent;
	Button btnList;
	Button btnCommand;
	Button btnLine;
	Button btnData;

	EditText etDelay;
	EditText etInterval;
	EditText etUp;
	EditText etDown;
	EditText etExpire;

	TextView tvStart;
	TextView tvDelay;
	TextView tvInterval;
	TextView tvUpDown;
	TextView tvExpire;

	int delay = 0, interval = 0, up = 0, down = 0, expire = 0;
	String nfcID = "";
	byte[] uid = new byte[8];

	private long cpt = 0;

	boolean start = true;
	boolean[] isWrite = new boolean[5];

	String addr = "0000";
	String addr2 = "002F";
	DataDevice ma = (DataDevice) getApplication();
	String[] catBlocks = null;
	String[] catValueBlocks = null;

	byte[] GetSystemInfoAnswer = null;
	byte[] ReadMultipleBlockAnswer = null;
	int nbblocks = 0;

	String sNbOfBlock = null;
	byte[] numberOfBlockToRead = null;

	String startAddressString = null;
	byte[] addressStart = null;
	byte[] WriteSingleBlockAnswer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.command);

		ma = (DataDevice) getApplication();
		initListener();

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

			GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
					tagFromIntent, (DataDevice) getApplication());

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				start = true;// false;// true;
				new StartReadTask().execute();
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

	private String hex2ASCII(String hex) {
		String ascii = "";
		try {
			byte[] bytes = hex.getBytes("UTF-8");
			byte[] bytes2 = new byte[4];
			for (int j = 0; j < bytes.length; j += 2) {
				byte c1 = 0;
				if (bytes[j] > 'A')
					c1 = (byte) (bytes[j] - 'A' + 10);
				else
					c1 = (byte) (bytes[j] - '0');
				byte c2 = (byte) (bytes[j + 1] & 0x0f);
				if (bytes[j + 1] > 'A')
					c2 = (byte) (bytes[j + 1] - 'A' + 10);
				else
					c2 = (byte) (bytes[j + 1] - '0');
				bytes2[j >> 1] = (byte) ((c1 << 4) | c2);
			}
			ascii = new String(bytes2, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ascii;
	}

	public int convertDIPtoPixel(int dp) {
		final float scale = getResources().getDisplayMetrics().density;
		int pixels = (int) (dp * scale + 0.5f);
		return pixels;
	}

	private void initListener() {
		etDelay = (EditText) findViewById(R.id.etDelay);
		etInterval = (EditText) findViewById(R.id.etInterval);
		etUp = (EditText) findViewById(R.id.etUp);
		etDown = (EditText) findViewById(R.id.etDown);
		etExpire = (EditText) findViewById(R.id.etExpire);

		tvStart = (TextView) findViewById(R.id.tvStart);
		tvDelay = (TextView) findViewById(R.id.tvDelay);
		tvInterval = (TextView) findViewById(R.id.tvInterval);
		tvUpDown = (TextView) findViewById(R.id.tvUpDown);
		tvExpire = (TextView) findViewById(R.id.tvExpire);

		btnCurrent = (Button) findViewById(R.id.btnCurrent);
		btnList = (Button) findViewById(R.id.btnList);
		btnCommand = (Button) findViewById(R.id.btnCommand);
		btnLine = (Button) findViewById(R.id.btnLine);
		btnData = (Button) findViewById(R.id.btnData);

		tvStart.setText("");
		tvDelay.setText("");
		tvInterval.setText("");
		tvUpDown.setText("");
		tvExpire.setText("");

		btnCurrent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(command.this, result.class);
				startActivity(intent);
				finish();
			}
		});
		btnList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(command.this, tag_list.class);
				startActivity(intent);
				finish();
			}
		});
		btnCommand.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
		btnLine.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(command.this, curve.class);
				startActivity(intent);
				finish();
			}
		});
		btnData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(command.this, data.class);
				startActivity(intent);
				finish();
			}
		});

		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isWrite[0] = true;
				isWrite[1] = false;
				isWrite[2] = false;
				isWrite[3] = false;
				isWrite[4] = false;
				new StartWriteTask().execute();
			}
		});
		btnDelay = (Button) findViewById(R.id.btnDelay);
		btnDelay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (etDelay.getText().toString() != null
						&& etDelay.getText().toString().length() > 0) {
					delay = Integer.valueOf(etDelay.getText().toString());
					isWrite[0] = false;
					isWrite[1] = true;
					isWrite[2] = false;
					isWrite[3] = false;
					isWrite[4] = false;
					new StartWriteTask().execute();
				}
			}
		});
		btnInterval = (Button) findViewById(R.id.btnInterval);
		btnInterval.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (etInterval.getText().toString() != null
						&& etInterval.getText().toString().length() > 0) {
					interval = Integer.valueOf(etInterval.getText().toString());
					isWrite[0] = false;
					isWrite[1] = false;
					isWrite[2] = true;
					isWrite[3] = false;
					isWrite[4] = false;
					new StartWriteTask().execute();
				}
			}
		});
		btnUpDown = (Button) findViewById(R.id.btnUpDown);
		btnUpDown.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (etUp.getText().toString() != null
						&& etDown.getText().toString() != null
						&& etUp.getText().toString().length() > 0
						&& etDown.getText().toString().length() > 0) {
					up = Integer.valueOf(etUp.getText().toString());
					down = Integer.valueOf(etDown.getText().toString());
					isWrite[0] = false;
					isWrite[1] = false;
					isWrite[2] = false;
					isWrite[3] = true;
					isWrite[4] = false;
					new StartWriteTask().execute();
				}
			}
		});
		btnExpire = (Button) findViewById(R.id.btnExpire);
		btnExpire.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (etExpire.getText().toString() != null
						&& etExpire.getText().toString().length() > 0) {
					expire = Integer.valueOf(etExpire.getText().toString());
					isWrite[0] = false;
					isWrite[1] = false;
					isWrite[2] = false;
					isWrite[3] = false;
					isWrite[4] = true;
					new StartWriteTask().execute();
				}
			}
		});
		btnWhat = (Button) findViewById(R.id.btnWhat);
		btnWhat.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new AlertDialog.Builder(command.this)
						.setTitle(
								getResources()
										.getString(R.string.t_explanation))
						.setMessage(
								"0 => 2 sec\n1 => 15 sec\n>1 => number x 15sec")
						.setNegativeButton("Close", null).show();
			}
		});
		btnAll = (Button) findViewById(R.id.btnAll);
		btnAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (etDelay.getText().toString() == null
						|| etDelay.getText().toString().length() == 0) {
					new AlertDialog.Builder(command.this)
							.setTitle("Delay time is null!")
							.setMessage("Please input delay time!")
							.setNegativeButton("Close", null).show();
				} else if (etInterval.getText().toString() == null
						|| etInterval.getText().toString().length() == 0) {
					new AlertDialog.Builder(command.this)
							.setTitle("Interval time is null!")
							.setMessage("Please input interval time!")
							.setNegativeButton("Close", null).show();
				} else if (etUp.getText().toString() == null
						|| etUp.getText().toString().length() == 0) {
					new AlertDialog.Builder(command.this)
							.setTitle("Up limit is null!")
							.setMessage("Please input up limit!")
							.setNegativeButton("Close", null).show();
				} else if (etDown.getText().toString() == null
						|| etDown.getText().toString().length() == 0) {
					new AlertDialog.Builder(command.this)
							.setTitle("Down limit is null!")
							.setMessage("Please input down limit!")
							.setNegativeButton("Close", null).show();
				} else if (etExpire.getText().toString() == null
						|| etExpire.getText().toString().length() == 0) {
					new AlertDialog.Builder(command.this)
							.setTitle("Quota of over limit is null!")
							.setMessage("Please input quota of over limit!")
							.setNegativeButton("Close", null).show();
				} else {
					isWrite[0] = true;
					isWrite[1] = false;
					isWrite[2] = false;
					isWrite[3] = false;
					isWrite[4] = false;
					if (etDelay.getText().toString() != null
							&& etDelay.getText().toString().length() > 0) {
						delay = Integer.valueOf(etDelay.getText().toString());
						isWrite[1] = true;
					}
					if (etInterval.getText().toString() != null
							&& etInterval.getText().toString().length() > 0) {
						interval = Integer.valueOf(etInterval.getText()
								.toString());
						isWrite[2] = true;
					}
					if (etUp.getText().toString() != null
							&& etDown.getText().toString() != null
							&& etUp.getText().toString().length() > 0
							&& etDown.getText().toString().length() > 0) {
						up = Integer.valueOf(etUp.getText().toString());
						down = Integer.valueOf(etDown.getText().toString());
						isWrite[3] = true;
					}
					if (etExpire.getText().toString() != null
							&& etExpire.getText().toString().length() > 0) {
						expire = Integer.valueOf(etExpire.getText().toString());
						isWrite[4] = true;
					}
					new StartWriteTask().execute();
				}
			}
		});
		showData();
	}

	private void showData() {
		tvStart.setText("Start time = \n"
				+ String.valueOf(ma.curTag.year + 2000) + "/"
				+ String.valueOf(ma.curTag.month + 1) + "/"
				+ String.valueOf(ma.curTag.day) + " "
				+ String.valueOf(ma.curTag.hour) + ":"
				+ String.valueOf(ma.curTag.min) + ":"
				+ String.valueOf(ma.curTag.sec));
		tvDelay.setText("Delay time = " + String.valueOf(ma.curTag.delay)
				+ " min");
		if (ma.curTag.unit == 0)
			tvInterval.setText("Interval = 2 sec");
		else {
			int ss = 15 * ma.curTag.unit;
			String uu = "Interval = ";
			if (ss >= 60) {
				int mm = ss / 60;
				if (mm >= 60) {
					int hh = mm / 60;
					if (hh >= 24) {
						int dd = hh / 24;
						uu += String.valueOf(dd) + " day ";
					}
					hh %= 24;
					uu += String.valueOf(hh) + " hour ";
				}
				mm %= 60;
				uu += String.valueOf(mm) + " minute ";
			}
			ss %= 60;
			uu += String.valueOf(ss) + " sec ";
			tvInterval.setText(uu);
		}
		tvUpDown.setText("Up limit=" + String.valueOf(ma.curTag.up_limit)
				+ "\nDown limit=" + String.valueOf(ma.curTag.dn_limit));
		tvExpire.setText("Quota of over limit = "
				+ String.valueOf(ma.curTag.over_t));
	}

	public boolean DecodeGetSystemInfoResponse(byte[] GetSystemInfoResponse) {
		int special = 0;
		if (GetSystemInfoResponse[0] == (byte) 0x00
				&& GetSystemInfoResponse.length >= 12) {
			String uidToString = "";
			for (int i = 1; i <= 8; i++) {
				uid[i - 1] = GetSystemInfoResponse[10 - i];
				uidToString += Helper.ConvertHexByteToString(uid[i - 1]);
			}
			// ***** TECHNO ******
			ma.uid = nfcID = uidToString.replaceAll(" ", "");
			ma.setUid(uidToString);
			if (uid[0] == (byte) 0xE0)
				ma.setTechno("ISO 15693");
			else if (uid[0] == (byte) 0xD0)
				ma.setTechno("ISO 14443");
			else
				ma.setTechno("Unknown techno");

			// ***** MANUFACTURER ****
			if (uid[1] == (byte) 0x02)
				ma.setManufacturer("STMicroelectronics");
			else if (uid[1] == (byte) 0x04)
				ma.setManufacturer("NXP");
			else if (uid[1] == (byte) 0x07)
				ma.setManufacturer("Texas Instrument");
			else
				ma.setManufacturer("Unknown manufacturer");

			// **** PRODUCT NAME *****
			if (uid[2] >= (byte) 0x04 && uid[2] <= (byte) 0x07) {
				ma.setProductName("LRI512");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x14 && uid[2] <= (byte) 0x17) {
				ma.setProductName("LRI64");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x20 && uid[2] <= (byte) 0x23) {
				ma.setProductName("LRI2K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x28 && uid[2] <= (byte) 0x2B) {
				ma.setProductName("LRIS2K");
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x2C && uid[2] <= (byte) 0x2F) {
				ma.setProductName("M24LR64");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x40 && uid[2] <= (byte) 0x43) {
				ma.setProductName("LRI1K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x44 && uid[2] <= (byte) 0x47) {
				ma.setProductName("LRIS64K");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x48 && uid[2] <= (byte) 0x4B) {
				ma.setProductName("M24LR01E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x4C && uid[2] <= (byte) 0x4F) {
				ma.setProductName("M24LR16E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false)
					return false;
			} else if (uid[2] >= (byte) 0x50 && uid[2] <= (byte) 0x53) {
				ma.setProductName("M24LR02E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[2] >= (byte) 0x54 && uid[2] <= (byte) 0x57) {
				ma.setProductName("M24LR32E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false)
					return false;
			} else if (uid[2] >= (byte) 0x58 && uid[2] <= (byte) 0x5B) {
				ma.setProductName("M24LR04E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x5C && uid[2] <= (byte) 0x5F) {
				ma.setProductName("M24LR64E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false)
					return false;
			} else if (uid[2] >= (byte) 0x60 && uid[2] <= (byte) 0x63) {
				ma.setProductName("M24LR08E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[2] >= (byte) 0x64 && uid[2] <= (byte) 0x67) {
				ma.setProductName("M24LR128E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false)
					return false;
			} else if (uid[2] >= (byte) 0x6C && uid[2] <= (byte) 0x6F) {
				ma.setProductName("M24LR256E");
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
				if (ma.isBasedOnTwoBytesAddress() == false)
					return false;
			} else if (uid[2] >= (byte) 0xF8 && uid[2] <= (byte) 0xFB) {
				ma.setProductName("detected product");
				ma.setBasedOnTwoBytesAddress(true);
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(true);
			} else if (uid[1] == (byte) 0x07 && uid[2] == -127) {
				ma.setProductName("Tag-it Plus");
				ma.setBasedOnTwoBytesAddress(false);
				ma.setMultipleReadSupported(true);
				ma.setMemoryExceed2048bytesSize(false);
			} else if (uid[1] == (byte) 0x07 && uid[2] == -60) {
				ma.setProductName("Tag-it Pro");
				ma.setBasedOnTwoBytesAddress(false);
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
				special = 1;
			} else {
				ma.setProductName("Unknown product");
				ma.setBasedOnTwoBytesAddress(false);
				ma.setMultipleReadSupported(false);
				ma.setMemoryExceed2048bytesSize(false);
			}

			if (special > 0) {
				if (special == 1) {
					// *** DSFID ***
					ma.setDsfid("0");
					// *** AFI ***
					ma.setAfi("0");
					// *** MEMORY SIZE ***
					ma.setMemorySize("7");
					// *** BLOCK SIZE ***
					ma.setBlockSize("3");
					// *** IC REFERENCE ***
					ma.setIcReference("0");
				}
			} else {
				// *** DSFID ***
				ma.setDsfid(Helper
						.ConvertHexByteToString(GetSystemInfoResponse[10]));

				// *** AFI ***
				ma.setAfi(Helper
						.ConvertHexByteToString(GetSystemInfoResponse[11]));

				// *** MEMORY SIZE ***
				if (ma.isBasedOnTwoBytesAddress()) {
					String temp = new String();
					temp += Helper
							.ConvertHexByteToString(GetSystemInfoResponse[13]);
					temp += Helper
							.ConvertHexByteToString(GetSystemInfoResponse[12]);
					ma.setMemorySize(temp);
				} else
					ma.setMemorySize(Helper
							.ConvertHexByteToString(GetSystemInfoResponse[12]));

				// *** BLOCK SIZE ***
				if (ma.isBasedOnTwoBytesAddress())
					ma.setBlockSize(Helper
							.ConvertHexByteToString(GetSystemInfoResponse[14]));
				else
					ma.setBlockSize(Helper
							.ConvertHexByteToString(GetSystemInfoResponse[13]));

				// *** IC REFERENCE ***
				if (ma.isBasedOnTwoBytesAddress())
					ma.setIcReference(Helper
							.ConvertHexByteToString(GetSystemInfoResponse[15]));
				else
					ma.setIcReference(Helper
							.ConvertHexByteToString(GetSystemInfoResponse[14]));
			}
			return true;
		}

		// if the tag has returned an error code
		else
			return false;
	}

	private class StartWriteTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(command.this);

		// can use UI thread here
		protected void onPreExecute() {
			this.dialog.setMessage("Please place your phone near the card");
			this.dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			DataDevice dataDevice = (DataDevice) getApplication();
			GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
					dataDevice.getCurrentTag(), dataDevice);
			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				byte[] dataToWrite = new byte[4];
				if (isWrite[0] == true) {
					Calendar c = Calendar.getInstance();
					int year = c.get(Calendar.YEAR) - 2000;
					int month = c.get(Calendar.MONTH);
					int day = c.get(Calendar.DAY_OF_MONTH);
					int hour = c.get(Calendar.HOUR_OF_DAY);
					int min = c.get(Calendar.MINUTE);
					int sec = c.get(Calendar.SECOND);

					ma.dataFromRead[2][0] = dataToWrite[0] = (byte) ((year << 2) | ((month >> 2) & 0x03));
					ma.dataFromRead[2][1] = dataToWrite[1] = (byte) (((month & 0x03) << 6)
							| (day << 1) | ((hour >> 4) & 0x01));
					ma.dataFromRead[2][2] = dataToWrite[2] = (byte) ((hour << 4) | ((min >> 2) & 0x0f));
					ma.dataFromRead[2][3] = dataToWrite[3] = (byte) ((min << 6) | (sec & 0x3f));
					addressStart = Helper.ConvertStringToHexBytes("002a");

					cpt = 0;
					WriteSingleBlockAnswer = null;
					if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1)
								&& cpt <= 10) {
							WriteSingleBlockAnswer = NFCCommand
									.SendWriteSingleBlockCommand(
											dataDevice.getCurrentTag(),
											addressStart, dataToWrite,
											dataDevice);
							cpt++;
						}
					}
				}
				if (isWrite[3] == true) {
					int upper = quick.findLimit(up);
					int lower = quick.findLimit(down);
					ma.dataFromRead[4][0] = dataToWrite[0] = (byte) (upper);
					ma.dataFromRead[4][1] = dataToWrite[1] = (byte) (upper >> 8);
					ma.dataFromRead[4][2] = dataToWrite[2] = (byte) (lower);
					ma.dataFromRead[4][3] = dataToWrite[3] = (byte) (lower >> 8);
					addressStart = Helper.ConvertStringToHexBytes("002c");

					cpt = 0;
					WriteSingleBlockAnswer = null;
					if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1)
								&& cpt <= 10) {
							WriteSingleBlockAnswer = NFCCommand
									.SendWriteSingleBlockCommand(
											dataDevice.getCurrentTag(),
											addressStart, dataToWrite,
											dataDevice);
							cpt++;
						}
					}
				}
				if (isWrite[2] == true) {
					int intt = interval * 4;
					ma.dataFromRead[6][0] = dataToWrite[0] = (byte) (intt);
					ma.dataFromRead[6][1] = dataToWrite[1] = (byte) (intt >> 8);
					dataToWrite[2] = ma.dataFromRead[6][2];
					dataToWrite[3] = ma.dataFromRead[6][3];
					addressStart = Helper.ConvertStringToHexBytes("002e");

					cpt = 0;
					WriteSingleBlockAnswer = null;
					if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1)
								&& cpt <= 10) {
							WriteSingleBlockAnswer = NFCCommand
									.SendWriteSingleBlockCommand(
											dataDevice.getCurrentTag(),
											addressStart, dataToWrite,
											dataDevice);
							cpt++;
						}
					}
				}
				if (isWrite[4] == true) {
					if (isWrite[1] == true)
						ma.dataFromRead[5][0] = dataToWrite[0] = (byte) (delay);
					else
						dataToWrite[0] = ma.dataFromRead[5][0];
					ma.dataFromRead[5][1] = dataToWrite[1] = (byte) (expire);
					dataToWrite[2] = ma.dataFromRead[5][2];
					dataToWrite[3] = ma.dataFromRead[5][3];
					addressStart = Helper.ConvertStringToHexBytes("002d");

					cpt = 0;
					WriteSingleBlockAnswer = null;
					if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1)
								&& cpt <= 10) {
							WriteSingleBlockAnswer = NFCCommand
									.SendWriteSingleBlockCommand(
											dataDevice.getCurrentTag(),
											addressStart, dataToWrite,
											dataDevice);
							cpt++;
						}
					}
				} else if (isWrite[1] == true) {
					ma.dataFromRead[5][0] = dataToWrite[0] = (byte) (delay);
					dataToWrite[1] = ma.dataFromRead[5][1];
					dataToWrite[2] = ma.dataFromRead[5][2];
					dataToWrite[3] = ma.dataFromRead[5][3];
					addressStart = Helper.ConvertStringToHexBytes("002d");

					cpt = 0;
					WriteSingleBlockAnswer = null;
					if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
						while ((WriteSingleBlockAnswer == null || WriteSingleBlockAnswer[0] == 1)
								&& cpt <= 10) {
							WriteSingleBlockAnswer = NFCCommand
									.SendWriteSingleBlockCommand(
											dataDevice.getCurrentTag(),
											addressStart, dataToWrite,
											dataDevice);
							cpt++;
						}
					}
				}
			}
			return null;
		}

		protected void findLimit(int temp) {

		}

		// can use UI thread here
		protected void onPostExecute(final Void unused) {
			if (this.dialog.isShowing())
				this.dialog.dismiss();
			if (WriteSingleBlockAnswer == null) {
				Toast.makeText(getApplicationContext(),
						"ERROR Write (No tag answer) ", Toast.LENGTH_SHORT)
						.show();
			} else if (WriteSingleBlockAnswer[0] == (byte) 0x01) {
				Toast.makeText(getApplicationContext(), "ERROR Write ",
						Toast.LENGTH_SHORT).show();
			} else if (WriteSingleBlockAnswer[0] == (byte) 0xFF) {
				Toast.makeText(getApplicationContext(), "ERROR Write ",
						Toast.LENGTH_SHORT).show();
			} else if (WriteSingleBlockAnswer[0] == (byte) 0x00) {
				Toast.makeText(getApplicationContext(), "Write Sucessfull ",
						Toast.LENGTH_SHORT).show();
				// finish();
			} else {
				Toast.makeText(getApplicationContext(), "Write ERROR ",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class StartReadTask extends AsyncTask<Void, Void, Void> {
		private final ProgressDialog dialog = new ProgressDialog(command.this);

		protected void onPreExecute() {
			DataDevice dataDevice = (DataDevice) getApplication();

			GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
					dataDevice.getCurrentTag(), dataDevice);

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				if (start)
					startAddressString = addr;
				else
					startAddressString = addr2;
				startAddressString = Helper.castHexKeyboard(startAddressString);
				startAddressString = Helper.FormatStringAddressStart(
						startAddressString, dataDevice);
				addressStart = Helper
						.ConvertStringToHexBytes(startAddressString);
				if (start)
					sNbOfBlock = "0047";// 1
				else {
					sNbOfBlock = "";
					int ii = (ma.items >> 1) + 1;
					for (int i = 0; i < 4; i++) {
						char c = (char) (ii % 10);
						c += '0';
						sNbOfBlock = c + sNbOfBlock;
						ii /= 10;
					}
				}

				sNbOfBlock = Helper.FormatStringNbBlockInteger(sNbOfBlock,
						startAddressString, dataDevice);
				numberOfBlockToRead = Helper
						.ConvertIntTo2bytesHexaFormat(Integer
								.parseInt(sNbOfBlock));
				this.dialog
						.setMessage("Please hold your phone close to the data logger.");
				this.dialog.show();
			} else {
				this.dialog.setMessage("No data logger detected");
				this.dialog.show();
			}
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params) {
			DataDevice dataDevice = (DataDevice) getApplication();

			ReadMultipleBlockAnswer = null;
			cpt = 0;

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				if (ma.isMultipleReadSupported() == false
						|| Helper
								.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <= 1) // ex:
																							// LRIS2K
				{
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.Send_several_ReadSingleBlockCommands_NbBlocks(
										dataDevice.getCurrentTag(),
										addressStart, numberOfBlockToRead,
										dataDevice);
						cpt++;
					}
					cpt = 0;
				} else if (Helper
						.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.SendReadMultipleBlockCommandCustom(
										dataDevice.getCurrentTag(),
										addressStart, numberOfBlockToRead[1],
										dataDevice);
						cpt++;
					}
					cpt = 0;
				} else {
					ReadMultipleBlockAnswer = null;
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.SendReadMultipleBlockCommandCustom2(
										dataDevice.getCurrentTag(),
										addressStart, numberOfBlockToRead,
										dataDevice);
						cpt++;
					}
					cpt = 0;
				}
			}
			return null;
		}

		// can use UI thread here
		protected void onPostExecute(final Void unused) {
			Log.i("ScanRead", "Button Read CLICKED **** On Post Execute ");
			if (this.dialog.isShowing())
				this.dialog.dismiss();

			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				nbblocks = Integer.parseInt(sNbOfBlock);

				if (ReadMultipleBlockAnswer != null
						&& ReadMultipleBlockAnswer.length - 1 > 0) {
					if (ReadMultipleBlockAnswer[0] == 0x00) {
						if (start) {
							// 1+
							ma.dataFromRead[2][0] = ReadMultipleBlockAnswer[5 + ma.offset];
							ma.dataFromRead[2][1] = ReadMultipleBlockAnswer[6 + ma.offset];
							ma.dataFromRead[2][2] = ReadMultipleBlockAnswer[7 + ma.offset];
							ma.dataFromRead[2][3] = ReadMultipleBlockAnswer[8 + ma.offset];
							ma.dataFromRead[3][0] = ReadMultipleBlockAnswer[9 + ma.offset];
							ma.dataFromRead[3][1] = ReadMultipleBlockAnswer[10 + ma.offset];
							ma.dataFromRead[3][2] = ReadMultipleBlockAnswer[11 + ma.offset];
							ma.dataFromRead[3][3] = ReadMultipleBlockAnswer[12 + ma.offset];
							ma.dataFromRead[4][0] = ReadMultipleBlockAnswer[13 + ma.offset];
							ma.dataFromRead[4][1] = ReadMultipleBlockAnswer[14 + ma.offset];
							ma.dataFromRead[4][2] = ReadMultipleBlockAnswer[15 + ma.offset];
							ma.dataFromRead[4][3] = ReadMultipleBlockAnswer[16 + ma.offset];
							ma.dataFromRead[5][0] = ReadMultipleBlockAnswer[17 + ma.offset];
							ma.dataFromRead[5][1] = ReadMultipleBlockAnswer[18 + ma.offset];
							ma.dataFromRead[5][2] = ReadMultipleBlockAnswer[19 + ma.offset];
							ma.dataFromRead[5][3] = ReadMultipleBlockAnswer[20 + ma.offset];
							ma.dataFromRead[6][0] = ReadMultipleBlockAnswer[21 + ma.offset];
							ma.dataFromRead[6][1] = ReadMultipleBlockAnswer[22 + ma.offset];
							ma.dataFromRead[6][2] = ReadMultipleBlockAnswer[23 + ma.offset];
							ma.dataFromRead[6][3] = ReadMultipleBlockAnswer[24 + ma.offset];
							ma.delay0=(((int) ReadMultipleBlockAnswer[3 + ma.offset0] & 0x00ff) << 16)
									+ (((int) ReadMultipleBlockAnswer[2 + ma.offset0] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[1 + ma.offset0] & 0x00ff));
							ma.year = (((int) ReadMultipleBlockAnswer[5 + ma.offset] >> 2) & 0x3f);
							ma.month = (((int) ReadMultipleBlockAnswer[6 + ma.offset] >> 6) & 0x03)
									+ (((int) ReadMultipleBlockAnswer[5 + ma.offset] & 0x03) << 2);
							ma.day = (((int) ReadMultipleBlockAnswer[6 + ma.offset] >> 1) & 0x1f);
							ma.hour = (((int) ReadMultipleBlockAnswer[7 + ma.offset] >> 4) & 0x0f)
									+ (((int) ReadMultipleBlockAnswer[6 + ma.offset] & 0x01) << 4);
							ma.min = (((int) ReadMultipleBlockAnswer[8 + ma.offset] >> 6) & 0x03)
									+ (((int) ReadMultipleBlockAnswer[7 + ma.offset] & 0x0f) << 2);
							ma.sec = ((int) ReadMultipleBlockAnswer[8 + ma.offset] & 0x3f);

							ma.adc = (((int) ReadMultipleBlockAnswer[10 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[9 + ma.offset] & 0x00ff));
							if(ma.adc>0)
								ma.formula=1;
							else
								ma.formula=0;
							
							ma.items = (((int) ReadMultipleBlockAnswer[12 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[11 + ma.offset] & 0x00ff));

							ma.up_limit = (((int) ReadMultipleBlockAnswer[14 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[13 + ma.offset] & 0x00ff));
							ma.dn_limit = (((int) ReadMultipleBlockAnswer[16 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[15 + ma.offset] & 0x00ff));
							ma.up_limit = (int) quick.Search(ma.up_limit,
									ma.formula);
							ma.dn_limit = (int) quick.Search(ma.dn_limit,
									ma.formula);

							ma.delay = ((int) ReadMultipleBlockAnswer[17 + ma.offset] & 0x00ff);
							ma.over_t = ((int) ReadMultipleBlockAnswer[18 + ma.offset] & 0x00ff);
							ma.lave = ((int) ReadMultipleBlockAnswer[19 + ma.offset] & 0x00ff);
							ma.cal = ((int) ReadMultipleBlockAnswer[20 + ma.offset] & 0x00ff);

							ma.unit = (((int) ReadMultipleBlockAnswer[22 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[21 + ma.offset] & 0x00ff));

							ma.is_using = ((int) ReadMultipleBlockAnswer[23 + ma.offset] & 0x00ff);
							// 1-
							start = false;
							new StartReadTask().execute();
						} else {
							tag ntag = new tag();

							ntag.uid = ma.uid.replaceAll(" ", "");
							ntag.year = ma.year;
							ntag.month = ma.month;
							ntag.day = ma.day;
							ntag.hour = ma.hour;
							ntag.min = ma.min;
							ntag.sec = ma.sec;
							ntag.adc = ma.adc;
							ntag.items = ma.items;
							ntag.up_limit = ma.up_limit;
							ntag.dn_limit = ma.dn_limit;
							ntag.delay = ma.delay;
							ntag.delay0 = ma.delay0;// 1
							ntag.over_t = ma.over_t;
							ntag.lave = ma.lave;
							ntag.cal = ma.cal;
							ntag.unit = ma.unit;
							ntag.is_using = ma.is_using;
							ntag.tmax = -255;
							ntag.tmin = 255;
							for (int i = 1; i < ((ntag.items << 1) + 1); i += 2) {
								int a = 0;
								a = ((int) ReadMultipleBlockAnswer[i + 1] & 0x00ff);
								a = (a << 8)
										| ((int) ReadMultipleBlockAnswer[i] & 0x00ff);
								double b = quick.Search(a,ma.formula);
								ntag.datas.add(b);
								if (ntag.tmax < b) {
									ntag.tmax = b;
								}
								if (ntag.tmin > b) {
									ntag.tmin = b;
								}
							}
							ma.tmax = ntag.tmax;
							ma.tmin = ntag.tmin;
							long serial = Helper.ConvertStringToLong(ntag.uid
									.substring(4));
							String ss = Helper
									.ConvertLongToDecFormatString(serial);
							ntag.SERIAL = ntag.uid + "_"
									+ String.valueOf(ntag.year + 2000) + "_"
									+ String.valueOf(ntag.month + 1) + "_"
									+ String.valueOf(ntag.day) + "_"
									+ String.valueOf(ntag.hour) + "_"
									+ String.valueOf(ntag.min);
							boolean find = false;
							int i = 0;
							Exit: for (tag t : ma.tags) {
								if (t.SERIAL.equals(ntag.SERIAL)) {
									find = true;
									break Exit;
								}
								i++;
							}
							if (!find) {
								ma.tags.add(0, ntag);
								SharedPreference.save(command.this);
								ma.curTag = ma.tags.get(0);
							} else {
								ma.tags.set(i, ntag);
								ma.curTag = ntag;// ma.tags.get(i);
							}
							showData();
						}
					}
				}
			}
		}
	}
}
