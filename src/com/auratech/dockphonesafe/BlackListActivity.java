package com.auratech.dockphonesafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.utils.DockCmdUtils;
import com.auratech.dockphonesafe.utils.Utils;
import com.auratech.dockphonesafe.utils.Utils.IChildViewOnClick;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

public class BlackListActivity extends Activity implements OnClickListener, IChildViewOnClick {

	private List<BaseListBean> mContactListBean;
	private ContactListAdapter contactAdapter;
	private AlertDialog contactDialog;
	private AlertDialog customDialog;
	private List<BlackListBean> mListBean;
	private ListAdapter<BlackListBean> listAdapter;
	private ListView mListView;
	private LiteOrm listOrm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		setTitle("黑名单");
		
		initView();
		initParam();
	}

	public void initParam() {
		listOrm = DbUtils.getInstance(BlackListActivity.this).getDbInstance();
		QueryBuilder<BlackListBean> qb = new QueryBuilder<BlackListBean>(BlackListBean.class).whereNoEquals(BlackListBean.ENABLE, "false");
		mListBean = listOrm.query(qb);
		Collections.sort(mListBean);
		listAdapter = new ListAdapter<BlackListBean>(BlackListActivity.this, mListBean, this);
		mListView.setAdapter(listAdapter);
		
		mContactListBean = new ArrayList<BaseListBean>();
		Utils.getPhoneContactsData(this, mContactListBean);
		contactAdapter = new ContactListAdapter(BlackListActivity.this, mContactListBean, this);
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
			DockCmdUtils.syncBlackList();
			break;

		default:
			break;
		}
	}
	
	private void showContactDialog() {
		if (contactDialog == null) {
			View view = View.inflate(BlackListActivity.this, R.layout.list_contact_layout, null);
			ListView contactListView = (ListView) view.findViewById(R.id.id_content);
			contactListView.setAdapter(contactAdapter);
			contactDialog = new AlertDialog.Builder(BlackListActivity.this).setTitle("联系人").setView(view).create();
		}
		contactDialog.show();
	}
	
	private void showCutomDialog() {
		if (customDialog == null) {
			View view = View.inflate(BlackListActivity.this, R.layout.list_custom_layout, null);
			final EditText nameET = (EditText) view.findViewById(R.id.id_etname);
			final EditText phoneET = (EditText) view.findViewById(R.id.id_etnumber);
			customDialog = new AlertDialog.Builder(BlackListActivity.this).setTitle("联系人").setView(view)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = nameET.getText().toString();
							String number = phoneET.getText().toString();
							BlackListBean bean = new BlackListBean();
							bean.setName(name);
							bean.setNumber(number);
							bean.setPhone(number);
							
							if (!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(number)) {
								add(bean);
							} else {
								Toast.makeText(BlackListActivity.this, "添加失败！名字或者号码不能为空", Toast.LENGTH_LONG).show();
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

	@Override
	public void onChildViewClick(View v) {
		int id = v.getId();
		int position = (Integer) v.getTag();
		
		switch (id) {
		case R.id.id_itemcontent:
			contactDialog.cancel();
			BaseListBean baseBean = mContactListBean.get(position);
			BlackListBean bean = new BlackListBean(baseBean);
			add(bean);
			break;
		case R.id.id_del:
			BlackListBean listbean = mListBean.get(position);
			remove(listbean);
			break;

		default:
			break;
		}
	}
	
	private void add(BlackListBean bean) {
		boolean isHave = mListBean.contains(bean);
		bean.setEnable(true);
		Log.d("TAG", "add:"+bean);
		
		QueryBuilder<WhiteListBean> blackQB = new QueryBuilder<WhiteListBean>(WhiteListBean.class)
				.whereEquals(WhiteListBean.PHONE, bean.getPhone())
				.whereAppendAnd()
				.whereEquals(WhiteListBean.ENABLE, "true");
		ArrayList<WhiteListBean> blackList = listOrm.query(blackQB);
		if (blackList.size() > 0) {
			Toast.makeText(this, "添加失败！"+bean.getNumber()+"该号码已存在白名单中!", Toast.LENGTH_LONG).show();
			return ;
		}
		
		if (!isHave) {
			int size = mListBean.size();
			int id = bean.getId();
			ArrayList<BlackListBean> list = null;
			//添加的号码是否超过最大值
			if (size == BlackListBean.MAX) {
				Toast.makeText(this, "添加失败！"+"已超过名单最大限制"+BlackListBean.MAX+",请删除无用的号码再添加新的号码!", Toast.LENGTH_LONG).show();
				return ;
			}
			
			long count = listOrm.queryCount(BlackListBean.class);
			QueryBuilder<BlackListBean> whiteQB = new QueryBuilder<BlackListBean>(BlackListBean.class).whereEquals(BlackListBean.PHONE, bean.getPhone());
			//查询表中是否有该电话号码
			list = listOrm.query(whiteQB); 
			//表中项是否已经添加到了最大值
			if (count == BlackListBean.MAX) {
				if (size < BlackListBean.MAX) {
					//表中没有该号码,就是使用enable为false的项更新
					if (list.size() == 0) { 
						whiteQB = new QueryBuilder<BlackListBean>(BlackListBean.class).whereEquals(BlackListBean.ENABLE, "false");
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
			mListBean.add(bean);
			Collections.sort(mListBean);
			listAdapter.notifyDataSetChanged();
			DockCmdUtils.addBlackList(bean.getId(), bean.getPhone());
		} else {
			Toast.makeText(this, "添加失败！"+bean.getNumber()+"该号码已存在黑名单中!", Toast.LENGTH_LONG).show();
		}
	}
	
	private void remove(BlackListBean bean) {
		bean.setEnable(false);
		mListBean.remove(bean);
		listOrm.save(bean);
		Collections.sort(mListBean);
		listAdapter.notifyDataSetChanged();
		DockCmdUtils.removeBlackList(bean.getId());
	}
}
