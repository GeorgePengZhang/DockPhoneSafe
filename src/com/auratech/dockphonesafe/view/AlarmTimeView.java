package com.auratech.dockphonesafe.view;

import java.util.List;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import com.auratech.dockphonesafe.utils.TimeBean;
import com.auratech.dockphonesafe.utils.TimeUtils;

public class AlarmTimeView extends View {
	
	private static final int COLOR_BASE = 0xff000000;
	private static final int COLOR_CURRENT_TIME = 0xff0000ff;
	private static final int COLOR_SELECTED_EN = 0xaa00ff00;
	private static final int COLOR_SELECTED_DIS = 0xaa888888;
	private static final int COLOR_ERROR_SELECTED = 0xaaff0000;
	
	private Paint basePaint;
	private Paint curTimePaint;
	private Paint selectedPaint;
	private Paint errorSelectedPaint;
	private List<TimeBean> mList;
	private TimeBean errorBean;
	private Time mCalendar;
	private boolean mAttached;
	private boolean mChanged;
	private int mMinutes;
    private int mHour;
	
	public AlarmTimeView(Context context) {
		super(context);
		init();
	}

	public AlarmTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		basePaint = new Paint();
		basePaint.setColor(COLOR_BASE);
		basePaint.setStrokeWidth(2);
		basePaint.setTextAlign(Align.CENTER);
		
		curTimePaint = new Paint();
		curTimePaint.setColor(COLOR_CURRENT_TIME);
		curTimePaint.setStrokeWidth(2);
		
		selectedPaint = new Paint();
		selectedPaint.setColor(COLOR_SELECTED_EN);
		
		errorSelectedPaint = new Paint();
		errorSelectedPaint.setColor(COLOR_ERROR_SELECTED);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (!mAttached) {
			mAttached = true;
			 IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter);
		}
		
		mCalendar = new Time();
        onTimeChanged();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mChanged = true;
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }

            onTimeChanged();
            
            invalidate();
        }
    };
	
    
    private void onTimeChanged() {
    	mCalendar.setToNow();
    	int hour = mCalendar.hour;
    	int minute = mCalendar.minute;
    	int second = mCalendar.second;
    	
    	mMinutes = minute + second / 60;
        mHour = hour + mMinutes / 60;
    	mChanged = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

		int width = getWidth();
		int height = getHeight();
		
		float baseLineHeight = height * 2.0f / 3;
		float perHourWidth = width / 24.0f;
		float perMinuteWidth = perHourWidth / 60.0f;
		float padding = perHourWidth / 2;
		
		//����ʱ����
		drawBaseTimeLine(canvas, width, baseLineHeight, perHourWidth, padding);
		
		if ( mList != null ) {
			for (int i = 0; i < mList.size(); i++) {
				TimeBean bean = mList.get(i);
				boolean open = bean.isOpen();
				Paint paint = selectedPaint;
				if (!open) {
					paint.setColor(COLOR_SELECTED_DIS);
				} else {
					paint.setColor(COLOR_SELECTED_EN);
				}
				//����ѡ���ʱ������
				drawSelectedTime(canvas, width, baseLineHeight, perHourWidth, perMinuteWidth, padding, bean, paint);
				drawSelectedIndex(canvas, width, height, perHourWidth,
						perMinuteWidth, padding, i, bean);
			}
		}
		
		//����ѡ���ʱ������
		drawSelectedTime(canvas, width, baseLineHeight, perHourWidth, perMinuteWidth, padding, errorBean, errorSelectedPaint);
		
		//���Ƶ�ǰʱ���
		drawCurrentTimePoint(canvas, width, baseLineHeight, perHourWidth,
				perMinuteWidth, padding);
	}

	/**
	 * ������ѡʱ�������˳��
	 * @param canvas
	 * @param width
	 * @param height
	 * @param perHourWidth
	 * @param perMinuteWidth
	 * @param padding
	 * @param i
	 * @param bean
	 */
	public void drawSelectedIndex(Canvas canvas, int width, int height,
			float perHourWidth, float perMinuteWidth, float padding, int i,
			TimeBean bean) {
		Rect rect = new Rect();
		basePaint.getTextBounds((i+1)+"", 0, 1, rect);
		float middleX = getMiddleXByTime(width, perHourWidth, perMinuteWidth, padding, bean);
		canvas.drawText((i+1)+"", middleX, height/2-rect.height(), basePaint);
	}

	/**
	 * ���Ƶ�ǰʱ���
	 * @param canvas
	 * @param width
	 * @param baseLineHeight
	 * @param perHourWidth
	 * @param perMinuteWidth
	 * @param padding
	 */
	private void drawCurrentTimePoint(Canvas canvas, int width, float baseLineHeight,
			float perHourWidth, float perMinuteWidth, float padding) {
		float curTimeX = getXByTime(width, perHourWidth, perMinuteWidth, padding, mHour, mMinutes);
		float curTimeStartY = 0;
		float curTimeStopY = baseLineHeight;
		
		canvas.drawLine(curTimeX, curTimeStartY, curTimeX, curTimeStopY, curTimePaint);
	}

	/**
	 * ��ȡʱ���������ʼ���߽���X����
	 * @param width
	 * @param perHourWidth
	 * @param perMinuteWidth
	 * @param padding
	 * @param hour
	 * @param minute
	 * @return
	 */
	private float getXByTime(int width, float perHourWidth,
			float perMinuteWidth, float padding, int hour, int minute) {
		float curTimeX = ((padding+perHourWidth * hour) + (perMinuteWidth*minute)) % width;
		return curTimeX;
	}
	
	/**
	 * ��ȡʱ��������м�X����
	 * @param width
	 * @param perHourWidth
	 * @param perMinuteWidth
	 * @param padding
	 * @param bean
	 * @return
	 */
	private float getMiddleXByTime(int width, float perHourWidth,
			float perMinuteWidth, float padding, TimeBean bean) {
		int startHour = bean.getFromTime().getHour();
		int startMinute = bean.getFromTime().getMinute();
		int endHour = bean.getToTime().getHour();
		int endMinute = bean.getToTime().getMinute();
		float startX = getXByTime(width, perHourWidth, perMinuteWidth, padding, startHour, startMinute);
		float endX = getXByTime(width, perHourWidth, perMinuteWidth, padding, endHour, endMinute);
		
		float middleX;
		if (startX >= endX) {
			middleX = ((startX + startX + endX) / 2.0f) % width;
		} else {
			middleX = (startX + endX) / 2.0f;
		}
		
		return middleX;
	}

	/**
	 * ����ʱ����
	 * @param canvas
	 * @param width
	 * @param baseLineHeight
	 * @param perHourWidth
	 * @param padding
	 */
	private void drawBaseTimeLine(Canvas canvas, int width,
			float baseLineHeight, float perHourWidth, float padding) {
		canvas.drawLine(0, baseLineHeight, width, baseLineHeight, basePaint);
		int perHeight = 4;
		
		for (int i = 0; i < 24; i++) {
			float startX = padding+perHourWidth * i;
			float startY = baseLineHeight - perHeight;
		    float stopX = startX;
		    float stopY = baseLineHeight;
			
			canvas.drawLine(startX, startY, stopX, stopY, basePaint);
			
			String number = i + "";
			Rect rect = new Rect();
			basePaint.getTextBounds(number, 0, 1, rect);
			canvas.drawText(i+"", stopX, stopY+rect.height()+4, basePaint);
		}
	}
	
	/**
	 * ������ѡ���ʱ������
	 * @param canvas
	 * @param width
	 * @param baseLineHeight
	 * @param perHourWidth
	 * @param perMinuteWidth
	 * @param padding
	 * @param bean
	 * @param paint
	 */
	private void drawSelectedTime(Canvas canvas, int width, float baseLineHeight,
			float perHourWidth, float perMinuteWidth, float padding, TimeBean bean, Paint paint) {
		if (bean == null) {
			return ;
		}
		
		TimeUtils fromTime = bean.getFromTime();
		TimeUtils toTime = bean.getToTime();
		
		int hour = fromTime.getHour();
		int minute = fromTime.getMinute();
		float startX = getXByTime(width, perHourWidth, perMinuteWidth, padding, hour, minute);
		
		int endHour = toTime.getHour();
		int endMinute = toTime.getMinute();
		float endX = getXByTime(width, perHourWidth, perMinuteWidth, padding, endHour, endMinute);
		if (endX <= startX) {
			canvas.drawRect(startX, 0, width, baseLineHeight, paint);
			canvas.drawRect(0, 0, endX, baseLineHeight, paint);
		} else {
			canvas.drawRect(startX, 0, endX, baseLineHeight, paint);
		}
	}
	
	/**
	 * ����ѡ���ʱ������
	 * @param list
	 */
	public void setSelectedTimeBean(List<TimeBean> list) {
		mList = list;
		errorBean = null;
		mChanged = true;
		postInvalidate();
	}
	
	/**
	 * ���ô����ʱ������
	 * @param bean
	 */
	public void setErrorSelectedTimeBean(TimeBean bean) {
		errorBean = bean;
		mChanged = true;
		postInvalidate();
	}
}
