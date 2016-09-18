package com.auratech.dockphonesafe.utils;

import java.sql.Time;
import java.util.Locale;

import com.auratech.dockphonesafe.service.Dock;
import com.auratech.dockphonesafe.service.DockService;

public class DockCmdUtils {

	public static void addWhiteList(int id, String phone) {
		String num = getListID(id)+phone;
		Dock.addwhitelist(num);
		Utils.writeLogToSdcard("addWhiteList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	public static void addBlackList(int id, String phone) {
		String num = getListID(id)+phone;
		Dock.addblacklist(num);
		Utils.writeLogToSdcard("addBlackList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	public static void removeWhiteList(int id) {
		String num = getListID(id);
		Dock.delwhitelist(num);
		Utils.writeLogToSdcard("removeWhiteList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	public static void removeBlackList(int id) {
		String num = getListID(id);
		Dock.delblacklist(num);
		Utils.writeLogToSdcard("removeBlackList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	public static void opendisturb() {
		Utils.writeLogToSdcard("opendisturb"+new Time(System.currentTimeMillis()));
		DockService.setDisturb(true);
		Dock.opendisturb();
	}
	
	public static void closedisturb() {
		Utils.writeLogToSdcard("closedisturb"+new Time(System.currentTimeMillis()));
		DockService.setDisturb(false);
		Dock.closedisturb();
	}

	/**
	 * 获取名单编号
	 * @param id
	 * @return
	 */
	public static String getListID(int id) {
		return String.format(Locale.getDefault(), "%04d", id);
	}
}
