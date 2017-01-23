package com.jogtek.alpha.jlog.api;

import android.os.AsyncTask;

public class asyncPostTask extends AsyncTask<String, Void, String> {
	private OnTaskCompleted listener;
	
	public asyncPostTask(OnTaskCompleted listener){
        this.listener=listener;
    }
	@Override
	protected String doInBackground(String... param) {
		return myJsonPost.send(param[0], param[1]);
	}
	@Override
	protected void onPostExecute(String result) {
		listener.onTaskCompleted(result);
	}
}
