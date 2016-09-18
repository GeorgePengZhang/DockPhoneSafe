package com.auratech.dockphonesafe.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.auratech.dockphonesafe.receiver.AlarmStateManager;
import com.litesuits.orm.LiteOrm;

public class AlarmUtils {
	
	public final static String ALARM_TIME 			= "alarmTime"; 	//定时器实际的被触发时间
	public final static String ALARM_START_TIME 	= "startTime"; 	//定时器开始时被触发的时间
	public final static String ALARM_END_TIME 		= "endTime"; 	//定时器结束时被触发的时间
	public final static String ALARM_COUNTS 		= "counts";	   	//定时器间隔定时次数
	public final static String ALARM_ID 			= "id";	   		//定时器id
	public final static String ALARM_TIMEUTIL 		= "time";	   	//定时器timeUitls
	
	//"com.auratech.alarm.startup_"+count+"_"+id
	public final static String ALARM_START_UP_ACTION = "com.auratech.alarm.startup";
    
    public static void fixAlarmInstances(Context context) {
    	LiteOrm liteOrm = DbUtils.getInstance(context).getDbInstance();
    	ArrayList<TimeBean> list = liteOrm.query(TimeBean.class);
    	Collections.sort(list);
    	setupAlarmCollection(context, list);
    }
    
    public static void setupAlarmCollection(Context context, List<TimeBean> list) {
    	for (int i = 0; i < list.size(); i++) {
    		TimeBean bean = list.get(i);
    		setupAlarmInstance(context, bean);
		}
    }
    
    public static void setupAlarmInstance(Context context, TimeBean bean) {
    	TimeUtils fromTime = bean.getFromTime();
    	TimeUtils toTime = bean.getToTime();
    	
    	Calendar fromCalendar = createCalendarByTimeUtils(fromTime);
    	Calendar toCalendar = createCalendarByTimeUtils(toTime);
    	if (fromCalendar.getTimeInMillis() >= toCalendar.getTimeInMillis()) {
    		toCalendar.add(Calendar.DAY_OF_YEAR, 1);
    	}
    	
    	List<TimeUtils> list = Utils.getAllTimeUtilsByTimeBean(bean);
    	for (int i = 0; i < list.size(); i++) {
			TimeUtils timeUtils = list.get(i);
			
			Calendar calendar = createCalendarByTimeUtils(timeUtils);
	    	if (fromTime.getTimes() > timeUtils.getTimes()) {
	    		calendar.add(Calendar.DAY_OF_YEAR, 1);
	    	}
    	
	    	long alarmTimeMillis = calendar.getTimeInMillis();
	    	long currentTimeMillis = System.currentTimeMillis();
	    	
	    	if (currentTimeMillis > toCalendar.getTimeInMillis()) {
	    		calendar.add(Calendar.DAY_OF_YEAR, 1);
	    	}
	    	
	    	alarmTimeMillis = calendar.getTimeInMillis();
	    	
			scheduleInstanceStateChange(context, timeUtils, alarmTimeMillis);
	    	
	    	Utils.writeLogToSdcard("setupAlarmInstance:"+calendar.getTime()+",type:"+timeUtils.getType());
		}
    }
    
    public static Intent createIntent(Context context, TimeUtils time) {
    	Intent intent = new Intent(context, AlarmStateManager.class);
    	intent.setAction(ALARM_START_UP_ACTION+"_"+time.getTotal()+"_"+time.getType());
    	intent.putExtra(ALARM_TIMEUTIL, time);
    	return intent;
    }
    
    public static void updateNextAlarmInstance(Context context, Intent intent) {
    	if (intent == null) {
    		return ;
    	}
    	
    	String action = intent.getAction();
    	
    	String[] info = action.split("_");
    	if (info == null || info.length != 3) {
    		return ;
    	}
    	
    	if (!ALARM_START_UP_ACTION.equals(info[0])) {
    		return ;
    	}
    	
    	TimeUtils timeUtils = (TimeUtils) intent.getSerializableExtra(ALARM_TIMEUTIL);
    	
    	Calendar calendar = createCalendarByTimeUtils(timeUtils);
    	long currentTimeMillis = System.currentTimeMillis();
    	long alarmTimeMillis = calendar.getTimeInMillis();
    	if (currentTimeMillis >= alarmTimeMillis) {
    		calendar.add(Calendar.DAY_OF_YEAR, 1);
    	}
    	
    	Utils.writeLogToSdcard("updateNextAlarmInstance:"+calendar.getTime()+",type:"+timeUtils.getType());
    	if (timeUtils.getType() == TimeUtils.ALARM_TYPE_START || timeUtils.getType() == TimeUtils.ALARM_TYPE_MIDDLE) {
    		DockCmdUtils.opendisturb();
    	} else if (timeUtils.getType() == TimeUtils.ALARM_TYPE_END) {
    		DockCmdUtils.closedisturb();
    	}

    	alarmTimeMillis = calendar.getTimeInMillis();
    	scheduleInstanceStateChange(context, timeUtils, alarmTimeMillis);
    }
    
	public static void scheduleInstanceStateChange(Context context, TimeUtils timeUtils, long alarmTimeMillis) {
		Intent i = createIntent(context, timeUtils);
    	PendingIntent operation = PendingIntent.getBroadcast(context, 0, i , PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (Utils.isKitKatOrLater()) {
			am.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, operation);
		} else {
			am.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, operation);
		}
	}
    
    public static Calendar createCalendarByTimeUtils(TimeUtils time) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
    	calendar.set(Calendar.MINUTE, time.getMinute());
    	calendar.set(Calendar.MILLISECOND, 0);
    	if (time.getType() == TimeUtils.ALARM_TYPE_START) {
    		calendar.set(Calendar.SECOND, 30);
    	} else {
    		calendar.set(Calendar.SECOND, 0);
    	}
    	return calendar;
    }
    
    public static void cancelAlarmByTimeUtils(Context context, TimeUtils time) {
    	AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	Intent intent = createIntent(context, time);
		PendingIntent operation = PendingIntent.getBroadcast(context, 0, intent , PendingIntent.FLAG_UPDATE_CURRENT);
    	am.cancel(operation);
    	
    	Utils.writeLogToSdcard("cancelAlarmByTimeUtils:"+time+",type:"+time.getType());
    }
    
    public static void cancelAlarmInstance(Context context, TimeBean bean) {
    	TimeUtils fromTime = bean.getFromTime();
    	TimeUtils toTime = bean.getToTime();
    	
    	Calendar fromCalendar = createCalendarByTimeUtils(fromTime);
    	Calendar toCalendar = createCalendarByTimeUtils(toTime);
    	if (fromCalendar.getTimeInMillis() >= toCalendar.getTimeInMillis()) {
    		toCalendar.add(Calendar.DAY_OF_YEAR, 1);
    	}
    	
    	long currentTimeMillis = System.currentTimeMillis();
    	if (currentTimeMillis >= fromCalendar.getTimeInMillis() && currentTimeMillis <= toCalendar.getTimeInMillis()) {
    		DockCmdUtils.closedisturb();
    	}
    	
    	List<TimeUtils> list = Utils.getAllTimeUtilsByTimeBean(bean);
    	for (int i = 0; i < list.size(); i++) {
			TimeUtils timeUtils = list.get(i);
			cancelAlarmByTimeUtils(context, timeUtils);
    	}
    }
    
}
