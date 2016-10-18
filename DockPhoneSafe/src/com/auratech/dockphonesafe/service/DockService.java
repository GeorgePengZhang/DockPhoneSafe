package com.auratech.dockphonesafe.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.fragment.SettingsFragment;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.DockCmdUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.view.MyDialog;
import com.litesuits.orm.db.assit.QueryBuilder;

public class DockService extends Service {
	
	public static final String ACTION_DOCKCHANGED = "com.auratech.dockphone.changed";
	private static final String SYNC_START_FLAG = "1000";
	
	private Handler mHandler;
	private String mResult;
	private HandlerThread mHandlerThread;
	private boolean existUSBNode;
	private ArrayList<WhiteListBean> whiteList;
	
	private static List<OnDockInfoListener> mListener = new ArrayList<OnDockInfoListener>();
	
	public static void addOnDockInfoListener(OnDockInfoListener onDockInfoListener) {
		if (!mListener.contains(onDockInfoListener)) {
			mListener.add(onDockInfoListener);
		}
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
				Intent intent = new Intent(ACTION_DOCKCHANGED);
				LocalBroadcastManager.getInstance(DockService.this).sendBroadcast(intent);
				
				if (isDisturb) {
					DockCmdUtils.opendisturb();
				} else {
					DockCmdUtils.closedisturb();
				}
				
				if (isBlackList) {
					DockCmdUtils.openblacklist();
				} else {
					DockCmdUtils.closedisturb();
				}
				
				if (isRing) {
					DockCmdUtils.openRing();
				} else {
					DockCmdUtils.closeRing();
				}
				
				DockCmdUtils.syncAllList();
			} else if (!existNode && existNode != existUSBNode){
				Toast.makeText(DockService.this, "平板取出底座", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(ACTION_DOCKCHANGED);
				LocalBroadcastManager.getInstance(DockService.this).sendBroadcast(intent);
				
				if (syncflag) {
					Toast.makeText(DockService.this, "名单同步失败!", Toast.LENGTH_LONG).show();
					hideWaitingDialog();
				}
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
					showWaitingDialog();
				} else {
					hideWaitingDialog();
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
	private static boolean isDisturb = SettingsFragment.TIME_OPENED;
	private static boolean isBlackList = SettingsFragment.BLACKLIST_OPENED;
	private static boolean isRing = SettingsFragment.RING_OPENED;
	
	public static interface OnDockInfoListener {
		public void onUpdated(String result);
	}
	
	/**
	 * 设置是否全部同步还是同步黑名单或者白名单
	 * @param syncAllList
	 */
	public static void setSyncAllList(boolean syncAllList) {
		isSyncAllList = syncAllList;
	}
	
	/**
	 * 设置免打扰的状态
	 * @param disturb
	 */
	public static void setDisturb(boolean disturb) {
		isDisturb = disturb;
	}
	
	/**
	 * 设置黑名单功能是否开启
	 * @param enabled
	 */
	public static void setBlackList(boolean enabled) {
		isBlackList = enabled;
	}
	
	/**
	 * 设置底座响铃是否开启
	 * @param enabled
	 */
	public static void setRing(boolean enabled) {
		isRing = enabled;
	}
	
	
	/**
	 * 同步名单
	 * @param info
	 */
	private void syncList(String info) {
		String[] syncResult = Dock.getPhoneSyncResult(info);
		if (syncResult == null) {
			return ;
		}
		
		String type = syncResult[0];
		String status = syncResult[1];
		String id = syncResult[2];
		
		Dock.parseStatus(status);
		Utils.writeLogToSdcard("dock.txt", "syncList:"+type+",id:"+id+",status:"+status);
		
		syncBlackList(type, id);
		syncWhiteList(type, id);
	}
	
	/** 同步白名单
	 * @param id 
	 */
	private void syncWhiteList(String type, String id) {
		Log.d("TAG", "syncWhiteList:"+id+",size:"+size+",position:"+position+",phoneId:"+phoneId);
		
		if (Dock.RESULT_SYNC_WHITE_LIST.equals(type) && SYNC_START_FLAG.equals(id)) {
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
		} else if (syncflag && Dock.RESULT_ADD_WHITE_LIST.equals(type)) {
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
					Toast.makeText(DockService.this, "白名单同步成功!", Toast.LENGTH_LONG).show();
				}
			} else {
				//同步失败
				syncflag = false;
				Toast.makeText(DockService.this, "白名单同步失败!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	/** 同步黑名单
	 * @param id
	 */
	private void syncBlackList(String type, String id) {
		if (Dock.RESULT_SYNC_BLACK_LIST.equals(type) && SYNC_START_FLAG.equals(id)) {
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
		} else if (syncflag && Dock.RESULT_ADD_BLACK_LIST.equals(type)) {
			if (phoneId.equals(id)) {
				if (++position < size) {
					BlackListBean bean = blackList.get(position);
					phoneId = DockCmdUtils.getListID(bean.getId());
					DockCmdUtils.addBlackList(bean.getId(), bean.getPhone());
				} else {
					//同步结束
					syncflag = false;
					Toast.makeText(DockService.this, "黑名单同步成功!", Toast.LENGTH_LONG).show();
				}
			} else {
				//同步失败
				syncflag = false;
				Toast.makeText(DockService.this, "黑名单同步失败!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private MyDialog waitingDialog;
	
	private void showWaitingDialog() {
		if (waitingDialog == null) {
			waitingDialog = new MyDialog(DockService.this);
			waitingDialog.setCancelable(false);
			waitingDialog.setCustomContentView(R.layout.dialog_sync_layout);
			waitingDialog.showCustomTitle(false);
			waitingDialog.showCustomButtonGroup(false);
			waitingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		
		if (Utils.isTopApp(DockService.this, getPackageName())) {
			waitingDialog.show();
		} else {
			waitingDialog.cancel();
		}
	}	
	
	private void hideWaitingDialog() {
		if (waitingDialog != null && waitingDialog.isShowing()) {
			waitingDialog.cancel();
		}
	}
}
