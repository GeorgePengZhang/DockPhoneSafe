package com.auratech.dockphonesafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.auratech.dockphonesafe.adapter.ContactListAdapter;
import com.auratech.dockphonesafe.adapter.ListAdapter;
import com.auratech.dockphonesafe.bean.BaseListBean;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.service.Dock;
import com.auratech.dockphonesafe.service.DockService;
import com.auratech.dockphonesafe.service.DockService.OnDockInfoListener;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.DockCmdUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.utils.Utils.IChildViewOnClick;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

public class WhiteListActivity extends Activity implements OnClickListener, IChildViewOnClick, OnDockInfoListener {

	private static final int OPERATOR_NONE = 0;
	private static final int OPERATOR_ADD = 1;
	private static final int OPERATOR_REMOVE = 2;
	
	private List<BaseListBean> mContactListBean;
	private ContactListAdapter contactAdapter;
	private AlertDialog contactDialog;
	private AlertDialog customDialog;
	private ProgressDialog waitingDialog;
	private List<WhiteListBean> mListBean;
	private ListAdapter<WhiteListBean> listAdapter;
	private ListView mListView;
	private LiteOrm listOrm;

	private int operatorType = OPERATOR_NONE;
	private WhiteListBean operatorBean;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		setTitle("白名单");
		
		initView();
		initParam();
	}

	public void initParam() {
		listOrm = DbUtils.getInstance(WhiteListActivity.this).getDbInstance();
		QueryBuilder<WhiteListBean> qb = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereNoEquals(WhiteListBean.ENABLE, "false");
		mListBean = listOrm.query(qb);
		Collections.sort(mListBean);
		listAdapter = new ListAdapter<WhiteListBean>(WhiteListActivity.this, mListBean, this);
		mListView.setAdapter(listAdapter);
		
		mContactListBean = new ArrayList<BaseListBean>();
		Utils.getPhoneContactsData(this, mContactListBean);
		contactAdapter = new ContactListAdapter(WhiteListActivity.this, mContactListBean, this);
		
		DockService.addOnDockInfoListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DockService.removeOnDockInfoListener(this);
	}

	public void initView() {
		findViewById(R.id.id_contactadd).setOnClickListener(this);
		findViewById(R.id.id_customadd).setOnClickListener(this);
		findViewById(R.id.id_sync).setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.id_listview);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_contactadd:
			showContactDialog();
			break;
		case R.id.id_customadd:
			showCutomDialog();
			break;
		case R.id.id_sync:
			DockCmdUtils.syncWhiteList();
			break;

		default:
			break;
		}
	}
	
	private void showContactDialog() {
		if (contactDialog == null) {
			View view = View.inflate(WhiteListActivity.this, R.layout.list_contact_layout, null);
			ListView contactListView = (ListView) view.findViewById(R.id.id_content);
			contactListView.setAdapter(contactAdapter);
			contactDialog = new AlertDialog.Builder(WhiteListActivity.this).setTitle("联系人").setView(view).create();
		}
		contactDialog.show();
	}
	
	private void showCutomDialog() {
		if (customDialog == null) {
			View view = View.inflate(WhiteListActivity.this, R.layout.list_custom_layout, null);
			final EditText nameET = (EditText) view.findViewById(R.id.id_etname);
			final EditText phoneET = (EditText) view.findViewById(R.id.id_etnumber);
			customDialog = new AlertDialog.Builder(WhiteListActivity.this).setTitle("联系人").setView(view)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = nameET.getText().toString();
							String number = phoneET.getText().toString();
							WhiteListBean bean = new WhiteListBean();
							bean.setName(name);
							bean.setNumber(number);
							bean.setPhone(number);
							
							if (!TextUtils.isEmpty(number)) {
								if (TextUtils.isEmpty(name)) {
									bean.setName(number);
								}
								add(bean);
							} else {
								Toast.makeText(WhiteListActivity.this, "添加失败！号码不能为空", Toast.LENGTH_LONG).show();
							}
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							customDialog.cancel();
						}
					}).create();
		}
		customDialog.show();
	}
	
	private void waitingDialog() {
		if (waitingDialog == null) {
			waitingDialog = new ProgressDialog(WhiteListActivity.this);
		}
		
		waitingDialog.show();
	}

	@Override
	public void onChildViewClick(View v) {
		int id = v.getId();
		int position = (Integer) v.getTag();
		
		switch (id) {
		case R.id.id_itemcontent:
			contactDialog.cancel();
			BaseListBean baseBean = mContactListBean.get(position);
			WhiteListBean bean = new WhiteListBean(baseBean);
			add(bean);
			break;
		case R.id.id_del:
			WhiteListBean listbean = mListBean.get(position);
			remove(listbean);
			break;

		default:
			break;
		}
	}
	
	private void add(WhiteListBean bean) {
		boolean isHave = mListBean.contains(bean);
		bean.setEnable(true);
		Log.d("TAG", "add:"+bean);
		
		QueryBuilder<BlackListBean> blackQB = new QueryBuilder<BlackListBean>(BlackListBean.class)
				.whereEquals(BlackListBean.PHONE, bean.getPhone())
				.whereAppendAnd()
				.whereEquals(BlackListBean.ENABLE, "true");
		ArrayList<BlackListBean> blackList = listOrm.query(blackQB);
		if (blackList.size() > 0) {
			Toast.makeText(this, "添加失败！"+bean.getNumber()+"该号码已存在黑名单中!", Toast.LENGTH_LONG).show();
			return ;
		}
		
		if (!isHave) {
			int size = mListBean.size();
			int id = bean.getId();
			ArrayList<WhiteListBean> list = null;
			//添加的号码是否超过最大值
			if (size == WhiteListBean.MAX) {
				Toast.makeText(this, "添加失败！"+"已超过名单最大限制"+WhiteListBean.MAX+",请删除无用的号码再添加新的号码!", Toast.LENGTH_LONG).show();
				return ;
			}
			
			long count = listOrm.queryCount(WhiteListBean.class);
			QueryBuilder<WhiteListBean> whiteQB = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereEquals(WhiteListBean.PHONE, bean.getPhone());
			//查询表中是否有该电话号码
			list = listOrm.query(whiteQB); 
			//表中项是否已经添加到了最大值
			if (count == WhiteListBean.MAX) {
				if (size < WhiteListBean.MAX) {
					//表中没有该号码,就是使用enable为false的项更新
					if (list.size() == 0) { 
						whiteQB = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereEquals(WhiteListBean.ENABLE, "false");
						list = listOrm.query(whiteQB);
					}
				} 
			}
			
			//表中存在可以更新的项，设置id并更新该项,如果不存在就添加新的项
			if (list.size() > 0) {
				id = list.get(0).getId();
				bean.setId(id);
			}
			
			listOrm.save(bean);
			operatorBean = bean;
			if (Dock.isExistUSBNode()) {
				operatorType = OPERATOR_ADD;
				DockCmdUtils.addWhiteList(bean.getId(), bean.getPhone());
				waitingDialog();
			} else {
				operatorType = OPERATOR_NONE;
				mListBean.add(operatorBean);
				Collections.sort(mListBean);
				listAdapter.notifyDataSetChanged();
			}
		} else {
			Toast.makeText(this, "添加失败！"+bean.getNumber()+"该号码已存在白名单中!", Toast.LENGTH_LONG).show();
		}
	}
	
	private void remove(WhiteListBean bean) {
		bean.setEnable(false);
		listOrm.save(bean);
		
		operatorBean = bean;
		if (Dock.isExistUSBNode()) {
			operatorType = OPERATOR_REMOVE;
			DockCmdUtils.removeWhiteList(bean.getId());
			waitingDialog();
		} else {
			operatorType = OPERATOR_NONE;
			mListBean.remove(operatorBean);
			listOrm.save(operatorBean);
			Collections.sort(mListBean);
			listAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onUpdated(String result) {
		if (waitingDialog != null) {
			waitingDialog.cancel();
		}
		Utils.writeLogToSdcard("dock.txt", "step1"+operatorBean+",operatorType:"+operatorType);
		
		if (operatorType == OPERATOR_NONE) {
			return ;
		}
		Utils.writeLogToSdcard("dock.txt", "step2"+operatorBean+",operatorType:"+operatorType);
		
		if (operatorBean == null) {
			return ;
		}
		Utils.writeLogToSdcard("dock.txt", "step3"+operatorBean+",operatorType:"+operatorType);
		
		String[] syncResult = Dock.getPhoneSyncResult(result);
		if (syncResult == null) {
			return ;
		}
		
		Utils.writeLogToSdcard("dock.txt", "step4"+operatorBean+",operatorType:"+operatorType+",syncResult[0]:"+syncResult[0]+",syncResult[1]:"+syncResult[1]);
		
		if (syncResult[0].equals("1")) {
			String id = syncResult[1];
			if (operatorBean.getId() == Integer.valueOf(id)) {
				if (operatorType == OPERATOR_ADD) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							listOrm.save(operatorBean);
							mListBean.add(operatorBean);
							Collections.sort(mListBean);
							listAdapter.notifyDataSetChanged();
							Toast.makeText(WhiteListActivity.this, "添加成功!", Toast.LENGTH_LONG).show();
							Utils.writeLogToSdcard("dock.txt", "添加成功!"+operatorBean.getId());
						}
					});
				} else if (operatorType == OPERATOR_REMOVE) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							mListBean.remove(operatorBean);
							listOrm.save(operatorBean);
							Collections.sort(mListBean);
							listAdapter.notifyDataSetChanged();
							Toast.makeText(WhiteListActivity.this, "删除成功!", Toast.LENGTH_LONG).show();
							Utils.writeLogToSdcard("dock.txt", "删除成功!"+operatorBean.getId());
						}
					});
				} else {
					Utils.writeLogToSdcard("dock.txt", "其他!"+operatorBean+",id:"+id+",operatorType:"+operatorType);
				}
			} else {
				if (operatorType == OPERATOR_ADD) {
					operatorBean.setEnable(false);
					listOrm.save(operatorBean);
				} else if (operatorType == OPERATOR_REMOVE) {
					operatorBean.setEnable(true);
					listOrm.save(operatorBean);
				}
				Toast.makeText(WhiteListActivity.this, "操作失败!", Toast.LENGTH_LONG).show();
			}
		}
		
		operatorType = OPERATOR_NONE;
	}
}
