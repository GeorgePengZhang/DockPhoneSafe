/**  

 * Copyright © 2016公司名字. All rights reserved.

 * @Title: SettingsFragment.java

 * @Prject: DockPhoneSafe

 * @Package: com.auratech.dockphonesafe.fragment

 * @Description: TODO

 * @author: Administrator  

 * @date: Oct 9, 2016 11:12:50 AM

 */
package com.auratech.dockphonesafe.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import mirko.android.datetimepicker.time.RadialPickerLayout;
import mirko.android.datetimepicker.time.TimePickerDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.adapter.DisturbAdapter;
import com.auratech.dockphonesafe.bean.TimeBean;
import com.auratech.dockphonesafe.service.Dock;
import com.auratech.dockphonesafe.utils.AlarmUtils;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.TimeUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.utils.Utils.IChildViewOnClick;
import com.auratech.dockphonesafe.view.AlarmTimeView;
import com.auratech.dockphonesafe.view.SettingsItemView;
import com.auratech.dockphonesafe.view.SettingsItemView.OnItemClickListener;
import com.litesuits.orm.LiteOrm;

/**
 * @ClassName: SettingsFragment
 * @Description: TODO
 * @author: steven zhang
 * @date: Oct 9, 2016 11:12:50 AM
 */
public class SettingsFragment extends Fragment implements OnClickListener, OnTouchListener, IChildViewOnClick  {
	
	private static final TimeUtils START_TIME = new TimeUtils(21, 30, TimeUtils.ALARM_TYPE_START);
	private static final TimeUtils END_TIME = new TimeUtils(9, 00, TimeUtils.ALARM_TYPE_END);
	private static final boolean TIME_OPENED = false;
	
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
	private View mView;
	private TimePickerDialog dialog;
	private SettingsItemView blackItemsView;
	private SettingsItemView ringItemsView;
	private SettingsItemView disturbItemsView;
	private SettingsItemView disturbItemStart;
	private SettingsItemView disturbItemEnd;
	private TimeBean timeBean;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.disturb_layout, container, false);
		
		initView(mView);
		initParam();
		return mView;
	}

	public void initView(View view) {
		mListView = (ListView) view.findViewById(R.id.id_listview);
		mFromTimeET = (EditText) view.findViewById(R.id.id_fromtime);
		mToTimeET = (EditText) view.findViewById(R.id.id_totime);
		mAddBtn = (Button) view.findViewById(R.id.id_add);
		mAlarmView = (AlarmTimeView) view.findViewById(R.id.id_alarmview);
		
		blackItemsView = (SettingsItemView) view.findViewById(R.id.id_blacklistitem);
		blackItemsView.showHeader(true);
		blackItemsView.setHeaderText(getResources().getString(R.string.general));
		blackItemsView.setItemText("黑名单屏蔽功能");
		blackItemsView.setItemContent("本功能需将平板放入底座才可修改");
		blackItemsView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				boolean checked = !blackItemsView.isChecked();
				if (checked) {
					if (Dock.openblacklist()) { //设置开启黑名单屏蔽成功，才修改UI的变化
						blackItemsView.setChecked(checked);
					}
				} else {
					if (Dock.closeblacklist()) { //设置关闭黑名单屏蔽成功，才修改UI的变化
						blackItemsView.setChecked(checked);
					}
				}
			}
		});
		
		ringItemsView = (SettingsItemView) view.findViewById(R.id.id_ringitem);
		ringItemsView.setItemText("底座响铃功能");
		ringItemsView.setItemContent("本功能需将平板放入底座才可修改");
		ringItemsView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				boolean checked = !ringItemsView.isChecked();
				if (checked) {
					if (Dock.openRing()) { //设置开启底座响应成功，才修改UI的变化
						ringItemsView.setChecked(checked);
					}
				} else {
					if (Dock.closeRing()) { //设置关闭底座响应成功，才修改UI的变化
						ringItemsView.setChecked(checked);
					}
				}
			}
		});
		
		disturbItemsView = (SettingsItemView) view.findViewById(R.id.id_disturbitem);
		disturbItemsView.showHeader(true);
		disturbItemsView.setHeaderText("SCHEDULE");
		disturbItemsView.setItemText("Do not disturb");
		disturbItemsView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				boolean checked = !disturbItemsView.isChecked();
				timeBean.setOpen(checked);
				update(timeBean);
				setDisturbItemVisibility(checked);
			}
		});
		
		disturbItemStart = (SettingsItemView) view.findViewById(R.id.id_disturbitemstart);
		disturbItemStart.setItemText("Start");
		disturbItemStart.showSwitch(false);
		disturbItemStart.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				setDisturbTime(disturbItemStart);
			}
		});
		disturbItemEnd = (SettingsItemView) view.findViewById(R.id.id_disturbitemend);
		disturbItemEnd.setItemText("End");
		disturbItemEnd.showSwitch(false);
		disturbItemEnd.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				setDisturbTime(disturbItemEnd);
			}
		});
		
		mAddBtn.setOnClickListener(this);
		mFromTimeET.setOnTouchListener(this);
		mToTimeET.setOnTouchListener(this);
		
	}

	public void initParam() {
		liteOrm = DbUtils.getInstance(getActivity()).getDbInstance();
		initListView();
		
		
		ArrayList<TimeBean> list = liteOrm.query(TimeBean.class);
		if (list.size() == 0) {
			TimeUtils timeDifference = Utils.getTimeDifference(START_TIME, END_TIME);
			int intervalsCount = Utils.getIntervalsCount(timeDifference);
			timeBean = new TimeBean(START_TIME, END_TIME, timeDifference, intervalsCount, TIME_OPENED);
			add(timeBean);
		} else {
			timeBean = list.get(0);
		}
		
		disturbItemStart.setItemContent(timeBean.getFromTime().toString());
		disturbItemEnd.setItemContent(timeBean.getToTime().toString());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setGeneralItemEnabled(Dock.isExistUSBNode());
		setDisturbItemVisibility(timeBean.isOpen());
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_add:
			TextView tv = (TextView) mView.findViewById(R.id.id_show);
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
		case R.id.id_blacklistitem:
			boolean enabled = ((SettingsItemView) v).isChecked();
			((SettingsItemView) v).setChecked(!enabled);
			break;

		default:
			break;
		}
	}
	
	/**
	 * 设置黑名单屏蔽、底座响铃等功能是否可以开启，关闭
	 * @param Enabled
	 */
	private void setGeneralItemEnabled(boolean Enabled) {
		blackItemsView.setItemEnabled(Enabled);
		ringItemsView.setItemEnabled(Enabled);
	}
	
	/**
	 * 设置免打扰的时间是否显示
	 * @param checked
	 */
	private void setDisturbItemVisibility(boolean checked) {
		disturbItemsView.setChecked(checked);
		if (checked) {
			disturbItemStart.setVisibility(View.VISIBLE);
			disturbItemEnd.setVisibility(View.VISIBLE);
		} else {
			disturbItemStart.setVisibility(View.GONE);
			disturbItemEnd.setVisibility(View.GONE);
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
		
		
		dialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
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
		}, hourOfDay, minute, true);
		dialog.show(getChildFragmentManager(), "dialog");
	}
	
	
	/**
	 * 弹出时间选择框，设置免打扰的时间间隔
	 * @param editText
	 */
	public void setDisturbTime(final SettingsItemView itemView) {
		TimeUtils timeUtils = new TimeUtils();
		timeUtils.parse((String) itemView.getItemContent());
		int hourOfDay = timeUtils.getHour();
		int minute = timeUtils.getMinute();
		
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		
		dialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
				TimeUtils time = new TimeUtils(hourOfDay, minute, TimeUtils.ALARM_TYPE_START);
				switch (itemView.getId()) {
				case R.id.id_disturbitemstart:
					time.setType(TimeUtils.ALARM_TYPE_START);
					AlarmUtils.cancelAlarmInstance(getActivity(), timeBean);
					timeBean.setFromTime(time);
					add(timeBean);
					break;
				case R.id.id_disturbitemend:
					time.setType(TimeUtils.ALARM_TYPE_END);
					AlarmUtils.cancelAlarmInstance(getActivity(), timeBean);
					timeBean.setToTime(time);
					add(timeBean);
					break;

				default:
					break;
				}
				itemView.setItemContent(time.toString());
			}
		}, hourOfDay, minute, true);
		dialog.show(getChildFragmentManager(), "dialog");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
        if (dialog != null && dialog.isAdded()) {
        	dialog.dismiss();
            dialog.show(getChildFragmentManager(), "dialog");
        }
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
		mAdapter = new DisturbAdapter(getActivity(), mListTime, this);
		mListView.setAdapter(mAdapter);
	}
	
	
	
	private void add(TimeBean bean) {
		mListTime.add(bean);
		Collections.sort(mListTime);
		liteOrm.save(bean);
		
		mAlarmView.setSelectedTimeBean(mListTime);
		mAdapter.notifyDataSetChanged();
		
		
		AlarmUtils.setupAlarmInstance(getActivity(), bean);
	}
	
	private void remove(TimeBean bean) {
		liteOrm.delete(bean);
		mListTime.remove(bean);
		mAdapter.notifyDataSetChanged();
		
		AlarmUtils.cancelAlarmInstance(getActivity(), bean);
	}
	
	private void update(TimeBean bean) {
		liteOrm.update(bean);
		mAdapter.notifyDataSetChanged();
		if (bean.isOpen()) {
			AlarmUtils.setupAlarmInstance(getActivity(), bean);
		} else {
			AlarmUtils.cancelAlarmInstance(getActivity(), bean);
		}
	}
}
