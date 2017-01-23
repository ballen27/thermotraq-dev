package com.jogtek.alpha.jlog.api;

import java.util.List;
import java.util.ArrayList;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

/**
 * A utility class for creating TextRecords for a give NdefMessage
 * 
 * @author openmobster@gmail.com
 */
public class NdefMessageParser 
{
	private NdefMessageParser()
	{
		
	}
	
	public static List<TextRecord> parse(NdefMessage message)
	{
		List<TextRecord> textRecords = new ArrayList<TextRecord>();
		
		//Get the Records inside the message
		NdefRecord[] records = message.getRecords();
		
		//Iterate through and generate a list of text records
		if(records != null && records.length>0)
		{
			for(NdefRecord local: records)
			{
				TextRecord textRecord = TextRecord.parse(local);
				textRecords.add(textRecord);
			}
		}
		
		return textRecords;
	}
}
