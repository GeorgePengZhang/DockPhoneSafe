package com.auratech.dockphonesafe.utils;

import java.io.Serializable;

import com.litesuits.orm.db.annotation.Ignore;

public class TimeUtils implements Serializable, Comparable<TimeUtils> {

	private static final long serialVersionUID = -6923743516209320494L;
	@Ignore
	public static final int TIME_MAX = 3;
	public final static int ALARM_TYPE_START = 0; // 启动节点
	public final static int ALARM_TYPE_MIDDLE = 1; // 间隔节点
	public final static int ALARM_TYPE_END = 2; // 结束节点
	public static final String HOUR = "hour";
	public static final String MINUTE = "minute";
	public static final String TOTAL = "total";

	private int id;
	private int hour;
	private int minute;
	private int total;
	private int type;
	private boolean enable;

	public TimeUtils() {
		this(0, 0, ALARM_TYPE_START);
	}

	public TimeUtils(int hour, int minute, int type) {
		this.hour = hour;
		this.minute = minute;
		this.total = getTimes();
		this.type = type;
		this.enable = true;
	}

	public void parse(String msg) {
		String[] split = msg.split(":");
		hour = Integer.valueOf(split[0]);
		minute = Integer.valueOf(split[1]);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTimes() {
		return hour * 60 + minute;
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	/**
	 * 比较两个时间的大小
	 * 
	 * @param time
	 *            被比较的时间
	 * @return 大于0表示本时间大于被比较的时间，小于0表示本时间小于被比较的时间，等于0表示本时间等于被比较的时间，具体的值为它们之间的时间差值
	 */
	@Override
	public int compareTo(TimeUtils another) {
		int locTimes = this.getTimes();
		int destTimes = another.getTimes();

		return (locTimes - destTimes);
	}

	@Override
	public String toString() {
		return String.format("%02d:%02d", hour, minute);
	}
}
