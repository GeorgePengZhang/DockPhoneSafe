package com.auratech.dockphonesafe.bean;

import java.io.Serializable;

import com.auratech.dockphonesafe.utils.TimeUtils;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

@Table("alarm")
public class TimeBean implements Comparable<TimeBean>, Serializable {
	private static final long serialVersionUID = 3401670914984500511L;
	@PrimaryKey(AssignType.AUTO_INCREMENT)
	@Column("_id")
	private int id;
	private TimeUtils fromTime; //启动时间
	private TimeUtils toTime;	//结束时间
	private TimeUtils intervalsTime; //间隔时间
	@Column("count")
	private int intervalsCount; //间隔次数
	private boolean isOpen; //是否启动免打扰功能
	private boolean blacklistEnabled; //是否启动黑名单屏蔽功能
	private boolean ringEnabled; //是否启动底座响铃
	
	public TimeBean() {
	}
	
	public TimeBean(TimeUtils fromTime, TimeUtils toTime,TimeUtils intervalsTime, int count, boolean isOpen, boolean blacklistEnabled, boolean ringEnabled) {
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.intervalsTime = intervalsTime;
		this.intervalsCount = count;
		this.isOpen = isOpen;
		this.blacklistEnabled = blacklistEnabled;
		this.ringEnabled = ringEnabled;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public TimeUtils getFromTime() {
		return fromTime;
	}
	
	public void setFromTime(TimeUtils mFromTime) {
		this.fromTime = mFromTime;
	}
	
	public TimeUtils getToTime() {
		return toTime;
	}
	
	public void setToTime(TimeUtils mToTime) {
		this.toTime = mToTime;
	}
	
	public TimeUtils getIntervalsTime() {
		return intervalsTime;
	}

	public void setIntervalsTime(TimeUtils intervalsTime) {
		this.intervalsTime = intervalsTime;
	}

	public int getIntervalsCount() {
		return intervalsCount;
	}

	public void setIntervalsCount(int intervalsCount) {
		this.intervalsCount = intervalsCount;
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	
	public boolean isBlacklistEnabled() {
		return blacklistEnabled;
	}

	public void setBlacklistEnabled(boolean blacklistEnabled) {
		this.blacklistEnabled = blacklistEnabled;
	}

	public boolean isRingEnabled() {
		return ringEnabled;
	}

	public void setRingEnabled(boolean ringEnabled) {
		this.ringEnabled = ringEnabled;
	}

	public String info() {
		return fromTime+"-"+toTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(info()).append(",isOpen:").append(isOpen);
		return builder.toString();
	}

	@Override
	public int compareTo(TimeBean another) {
		return fromTime.compareTo(another.fromTime);
	}
}
