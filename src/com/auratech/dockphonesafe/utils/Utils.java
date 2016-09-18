package com.auratech.dockphonesafe.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

public class Utils {

	public static final boolean DEBUG = true;
	
	public interface IChildViewOnClick {
		public void onChildViewClick(View v);
	}
	
	/**
	 * Returns whether the SDK is KitKat or later
	 */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
    
    /**
	 * ��ȡʱ���
	 * 
	 * @param srcTime
	 *            ԭʼʱ��ֵ
	 * @param destTime
	 *            Ŀ��ʱ��ֵ
	 * @return ʱ���
	 */
	public static TimeUtils getTimeDifference(TimeUtils srcTime,
			TimeUtils destTime) {
		int resultHour = 0;
		int resultMinute = 0;

		int compare = srcTime.compareTo(destTime);
		if (compare < 0) {
			resultHour = -1 * compare / 60;
			resultMinute = -1 * compare % 60;
		} else if (compare == 0) {
			resultHour = 24;
			resultMinute = 0;
		} else {
			int tempTimes = 24 * 60 - compare;
			resultHour = tempTimes / 60;
			resultMinute = tempTimes % 60;
		}

		return new TimeUtils(resultHour, resultMinute,TimeUtils.ALARM_TYPE_START);
	}
    
    /**
	 * �ж�sTime��dTime�Ƿ���srcTime��destTime֮��
	 * 
	 * @param srcTime
	 *            ��ʼʱ��
	 * @param destTime
	 *            ��ֹʱ��
	 * @param sTime
	 *            ���ж���ʼʱ��
	 * @param sTime
	 *            ���ж���ֹʱ��
	 * @return trueΪ����֮�䣬false��������֮��
	 */
	public static boolean isInTimeDifference(TimeUtils srcTime,
			TimeUtils destTime, TimeUtils sTime, TimeUtils dTime) {
		boolean result = true;

		int srcTimes = srcTime.getTimes();
		int destTimes = destTime.getTimes();

		int sTimes = sTime.getTimes();
		int dTimes = dTime.getTimes();

		if (srcTimes < destTimes) {
			if (sTimes < dTimes && (sTimes >= destTimes || dTimes <= srcTimes)) {
				result = false;
			} else if (sTimes > dTimes && sTimes >= destTimes
					&& dTimes <= srcTimes) {
				result = false;
			}
		} else if (srcTimes > destTimes) {
			if (sTimes < dTimes && sTimes >= destTimes && dTimes <= srcTimes) {
				result = false;
			}
		}

		return result;
	}

	/**
	 * ��ȡʱ�������� ���ʱ��ΪTIME_MAXСʱ
	 * 
	 * @param time
	 * @return
	 */
	public static int getIntervalsCount(TimeUtils time) {
		int hour = time.getHour();
		int minute = time.getMinute();
		int count = hour / TimeUtils.TIME_MAX;
		int n =	hour % TimeUtils.TIME_MAX;
		if (count > 0 && n == 0 && minute == 0) {
			count--;
		}
		return count;
	}
	
	/**
	 * ��ȡ��ʼʱ��ͽ���ʱ�������еļ��ʱ���
	 * @param fromTime
	 * @param toTime
	 * @return
	 */
	public static List<TimeUtils> getAllTimeUtilsByTimeUtils(TimeUtils fromTime, TimeUtils toTime) {
		TimeUtils middleTime = getTimeDifference(fromTime, toTime);
		int count = getIntervalsCount(middleTime);
		
		ArrayList<TimeUtils> list = new ArrayList<TimeUtils>();
		fromTime.setType(TimeUtils.ALARM_TYPE_START);
		list.add(fromTime);
		
		for (int i = 0; i < count; i++) {
			int hour = (fromTime.getHour() + TimeUtils.TIME_MAX *(i+1)) % 24;
			TimeUtils timeUtils = new TimeUtils(hour, fromTime.getMinute(), TimeUtils.ALARM_TYPE_MIDDLE);
			list.add(timeUtils);
		}
		
		if (fromTime.compareTo(toTime) != 0) {
			toTime.setType(TimeUtils.ALARM_TYPE_END);
			list.add(toTime);
		}
		
		return list;
	}
	
	public static List<TimeUtils> getAllTimeUtilsByTimeBean(TimeBean bean) {
		List<TimeUtils> list = getAllTimeUtilsByTimeUtils(bean.getFromTime(), bean.getToTime());
		return list;
	}
	
	
	public static void writeLogToSdcard(String msg) {
		if (!Utils.DEBUG) {
			return;
		}
		
		Log.d("TAG", msg);
		msg = msg+"\n";
		try {
			File file = new File(Environment.getExternalStorageDirectory().getPath(), "alarmlog.txt");
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(msg.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeLogToSdcard(String fileName, String msg) {
		if (!Utils.DEBUG) {
			return;
		}
		
		Log.d("TAG", msg);
		msg = msg+"\n";
		try {
			File file = new File(Environment.getExternalStorageDirectory().getPath(), fileName);
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write(msg.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void getPhoneContactsData(Context context, List<BaseListBean> list) {
		// content://com.android.contacts/data/callables?directory=0&address_book_index_extras=true&remove_duplicate_entries=true
		Uri uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "callables");
		Builder builder = uri.buildUpon();
		builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, "0");
		builder.appendQueryParameter("address_book_index_extras", "true");
		builder.appendQueryParameter("remove_duplicate_entries", "true");
		final Uri useUri = builder.build();
//		Cursor cursor = context.getContentResolver().query(useUri, PhoneQuery.PROJECTION_PRIMARY, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY,
//				new CancellationSignal());
		Cursor cursor = context.getContentResolver().query(useUri, PhoneQuery.PROJECTION_PRIMARY, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
		
		if (cursor != null) {
			
			Bundle bundle = cursor.getExtras();
			if (bundle != null) {
				if (bundle.containsKey("address_book_index_titles")) {
					String[] stringArray = bundle.getStringArray("address_book_index_titles");
					for (int i = 0; i < stringArray.length; i++) {
						Log.d("TAG", "address_book_index_titles:"+stringArray[i]);
					}
				}
			}
			
			Log.d("TAG", "count:"+cursor.getCount());
			while (cursor.moveToNext()) {
				String name = cursor.getString(PhoneQuery.PHONE_DISPLAY_NAME);
				String number = cursor.getString(PhoneQuery.PHONE_NUMBER);
				String sort_key = cursor.getString(PhoneQuery.PHONE_SORT_KEY);
				
				Log.d("TAG", "count:"+name+",number:"+number);
				BaseListBean bean = new BaseListBean();
				bean.setName(name);
				bean.setNumber(number);
				bean.setPhone(number);
				bean.setSort_key(sort_key);
				if (!list.contains(bean)) {
					list.add(bean);
				}
			}
			
			cursor.close();
			cursor = null;
		}
	}
	
	public static Cursor getPhoneContactsCursor(Context context) {
		Uri uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "callables");
		Builder builder = uri.buildUpon();
		builder.appendQueryParameter(ContactsContract.DIRECTORY_PARAM_KEY, "0");
		builder.appendQueryParameter("address_book_index_extras", "true");
		builder.appendQueryParameter("remove_duplicate_entries", "true");
		final Uri useUri = builder.build();
		Cursor cursor = context.getContentResolver().query(useUri, PhoneQuery.PROJECTION_PRIMARY, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY,
				new CancellationSignal());
		return cursor;
	}
	
	public static class PhoneQuery {
		private static final String[] PROJECTION_PRIMARY = new String[] {
			ContactsContract.CommonDataKinds.Phone._ID, // 0
			ContactsContract.CommonDataKinds.Phone.TYPE, // 1
			ContactsContract.CommonDataKinds.Phone.LABEL, // 2
			ContactsContract.CommonDataKinds.Phone.NUMBER, // 3
			ContactsContract.CommonDataKinds.Phone.CONTACT_ID, // 4
			ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY, // 5
			ContactsContract.CommonDataKinds.Phone.PHOTO_ID, // 6
			ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY, // 7
			ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY, //8
		};
		
		public static final int PHONE_ID           	= 0;
        public static final int PHONE_TYPE         	= 1;
        public static final int PHONE_LABEL        	= 2;
        public static final int PHONE_NUMBER       	= 3;
        public static final int PHONE_CONTACT_ID   	= 4;
        public static final int PHONE_LOOKUP_KEY   	= 5;
        public static final int PHONE_PHOTO_ID     	= 6;
        public static final int PHONE_DISPLAY_NAME 	= 7;
        public static final int PHONE_SORT_KEY	 	= 8;
	}
}
