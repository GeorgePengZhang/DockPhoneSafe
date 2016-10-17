package com.auratech.dockphonesafe.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.adapter.ContactsPhoneAdapter;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.ContactsPhoneBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.service.ContactsSyncService;
import com.auratech.dockphonesafe.utils.CnToCharParser;
import com.auratech.dockphonesafe.utils.ContactsUtils;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.auratech.dockphonesafe.view.ClearEditText;
import com.auratech.dockphonesafe.view.SideBar;
import com.auratech.dockphonesafe.view.SideBar.OnTouchingLetterChangedListener;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;


/**
 * @ClassName: ContactsPhoneListFragment
 * @Description: TODO
 * @author: steven zhang
 * @date: Sep 27, 2016 2:31:49 PM
 */
public class ContactsPhoneListFragment extends Fragment implements OnTouchingLetterChangedListener, OnItemClickListener {
	
	public static final String CONTACTS_PHONE = "contacts_phone";
	public static final int CONTACTS_CODE = 1000;
	
	//是否显示搜索栏
	private static final boolean HAVE_EDIT = true;
	
	private ListView mListView;
	private SideBar mSideBar;
	private MyQueryHandler myQueryHandler;
	private List<ContactsPhoneBean> mDataList;
	private List<ContactsPhoneBean> mCurShowList;
	private ContactsPhoneAdapter mPhoneAdapter;
	private TextView mContactsCounts;
	private TextView mEmptyView;
	private ClearEditText mClearEdit;
	private LiteOrm listOrm;
	private ArrayList<WhiteListBean> mWhiteListBean;
	private ArrayList<BlackListBean> mBlackListBean;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.contacts_layout, container, false);
		mListView = (ListView) view.findViewById(R.id.id_listview);
		mSideBar = (SideBar) view.findViewById(R.id.id_sidebar);
		mSideBar.setOnTouchingLetterChangedListener(this);
		
		if (HAVE_EDIT) {
			mClearEdit = (ClearEditText) view.findViewById(R.id.id_edit);
			mClearEdit.setVisibility(View.VISIBLE);
			mClearEdit.addTextChangedListener(mTextWatcher);
		}
		
		myQueryHandler = new MyQueryHandler(getActivity().getContentResolver());
		mDataList = new ArrayList<ContactsPhoneBean>();
		
		mEmptyView = (TextView) view.findViewById(R.id.id_empty);
	    mEmptyView.setText(getString(R.string.noContactsHelpText));
	    mListView.setEmptyView(mEmptyView);
		
			
	    mContactsCounts = new TextView(getActivity());
	    mContactsCounts.setGravity(Gravity.CENTER);
	    mListView.addFooterView(mContactsCounts);
	    mContactsCounts.setText(mDataList.size()+" "+getActivity().getResources().getString(R.string.contacts_num));
	    
	    
	    mListView.setOnItemClickListener(this);
	    
	    Intent intent = new Intent(getActivity(), ContactsSyncService.class);
	    getActivity().startService(intent);
	    
	    IntentFilter filter = new IntentFilter(ContactsSyncService.UPDATE_CONTACTS);
	    LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, filter);
	    
	    
	    listOrm = DbUtils.getInstance(getActivity()).getDbInstance();
		QueryBuilder<WhiteListBean> whiteqb = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereNoEquals(WhiteListBean.ENABLE, "false");
		mWhiteListBean = listOrm.query(whiteqb);
	    
		QueryBuilder<BlackListBean> blackqb = new QueryBuilder<BlackListBean>(BlackListBean.class).whereNoEquals(BlackListBean.ENABLE, "false");
		mBlackListBean = listOrm.query(blackqb);
	    
	   	return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mPhoneAdapter != null) {
			mPhoneAdapter.notifyDataSetChanged();
		} else {
			myQueryHandler.startQuery(0, null, ContactsUtils.Phone.CONTENT_CALLABLES_URI, ContactsUtils.Phone.PROJECTION_PRIMARY, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
	}
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ContactsSyncService.UPDATE_CONTACTS.equals(action)) {
				if (myQueryHandler != null) {
					myQueryHandler.cancelOperation(0);
					myQueryHandler.startQuery(0, null, ContactsUtils.Phone.CONTENT_CALLABLES_URI, ContactsUtils.Phone.PROJECTION_PRIMARY, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
				}
			}
		}
		
	};
	

	@Override
	public void onTouchingLetterChanged(String s) {
		if (mPhoneAdapter != null && mCurShowList != null) {
			for (int i = 0; i < mCurShowList.size(); i++) {
				ContactsPhoneBean bean = mCurShowList.get(i);
				if (bean.getFirstLetter().equals(s)) {
					mListView.setSelection(i);
					break;
				}
			}
		}
	}
	
	//异步查询联系人数据库
	private class MyQueryHandler extends AsyncQueryHandler {

		public MyQueryHandler(ContentResolver cr) {
			super(cr);
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			super.onQueryComplete(token, cookie, cursor);
			if (cursor != null && cursor.getCount() > 0) {
				mDataList.clear();
				
				while (cursor.moveToNext()) {
					ContactsPhoneBean bean = ContactsPhoneBean.getBeanFromCursor(cursor);
					//去掉电话号码一样的
					if (!mDataList.contains(bean)) {
						mDataList.add(bean);
					}
					Collections.sort(mDataList);
				}
				
				cursor.close();
				updateListAdapter(mDataList);
			}
		}
	}
	
	private TextWatcher mTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			udpateFilterDataList(s.toString());
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			
		}
	};
	
	/**
	 * 搜索字符查找特定联系人
	 * @param s
	 */
	private void udpateFilterDataList(String s) {
		if (TextUtils.isEmpty(s)) {
			updateListAdapter(mDataList);
		} else {
			//汉字转拼音
			String filterName = CnToCharParser.getInstance().getSpell(s, false).toUpperCase(Locale.getDefault());
			List<ContactsPhoneBean> list = new ArrayList<ContactsPhoneBean>();
			int size = mDataList.size();
			for (int i = 0; i < size; i++) {
				ContactsPhoneBean bean = mDataList.get(i);
				
				String spellname = bean.getSpellname().toUpperCase(Locale.getDefault());
				String phone = bean.getPhone();
				String name = bean.getName();
				String number = bean.getNumber();
				
				if (spellname.indexOf(filterName) != -1 ) {
					ContactsPhoneBean clone = bean.clone();
					clone.setName(matcherSearchTitle(name, s));
					list.add(clone);
				} else if (phone.indexOf(filterName) != -1) {
					ContactsPhoneBean clone = bean.clone();
					clone.setNumber(matcherSearchTitle(number, s));
					list.add(clone);
				}
				
			}
			updateListAdapter(list);
		}
	}

	/**
	 * 更新联系人列表
	 * @param list
	 */
	private void updateListAdapter(List<ContactsPhoneBean> list) {
		if (mCurShowList != null) {
			mCurShowList = null;
		}
		mCurShowList = list;
		mPhoneAdapter = new ContactsPhoneAdapter(getActivity(), list, mBlackListBean, mWhiteListBean);
		mListView.setAdapter(mPhoneAdapter);
		mContactsCounts.setText(list.size()+" "+getActivity().getResources().getString(R.string.contacts_num));
	}
	
	 /** 
	 * 搜索关键字标红 
	 * @param title 
	 * @param keyword 
	 * @return 
	 */  
	public static String matcherSearchTitle(String title,String keyword){  
	    String content = title;    
	    String wordReg = "(?i)"+keyword;//用(?i)来忽略大小写    
	    StringBuffer sb = new StringBuffer();    
	    Matcher matcher = Pattern.compile(wordReg).matcher(content);    
	    while(matcher.find()){    
	        //这样保证了原文的大小写没有发生变化    
	        matcher.appendReplacement(sb, "<font color=#33B5E5>"+matcher.group()+"</font>");  
	    }    
	    matcher.appendTail(sb);    
	    content = sb.toString();   
	    return content;  
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ContactsPhoneBean bean = mCurShowList.get(position);
		
		Intent intent = new Intent();
		intent.putExtra(CONTACTS_PHONE, bean);
		
		getActivity().setResult(Activity.RESULT_OK, intent);
		getActivity().finish();
	}  
}
