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

import mirko.android.datetimepicker.time.RadialPickerLayout;
import mirko.android.datetimepicker.time.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.TimeBean;
import com.auratech.dockphonesafe.service.Dock;
import com.auratech.dockphonesafe.service.DockService;
import com.auratech.dockphonesafe.utils.AlarmUtils;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.TimeUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.view.SettingsItemView;
import com.auratech.dockphonesafe.view.SettingsItemView.OnItemClickListener;
import com.litesuits.orm.LiteOrm;

/**
 * @ClassName: SettingsFragment
 * @Description: TODO
 * @author: steven zhang
 * @date: Oct 9, 2016 11:12:50 AM
 */
public class SettingsFragment extends Fragment {
	
	private static final TimeUtils START_TIME = new TimeUtils(21, 30, TimeUtils.ALARM_TYPE_START);
	private static final TimeUtils END_TIME = new TimeUtils(9, 00, TimeUtils.ALARM_TYPE_END);
	public static final boolean TIME_OPENED = false;
	public static final boolean BLACKLIST_OPENED = true;
	public static final boolean RING_OPENED = true;
	
	private LiteOrm liteOrm;
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
		View view = inflater.inflate(R.layout.disturb_layout, container, false);
		
		initView(view);
		initParam();
		
		IntentFilter filter = new IntentFilter(DockService.ACTION_DOCKCHANGED);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, filter);
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
	}
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			setGeneralItemEnabled(Dock.isExistUSBNode());
		}
	};

	public void initView(View view) {
		blackItemsView = (SettingsItemView) view.findViewById(R.id.id_blacklistitem);
		blackItemsView.showHeader(true);
		blackItemsView.setHeaderText(getResources().getString(R.string.general));
		blackItemsView.setItemText("黑名单屏蔽功能");
		blackItemsView.setItemContent("本功能需将平板放入底座才可修改");
		blackItemsView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				boolean checked = !blackItemsView.isItemChecked();
				blacklistEnabled(checked);
			}
		});
		
		ringItemsView = (SettingsItemView) view.findViewById(R.id.id_ringitem);
		ringItemsView.setItemText("底座响铃功能");
		ringItemsView.setItemContent("本功能需将平板放入底座才可修改");
		ringItemsView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				boolean checked = !ringItemsView.isItemChecked();
				ringEnabled(checked);
			}
		});
		
		disturbItemsView = (SettingsItemView) view.findViewById(R.id.id_disturbitem);
		disturbItemsView.showHeader(true);
		disturbItemsView.setHeaderText("SCHEDULE");
		disturbItemsView.setItemText("Do not disturb");
		disturbItemsView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(View v) {
				boolean checked = !disturbItemsView.isItemChecked();
				disturbEnabled(checked);
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
		
	}

	public void initParam() {
		liteOrm = DbUtils.getInstance(getActivity()).getDbInstance();
		
		ArrayList<TimeBean> list = liteOrm.query(TimeBean.class);
		if (list.size() == 0) {
			TimeUtils timeDifference = Utils.getTimeDifference(START_TIME, END_TIME);
			int intervalsCount = Utils.getIntervalsCount(timeDifference);
			timeBean = new TimeBean(START_TIME, END_TIME, timeDifference, intervalsCount, TIME_OPENED, BLACKLIST_OPENED, RING_OPENED);
			disturbAdd();
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
		blackItemsView.setItemChecked(timeBean.isBlacklistEnabled());
		ringItemsView.setItemChecked(timeBean.isRingEnabled());
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
		disturbItemsView.setItemChecked(checked);
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
					disturbUpdate(time, TimeUtils.ALARM_TYPE_START);
					break;
				case R.id.id_disturbitemend:
					disturbUpdate(time, TimeUtils.ALARM_TYPE_END);
					break;

				default:
					return ;
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
	
	
	//--------------------------------------------上面为UI交互部分-----------------------------------------------------
	
	//TODO --------------------------------------------华丽丽的分割线--------------------------------------------------
		
	//--------------------------------------------下面面为逻辑处理部分---------------------------------------------------
	
	/**
	 * 更新定时免打扰时间区间
	 * @param time 修改的时间
	 * @param type 修改的是开始还是结束时间
	 */
	private void disturbUpdate(TimeUtils time, int type) {
		AlarmUtils.cancelAlarmInstance(getActivity(), timeBean);
		
		time.setType(type);
		if (type == TimeUtils.ALARM_TYPE_START) {
			timeBean.setFromTime(time);
		} else {
			timeBean.setToTime(time);
		}
		
		liteOrm.save(timeBean);
		AlarmUtils.setupAlarmInstance(getActivity(), timeBean);
	}
	
	/**
	 * 增加一个定时免打扰
	 */
	private void disturbAdd() {
		liteOrm.save(timeBean);
		AlarmUtils.setupAlarmInstance(getActivity(), timeBean);
	}
	
	/**
	 * 是否开启定时免打扰功能
	 * @param enabled
	 */
	private void disturbEnabled(boolean enabled) {
		timeBean.setOpen(enabled);
		liteOrm.update(timeBean);
		if (enabled) {
			AlarmUtils.setupAlarmInstance(getActivity(), timeBean);
		} else {
			AlarmUtils.cancelAlarmInstance(getActivity(), timeBean);
		}
	}
	
	/**
	 * 是否开启黑名单屏蔽功能
	 * @param enabled
	 */
	private void blacklistEnabled(boolean enabled) {
		boolean flag = false;
		
		if (enabled) {
			flag = Dock.openblacklist(); //设置开启黑名单屏蔽成功，才修改UI的变化
		} else {
			flag = Dock.closeblacklist(); //设置关闭黑名单屏蔽成功，才修改UI的变化
		}
		
		if (flag) {
			blackItemsView.setItemChecked(enabled);
			timeBean.setBlacklistEnabled(enabled);
			liteOrm.update(timeBean);
		}
	}
	
	/**
	 * 是否开启响铃功能
	 * @param enabled
	 */
	private void ringEnabled(boolean enabled) {
		boolean flag = false;
		
		if (enabled) {
			flag = Dock.openRing(); //设置开启底座响应成功，才修改UI的变化
		} else {
			flag = Dock.closeRing(); //设置关闭底座响应成功，才修改UI的变化
		}
		
		if (flag) {
			ringItemsView.setItemChecked(enabled);
			timeBean.setRingEnabled(enabled);
			liteOrm.update(timeBean);
		}
	}
}
