package com.auratech.dockphonesafe.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.adapter.CallsLogAdapter;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.CallsLogBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.service.ContactsSyncService;
import com.auratech.dockphonesafe.utils.ContactsUtils;
import com.auratech.dockphonesafe.utils.DbUtils;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;


/**
 * @ClassName: CallsLogListFragment
 * @Description: TODO
 * @author: steven zhang
 * @date: Sep 27, 2016 2:31:44 PM
 */
public class CallsLogListFragment extends Fragment implements OnItemClickListener {
	
	public static final String CALLLOG_PHONE = "calllog_phone";
	public static final int CALLLOG_CODE = 2000;

	private MyQueryHandler myQueryHandler;
	private ListView mListView;
	private List<CallsLogBean> mDataList;
	private TextView mEmptyView;
	private CallsLogAdapter callsLogAdapter;
	private ArrayList<WhiteListBean> mWhiteListBean;
	private ArrayList<BlackListBean> mBlackListBean;
	private LiteOrm listOrm;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.callslog_layout, container, false);
		mListView = (ListView) view.findViewById(R.id.id_listview);
		mDataList = new ArrayList<CallsLogBean>();
		
		mEmptyView = (TextView) view.findViewById(R.id.id_empty);
	    mListView.setEmptyView(mEmptyView);
		
		myQueryHandler = new MyQueryHandler(getActivity().getContentResolver());
		
		mListView.setOnItemClickListener(this);
		
		Intent intent = new Intent(getActivity(), ContactsSyncService.class);
	    getActivity().startService(intent);
	    
	    IntentFilter filter = new IntentFilter(ContactsSyncService.UPDATE_CONTACTS);
	    LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, filter);
	    
	    myQueryHandler.startQuery(0, null, ContactsUtils.Calls.CONTENT_URI, ContactsUtils.Calls.PROJECTION_PRIMARY, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
	    
	    listOrm = DbUtils.getInstance(getActivity()).getDbInstance();
		QueryBuilder<WhiteListBean> whiteqb = new QueryBuilder<WhiteListBean>(WhiteListBean.class).whereNoEquals(WhiteListBean.ENABLE, "false");
		mWhiteListBean = listOrm.query(whiteqb);
	    
		QueryBuilder<BlackListBean> blackqb = new QueryBuilder<BlackListBean>(BlackListBean.class).whereNoEquals(BlackListBean.ENABLE, "false");
		mBlackListBean = listOrm.query(blackqb);
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (callsLogAdapter != null) {
			callsLogAdapter.notifyDataSetChanged();
		} else {
			myQueryHandler.startQuery(0, null, ContactsUtils.Calls.CONTENT_URI, ContactsUtils.Calls.PROJECTION_PRIMARY, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		}
	}
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ContactsSyncService.UPDATE_CONTACTS.equals(action)) {
				if (myQueryHandler != null) {
					myQueryHandler.cancelOperation(0);
					myQueryHandler.startQuery(0, null, ContactsUtils.Calls.CONTENT_URI, ContactsUtils.Calls.PROJECTION_PRIMARY, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
				}
			}
		}
		
	};
	
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
					CallsLogBean bean = CallsLogBean.getBeanFromCursor(cursor);
					if (!mDataList.contains(bean)) {
						mDataList.add(bean);
						Log.d("TAG", "onQueryComplete:"+bean);
					}
				}
				
				cursor.close();
				callsLogAdapter = new CallsLogAdapter(getActivity(), mDataList, mBlackListBean, mWhiteListBean);
				mListView.setAdapter(callsLogAdapter);
				
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CallsLogBean bean = mDataList.get(position);
		
		String phone = bean.getPhone();
		if (TextUtils.isEmpty(phone)) {
			Toast.makeText(getActivity(), "添加失败，无效的号码!", Toast.LENGTH_SHORT).show();
		} else {
			Intent intent = new Intent();
			intent.putExtra(CALLLOG_PHONE, bean);
			
			getActivity().setResult(Activity.RESULT_OK, intent);
			getActivity().finish();
		}
	}
}
