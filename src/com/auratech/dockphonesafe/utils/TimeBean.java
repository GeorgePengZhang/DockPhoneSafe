package com.auratech.dockphonesafe.utils;

import java.io.Serializable;

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
	private boolean isOpen; //是否启动
	
	public TimeBean() {
	}
	
	public TimeBean(TimeUtils fromTime, TimeUtils toTime,TimeUtils intervalsTime, int count, boolean isOpen) {
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.intervalsTime = intervalsTime;
		this.intervalsCount = count;
		this.isOpen = isOpen;
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
	
	public String info() {
		return fromTime+"-"+toTime+"   间隔时间:"+intervalsTime+",间隔次数:"+intervalsCount;
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
