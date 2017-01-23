package com.jogtek.alpha.jlog.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.google.gson.Gson;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

public class my {
	public static int selectedItem = -1;
	public static boolean[] isChecked = null;
	static Gson gson = new Gson();

	public static void postToServer(Context c, String ip, String com,
			String json, int alert, final OnTaskCompleted listener2) {

		final ProgressDialog dlg = new ProgressDialog(c);
		dlg.setMessage("Loading...");
		dlg.show();
		String[] params = new String[2];
		params[0] = "http://" + ip + "/" + com;
		params[1] = json;
		new asyncPostTask(new OnTaskCompleted() {
			@Override
			public void onTaskCompleted(String result) {
				dlg.dismiss();
				listener2.onTaskCompleted(result);
			}
		}).execute(params);
	}

	public static void comfirmbox(Context c, String title, String msg,
			String yes, String no, final OnTaskCompleted listener1) {
		new AlertDialog.Builder(c)
				.setPositiveButton(yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener1.onTaskCompleted("1");
						dialog.dismiss();
					}
				}).setNegativeButton(no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener1.onTaskCompleted("0");
						dialog.dismiss();
					}
				}).show();
	}

	public static void writeCSVfile(final Context c) {
		DataDevice ma;
		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/jogtek");
		boolean var = false;

		ma = (DataDevice) c.getApplicationContext();
		if (!folder.exists())
			var = folder.mkdir();
		// System.out.println("" + var);
		final String filename = folder.toString() + "/" + ma.curTag.SERIAL
				+ ".csv";
		try {
			FileWriter fw = new FileWriter(filename);
			/*
			fw.append(ma.curTag.SERIAL);
			fw.append(',');
			fw.append(ma.curTag.TIME);
			fw.append(',');
			fw.append(ma.curTag.uid);
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.year));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.month));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.day));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.hour));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.min));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.sec));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.up_limit));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.dn_limit));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.delay));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.over_t));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.lave));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.cal));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.items));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.is_using));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.unit));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.tmax));
			fw.append(',');
			fw.append(String.valueOf(ma.curTag.tmin));
			fw.append('\n');
			*/
			Calendar ca = Calendar.getInstance();
			ca.set(ma.curTag.year + 2000, ma.curTag.month, ma.curTag.day,
					ma.curTag.hour, ma.curTag.min + ma.curTag.delay,
					ma.curTag.sec);
			for (int i = 0; i < ma.curTag.datas.size(); i++) {
				if (ma.curTag.unit == 0)
					ca.add(Calendar.SECOND, 2);
				else
					ca.add(Calendar.SECOND, 15 * ma.curTag.unit);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy/MM/dd HH:mm:ss");
				String datetime = sdf.format(ca.getTime());
				fw.append(datetime);
				fw.append(',');
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(2); // 小數後兩位
				fw.append(nf.format(ma.curTag.datas.get(i)));
				fw.append('\n');
			}
			fw.flush();
			fw.close();
		} catch (Exception e) {
		}
	}

	public static FTPClient ftpConnect(String host, String username,
			String password, int port) {
		FTPClient ftpClient = new FTPClient();
		try {
			// connecting to the host
			ftpClient.connect(host, port);
			// now check the reply code, if positive mean connection success
			if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
				// login using username & password
				boolean status = ftpClient.login(username, password);
				if (ftpChangeDirectory(ftpClient, "/AnyOne/")) {
					/*
					 * Set File Transfer Mode To avoid corruption issue you must
					 * specified a correct transfer mode, such as
					 * ASCII_FILE_TYPE, BINARY_FILE_TYPE, EBCDIC_FILE_TYPE .etc.
					 * Here, I use BINARY_FILE_TYPE for transferring text,
					 * image, and compressed files.
					 */
					ftpClient
							.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
					ftpClient.enterLocalPassiveMode();
					ftpClient.setAutodetectUTF8(true);
					return ftpClient;
				}
			}
		} catch (Exception e) {
			// Log.d(TAG, "Error: could not connect to host " + host );
		}
		return null;
	}

	public static boolean ftpDisconnect(FTPClient ftpClient) {
		try {
			ftpClient.logout();
			ftpClient.disconnect();
			return true;
		} catch (Exception e) {
			// Log.d(TAG,
			// "Error occurred while disconnecting from ftp server.");
		}
		return false;
	}

	public static String ftpGetCurrentWorkingDirectory(FTPClient ftpClient) {
		try {
			String workingDir = ftpClient.printWorkingDirectory();
			return workingDir;
		} catch (Exception e) {
			// Log.d(TAG, "Error: could not get current working directory.");
		}
		return null;
	}

	public static boolean ftpChangeDirectory(FTPClient ftpClient,
			String directory_path) {
		try {
			if (!ftpClient.changeWorkingDirectory(directory_path)) {
				if (ftpClient.makeDirectory(directory_path)) {
					return true;
				}
			} else {
				return true;
			}
		} catch (Exception e) {
			// Log.d(TAG, "Error: could not change directory to " +
			// directory_path);
		}
		return false;
	}

	public void ftpPrintFilesList(FTPClient ftpClient, String dir_path) {
		try {
			FTPFile[] ftpFiles = ftpClient.listFiles(dir_path);
			int length = ftpFiles.length;

			for (int i = 0; i < length; i++) {
				String name = ftpFiles[i].getName();
				boolean isFile = ftpFiles[i].isFile();
				if (isFile) {
					// Log.i(TAG, "File : " + name);
				} else {
					// Log.i(TAG, "Directory : " + name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void ftpDownloadAll(FTPClient ftpClient) {
		try {
			boolean success = false;
			String path = ftpGetCurrentWorkingDirectory(ftpClient);
			FTPFile[] ftpFiles = ftpClient.listFiles(path);
			int length = ftpFiles.length;
			for (int i = 0; i < length; i++) {
				String name = ftpFiles[i].getName();
				// FileOutputStream desFileStream = new FileOutputStream(path
				// + "/" + name + ".csv");
				// File desFile = new File(
				// Environment.getExternalStorageDirectory() + "/jogtek/"
				// + name + ".csv");

				boolean isFile = ftpFiles[i].isFile();
				if (isFile) {

					String remoteFile = path + "/" + name + ".csv";
					File downloadFile = new File(
							Environment.getExternalStorageDirectory()
									+ "/jogtek/" + name + ".csv");
					// Create an InputStream to the File Data and use
					// FileOutputStream to write it
					// InputStream inputStream =
					// ftpClient.retrieveFileStream(path
					// + "/" + name + ".csv");
					// FileOutputStream fileOutputStream = new
					// FileOutputStream(Environment.getExternalStorageDirectory()
					// + "/jogtek/"
					// + name + ".csv");
					if (false) {
						// APPROACH #1: using retrieveFile(String, OutputStream)
						OutputStream outputStream1 = new BufferedOutputStream(
								new FileOutputStream(downloadFile));
						success = ftpClient.retrieveFile(remoteFile,
								outputStream1);
						outputStream1.close();

						if (success) {
							System.out
									.println("File #1 has been downloaded successfully.");
						} else {
							System.out
									.println("File #1 has been downloaded successfully.");
						}
					} else {
						// APPROACH #2: using InputStream
						// retrieveFileStream(String)
						OutputStream outputStream2 = new BufferedOutputStream(
								new FileOutputStream(downloadFile));
						InputStream inputStream = ftpClient
								.retrieveFileStream(remoteFile);
						// int response = ftpClient.getReply();
						// if (response != FTPReply.CLOSING_DATA_CONNECTION){
						// TODO
						// response-=1;
						// }
						inputStream.close();

						if (!ftpClient.completePendingCommand()) {
							ftpClient.logout();
							ftpClient.disconnect();
						}
					}
				} else {
					Log.i("", "Directory : " + name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean ftpMakeDirectory(FTPClient ftpClient, String new_dir_path) {
		try {
			boolean status = ftpClient.makeDirectory(new_dir_path);
			return status;
		} catch (Exception e) {
			// Log.d(TAG, "Error: could not create new directory named " +
			// new_dir_path);
		}
		return false;
	}

	public boolean ftpRemoveDirectory(FTPClient ftpClient, String dir_path) {
		try {
			boolean status = ftpClient.removeDirectory(dir_path);
			return status;
		} catch (Exception e) {
			// Log.d(TAG, "Error: could not remove directory named " +
			// dir_path);
		}
		return false;
	}

	public boolean ftpRemoveFile(FTPClient ftpClient, String filePath) {
		try {
			boolean status = ftpClient.deleteFile(filePath);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean ftpRenameFile(FTPClient ftpClient, String from, String to) {
		try {
			boolean status = ftpClient.rename(from, to);
			return status;
		} catch (Exception e) {
			// Log.d(TAG, "Could not rename file: " + from + " to: " + to);
		}

		return false;
	}

	/**
	 * ftpClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: path to the source file in FTP server desFilePath: path to
	 * the destination file to be saved in sdcard
	 */
	public boolean ftpDownload(FTPClient ftpClient, String srcFilePath,
			String desFilePath) {
		boolean status = false;
		try {
			FileOutputStream desFileStream = new FileOutputStream(desFilePath);

			status = ftpClient.retrieveFile(srcFilePath, desFileStream);
			desFileStream.close();

			return status;
		} catch (Exception e) {
			// Log.d(TAG, "download failed");
		}

		return status;
	}

	/**
	 * ftpClient: FTP client connection object (see FTP connection example)
	 * srcFilePath: source file path in sdcard desFileName: file name to be
	 * stored in FTP server desDirectory: directory path where the file should
	 * be upload to
	 */
	public boolean ftpUpload(FTPClient ftpClient, String srcFilePath,
			String desFileName, String desDirectory) {
		boolean status = false;
		try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);
			// change working directory to the destination directory
			ftpChangeDirectory(ftpClient, desDirectory);
			status = ftpClient.storeFile(desFileName, srcFileStream);

			srcFileStream.close();
			return status;
		} catch (Exception e) {
			// Log.d(TAG, "upload failed");
		}
		return status;
	}

	public boolean ftpUpload(FTPClient ftpClient, String srcFilePath,
			String desFileName) {
		boolean status = false;
		try {
			FileInputStream srcFileStream = new FileInputStream(srcFilePath);
			status = ftpClient.storeFile(desFileName, srcFileStream);
			srcFileStream.close();
			return status;
		} catch (Exception e) {
			// Log.d(TAG, "upload failed");
		}
		return status;
	}

	public static boolean ftpUpload(FTPClient ftpClient, String desFileName) {
		boolean status = false;
		long start = 0;
		long end = 0;
		try {
			String tmpFileName = Environment.getExternalStorageDirectory()
					.getPath() + "/jogtek/" + desFileName + ".csv";
			// File tmpFile = new File(Environment.getExternalStorageDirectory()
			// .getAbsolutePath() + "/jogtek/" + desFileName);
			FileInputStream srcFileStream = new FileInputStream(tmpFileName);
			File tmpFile = new File(tmpFileName);
			start = System.currentTimeMillis();
			status = ftpClient.storeFile(desFileName + ".csv", srcFileStream);
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				// System.out.println("upload failed!");
			}
			end = System.currentTimeMillis();
			srcFileStream.close();
			return status;
		} catch (Exception e) {
			Log.d("", "upload failed:" + e.toString());
		}
		return status;
	}
}
