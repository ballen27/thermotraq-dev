package com.jogtek.alpha.jlog.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.jogtek.alpha.jlog.api.DataDevice.tag;

public class ReadWrite {
	DataDevice ma;
	Context c = null;

	String addr = "0000";
	String addr2 = "002F";
	String[] catBlocks = null;
	String[] catValueBlocks = null;

	public byte[] GetSystemInfoAnswer = null;
	byte[] ReadMultipleBlockAnswer = null;
	int nbblocks = 0;

	String sNbOfBlock = null;
	byte[] numberOfBlockToRead = null;

	String startAddressString = null;
	byte[] addressStart = null;
	byte[] WriteSingleBlockAnswer = null;

	String nfcID = "";
	private long cpt = 0;
	boolean start = true;

	ProgressBar pbBar;
	ProgressDialog dialog;

	OnTaskCompleted listener;

	public ReadWrite(Context context, ProgressBar pbBar, ProgressDialog dialog) {
		this.c = context;
		this.pbBar = pbBar;
		this.dialog = dialog;
		ma = (DataDevice) c.getApplicationContext();
	}

	public void Read(final OnTaskCompleted listener1) {
		this.listener = listener1;
		start = true;
		new StartReadTask().execute();
	}

	private class StartReadTask extends AsyncTask<Void, Void, Void> {
		protected void onPreExecute() {
			// GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
			// ma.getCurrentTag(), ma);
			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				if (start)
					startAddressString = addr;
				else
					startAddressString = addr2;
				startAddressString = Helper.castHexKeyboard(startAddressString);
				startAddressString = Helper.FormatStringAddressStart(
						startAddressString, ma);
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
						startAddressString, ma);
				numberOfBlockToRead = Helper
						.ConvertIntTo2bytesHexaFormat(Integer
								.parseInt(sNbOfBlock));
				dialog.setMessage("Please hold your phone close to the data logger.");
				dialog.show();
			} else {
			}
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params) {
			ReadMultipleBlockAnswer = null;
			cpt = 0;
			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				if (ma.isMultipleReadSupported() == false
						|| Helper
								.Convert2bytesHexaFormatToInt(numberOfBlockToRead) <= 1)
				{// ex: LRIS2K
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.Send_several_ReadSingleBlockCommands_NbBlocks(
										ma.getCurrentTag(), addressStart,
										numberOfBlockToRead, ma);
						cpt++;
					}
					cpt = 0;
				} else if (Helper
						.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.SendReadMultipleBlockCommandCustom(
										ma.getCurrentTag(), addressStart,
										numberOfBlockToRead[1], ma);
						cpt++;
					}
					cpt = 0;
				} else {
					ReadMultipleBlockAnswer = null;
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.SendReadMultipleBlockCommandCustom2(
										ma.getCurrentTag(), addressStart,
										numberOfBlockToRead, ma);
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
			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				nbblocks = Integer.parseInt(sNbOfBlock);

				if (ReadMultipleBlockAnswer != null
						&& ReadMultipleBlockAnswer.length - 1 > 0) {
					if (ReadMultipleBlockAnswer[0] == 0x00) {
						if (start) {
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
							ma.btnFlag = ((int) ReadMultipleBlockAnswer[4 + ma.offset0] & 0x00ff);// v2
							ma.delay0 = (((int) ReadMultipleBlockAnswer[3 + ma.offset0] & 0x00ff) << 16)
									+ (((int) ReadMultipleBlockAnswer[2 + ma.offset0] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[1 + ma.offset0] & 0x00ff));
							// v2+
							ma.firstAssignTime_y = (((int) ReadMultipleBlockAnswer[5 + ma.offset0] >> 2) & 0x3f);
							ma.firstAssignTime_m = (((int) ReadMultipleBlockAnswer[6 + ma.offset0] >> 6) & 0x03)
									+ (((int) ReadMultipleBlockAnswer[5 + ma.offset] & 0x03) << 2);
							ma.firstAssignTime_d = (((int) ReadMultipleBlockAnswer[6 + ma.offset0] >> 1) & 0x1f);
							ma.firstAssignTime_h = (((int) ReadMultipleBlockAnswer[7 + ma.offset0] >> 4) & 0x0f)
									+ (((int) ReadMultipleBlockAnswer[6 + ma.offset] & 0x01) << 4);
							ma.firstAssignTime_i = (((int) ReadMultipleBlockAnswer[8 + ma.offset0] >> 6) & 0x03)
									+ (((int) ReadMultipleBlockAnswer[7 + ma.offset] & 0x0f) << 2);
							ma.firstAssignTime_s = ((int) ReadMultipleBlockAnswer[8 + ma.offset0] & 0x3f);

							ma.firstRecordTime_y = (((int) ReadMultipleBlockAnswer[1 + ma.offset] >> 2) & 0x3f);
							ma.firstRecordTime_m = (((int) ReadMultipleBlockAnswer[2 + ma.offset] >> 6) & 0x03)
									+ (((int) ReadMultipleBlockAnswer[1 + ma.offset] & 0x03) << 2);
							ma.firstRecordTime_d = (((int) ReadMultipleBlockAnswer[2 + ma.offset] >> 1) & 0x1f);
							ma.firstRecordTime_h = (((int) ReadMultipleBlockAnswer[3 + ma.offset] >> 4) & 0x0f)
									+ (((int) ReadMultipleBlockAnswer[2 + ma.offset] & 0x01) << 4);
							ma.firstRecordTime_i = (((int) ReadMultipleBlockAnswer[4 + ma.offset] >> 6) & 0x03)
									+ (((int) ReadMultipleBlockAnswer[3 + ma.offset] & 0x0f) << 2);
							ma.firstRecordTime_s = ((int) ReadMultipleBlockAnswer[4 + ma.offset] & 0x3f);
							// v2-
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
							if (ma.adc > 0)
								ma.formula = 1;
							else
								ma.formula = 0;

							ma.items = (((int) ReadMultipleBlockAnswer[12 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[11 + ma.offset] & 0x00ff));

							ma.up_limit = (((int) ReadMultipleBlockAnswer[14 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[13 + ma.offset] & 0x00ff));
							ma.dn_limit = (((int) ReadMultipleBlockAnswer[16 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[15 + ma.offset] & 0x00ff));

							ma.delay = ((int) ReadMultipleBlockAnswer[17 + ma.offset] & 0x00ff);
							ma.over_t = ((int) ReadMultipleBlockAnswer[18 + ma.offset] & 0x00ff);
							ma.lave = ((int) ReadMultipleBlockAnswer[19 + ma.offset] & 0x00ff);
							ma.cal = ((int) ReadMultipleBlockAnswer[20 + ma.offset] & 0x00ff);

							ma.unit = (((int) ReadMultipleBlockAnswer[22 + ma.offset] & 0x00ff) << 8)
									+ (((int) ReadMultipleBlockAnswer[21 + ma.offset] & 0x00ff));

							ma.is_using = ((int) ReadMultipleBlockAnswer[23 + ma.offset] & 0x00ff);

							ma.version = ((int) ReadMultipleBlockAnswer[24 + ma.offset] & 0x00ff);// v2
							if (ma.version >= 0x20)
								ma.formula = 2;

							ma.up_limit = (int) Math.round(quick.Search(
									ma.up_limit, ma.formula));
							ma.dn_limit = (int) Math.round(quick.Search(
									ma.dn_limit, ma.formula));
							// 2+
							ma.name = "";
							for (int j = 1; j < 21; j++) {
								if (ReadMultipleBlockAnswer[j] > 0) {
									ma.name += (char) ReadMultipleBlockAnswer[j];
								}
							}
							// 2-
							start = false;
							new StartReadTask2().execute();
						} else {
							if (dialog.isShowing())
								dialog.dismiss();
						}
					}
				}
			}
		}
	}

	private class StartReadTask2 extends AsyncTask<Void, Void, Void> {
		private int index = 0;

		protected void onPreExecute() {
			GetSystemInfoAnswer = NFCCommand.SendGetSystemInfoCommandCustom(
					ma.getCurrentTag(), ma);
			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				if (start)
					startAddressString = addr;
				else
					startAddressString = addr2;
				startAddressString = Helper.castHexKeyboard(startAddressString);
				startAddressString = Helper.FormatStringAddressStart(
						startAddressString, ma);
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
						startAddressString, ma);
				numberOfBlockToRead = Helper
						.ConvertIntTo2bytesHexaFormat(Integer
								.parseInt(sNbOfBlock));
				pbBar.setMax(ma.items);
				index = 0;
				pbBar.setProgress(index);
				pbBar.setVisibility(View.VISIBLE);

				final Handler myHandle = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						index += 70;
						pbBar.setProgress(index);
					}
				};
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (index < ma.items) {
							try {
								myHandle.sendMessage(myHandle.obtainMessage());
								Thread.sleep(100);
							} catch (Throwable t) {
							}
						}
					}
				}).start();
			} else {
			}
		}

		// automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params) {
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
										ma.getCurrentTag(), addressStart,
										numberOfBlockToRead, ma);
						cpt++;
					}
					cpt = 0;
				} else if (Helper
						.Convert2bytesHexaFormatToInt(numberOfBlockToRead) < 32) {
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.SendReadMultipleBlockCommandCustom(
										ma.getCurrentTag(), addressStart,
										numberOfBlockToRead[1], ma);
						cpt++;
					}
					cpt = 0;
				} else {
					ReadMultipleBlockAnswer = null;
					while ((ReadMultipleBlockAnswer == null || ReadMultipleBlockAnswer[0] == 1)
							&& cpt <= 10) {
						ReadMultipleBlockAnswer = NFCCommand
								.SendReadMultipleBlockCommandCustom2(
										ma.getCurrentTag(), addressStart,
										numberOfBlockToRead, ma);
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
			pbBar.setVisibility(View.GONE);
			if (dialog.isShowing())
				dialog.dismiss();
			if (DecodeGetSystemInfoResponse(GetSystemInfoAnswer)) {
				nbblocks = Integer.parseInt(sNbOfBlock);

				if (ReadMultipleBlockAnswer != null
						&& ReadMultipleBlockAnswer.length - 1 > 0) {
					if (ReadMultipleBlockAnswer[0] == 0x00) {
						if (start) {
						} else {
							tag ntag = new tag();

							ntag.uid = ma.uid.replaceAll(" ", "");
							ntag.name = ma.name;// 2
							ntag.year = ma.year;
							ntag.month = ma.month;
							ntag.day = ma.day;
							ntag.hour = ma.hour;
							ntag.min = ma.min;
							ntag.sec = ma.sec;
							// v2+
							ntag.btnFlag = ma.btnFlag;
							ntag.version = ma.version;
							ntag.firstAssignTime_y = ma.firstAssignTime_y;
							ntag.firstAssignTime_m = ma.firstAssignTime_m;
							ntag.firstAssignTime_d = ma.firstAssignTime_d;
							ntag.firstAssignTime_h = ma.firstAssignTime_h;
							ntag.firstAssignTime_i = ma.firstAssignTime_i;
							ntag.firstAssignTime_s = ma.firstAssignTime_s;
							ntag.firstRecordTime_y = ma.firstRecordTime_y;
							ntag.firstRecordTime_m = ma.firstRecordTime_m;
							ntag.firstRecordTime_d = ma.firstRecordTime_d;
							ntag.firstRecordTime_h = ma.firstRecordTime_h;
							ntag.firstRecordTime_i = ma.firstRecordTime_i;
							ntag.firstRecordTime_s = ma.firstRecordTime_s;
							// v2-
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
								double b = quick.Search(a, ma.formula);
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
							if (ntag.name.equals("")) {// 2+
								ntag.SERIAL = ntag.uid + "_"
										+ String.valueOf(ntag.year + 2000)
										+ "_" + String.valueOf(ntag.month + 1)
										+ "_" + String.valueOf(ntag.day) + "_"
										+ String.valueOf(ntag.hour) + "_"
										+ String.valueOf(ntag.min);
							} else {
								ntag.SERIAL = ntag.name;
							}// 2-
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
								SharedPreference.save(c);
								ma.curTag = ma.tags.get(0);
							} else {
								ma.tags.set(i, ntag);
								ma.curTag = ntag;// ma.tags.get(i);
							}
							listener.onTaskCompleted("1");
						}
					}
				}
			}
		}
	}

	public boolean DecodeGetSystemInfoResponse(byte[] GetSystemInfoResponse) {
		int special = 0;
		byte[] uid = new byte[8];
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
		} else
			return false;
	}
}
