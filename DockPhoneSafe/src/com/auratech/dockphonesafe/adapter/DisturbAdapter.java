package com.auratech.dockphonesafe.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.TimeBean;
import com.auratech.dockphonesafe.utils.Utils.IChildViewOnClick;

public class DisturbAdapter extends BaseAdapter implements OnClickListener {

	private ArrayList<TimeBean> mList;
	private Context mContext;
	private IChildViewOnClick mIChildViewOnClick;

	public DisturbAdapter(Context context, ArrayList<TimeBean> list, IChildViewOnClick iChildViewOnClick) {
		mContext = context;
		mList = list;
		mIChildViewOnClick = iChildViewOnClick;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHold;

		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.disturb_item, null);
			viewHold = new ViewHolder();
			viewHold.mInfoTv = (TextView) convertView.findViewById(R.id.id_info);
			viewHold.mSwitchBtn = (Button) convertView.findViewById(R.id.id_switch);
			viewHold.mDelBtn = (Button) convertView.findViewById(R.id.id_del);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHolder) convertView.getTag();
		}
		
		TimeBean bean = (TimeBean) getItem(position);
		viewHold.mInfoTv.setText((position+1)+"."+bean.info());
		if (bean.isOpen()) {
			viewHold.mSwitchBtn.setText("关闭");
			convertView.setBackgroundColor(Color.GREEN);
		} else {
			viewHold.mSwitchBtn.setText("开启");
			convertView.setBackgroundColor(0xffc2c2c2);
		}
		
		viewHold.mSwitchBtn.setTag(position);
		viewHold.mDelBtn.setTag(position);
		
		viewHold.mSwitchBtn.setOnClickListener(this);
		viewHold.mDelBtn.setOnClickListener(this);

		return convertView;
	}

	public static class ViewHolder {
		private TextView mInfoTv;
		private Button mSwitchBtn;
		private Button mDelBtn;
	}
	
	@Override
	public void onClick(View v) {
		mIChildViewOnClick.onChildViewClick(v);
	}
}