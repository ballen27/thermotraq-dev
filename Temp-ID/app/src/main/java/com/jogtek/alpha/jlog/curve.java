package com.jogtek.alpha.jlog;

import java.text.NumberFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

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
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class curve extends Activity {
	DataDevice ma;
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;

	Button btnCurrent;
	Button btnList;
	Button btnCommand;
	Button btnLine;
	Button btnData;

	TextView tvMax;
	TextView tvMin;

	GraphicalView chart;
	LinearLayout ll1;

	// line
	String[] titles = null;
	List<double[]> x = new ArrayList<double[]>();
	List<Date[]> t = new ArrayList<Date[]>();
	List<double[]> y = new ArrayList<double[]>();

	// pie
	float[] Percent = new float[5];
	String[] CATEGORY = new String[5];
	int[] cc = new int[5];

	ProgressBar pbBar;
	ProgressDialog dialog;

	ReadWrite rw = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.curve);
		titles = new String[] {
				getResources().getString(R.string.t_up_limit).toString(),
				getResources().getString(R.string.t_dn_limit).toString(),
				getResources().getString(R.string.t_record).toString() };
		initListener();
		rw = new ReadWrite(curve.this,pbBar,dialog);
		showData();

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

	private void initListener() {
		btnCurrent = (Button) findViewById(R.id.btnCurrent);
		btnList = (Button) findViewById(R.id.btnList);
		btnCommand = (Button) findViewById(R.id.btnCommand);
		btnLine = (Button) findViewById(R.id.btnLine);
		btnData = (Button) findViewById(R.id.btnData);
		pbBar = (ProgressBar) findViewById(R.id.pbBar);

		tvMax = (TextView) findViewById(R.id.tvMax);
		tvMin = (TextView) findViewById(R.id.tvMin);

		dialog = new ProgressDialog(curve.this);
		btnCurrent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(curve.this, result.class);
				startActivity(intent);
				finish();
			}
		});
		btnList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(curve.this, tag_list.class);
				startActivity(intent);
				finish();
			}
		});
		btnCommand.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(curve.this, command.class);
				startActivity(intent);
				finish();
			}
		});
		btnLine.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			}
		});
		btnData.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(curve.this, data.class);
				startActivity(intent);
				finish();
			}
		});
	}

	// Time+
	private void initLineTimeData() {
		ma = (DataDevice) getApplication();
		Calendar c = Calendar.getInstance();
		c.set(ma.curTag.year + 2000, ma.curTag.month, ma.curTag.day,
				ma.curTag.hour, ma.curTag.min + ma.curTag.delay
						+ ma.curTag.delay0, ma.curTag.sec);
		Date[] d = new Date[ma.curTag.items];
		double[] up = new double[ma.curTag.items];
		double[] dn = new double[ma.curTag.items];

		// double[] line = new double[ma.curTag.items];
		for (int i = 0; i < ma.curTag.items; i++) {
			if (ma.curTag.unit == 0)
				c.add(Calendar.SECOND, 2);
			else
				c.add(Calendar.SECOND, 15 * ma.curTag.unit);
			d[i] = (Date) c.getTime();
			up[i] = ma.curTag.up_limit;
			dn[i] = ma.curTag.dn_limit;
		}
		ma.data = new double[ma.curTag.datas.size()];
		for (int i = 0; i < ma.data.length; i++) {
			ma.data[i] = ma.curTag.datas.get(i);
		}
		t.clear();
		y.clear();
		t.add(d);
		t.add(d);
		t.add(d);
		y.add(up);
		y.add(dn);
		y.add(ma.data);
	}

	protected XYMultipleSeriesDataset buildDateDataset(String[] titles,
			List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		return dataset;
	}

	private GraphicalView initialLineTimeChartBuilder() {
		initLineTimeData();
		XYMultipleSeriesDataset dataset = buildDateDataset(titles, t, y);
		int[] colors = new int[] { Color.RED, Color.BLUE, Color.GREEN };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT,
				PointStyle.POINT, PointStyle.CIRCLE };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles, true);
		setTimeChartSettings(renderer, "", "", "", -40, 40, Color.RED);
		GraphicalView chart = ChartFactory.getTimeChartView(this, dataset,
				renderer, "M/d\ny\nHH:mm:ss");
		return chart;
	}

	protected void setTimeChartSettings(XYMultipleSeriesRenderer renderer,
			String title, String xTitle, String yTitle, double yMin,
			double yMax, int axesColor) {
		int[] margins = new int[4];
		margins[0] = 25;
		margins[1] = 25;
		margins[2] = -60;
		margins[3] = 10;
		renderer.setChartTitle(title);
		renderer.setChartTitleTextSize(60);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setAxisTitleTextSize(60);
		renderer.setLabelsColor(Color.rgb(0, 0, 255));
		renderer.setLegendTextSize(36);
		// renderer.setXAxisMin(xMin);
		// renderer.setXAxisMax(xMax);
		// 1 renderer.setYLabelsColor(0, Color.WHITE);
		renderer.setYLabelsColor(0, Color.RED);
		renderer.setXLabelsColor(Color.YELLOW);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		// 1 renderer.setGridColor(Color.WHITE);
		renderer.setGridColor(Color.BLACK);
		renderer.setMargins(margins);
		renderer.setLabelsTextSize(36);//
		// renderer.setMarginsColor(Color.WHITE);
		renderer.setMarginsColor(Color.TRANSPARENT);
		renderer.setPointSize(3);

		renderer.setBackgroundColor(Color.WHITE);
		renderer.setApplyBackgroundColor(true);
		// renderer.setInitialRange(range);
		// renderer.setRange(range);
		renderer.setShowGrid(true);
	}

	// Time-
	// Line+Time
	private XYMultipleSeriesRenderer buildRenderer(int[] colors,
			PointStyle[] styles, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			XYSeriesRenderer r = new XYSeriesRenderer();
			r.setColor(colors[i]);
			r.setPointStyle(styles[i]);
			r.setFillPoints(fill);
			r.setLineWidth(5);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	// NFC
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

	private void showData() {
		// GraphicalView chart=initialLineChartBuilder();
		chart = initialLineTimeChartBuilder();
		// GraphicalView chart=initialPieChartBuilder();
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll1.removeAllViews();
		ll1.addView(chart);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2); // 小數後兩位
		tvMax.setText("Max:" + nf.format(ma.tmax));
		tvMin.setText("Min:" + nf.format(ma.tmin));
	}
}
