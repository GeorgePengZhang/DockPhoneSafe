package com.auratech.dockphonesafe.adapter;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.BaseListBean;
import com.auratech.dockphonesafe.utils.ContactsPhotoLoader;

public class ListAdapter<T> extends BaseAdapter {
	
	private List<T> mList;
	private Context mContext;

	public ListAdapter(Context context, List<T> list) {
		mContext = context;
		mList = list;
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
			viewHolder.header = convertView.findViewById(R.id.id_header);
			viewHolder.headerIndex = (TextView) convertView.findViewById(R.id.id_headerindex);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.id_photo);
			viewHolder.nameTV = (TextView) convertView.findViewById(R.id.id_name);
			viewHolder.numberTV = (TextView) convertView.findViewById(R.id.id_number);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		BaseListBean bean = (BaseListBean) getItem(position);
		
		// 上一条数据的首字母
		String previewLetter = (position - 1) >= 0 ? ((BaseListBean) mList.get(position-1)).getFirstLetter() : " ";
		String firstLetter = bean.getFirstLetter();
		if (!firstLetter.equals(previewLetter)) {
			viewHolder.header.setVisibility(View.VISIBLE);
			viewHolder.headerIndex.setText(firstLetter);
		} else {
			viewHolder.header.setVisibility(View.GONE);
		}
		
		viewHolder.nameTV.setText(bean.getName());
		viewHolder.numberTV.setText(bean.getNumber());
		
		if (bean.getPhotoUri() != null) {
			ContactsPhotoLoader.getInstance().loadImage(mContext, viewHolder.image, Uri.parse(bean.getPhotoUri()), bean.getPhotoid(), R.drawable.ic_contact_picture_holo_dark);
		} else {
			viewHolder.image.setImageResource(R.drawable.ic_contact_picture_holo_dark);
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		private View header;
		private TextView headerIndex;
		private ImageView image;
		private TextView nameTV;
		private TextView numberTV;
	}

}
