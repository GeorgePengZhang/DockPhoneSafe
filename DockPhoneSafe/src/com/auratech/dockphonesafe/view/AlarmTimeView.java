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

import com.auratech.dockphonesafe.bean.TimeBean;
import com.auratech.dockphonesafe.utils.DisplayUtil;
import com.auratech.dockphonesafe.utils.TimeUtils;
/**
 * @ClassName: AlarmTimeView
 * @Description: TODO
 * @author: steven zhang
 * @date: Sep 27, 2016 2:32:30 PM
 */
public class AlarmTimeView extends View {
	
	private static final int LINE_WIDTH = 2;
	private static final float TEXT_SIZE = 16;
	
	private static final int COLOR_BASE = 0xff000000;
	private static final int COLOR_CURRENT_TIME = 0xffee0000;
	private static final int COLOR_SELECTED_ENABLE = 0xbb56abe4;
	private static final int COLOR_SELECTED_DISABLE = 0x44b2b2b2;
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
	private int textHeight;
	private int textWidth;
	
	public AlarmTimeView(Context context) {
		super(context);
		init();
	}

	public AlarmTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		int lineWidth = DisplayUtil.dp2px(getContext(), LINE_WIDTH);
		
		basePaint = new Paint();
		basePaint.setColor(COLOR_BASE);
		basePaint.setStrokeWidth(lineWidth);
		basePaint.setAntiAlias(true);
		basePaint.setTextSize(DisplayUtil.dp2px(getContext(), TEXT_SIZE));
		basePaint.setTextAlign(Align.CENTER);
		
		curTimePaint = new Paint();
		curTimePaint.setColor(COLOR_CURRENT_TIME);
		curTimePaint.setAntiAlias(true);
		curTimePaint.setStrokeWidth(lineWidth);
		
		selectedPaint = new Paint();
		selectedPaint.setStrokeWidth(lineWidth);
		selectedPaint.setAntiAlias(true);
		selectedPaint.setColor(COLOR_SELECTED_ENABLE);
		
		errorSelectedPaint = new Paint();
		errorSelectedPaint.setColor(COLOR_ERROR_SELECTED);
		
		Rect rect = new Rect();
		basePaint.getTextBounds("00", 0, 2, rect);
		textHeight = rect.height();
		textWidth = rect.width();
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
		
		float perHourWidth = width / 24.0f;
		float perMinuteWidth = perHourWidth / 60.0f;
		float padding = perHourWidth / 2;
		
		if (textWidth >= perHourWidth) {
			basePaint.setTextSize(perHourWidth*3/4);
			Rect rect = new Rect();
			basePaint.getTextBounds("00", 0, 2, rect);
			textHeight = rect.height();
			textWidth = rect.width();
		}
		
		//绘制时间轴
		float baseLineHeight = drawBaseTimeLine(canvas, width, height, perHourWidth, padding);
		baseLineHeight = baseLineHeight - LINE_WIDTH / 2;
		
		if ( mList != null ) {
			for (int i = 0; i < mList.size(); i++) {
				TimeBean bean = mList.get(i);
				boolean open = bean.isOpen();
				Paint paint = selectedPaint;
				if (!open) {
					paint.setColor(COLOR_SELECTED_DISABLE);
				} else {
					paint.setColor(COLOR_SELECTED_ENABLE);
				}
				//绘制选择的时间区域
				drawSelectedTime(canvas, width, baseLineHeight, perHourWidth, perMinuteWidth, padding, bean, paint);
				drawSelectedIndex(canvas, width, (int) baseLineHeight, perHourWidth, perMinuteWidth, padding, i, bean);
			}
		}
		
		//绘制错误的选择时间区域
		drawSelectedTime(canvas, width, baseLineHeight, perHourWidth, perMinuteWidth, padding, errorBean, errorSelectedPaint);
		
		//绘制当前时间点
		drawCurrentTimePoint(canvas, width, baseLineHeight, perHourWidth,
				perMinuteWidth, padding);
	}

	/**
	 * 绘制已选时间区域的顺序
	 * @param canvas
	 * @param width
	 * @param height
	 * @param perHourWidth
	 * @param perMinuteWidth
	 * @param padding
	 * @param i
	 * @param bean
	 */
	private void drawSelectedIndex(Canvas canvas, int width, int height,
			float perHourWidth, float perMinuteWidth, float padding, int i,
			TimeBean bean) {
		Rect rect = new Rect();
		basePaint.getTextBounds((i+1)+"", 0, 1, rect);
		float middleX = getMiddleXByTime(width, perHourWidth, perMinuteWidth, padding, bean);
		canvas.drawText(bean.info(), middleX, (height+textHeight)/2, basePaint);
	}

	/**
	 * 绘制当前时间点
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
	 * 获取时间区域的起始或者结束X坐标
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
	 * 获取时间区域的中间X坐标
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
	 * 绘制时间轴
	 * @param canvas
	 * @param width
	 * @param height
	 * @param perHourWidth
	 * @param padding
	 * @return 
	 */
	private float drawBaseTimeLine(Canvas canvas, int width,
			float height, float perHourWidth, float padding) {
		int perHeight = DisplayUtil.dp2px(getContext(), 4);
		float baseLineHeight = height - textHeight - perHeight;
		for (int i = 0; i < 24; i++) {
			float startX = padding+perHourWidth * i;
			float startY = baseLineHeight - perHeight;
		    float stopX = startX;
		    float stopY = baseLineHeight;
			
			canvas.drawLine(startX, startY, stopX, stopY, basePaint);
			canvas.drawText(i+"", stopX, height, basePaint);
		}
		
		canvas.drawLine(0, baseLineHeight, width, baseLineHeight, basePaint);
		return baseLineHeight;
	}
	
	/**
	 * 绘制已选择的时间区域
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
	 * 设置选择的时间区域
	 * @param list
	 */
	public void setSelectedTimeBean(List<TimeBean> list) {
		mList = list;
		errorBean = null;
		mChanged = true;
		postInvalidate();
	}
	
	/**
	 * 设置错误的时间区域
	 * @param bean
	 */
	public void setErrorSelectedTimeBean(TimeBean bean) {
		errorBean = bean;
		mChanged = true;
		postInvalidate();
	}
}
