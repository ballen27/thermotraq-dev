package com.jogtek.alpha.jlog.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import com.google.gson.Gson;
import com.jogtek.alpha.jlog.api.DataDevice.tag;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class net {
	DataDevice ma;
	Context c = null;
	FTPClient ftpClient;
	OnTaskCompleted listener;

	public net(Context context) {
		this.c = context;
		ma = (DataDevice) c.getApplicationContext();
	}

	public void uploadFTP(String IP, String name, String pw) {
		ma.m_IP = IP;
		ma.m_name = name;
		ma.m_pw = pw;
		if (ma.isFTP) {
			new asyncUploadFTP().execute();
		} else {
			upload();
		}
	}

	private class asyncUploadFTP extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... arg0) {
			ftpClient = my.ftpConnect(ma.m_IP, ma.m_name, ma.m_pw, 21);
			String ret = "0";
			if (ftpClient != null) {
				my.writeCSVfile(c.getApplicationContext());
				if (my.ftpUpload(ftpClient, ma.curTag.SERIAL)) {
					ret = "1";
				}
			}
			return ret;
		}

		protected void onPostExecute(final String ret) {
			Log.i("ScanRead", "Button Read CLICKED **** On Post Execute ");
			if (ret.equals("1")) {
				SharedPreference.saveNetSetting(c);
				Toast.makeText(c.getApplicationContext(),
						"Upload successfully!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(c.getApplicationContext(), "Upload failed!",
						Toast.LENGTH_SHORT).show();
			}
			my.ftpDisconnect(ftpClient);
		}
	}

	private void upload() {
		try {
			Gson gson = new Gson();
			my.postToServer(c, ma.m_IP, "jogtek/Service1.svc/addJogtek",
					gson.toJson(ma.curTag), 1, new OnTaskCompleted() {
						@Override
						public void onTaskCompleted(String result) {
							try {
								int ret = parser.parserInt(result);
								if (ret == 1) {// success
									Toast.makeText(c, "up load success!",
											Toast.LENGTH_SHORT).show();
								} else {// failed
									Toast.makeText(c, "net wort error!",
											Toast.LENGTH_SHORT).show();
								}
							} catch (Exception e) {
								Toast.makeText(c, "net wort error!",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
		} catch (Exception e) {
			Toast.makeText(c, "net wort error!", Toast.LENGTH_SHORT).show();
		}
	}

	public static class jogtek_get {
		public String SERIAL = "";
		public String TIME = "";
	}

	private void deleteAll(final OnTaskCompleted listener1) {
		try {
			SharedPreference.loadNetSetting(c);
			my.postToServer(c, ma.m_IP, "jogtek/Service1.svc/delAllJogtek", "",
					1, new OnTaskCompleted() {
						@Override
						public void onTaskCompleted(String result) {
							try {
								int ret = parser.parserInt(result);
								if (ret == 1) {// success
									ma.tags.clear();
									SharedPreference.save(c);
									listener1.onTaskCompleted("1");
									// showData();
									Toast.makeText(
											c,
											"All records have been successfully deleted!",
											Toast.LENGTH_SHORT).show();
								} else {// failed
									Toast.makeText(c, "net wort error!",
											Toast.LENGTH_SHORT).show();
								}
							} catch (Exception e) {
								Toast.makeText(c, "net wort error!",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
		} catch (Exception e) {
			Toast.makeText(c, "net wort error!", Toast.LENGTH_SHORT).show();
		}
	}

	private void deleteOne(final OnTaskCompleted listener1) {
		try {
			Gson gson = new Gson();
			SharedPreference.loadNetSetting(c);
			jogtek_get jg = new jogtek_get();
			jg.SERIAL = ma.curTag.SERIAL;
			jg.TIME = ma.curTag.TIME;
			my.postToServer(c, ma.m_IP, "jogtek/Service1.svc/delJogtek",
					gson.toJson(jg), 1, new OnTaskCompleted() {
						@Override
						public void onTaskCompleted(String result) {
							try {
								int ret = parser.parserInt(result);
								if (ret == 1) {// success
									ma.tags.remove(ma.curTag);
									SharedPreference.save(c);
									listener1.onTaskCompleted("1");
									// showData();
									Toast.makeText(
											c,
											"Record has been successfully deleted!",
											Toast.LENGTH_SHORT).show();
								} else {// failed
									Toast.makeText(c, "net wort error!",
											Toast.LENGTH_SHORT).show();
								}
							} catch (Exception e) {
								Toast.makeText(c, "net wort error!",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
		} catch (Exception e) {
			Toast.makeText(c, "net wort error!", Toast.LENGTH_SHORT).show();
		}
	}

	private void downloadFTP(OnTaskCompleted listener1) {
		listener=listener1;
		new asyncDownloadFTP().execute();
	}

	private class asyncDownloadFTP extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			SharedPreference.loadNetSetting(c);
			ftpClient = my.ftpConnect(ma.m_IP, ma.m_name, ma.m_pw, 21);
			if (ftpClient != null) {
				// clearFolder();
				my.ftpDownloadAll(ftpClient);
			}
			return null;
		}

		private void clearFolder() {
			File sdCardRoot = Environment.getExternalStorageDirectory();
			File yourDir = new File(sdCardRoot, "jogtek_dn");
			for (File f : yourDir.listFiles()) {
				if (f.isFile()) {
					String name = f.getName();
					File file = new File(name);
					file.setWritable(true);
					file.delete();
				}
			}
		}

		public String getfilesize(String path, String filename)
				throws IOException {
			String pathString = path + "\\" + filename;
			File f = new File(pathString);
			FileInputStream fis = new FileInputStream(f);
			String time = String.valueOf(((double) fis.available() / 1024));
			fis.close();// 當時這裏沒有關閉
			return time.substring(0, time.indexOf(".") + 2) + "K";
		}

		protected void onPostExecute(final Void unused) {
			Log.i("ScanRead", "Button Read CLICKED **** On Post Execute ");
			my.ftpDisconnect(ftpClient);

			ma.tags.clear();
			File sdCardRoot = Environment.getExternalStorageDirectory();
			File yourDir = new File(sdCardRoot, "jogtek_dn");
			for (File f : yourDir.listFiles()) {
				if (f.isFile()) {
					String name = f.getName();
					File file = new File(
							Environment.getExternalStorageDirectory()
									+ "/jogtek_dn/" + name);
					// StringBuilder text = new StringBuilder();
					try {
						BufferedReader br = new BufferedReader(new FileReader(
								file));
						String line;
						int i = 0;
						tag nt = new tag();
						while ((line = br.readLine()) != null) {
							// text.append(line);
							// text.append('\n');
							String[] field = line.split(",");
							if (i == 0) {
								nt.SERIAL = field[0];
								nt.TIME = field[1];
								nt.uid = field[2];
								nt.year = Integer.valueOf(field[3]);
								nt.month = Integer.valueOf(field[4]);
								nt.day = Integer.valueOf(field[5]);
								nt.hour = Integer.valueOf(field[6]);
								nt.min = Integer.valueOf(field[7]);
								nt.sec = Integer.valueOf(field[8]);
								nt.up_limit = Integer.valueOf(field[9]);
								nt.dn_limit = Integer.valueOf(field[10]);
								nt.delay = Integer.valueOf(field[11]);
								nt.over_t = Integer.valueOf(field[12]);
								nt.lave = Integer.valueOf(field[13]);
								nt.cal = Integer.valueOf(field[14]);
								nt.items = Integer.valueOf(field[15]);
								nt.is_using = Integer.valueOf(field[16]);
								nt.unit = Integer.valueOf(field[17]);
								nt.tmax = Integer.valueOf(field[18]);
								nt.tmin = Integer.valueOf(field[19]);
							} else {
								nt.datas.add(Double.valueOf(field[1]));
							}
							i++;
						}
						br.close();
						ma.tags.add(nt);
					} catch (IOException e) {
						// You'll need to add proper error handling here
						String ss = e.toString();
						ss += " ";
					}
				}
			}
			listener.onTaskCompleted("1");
//			showData();
		}
	}

	private void download(final OnTaskCompleted listener1) {
		try {
			List<jogtek_get> jgs = new ArrayList<jogtek_get>();
			for (tag tg : ma.tags) {
				jogtek_get jg = new jogtek_get();
				jg.SERIAL = tg.SERIAL;
				jg.TIME = tg.TIME;
				jgs.add(jg);
			}
			Gson gson = new Gson();
			SharedPreference.loadNetSetting(c);
			my.postToServer(c, ma.m_IP, "jogtek/Service1.svc/getJogtek",
					gson.toJson(jgs), 1, new OnTaskCompleted() {
						@Override
						public void onTaskCompleted(String result) {
							try {
								int ret = parser.parserMessage(c, result);
								if (ret == 1) {// success
									listener1.onTaskCompleted("1");
									// showData();
									Toast.makeText(c, "Down load success!",
											Toast.LENGTH_SHORT).show();
								} else {// failed
									Toast.makeText(c, "net wort error!",
											Toast.LENGTH_SHORT).show();
								}
							} catch (Exception e) {
								Toast.makeText(c, "net wort error!",
										Toast.LENGTH_SHORT).show();
							}
						}
					});
		} catch (Exception e) {
			Toast.makeText(c, "net wort error!", Toast.LENGTH_SHORT).show();
		}
	}
}
