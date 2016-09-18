package com.auratech.dockphonesafe.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.auratech.dockphonesafe.utils.Utils;

import android.text.TextUtils;
import android.util.Log;

public class Dock {
	//usb驱动通信节点
	public static final String PATH = "/sys/class/dock/usbdock/dock_file/dock_intf";

	static String[] CMD = new String[3];

	public static class CmdRet {
		public int retCode;
		public String retStr;
	}

	public class DockInfo {
		public char hook;
		public char handle;
		public char spk;
		public char vol;
		public char pop;
		public char is_call_in;
		public String cid_num;
		public String version;
		public char mIsFlash;
		public char mIsMute;
		public long mOutCallTime;
		public String mOutCallNum;

		@Override
		public String toString() {
			return "hook = " + hook + "\nvol=" + vol + "\nis_call_in="
					+ is_call_in + "\ncid_num=" + cid_num + "\nmOutCallTime = "
					+ mOutCallTime + "\nmIsMute = " + mIsMute
					+ "\nmOutCallNum = " + mOutCallNum + "\nversion=" + version;
		}

	}

	public static CmdRet busyboxCmd(String cmd) {
		CMD[0] = "/system/bin/sh";
		CMD[1] = "-c";
		CMD[2] = "busybox " + cmd;
		Process proc = null;
		
		InputStreamReader inputStreamReader = null;
		BufferedReader reader = null;
		
		CmdRet cmdret = new CmdRet();
		try {
			proc = Runtime.getRuntime().exec(CMD);
			
			inputStreamReader = new InputStreamReader(proc.getInputStream());
			reader = new BufferedReader(inputStreamReader);
			cmdret.retStr = reader.readLine();
			cmdret.retCode = proc.waitFor();

		} catch (Exception ex) {
			Log.d("bunchen", "busyboxCmd", ex);
		} finally {
			try {
				if (reader != null) {
					reader.close();
					reader = null;
				}
				
				if (inputStreamReader != null) {
					inputStreamReader.close();
					inputStreamReader = null;
				}
				
			} catch (Exception e) {
				Log.d("bunchen", "busyboxCmd", e);
			}
		}
		return cmdret;
	}

	/** send vol */
	public boolean setVol(int v) {
		CmdRet ret = busyboxCmd("echo \"v" + v + "\" > " + PATH);
		return ret.retCode == 0;
	}

	/** holddown */
	public boolean hangdown() {

		CmdRet ret = busyboxCmd("echo \"r0" + "\" > " + PATH);
		return ret.retCode == 0;
	}

	/** holdoff */
	public boolean hookoff() {
		CmdRet ret = busyboxCmd("echo \"r1" + "\" > " + PATH);
		return ret.retCode == 0;
	}

	/** send number */
	public boolean sendTelnum(String num) {
		CmdRet ret = busyboxCmd("echo \"t" + num + "\" > " + PATH);
		return ret.retCode == 0;
	}

	/** send infunction */
	public boolean sendKey(char ch) {
		CmdRet ret = busyboxCmd("echo \"k" + ch + "\" > " + PATH);
		return ret.retCode == 0;
	}

	/** send mute ? 1:0 */
	public boolean sendMute(char ch) {
		CmdRet ret = busyboxCmd("echo \"m" + ch + "\" > " + PATH);
		return ret.retCode == 0;
	}

	/** holddown */
	public boolean ack() {

		CmdRet ret = busyboxCmd("echo \"p" + "\" > " + PATH);
		return ret.retCode == 0;
	}

	public boolean copyfile() {
		String cmd = "/system/bin/aura_copy_file set";

		CmdRet ret = busyboxCmd(cmd);
		return ret.retCode == 0;
	}

	public boolean queryDockOutTimeNum() {
		String cmd = "echo \"T\" > " + PATH;
		CmdRet ret = busyboxCmd(cmd);
		return ret.retCode == 0;
	}
	
	/**add steven----------------------------------------------------------->*/
	public static boolean addwhitelist(String num)
	{
		CmdRet ret = busyboxCmd("echo \"lw"+num+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean addblacklist(String num)
	{
		CmdRet ret = busyboxCmd("echo \"lb"+num+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean syncwhitelist()
	{
		Utils.writeLogToSdcard("syncwhitelist");
		CmdRet ret = busyboxCmd("echo \"lws"+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean syncblacklist()
	{
		Utils.writeLogToSdcard("syncblacklist");
		CmdRet ret = busyboxCmd("echo \"lbs"+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean delwhitelist(String num)
	{
		CmdRet ret = busyboxCmd("echo \"ldw"+num+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean delblacklist(String num)
	{
		CmdRet ret = busyboxCmd("echo \"ldb"+num+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean opendisturb()
	{
		CmdRet ret = busyboxCmd("echo \"lde"+"\" > "+PATH);
		return ret.retCode==0;
	}
	public static boolean closedisturb()
	{
		CmdRet ret = busyboxCmd("echo \"ldd"+"\" > "+PATH);
		return ret.retCode==0;
	}
	
	public static boolean isExistUSBNode() {
		return new File(PATH).exists();
	}
	
	public static String getInfo() {
		CmdRet ret = busyboxCmd("cat " + PATH);
		String str = ret.retCode == 0 ? ret.retStr : null;
		
		if (str == null || str.charAt(0) != 'i') {
			return null;
		}
		
		return str.trim();
	}
	
	public static String[] getPhoneSyncResult(String info) {
		if (TextUtils.isEmpty(info)) {
			return null;
		}
		
		int index = info.indexOf("L")+1;
		if (index < info.length()) {
			String type = info.substring(index, index+1);
			String id = info.substring(index+1, info.length());
			
			return new String[] {type, id.trim()}; 
		}
		
		
		return null;
	}
	/**end steven--------------------------------------------------->*/

	/**
	 * get information i 000
	 * */
	public DockInfo getDockInfo() {
		CmdRet ret = busyboxCmd("cat " + PATH);
		String str = ret.retCode == 0 ? ret.retStr : null;

		// i00030000Tv20141121V1
		// i10130000T0000123456789v20141121V1
		// String str = "i1100011822T0000913066868755v20141121V1";
		// Log.e("liangbo","getDockInfo str = " +str);
		if (str == null || str.charAt(0) != 'i') {
			return null;
		}

		DockInfo info = new DockInfo();

		info.hook = str.charAt(1);
		info.handle = str.charAt(2);
		info.spk = str.charAt(3);
		info.vol = str.charAt(4);
		info.mIsMute = str.charAt(5);
		info.mIsFlash = str.charAt(6);
		info.pop = str.charAt(7);
		info.is_call_in = str.charAt(8);
		int vs = str.indexOf('v', 9);
		int ts = str.indexOf('T', 9);
		/*
		 * if(vs>=7) { info.cid_num = str.substring(7,vs); if(vs+1<str.length())
		 * { info.version = str.substring(vs+1); }else { info.version =
		 * "unknown" ; } }else { info.cid_num = str.substring(7); info.version =
		 * "unknown" ; }
		 */

		String cid_num = null;
		String version = null;
		String timeAndNumStr = null;
		if (vs >= 10) {

			if (ts < 0) {
				cid_num = str.substring(9, vs);
				timeAndNumStr = "0000";
			}
			if (vs > ts) {

				if (ts > 9) {
					cid_num = str.substring(9, ts);
					timeAndNumStr = str.substring(ts + 1, vs);
				} else {
					cid_num = null;
					timeAndNumStr = str.substring(ts + 1, vs);
				}
				if (vs + 1 < str.length()) {
					version = str.substring(vs + 1);
				} else {
					version = "unknown";
				}
			} else {
				cid_num = str.substring(9, vs);
				version = str.substring(vs + 1, ts);
				if (ts + 1 < str.length()) {
					timeAndNumStr = str.substring(ts + 1);
				} else {
					timeAndNumStr = "0000";
				}
			}
		} else {
			cid_num = str.substring(9);
			timeAndNumStr = "0000";
			version = "unknown";
		}
		Log.e("Dock", "updateDockInfo cid_num = " + cid_num);
		Log.e("Dock", "updateDockInfo version = " + version);
		Log.e("Dock", "updateDockInfo timeAndNumStr = " + timeAndNumStr);
		if (!TextUtils.isEmpty(timeAndNumStr) && timeAndNumStr.length() >= 4) {
			int timeM = Integer.valueOf(timeAndNumStr.substring(0, 2));
			int timeS = Integer.valueOf(timeAndNumStr.substring(2, 4));
			info.mOutCallTime = timeM * 60000 + timeS * 1000;
			if (timeAndNumStr.length() > 4) {
				String num = timeAndNumStr.substring(4, timeAndNumStr.length());
				info.mOutCallNum = num;
				Log.e("Dock", "updateDockInfo num = " + num);
			} else {
				info.mOutCallNum = null;
			}
		} else {
			info.mOutCallTime = 0;
			info.mOutCallNum = null;
		}
		Log.e("Dock", "updateDockInfo info.mOutCallNum = " + info.mOutCallNum);
		if (!TextUtils.isEmpty(cid_num)) {
			int Vs = cid_num.indexOf('V', 0);
			if (Vs > 0) {
				cid_num = null;
			}
		}
		if (info.is_call_in == '1') {
			info.cid_num = cid_num;
		} else {
			info.cid_num = info.mOutCallNum;
		}

		Log.e("Dock", "updateDockInfo info.cid_num = " + info.cid_num);
		info.version = version;
		return info;
	}

}
