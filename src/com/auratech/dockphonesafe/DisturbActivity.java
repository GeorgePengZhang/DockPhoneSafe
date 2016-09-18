package com.auratech.dockphonesafe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.auratech.dockphonesafe.adapter.DisturbAdapter;
import com.auratech.dockphonesafe.bean.TimeBean;
import com.auratech.dockphonesafe.utils.AlarmUtils;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.TimeUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.utils.Utils.IChildViewOnClick;
import com.auratech.dockphonesafe.view.AlarmTimeView;
import com.litesuits.orm.LiteOrm;

public class DisturbActivity extends Activity implements OnClickListener, OnTouchListener, IChildViewOnClick {
	
	private EditText mFromTimeET;
	private EditText mToTimeET;
	private TimeUtils mFromTime;
	private TimeUtils mToTime;
	private ListView mListView;
	private Button mAddBtn;
	private ArrayList<TimeBean> mListTime;
	private DisturbAdapter mAdapter;
	private LiteOrm liteOrm;
	private AlarmTimeView mAlarmView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.disturb_layout);
		setTitle("免打扰");
		
		initView();
		initParam();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void initView() {
		mListView = (ListView) findViewById(R.id.id_listview);
		mFromTimeET = (EditText) findViewById(R.id.id_fromtime);
		mToTimeET = (EditText) findViewById(R.id.id_totime);
		mAddBtn = (Button) findViewById(R.id.id_add);
		mAlarmView = (AlarmTimeView) findViewById(R.id.id_alarmview);
		
		mAddBtn.setOnClickListener(this);
		mFromTimeET.setOnTouchListener(this);
		mToTimeET.setOnTouchListener(this);
	}

	public void initParam() {
		liteOrm = DbUtils.getInstance(this).getDbInstance();
		initListView();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_add:
			TextView tv = (TextView) findViewById(R.id.id_show);
			if (mFromTime == null || mToTime == null) {
				tv.setText("添加失败,请设置免打扰时间");
				break;
			}
			
			TimeUtils timeDifference = Utils.getTimeDifference(mFromTime, mToTime);
			int intervalsCount = Utils.getIntervalsCount(timeDifference);
			TimeBean bean = new TimeBean(mFromTime, mToTime, timeDifference, intervalsCount, true);
			tv.setText(bean.toString());
			
			boolean isUsed = false;

			for (TimeBean time : mListTime) {
				isUsed = Utils.isInTimeDifference(bean.getFromTime(), bean.getToTime(), time.getFromTime(), time.getToTime());
				if (isUsed) {
					mAlarmView.setErrorSelectedTimeBean(bean);
					tv.setText("添加失败,时间间隔不能重叠,如有需要，请删除可能重叠的时间间隔重新添加。");
					break;
				}
			}
			
			if (!isUsed) {
				add(bean);
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 弹出时间选择框，设置免打扰的时间间隔
	 * @param editText
	 */
	public void setDisturbTime(final EditText editText) {
		Calendar instance = Calendar.getInstance();
		instance.setTimeInMillis(System.currentTimeMillis());
		int hourOfDay = instance.get(Calendar.HOUR_OF_DAY);
		int minute = instance.get(Calendar.MINUTE);
		
		String sTime = editText.getText().toString();
		if (!TextUtils.isEmpty(sTime)) {
			TimeUtils timeUtils = new TimeUtils();
			timeUtils.parse(sTime);
			hourOfDay = timeUtils.getHour();
			minute = timeUtils.getMinute();
		}
		
		new TimePickerDialog(DisturbActivity.this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				TimeUtils time = new TimeUtils(hourOfDay, minute, TimeUtils.ALARM_TYPE_START);
				switch (editText.getId()) {
				case R.id.id_fromtime:
					time.setType(TimeUtils.ALARM_TYPE_START);
					mFromTime = time;
					break;
				case R.id.id_totime:
					time.setType(TimeUtils.ALARM_TYPE_END);
					mToTime = time;
					break;

				default:
					break;
				}
				editText.setText(time.toString());
			}
		}, hourOfDay, minute, true).show();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.id_fromtime:
				setDisturbTime(mFromTimeET);
				break;
			case R.id.id_totime:
				setDisturbTime(mToTimeET);
				break;

			default:
				break;
			}
		}
		return false;
	}

	@Override
	public void onChildViewClick(View v) {
		Integer position = (Integer) v.getTag();
		int iPosition = position.intValue();
		TimeBean bean = mListTime.get(iPosition);
		
		switch (v.getId()) {
		case R.id.id_del:
			remove(bean);
			break;
		case R.id.id_switch:
			bean.setOpen(!bean.isOpen());
			update(bean);
			break;

		default:
			break;
		}
		
		mAlarmView.setSelectedTimeBean(mListTime);
	}
	
	private void initListView() {
		mListTime = liteOrm.query(TimeBean.class);
		Collections.sort(mListTime);
		mAlarmView.setSelectedTimeBean(mListTime);
//		AlarmUtils.setupAlarmCollection(DisturbActivity.this, mListTime);
		mAdapter = new DisturbAdapter(DisturbActivity.this, mListTime, this);
		mListView.setAdapter(mAdapter);
	}
	
	private void add(TimeBean bean) {
		
		mListTime.add(bean);
		Collections.sort(mListTime);
		liteOrm.save(bean);
		
		mAlarmView.setSelectedTimeBean(mListTime);
		mAdapter.notifyDataSetChanged();
		
		AlarmUtils.setupAlarmInstance(this, bean);
	}
	
	private void remove(TimeBean bean) {
		liteOrm.delete(bean);
		mListTime.remove(bean);
		mAdapter.notifyDataSetChanged();
		
		AlarmUtils.cancelAlarmInstance(this, bean);
	}
	
	private void update(TimeBean bean) {
		liteOrm.update(bean);
		mAdapter.notifyDataSetChanged();
		if (bean.isOpen()) {
			AlarmUtils.setupAlarmInstance(this, bean);
		} else {
			AlarmUtils.cancelAlarmInstance(this, bean);
		}
	}
}
