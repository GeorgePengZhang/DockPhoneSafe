/**  

 * Copyright © 2016公司名字. All rights reserved.

 * @Title: WhiteListFragment.java

 * @Prject: DockPhoneSafe

 * @Package: com.auratech.dockphonesafe.fragment

 * @Description: TODO

 * @author: Administrator  

 * @date: Oct 9, 2016 11:02:49 AM

 */
package com.auratech.dockphonesafe.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auratech.dockphonesafe.ListActivity;
import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.adapter.ListAdapter;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.CallsLogBean;
import com.auratech.dockphonesafe.bean.ContactsPhoneBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.service.Dock;
import com.auratech.dockphonesafe.service.DockService;
import com.auratech.dockphonesafe.service.DockService.OnDockInfoListener;
import com.auratech.dockphonesafe.utils.CnToCharParser;
import com.auratech.dockphonesafe.utils.ContactsUtils;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.DockCmdUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.view.FloatingActionButton;
import com.auratech.dockphonesafe.view.FloatingActionsMenu;
import com.auratech.dockphonesafe.view.MyDialog;
import com.auratech.dockphonesafe.view.MyDialog.IOnCancelAndOkClickListener;
import com.auratech.dockphonesafe.view.SideBar;
import com.auratech.dockphonesafe.view.SideBar.OnTouchingLetterChangedListener;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

/**
 * @ClassName: WhiteListFragment
 * @Description: TODO
 * @author: steven zhang
 * @date: Oct 9, 2016 11:02:49 AM
 */
public class WhiteListFragment extends Fragment implements OnClickListener, OnDockInfoListener, OnTouchingLetterChangedListener, OnItemClickListener {
	
	private static final int OPERATOR_NONE = 0;
	private static final int OPERATOR_ADD = 1;
	private static final int OPERATOR_REMOVE = 2;
	
	private MyDialog mDialog;
	private List<WhiteListBean> mListBean;
	private ListAdapter<WhiteListBean> listAdapter;
	private ListView mListView;
	private LiteOrm listOrm;

	private int operatorType = OPERATOR_NONE;
	private WhiteListBean operatorBean;
	private TextView mEmptyView;
	private SideBar mSideBar;
	private FloatingActionsMenu mMenuFAB;
	private FloatingActionButton mSyncFAB;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_layout, container, false);
		initView(view);
		initParam();
		return view;
	}
	
	public void initView(View view) {
		mMenuFAB = (FloatingActionsMenu) view.findViewById(R.id.menufab);
		view.findViewById(R.id.addfab).setOnClickListener(this);
		view.findViewById(R.id.delfab).setOnClickListener(this);
		mSyncFAB = (FloatingActionButton) view.findViewById(R.id.syncfab);
		mSyncFAB.setOnClickListener(this);
		mMenuFAB.getAddButton().setOnClickListener(this);
		
		mListView = (ListView) view.findViewById(R.id.id_listview);
		mEmptyView = (TextView) view.findViewById(R.id.id_empty);
		mSideBar = (SideBar) view.findViewById(R.id.id_sidebar);
		mSideBar.setOnTouchingLetterChangedListener(this);
		
	    mEmptyView.setText(getString(R.string.noWhiteListText));
	    mListView.setEmptyView(mEmptyView);
	}
	
	public void initParam() {
		listOrm = DbUtils.getInstance(getActivity()).getDbInstance();
		QueryBuilder<WhiteListBean> qb = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereNoEquals(WhiteListBean.ENABLE, "false");
		mListBean = listOrm.query(qb);
		Collections.sort(mListBean);
		listAdapter = new ListAdapter<WhiteListBean>(getActivity(), mListBean);
		mListView.setAdapter(listAdapter);
		mListView.setOnItemClickListener(this);
		
		DockService.addOnDockInfoListener(this);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		DockService.removeOnDockInfoListener(this);
	}
	
	/**
	 * 判断fragment是否显示
	 */
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (!isVisibleToUser) {
			if (mMenuFAB != null) {
				mMenuFAB.collapse();
			}
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		hideAddDialog();
		if (mMenuFAB != null) {
			mMenuFAB.collapse();
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = null;
		
		switch (v.getId()) {
		case R.id.fab_expand_menu_button:
			if (Dock.isExistUSBNode()) {
				mSyncFAB.setVisibility(View.VISIBLE);
			} else {
				mSyncFAB.setVisibility(View.GONE);
			}
			mMenuFAB.toggle();
			break;
		case R.id.addfab:
			//弹出增加名单列表的几种方法
			showAddDialog();
			break;
		case R.id.delfab:
			showDelDialog(getResources().getString(R.string.delete_all_list), null);
			break;
		case R.id.syncfab:
			//同步白名单
			DockCmdUtils.syncWhiteList();
			break;
		case R.id.id_calllog:
			//打开通话记录
			intent = new Intent(getActivity(), ListActivity.class);
			intent.putExtra(ListActivity.SHOW_TYPE, ListActivity.SHOW_CALLLOG);
			startActivityForResult(intent, CallsLogListFragment.CALLLOG_CODE);
			break;
		case R.id.id_contacts:
			//打开联系人
			intent = new Intent(getActivity(), ListActivity.class);
			intent.putExtra(ListActivity.SHOW_TYPE, ListActivity.SHOW_CONTACTS);
			startActivityForResult(intent, ContactsPhoneListFragment.CONTACTS_CODE);
			break;
		case R.id.id_manual:
			//弹出手动输入名单的对话框
			showManualDialog();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		WhiteListBean bean = mListBean.get(position);
		String info = bean.getName()+"("+bean.getNumber()+")";
		//删除单个名单
		showDelDialog("This will delete "+info+",are you sure?", bean);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == ContactsPhoneListFragment.CONTACTS_CODE) { //从联系人中添加白名单
				ContactsPhoneBean phoneBean = data.getParcelableExtra(ContactsPhoneListFragment.CONTACTS_PHONE);
				Uri uri = ContactsUtils.getContactPhotoUri(phoneBean.getContactId());
				WhiteListBean bean = getWhiteListBean(phoneBean.getName(), phoneBean.getNumber(), uri.toString(), phoneBean.getPhotoId());
				add(bean);
			} else if (requestCode == CallsLogListFragment.CALLLOG_CODE) { //从通话记录中添加白名单
				CallsLogBean phoneBean = data.getParcelableExtra(CallsLogListFragment.CALLLOG_PHONE);
				WhiteListBean bean = getWhiteListBean(phoneBean.getName(), phoneBean.getNumber(), phoneBean.getLookupuri(), phoneBean.getPhotoid());
				add(bean);
			}
		}
	}
	
	/**
	 * 显示几种可以增加名单的对话框
	 */
	private void showAddDialog() {
		hideAddDialog();
		
		mDialog = new MyDialog(getActivity());
		mDialog.setCustomContentView(R.layout.dialog_add_list);
		mDialog.setCustomTitle(R.string.add_to_the_white_list);
		mDialog.showCustomButtonGroup(false);
		mDialog.findViewById(R.id.id_calllog).setOnClickListener(this);
		mDialog.findViewById(R.id.id_contacts).setOnClickListener(this);
		mDialog.findViewById(R.id.id_manual).setOnClickListener(this);
		
		mDialog.show();
	}
	
	/**
	 * 隐藏增加名单的对话框
	 */
	private void hideAddDialog() {
		if (mDialog != null) {
			mDialog.cancel();
			mDialog = null;
		}
	}
	
	/**
	 * 显示手动添加名单的对话框
	 */
	private void showManualDialog() {
		hideAddDialog();
		
		mDialog = new MyDialog(getActivity());
		mDialog.setCustomTitle(R.string.manual);
		mDialog.setCustomContentView(R.layout.dialog_add_custom);
		final EditText nameET = (EditText) mDialog.findViewById(R.id.id_etname);
		final EditText phoneET = (EditText) mDialog.findViewById(R.id.id_etnumber);
		mDialog.setOnCancelAndOkClickListener(new IOnCancelAndOkClickListener() {
			
			@Override
			public void onOK() {
				String name = nameET.getText().toString();
				String number = phoneET.getText().toString();
				WhiteListBean bean = getWhiteListBean(name, number, null, 0);
				add(bean);
			}
			
			@Override
			public void onCancel() {
				
			}
		});
		
		final Button okBtn = mDialog.getOkButton();
		okBtn.setEnabled(false);
		//是否输入电话号码，当有电话号码输入时，才可以点击ok键
		phoneET.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int length = s.toString().trim().length();
				okBtn.setEnabled(length > 0);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		mDialog.show();
	}
	
	/**
	 * 显示删除名单对话框
	 * @param msg 显示提示内容的字符串
	 */
	private void showDelDialog(String msg, final WhiteListBean bean) {
		hideAddDialog();
		
		mDialog = new MyDialog(getActivity());
		mDialog.setCustomTitle(R.string.delete_from_the_white_list);
		mDialog.setCustomContentView(R.layout.dialog_hint_layout);
		TextView info = (TextView) mDialog.findViewById(R.id.id_info);
		info.setText(msg);
		mDialog.setOnCancelAndOkClickListener(new IOnCancelAndOkClickListener() {
			
			@Override
			public void onOK() {
				if (bean != null) {
					remove(bean);
				} else {
					removeAll();
				}
			}
			
			@Override
			public void onCancel() {
				
			}
		});
		
		mDialog.show();
	}
	
	/**
	 * 显示同步对话框
	 */
	private void showSyncDialog() {
		hideAddDialog();
		
		mDialog = new MyDialog(getActivity());
		mDialog.setCustomContentView(R.layout.dialog_sync_layout);
		mDialog.showCustomTitle(false);
		mDialog.showCustomButtonGroup(false);
		
		mDialog.show();
	}
	
	/**
	 * 根据名字和电话号码得到WhiteListBean
	 * @param name 名字 
	 * @param number 电话号码
	 * @param photoUri 头像的Uri
	 * @param photoId 头像id大于0才有头像
	 * @return
	 */
	private WhiteListBean getWhiteListBean(String name, String number, String photoUri, long photoId) {
		WhiteListBean bean = new WhiteListBean();
		bean.setName(name);
		bean.setNumber(number);
		bean.setPhone(number);
		bean.setPhotoUri(photoUri);
		bean.setPhotoid(photoId);
		
		if (TextUtils.isEmpty(name)) {
			bean.setName(number);
		}
		
		//汉字转拼音
		String spellname = CnToCharParser.getInstance().getSpell(bean.getName(), false);
		//获取首字母
		String firstLetter = Utils.getFirstLetter(spellname);
		bean.setSpellname(spellname);
		bean.setFirstLetter(firstLetter);
		return bean;
	}
	
	@Override
	public void onTouchingLetterChanged(String s) {
		if (listAdapter != null && mListBean != null) {
			for (int i = 0; i < mListBean.size(); i++) {
				WhiteListBean bean = mListBean.get(i);
				if (bean.getFirstLetter().equals(s)) {
					mListView.setSelection(i);
					break;
				}
			}
		}
	}
	
	//--------------------------------------------上面为UI交互部分-----------------------------------------------------
	
	//TODO --------------------------------------------华丽丽的分割线--------------------------------------------------
	
	//--------------------------------------------下面面为逻辑处理部分---------------------------------------------------
	
	/**
	 * 添加白名单
	 * @param bean
	 */
	private void add(WhiteListBean bean) {
		boolean isHave = mListBean.contains(bean);
		bean.setEnable(true);
		
		QueryBuilder<BlackListBean> blackQB = new QueryBuilder<BlackListBean>(BlackListBean.class)
				.whereEquals(BlackListBean.PHONE, bean.getPhone())
				.whereAppendAnd()
				.whereEquals(BlackListBean.ENABLE, "true");
		ArrayList<BlackListBean> blackList = listOrm.query(blackQB);
		if (blackList.size() > 0) {
			Toast.makeText(getActivity(), "添加失败！"+bean.getNumber()+"该号码已存在黑名单中!", Toast.LENGTH_LONG).show();
			return ;
		}
		
		if (!isHave) {  // 没有添加到名单中
			int size = mListBean.size();
			int id = bean.getId();
			ArrayList<WhiteListBean> list = null;
			//添加的号码是否超过最大值ֵ
			if (size == WhiteListBean.MAX) {
				Toast.makeText(getActivity(), "添加失败！"+"已超过名单最大限制"+WhiteListBean.MAX+",请删除无用的号码再添加新的号码!", Toast.LENGTH_LONG).show();
				return ;
			}
			
			long count = listOrm.queryCount(WhiteListBean.class);
			QueryBuilder<WhiteListBean> whiteQB = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereEquals(WhiteListBean.PHONE, bean.getPhone());
			//查询表中是否有该电话号码
			list = listOrm.query(whiteQB); 
			//表中项是否已经添加到了最大值ֵ
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
				showSyncDialog();
				DockCmdUtils.addWhiteList(bean.getId(), bean.getPhone());
			} else {
				operatorType = OPERATOR_NONE;
				mListBean.add(operatorBean);
				Collections.sort(mListBean);
				listAdapter.notifyDataSetChanged();
			}
		} else {
			Toast.makeText(getActivity(), "添加失败！"+bean.getNumber()+"该号码已存在白名单中!", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * 删除白名单
	 * @param bean
	 */
	private void remove(WhiteListBean bean) {
		bean.setEnable(false);
		listOrm.save(bean);
		
		
		operatorBean = bean;
		if (Dock.isExistUSBNode()) { //平板是否插入电话底座
			operatorType = OPERATOR_REMOVE;
			showSyncDialog();
			DockCmdUtils.removeWhiteList(bean.getId());
		} else {
			operatorType = OPERATOR_NONE;
			mListBean.remove(operatorBean);
			listOrm.save(operatorBean);
			Collections.sort(mListBean);
			listAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * 删除全部白名单
	 */
	private void removeAll() {
		int size = mListBean.size();
		for (int i = 0; i < size; i++) {
			WhiteListBean bean = mListBean.get(i);
			bean.setEnable(false);
			listOrm.save(bean);
		}
		
		mListBean.removeAll(mListBean);
		listAdapter.notifyDataSetChanged();
		
		if (Dock.isExistUSBNode()) { 
			DockCmdUtils.syncWhiteList();
		}
	}

	@Override
	public void onUpdated(String result) {
		hideAddDialog();
		
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
		
		if (syncResult[0].equals(Dock.RESULT_WHITE_LIST_FLAG)) {
			String id = syncResult[1];
			if (operatorBean.getId() == Integer.valueOf(id)) {
				if (operatorType == OPERATOR_ADD) {
					getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							listOrm.save(operatorBean);
							mListBean.add(operatorBean);
							Collections.sort(mListBean);
							listAdapter.notifyDataSetChanged();
							Toast.makeText(getActivity(), "添加成功!", Toast.LENGTH_LONG).show();
							Utils.writeLogToSdcard("dock.txt", "添加成功!"+operatorBean.getId());
						}
					});
				} else if (operatorType == OPERATOR_REMOVE) {
					getActivity().runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							mListBean.remove(operatorBean);
							listOrm.save(operatorBean);
							Collections.sort(mListBean);
							listAdapter.notifyDataSetChanged();
							Toast.makeText(getActivity(), "删除成功!", Toast.LENGTH_LONG).show();
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
				Toast.makeText(getActivity(), "操作失败!", Toast.LENGTH_LONG).show();
			}
		}
		
		operatorType = OPERATOR_NONE;
	}
}
