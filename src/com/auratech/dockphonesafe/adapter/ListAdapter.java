package com.auratech.dockphonesafe.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.BaseListBean;
import com.auratech.dockphonesafe.utils.Utils.IChildViewOnClick;

public class ListAdapter<T> extends BaseAdapter implements OnClickListener {
	
	private List<T> mList;
	private Context mContext;
	private IChildViewOnClick mIChildViewOnClick;

	public ListAdapter(Context context, List<T> list, IChildViewOnClick iChildViewOnClick) {
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
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.list_item, null);
			viewHolder = new ViewHolder();
//			viewHolder.contentLL = (LinearLayout) convertView.findViewById(R.id.id_itemcontent);
			viewHolder.nameTV = (TextView) convertView.findViewById(R.id.id_name);
			viewHolder.numberTV = (TextView) convertView.findViewById(R.id.id_number);
			viewHolder.delBtn = (Button) convertView.findViewById(R.id.id_del);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		BaseListBean item = (BaseListBean) getItem(position);
		viewHolder.nameTV.setText(item.getName());
		viewHolder.numberTV.setText(item.getNumber());
		Log.d("TAG", "getView:"+item.getName()+",number:"+item.getNumber());
//		viewHolder.contentLL.setTag(position);
//		viewHolder.contentLL.setOnClickListener(this);
//		
		viewHolder.delBtn.setTag(position);
		viewHolder.delBtn.setOnClickListener(this);
		return convertView;
	}
	
	private static class ViewHolder {
//		private LinearLayout contentLL;
		private TextView nameTV;
		private TextView numberTV;
		private Button delBtn;
	}

	@Override
	public void onClick(View v) {
		mIChildViewOnClick.onChildViewClick(v);
	}

}
