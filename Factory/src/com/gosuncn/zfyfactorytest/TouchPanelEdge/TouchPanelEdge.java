/*
 * Copyright (c) 2011-2014, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.gosuncn.zfyfactorytest.TouchPanelEdge;

import java.util.ArrayList;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.gosuncn.zfyfactorytest.Utils;
import com.gosuncn.zfyfw.service.GSFWManager;

public class TouchPanelEdge extends Activity {

	String TAG = "TouchPanelEdge";
	ArrayList<EdgePoint> mArrayList;
	String resultString = "Failed";
	int mHightPix = 0, mWidthPix = 0, mRadius = 20, mStep = 0;
	float w = 0, h = 0;
	Context mContext;
	// If points is too more, it will be hard to touch edge points.
	private final int X_MAX_POINTS = 16;

	public class EdgePoint {

		int x;
		int y;
		boolean isChecked = false;

		public EdgePoint(int x, int y, boolean isCheck) {

			this.x = x;
			this.y = y;
			this.isChecked = isCheck;
		}

	}

	int getStep(int hightPix, int widthPix) {

		int MIN_STEP = widthPix / X_MAX_POINTS;
		int step = MIN_STEP;
		for (int i = MIN_STEP; i < widthPix / 5; i++) {
			if (hightPix % i == 0 && widthPix % i == 0)
				return i;
		}

		return step;
	}

	public ArrayList<EdgePoint> getTestPoint() {

		ArrayList<EdgePoint> list = new ArrayList<EdgePoint>();

		//Log.d(TAG, "getTestPoint mRadius:"+mRadius);
		//Log.d(TAG, "getTestPoint mWidthPix:"+mWidthPix);
		//Log.d(TAG, "getTestPoint mStep:"+mStep);

		for (int x = mRadius; x < mWidthPix + mRadius; x += mStep) {
			for (int y = mRadius; y < mHightPix + mRadius; y += mStep) {
				if (x > mRadius && x < mWidthPix - mRadius && y > mRadius
						&& y < mHightPix - mRadius)
					continue;
				list.add(new EdgePoint(x, y, false));
			}
		}

		Point leftTop = new Point();
		Point rightBottom = new Point();
		leftTop.x = mRadius * 3;
		leftTop.y = leftTop.x;
		rightBottom.x = mWidthPix - leftTop.x;
		rightBottom.y = mHightPix - leftTop.x;
		int count = (mHightPix / mStep) - 2;
		int xStep = (int)((mWidthPix - 2.5f * mStep) / count);
		for(int j = 0; j < count; j++){
			list.add(new EdgePoint(leftTop.x + j * xStep, leftTop.y + j * mStep, false));
			list.add(new EdgePoint(mWidthPix - (leftTop.x + j * xStep), leftTop.y + j * mStep, false));
		}

		return list;

	}

	@Override
	public void finish() {

		Utils.writeCurMessage(this, TAG, resultString);
		super.finish();
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		init(this);

		// full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// get panel size
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		mHightPix = mDisplayMetrics.heightPixels;
		mWidthPix = mDisplayMetrics.widthPixels;
		// It must be common divisor of width and hight
		mStep = getStep(mWidthPix, mHightPix);
		mRadius = mStep / 2;
		logd(mHightPix + "x" + mWidthPix + " Step:" + mStep);
		setContentView(new Panel(this));
	}

	private void init(Context context) {

		mContext = context;
		resultString = "Failed";
	}

	class Panel extends View {

		public static final int TOUCH_TRACE_NUM = 30;
		public static final int PRESSURE = 500;
		private TouchData[] mTouchData = new TouchData[TOUCH_TRACE_NUM];
		private int traceCounter = 0;
		private Paint mPaint = new Paint();
		private int mTextSize = 0;

		public class TouchData {

			public float x;
			public float y;
			public float r;
		}

		public Panel(Context context) {

			super(context);
			mTextSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30.0f, getResources().getDisplayMetrics());
			mArrayList = getTestPoint();
			mPaint.setARGB(100, 100, 100, 100);
			for (int i = 0; i < TOUCH_TRACE_NUM; i++) {
				mTouchData[i] = new TouchData();
			}

		}

		private int getNext(int c) {

			int temp = c + 1;
			return temp < TOUCH_TRACE_NUM ? temp : 0;
		}

		public void onDraw(Canvas canvas) {

			super.onDraw(canvas);
			mPaint.setColor(Color.LTGRAY);
			mPaint.setTextSize(mTextSize);
			canvas.drawText("W: " + w, mRadius * 5, mStep * 2,
					mPaint);
			canvas.drawText("H: " + h, mRadius * 5, mStep * 2 + mTextSize,
					mPaint);

			mPaint.setColor(Color.RED);
			mPaint.setStrokeWidth(mRadius);
			for (int i = 0; i < mArrayList.size(); i++) {
				EdgePoint point = mArrayList.get(i);
				mPaint.setColor(Color.RED);
				canvas.drawCircle(point.x, point.y, mPaint.getStrokeWidth(),
						mPaint);

			}

			for (int i = 0; i < TOUCH_TRACE_NUM; i++) {
				TouchData td = mTouchData[i];
				mPaint.setColor(Color.BLUE);
				if (td.r > 0) {
					canvas.drawCircle(td.x, td.y, 2, mPaint);
				}

			}
			invalidate();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			final int eventAction = event.getAction();

			w = event.getRawX();
			h = event.getRawY();
			if ((eventAction == MotionEvent.ACTION_MOVE)
					|| (eventAction == MotionEvent.ACTION_UP)) {
				for (int i = 0; i < mArrayList.size(); i++) {
					EdgePoint point = mArrayList.get(i);
					if (!point.isChecked
							&& ((w >= (point.x - mRadius)) && (w <= (point.x + mRadius)))
							&& ((h >= (point.y - mRadius)) && (h <= (point.y + mRadius)))) {
						mArrayList.remove(i);
						break;
					}

				}

				if (mArrayList.isEmpty()) {
					((Activity) mContext).setResult(RESULT_OK);
					resultString = Utils.RESULT_PASS;
					finish();
				}

				TouchData tData = mTouchData[traceCounter];
				tData.x = event.getX();
				tData.y = event.getY();
				tData.r = event.getPressure() * PRESSURE;
				traceCounter = getNext(traceCounter);
				invalidate();

			}
			return true;
		}

	}

	void logd(Object d) {

		Log.d(TAG, "" + d);
	}

	void loge(Object e) {

		Log.e(TAG, "" + e);
	}

	@Override
	protected void onResume() {
		super.onResume();
		GSFWManager.getInstance().setHomeKeyDispatched(getWindow());
		SystemProperties.set("persist.sys.qsdown", "false");
	}

	@Override
	protected void onPause() {
		super.onPause();
		SystemProperties.set("persist.sys.qsdown", "true");
	}
}
