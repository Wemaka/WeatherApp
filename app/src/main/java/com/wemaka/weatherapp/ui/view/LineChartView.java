package com.wemaka.weatherapp.ui.view;

import static com.wemaka.weatherapp.ui.MainActivity.TAG;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.wemaka.weatherapp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LineChartView implements OnChartGestureListener, OnChartValueSelectedListener {
	private LineChart chart;
	private List<Entry> points = new ArrayList<>();
	private LineDataSet dataSet;

	public LineChartView(LineChart chart) {
		this.chart = chart;
		this.onCreate();
	}

	public void onCreate() {
		setPosition(XAxis.XAxisPosition.BOTTOM);

		chart.setDragEnabled(false);
		chart.setScaleEnabled(false);
		chart.setDrawBorders(false);
		chart.setOnChartValueSelectedListener(this);
		chart.getLegend().setEnabled(false);
		chart.setDrawGridBackground(false);
		chart.setMaxHighlightDistance(500);
		chart.getDescription().setEnabled(false);
		chart.getAxisRight().setEnabled(false);
		chart.animateY(1000, Easing.EaseInOutQuad);
	}

	public LineChart getChart() {
		return chart;
	}

	public LineDataSet getDataSet() {
		return dataSet;
	}

	public void setData(LineDataSet dataSet) {
		this.dataSet = dataSet;

		dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
		dataSet.setColor(Color.BLACK);
		dataSet.setLineWidth(2f);
		dataSet.setDrawCircles(false);
		dataSet.setDrawValues(false);
		dataSet.setDrawFilled(true);

		dataSet.setDrawHorizontalHighlightIndicator(false);
		dataSet.setHighlightLineWidth(2f);

		dataSet.setFormLineDashEffect(new DashPathEffect(new float[]{2f, 2f}, 10f));

		chart.setData(new LineData(this.dataSet));
	}

	public void changeAxisY(String[] arr) {
		chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(arr));
	}

	public void changeAxisY(Collection<String> lst) {
		chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(lst));
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
		return (int) Math.ceil(dataSet.getValues().stream().max((e1, e2) -> (int) (e1.getY() - e2.getY())).get().getY());
	}

	public int getAxisYMin() {
		return (int) Math.ceil(dataSet.getValues().stream().min((e1, e2) -> (int) (e1.getY() - e2.getY())).get().getY());
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
//		Log.i(TAG, "onValueSelected: " + e.getX() + ":" + e.getY() + " highLight: " + h);
//		lineData.getDataSetByIndex(h.getDataSetIndex());
//		lineData.setDrawValues(true);
	}

	@Override
	public void onNothingSelected() {
//		Log.i(TAG, "onNothingSelected");
//		lineData.setDrawValues(false);
	}


	public static class CustomMarkerView extends MarkerView {
		private TextView tvContent;
		private MPPointF mOffset;

		public CustomMarkerView(Context context, int layoutResource) {
			super(context, layoutResource);
			tvContent = (TextView) findViewById(R.id.tvCurrDegree);
		}

		@Override
		public MPPointF getOffset() {
			if (mOffset == null) {
				// center the marker horizontally and vertically
				mOffset = new MPPointF(-(getWidth() / 2), -getHeight() - 10);
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
