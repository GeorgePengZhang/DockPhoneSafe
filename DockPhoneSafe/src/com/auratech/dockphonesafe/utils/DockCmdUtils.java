package com.auratech.dockphonesafe.utils;

import java.sql.Time;
import java.util.Locale;

import com.auratech.dockphonesafe.service.Dock;
import com.auratech.dockphonesafe.service.DockService;

public class DockCmdUtils {

	/**
	 * 添加白名单
	 * @param id 
	 * @param phone
	 */
	public static void addWhiteList(int id, String phone) {
		String num = getListID(id)+phone;
		Dock.addwhitelist(num);
		Utils.writeLogToSdcard("addWhiteList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	/**
	 * 添加黑名单
	 * @param id
	 * @param phone
	 */
	public static void addBlackList(int id, String phone) {
		String num = getListID(id)+phone;
		Dock.addblacklist(num);
		Utils.writeLogToSdcard("addBlackList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	/**
	 * 删除白名单
	 * @param id
	 */
	public static void removeWhiteList(int id) {
		String num = getListID(id);
		Dock.delwhitelist(num);
		Utils.writeLogToSdcard("removeWhiteList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	/**
	 * 删除黑名单 
	 * @param id
	 */
	public static void removeBlackList(int id) {
		String num = getListID(id);
		Dock.delblacklist(num);
		Utils.writeLogToSdcard("removeBlackList"+",num:"+num+",time:"+new Time(System.currentTimeMillis()));
	}
	
	/**
	 * 打开免打扰
	 */
	public static void opendisturb() {
		Utils.writeLogToSdcard("opendisturb"+new Time(System.currentTimeMillis()));
		DockService.setDisturb(true);
		Dock.opendisturb();
	}
	
	/**
	 * 关闭免打扰
	 */
	public static void closedisturb() {
		Utils.writeLogToSdcard("closedisturb"+new Time(System.currentTimeMillis()));
		DockService.setDisturb(false);
		Dock.closedisturb();
	}
	
	/**
	 * 同步黑白名单
	 */
	public static void syncAllList() {
		DockService.setSyncAllList(true);
		Dock.syncwhitelist();
	}
	
	/**
	 * 同步白名单
	 */
	public static void syncWhiteList() {
		DockService.setSyncAllList(false);
		Dock.syncwhitelist();
	}
	
	/**
	 * 同步黑名单
	 */
	public static void syncBlackList() {
		DockService.setSyncAllList(false);
		Dock.syncblacklist();
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
