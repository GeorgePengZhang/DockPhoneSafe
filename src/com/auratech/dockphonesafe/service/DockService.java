package com.auratech.dockphonesafe.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.auratech.dockphonesafe.utils.BlackListBean;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.DockCmdUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.utils.WhiteListBean;
import com.litesuits.orm.db.assit.QueryBuilder;

public class DockService extends Service {

	public static final String ACTION_SHOW_DIALOG = "com.auratech.dockphonesafe.showdialog";
	public static final String ACTION_HIDE_DIALOG = "com.auratech.dockphonesafe.hidedialog";
	
	private Handler mHandler;
	private String mResult;
	private HandlerThread mHandlerThread;
	private boolean existUSBNode;
	private ArrayList<WhiteListBean> whiteList;
	
	private static List<OnDockInfoListener> mListener = new ArrayList<OnDockInfoListener>();
	
	public static void addOnDockInfoListener(OnDockInfoListener onDockInfoListener) {
		mListener.add(onDockInfoListener);
	}
	
	public static void removeOnDockInfoListener(OnDockInfoListener onDockInfoListener) {
		if (mListener.contains(onDockInfoListener)) {
			mListener.remove(onDockInfoListener);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread("alarm_info");
			mHandlerThread.start();
			mHandler = new Handler(mHandlerThread.getLooper());
			
			mHandler.removeCallbacks(mRunnable);
			mHandler.post(mRunnable);
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHandler != null) {
			mHandler.removeCallbacks(mRunnable);
			mHandler = null;
		}
		
		if (mHandlerThread != null) {
			mHandlerThread = null;
		}
	}
	
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			boolean existNode = Dock.isExistUSBNode();
			if (existNode && existNode != existUSBNode) {
				Toast.makeText(DockService.this, "平板放入底座", Toast.LENGTH_LONG).show();
				
				if (isDisturb) {
					DockCmdUtils.opendisturb();
				} else {
					DockCmdUtils.closedisturb();
				}
				
				setSyncAllList(true);
				Dock.syncwhitelist();
			} else if (!existNode && existNode != existUSBNode){
				Toast.makeText(DockService.this, "平板取出底座", Toast.LENGTH_LONG).show();
				//同步结束
				syncflag = false;
			}
			
			existUSBNode = existNode;
			
			String result = Dock.getInfo();
			
			if (result != null && !result.equals(mResult)) {
				mResult = result;
				Utils.writeLogToSdcard("dock.txt", mResult);
				for (int i = 0; i < mListener.size(); i++) {
					mListener.get(i).onUpdated(result);
				}
				syncList(result);
				Log.d("TAG", "mRunnable:"+syncflag);
				if (syncflag) {
					Intent intent = new Intent(ACTION_SHOW_DIALOG);
					DockService.this.sendBroadcast(intent);
				} else {
					Intent intent = new Intent(ACTION_HIDE_DIALOG);
					DockService.this.sendBroadcast(intent);
				}
			}

			mHandler.removeCallbacks(mRunnable);
			mHandler.postDelayed(mRunnable, 300);
		}
	};
	private int position;
	private String phoneId;
	private int size;
	private boolean syncflag;
	private ArrayList<BlackListBean> blackList;
	private static boolean isSyncAllList = false;
	private static boolean isDisturb = false;
	
	public static interface OnDockInfoListener {
		public void onUpdated(String result);
	}
	
	public static void setSyncAllList(boolean syncAllList) {
		isSyncAllList = syncAllList;
	}
	
	public static void setDisturb(boolean disturb) {
		isDisturb = disturb;
	}
	
	private void syncList(String info) {
		String[] syncResult = Dock.getPhoneSyncResult(info);
		if (syncResult == null) {
			return ;
		}
		
		String type = syncResult[0];
		String id = syncResult[1];
		
		Log.d("TAG", "syncList:"+type+",id:"+id);
		
		if ("0".equals(type)) {
			syncBlackList(id);
		} else if ("1".equals(type)) {
			syncWhiteList(id);
		}
	}
	
	//同步白名单
	private void syncWhiteList(String id) {
		Log.d("TAG", "syncWhiteList:"+id+",size:"+size+",position:"+position+",phoneId:"+phoneId);
		
		if ("0000".equals(id)) {
			QueryBuilder<WhiteListBean> qb = new QueryBuilder<WhiteListBean>(WhiteListBean.class)
					.whereNoEquals(WhiteListBean.ENABLE, "false")
					.orderBy("_id");
			whiteList = DbUtils.getInstance(DockService.this).getDbInstance().query(qb);
			
			String string = String.valueOf(whiteList);
			Log.d("TAG", "string:"+string);
			
			size = whiteList.size();
			if (whiteList.size() > 0) {
				position = 0;
				WhiteListBean bean = whiteList.get(position);
				phoneId = DockCmdUtils.getListID(bean.getId());
				DockCmdUtils.addWhiteList(bean.getId(), bean.getPhone());
				syncflag = true;
			}
		} else if (syncflag) {
			if (phoneId.equals(id)) {
				if (++position < size) {
					WhiteListBean bean = whiteList.get(position);
					phoneId = DockCmdUtils.getListID(bean.getId());
					DockCmdUtils.addWhiteList(bean.getId(), bean.getPhone());
				} else {
					//同步结束
					syncflag = false;
					if (isSyncAllList) {
						Dock.syncblacklist();
						isSyncAllList = false;
					}
				}
			} else {
				//同步失败
				syncflag = false;
			}
		}
	}
	
	//同步黑名单
	private void syncBlackList(String id) {
		if ("0000".equals(id)) {
			QueryBuilder<BlackListBean> qb = new QueryBuilder<BlackListBean>(BlackListBean.class)
					.whereNoEquals(BlackListBean.ENABLE, "false")
					.orderBy("_id");
			blackList = DbUtils.getInstance(DockService.this).getDbInstance().query(qb);
			size = blackList.size();
			if (blackList.size() > 0) {
				position = 0;
				BlackListBean bean = blackList.get(position);
				phoneId = DockCmdUtils.getListID(bean.getId());
				DockCmdUtils.addBlackList(bean.getId(), bean.getPhone());
				syncflag = true;
			}
		} else if (syncflag) {
			if (phoneId.equals(id)) {
				if (++position < size) {
					BlackListBean bean = blackList.get(position);
					phoneId = DockCmdUtils.getListID(bean.getId());
					DockCmdUtils.addBlackList(bean.getId(), bean.getPhone());
				} else {
					//同步结束
					syncflag = false;
				}
			} else {
				//同步失败
				syncflag = false;
			}
		}
	}
}
