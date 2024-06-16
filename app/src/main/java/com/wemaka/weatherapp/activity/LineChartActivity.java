package com.wemaka.weatherapp.activity;

import static com.wemaka.weatherapp.activity.MainActivity.TAG;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.ChartHighlighter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.wemaka.weatherapp.R;

import java.util.ArrayList;
import java.util.List;

public class LineChartActivity implements OnChartGestureListener, OnChartValueSelectedListener {
	private LineChart chart;
	private List<Entry> coordinatesXY = new ArrayList<>();
	private LineDataSet dataSet;
	private LineData lineData;

	public LineChartActivity(LineChart chart) {
		this.chart = chart;
		this.onCreate();
	}

	public void onCreate() {
		coordinatesXY.add(new Entry(0.5f, -5f));

		coordinatesXY.add(new Entry(0.75f, -4f));
		coordinatesXY.add(new Entry(1, -2.5f)); // mon
		coordinatesXY.add(new Entry(1.25f, -2f));

		coordinatesXY.add(new Entry(1.5f, -1f));

		coordinatesXY.add(new Entry(1.75f, -2f));
		coordinatesXY.add(new Entry(2, -2.25f)); // tue
		coordinatesXY.add(new Entry(2.25f, -1f));

		coordinatesXY.add(new Entry(2.5f, -1.25f));

		coordinatesXY.add(new Entry(2.75f, -2.4f));
		coordinatesXY.add(new Entry(3, -2.5f)); // wen
		coordinatesXY.add(new Entry(3.25f, -1.5f));

		coordinatesXY.add(new Entry(3.5f, -1.2f));

		coordinatesXY.add(new Entry(3.75f, 0f));
		coordinatesXY.add(new Entry(4, 2.3f)); // thu
		coordinatesXY.add(new Entry(4.25f, 2.5f));

		coordinatesXY.add(new Entry(4.5f, 2.7f));

		coordinatesXY.add(new Entry(4.75f, 2.5f));
		coordinatesXY.add(new Entry(5, 1.75f)); // fri
		coordinatesXY.add(new Entry(5.25f, 0f));

		coordinatesXY.add(new Entry(5.5f, -1f));

		coordinatesXY.add(new Entry(5.75f, -1.75f));
		coordinatesXY.add(new Entry(6, -2f)); // sat
		coordinatesXY.add(new Entry(6.25f, -2.5f));

		coordinatesXY.add(new Entry(6.5f, -4f));

		coordinatesXY.add(new Entry(6.75f, -3.5f));
		coordinatesXY.add(new Entry(7, -2.5f)); // sun
		coordinatesXY.add(new Entry(7.25f, -1.5f));

		coordinatesXY.add(new Entry(7.5f, -1f));

		this.changeAxisY(new String[]{"", "Mon", "Tue", "Wen", "Thu", "Fri", "Sat", "Sun"});

		dataSet = new LineDataSet(coordinatesXY, "");
		lineData = new LineData(dataSet);
		chart.setData(lineData);
	}

	public LineChart getChart() {
		return chart;
	}

	public LineDataSet getDataSet() {
		return dataSet;
	}

	public LineData getLineData() {
		return lineData;
	}

	public void changeAxisY(String[] arr) {
		chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(arr));
	}

	public void setPosition(XAxis.XAxisPosition pos) {
		chart.getXAxis().setPosition(pos);
	}

	public void setAxisYMax(int max) {
		chart.getAxisLeft().setAxisMaximum(max);
	}

	public void setAxisYMin(int min) {
		chart.getAxisLeft().setAxisMinimum(min);
	}

	public int getAxisYMax() {
		return (int) Math.ceil(coordinatesXY.stream().max((e1, e2) -> (int) (e1.getY() - e2.getY())).get().getY());
	}

	public int getAxisYMin() {
		return (int) Math.ceil(coordinatesXY.stream().min((e1, e2) -> (int) (e1.getY() - e2.getY())).get().getY());
	}

	public void refresh() {
		chart.invalidate();
	}

	@Override
	public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
	}

	@Override
	public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {

	}

	@Override
	public void onValueSelected(Entry e, Highlight h) {
		Log.i(TAG, "onValueSelected: " + e.getX() + ":" + e.getY() + " highLight: " + h);
//		lineData.getDataSetByIndex(h.getDataSetIndex());
//		lineData.setDrawValues(true);
	}

	@Override
	public void onNothingSelected() {
		Log.i(TAG, "onNothingSelected");
//		lineData.setDrawValues(false);
	}


	public static class CustomMarkerView extends MarkerView {
		private TextView tvContent;
		private MPPointF mOffset;

		/**
		 * Constructor. Sets up the MarkerView with a custom layout resource.
		 *
		 * @param context
		 * @param layoutResource the layout resource to use for the MarkerView
		 */
		public CustomMarkerView(Context context, int layoutResource) {
			super(context, layoutResource);
			tvContent = (TextView) findViewById(R.id.tvContent);
		}

		@Override
		public MPPointF getOffset() {
			if(mOffset == null) {
				// center the marker horizontally and vertically
				mOffset = new MPPointF(-(getWidth() / 2), -getHeight()-10);
			}
			return mOffset;
		}

		@Override
		public void refreshContent(Entry e, Highlight highlight) {
			Log.i(TAG, "refreshContent: " + e.getY());
			tvContent.setText((int) e.getY() + "°");

			super.refreshContent(e, highlight);
		}
	}
}
