package com.jogtek.alpha.jlog.api;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.nfc.Tag;

public class DataDevice extends Application {
	public static class tag {
		public String SERIAL = "";
		public String TIME = "";
		public List<Double> datas = new ArrayList<Double>();
		public String uid,name;
		public int year = 0, month = 0, day = 0, hour = 0, min = 0, sec = 0;
		public int up_limit = 0, dn_limit = 0;
		public int delay0 = 0;// 1
		public int delay = 0, over_t = 0, lave = 0, cal = 0;
		public int items = 0, is_using = 0, unit = 0;
		public double tmax = -255, tmin = 255;
		public int formula=0;// 0=old,1=new,2=version 2
		public int adc = 0;
		//ver 2+
		public int version=1;
		public int btnFlag=0;
		public int firstAssignTime_y=0;
		public int firstAssignTime_m=0;
		public int firstAssignTime_d=0;
		public int firstAssignTime_h=0;
		public int firstAssignTime_i=0;
		public int firstAssignTime_s=0;
		public int firstRecordTime_y=0;
		public int firstRecordTime_m=0;
		public int firstRecordTime_d=0;
		public int firstRecordTime_h=0;
		public int firstRecordTime_i=0;
		public int firstRecordTime_s=0;
	//ver 2-
	}
	String m_IP="59.124.222.151";
	String m_name="Administrator";
	String m_pw="Jogtek28185540";
	public int formula=0;// 0=old,1=new,2=version 2
	public int offset0=156;// 1 156 byte = 39 block = 0x27(block)
	public int offset=164;// 1 164 byte = 41 block = 0x29(block)
	public boolean isFTP = true;
	public String PW = "";
	public tag curTag = null;
	public ArrayList<tag> tags = new ArrayList<tag>();
	public byte[][] dataFromRead = new byte[7][];
	public double[] data = null;
	public int[][] counters = null;
	public int counter_index1 = 0;
	public int counter_index2 = 0;

	public int year = 0, month = 0, day = 0, hour = 0, min = 0, sec = 0;
	public int up_limit = 0, dn_limit = 0;
	public int delay0 = 0, delay = 0, over_t = 0, lave = 0, cal = 0;// 1
	public int items = 0, is_using = 0, unit = 0;
	public double tmax = -255, tmin = 255;
	public int adc = 0;
	//ver 2+
	public int version=1;
	public int btnFlag=0;
	public int firstAssignTime_y=0;
	public int firstAssignTime_m=0;
	public int firstAssignTime_d=0;
	public int firstAssignTime_h=0;
	public int firstAssignTime_i=0;
	public int firstAssignTime_s=0;
	public int firstRecordTime_y=0;
	public int firstRecordTime_m=0;
	public int firstRecordTime_d=0;
	public int firstRecordTime_h=0;
	public int firstRecordTime_i=0;
	public int firstRecordTime_s=0;
//ver 2-
	private Tag currentTag;
	public String uid,name;
	private String techno;
	private String manufacturer;
	private String productName;
	private String dsfid;
	private String afi;
	private String memorySize;
	private String blockSize;
	private String icReference;
	private boolean basedOnTwoBytesAddress;
	private boolean MultipleReadSupported;
	private boolean MemoryExceed2048bytesSize;

	public void setCurrentTag(Tag currentTag) {
		this.currentTag = currentTag;
	}

	public Tag getCurrentTag() {
		return currentTag;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setTechno(String techno) {
		this.techno = techno;
	}

	public String getTechno() {
		return techno;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public void setDsfid(String dsfid) {
		this.dsfid = dsfid;
	}

	public String getDsfid() {
		return dsfid;
	}

	public void setAfi(String afi) {
		this.afi = afi;
	}

	public String getAfi() {
		return afi;
	}

	public void setMemorySize(String memorySize) {
		this.memorySize = memorySize;
	}

	public String getMemorySize() {
		return memorySize;
	}

	public void setBlockSize(String blockSize) {
		this.blockSize = blockSize;
	}

	public String getBlockSize() {
		return blockSize;
	}

	public void setIcReference(String icReference) {
		this.icReference = icReference;
	}

	public String getIcReference() {
		return icReference;
	}

	public void setBasedOnTwoBytesAddress(boolean basedOnTwoBytesAddress) {
		this.basedOnTwoBytesAddress = basedOnTwoBytesAddress;
	}

	public boolean isBasedOnTwoBytesAddress() {
		return basedOnTwoBytesAddress;
	}

	public void setMultipleReadSupported(boolean MultipleReadSupported) {
		this.MultipleReadSupported = MultipleReadSupported;
	}

	public boolean isMultipleReadSupported() {
		return MultipleReadSupported;
	}

	public void setMemoryExceed2048bytesSize(boolean MemoryExceed2048bytesSize) {
		this.MemoryExceed2048bytesSize = MemoryExceed2048bytesSize;
	}

	public boolean isMemoryExceed2048bytesSize() {
		return MemoryExceed2048bytesSize;
	}
}
