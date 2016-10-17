package com.auratech.dockphonesafe.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.ContactsPhoneBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.utils.ContactsPhotoLoader;
import com.auratech.dockphonesafe.utils.ContactsUtils;


/**
 * @ClassName: ContactsPhoneAdapter
 * @Description: TODO
 * @author: steven zhang
 * @date: Sep 27, 2016 2:31:00 PM
 */
public class ContactsPhoneAdapter extends BaseAdapter {
	
	private List<ContactsPhoneBean> mList;
	private Context mContext;
	private ArrayList<BlackListBean> mBlackListBean;
	private ArrayList<WhiteListBean> mWhiteListBean;
	
	public ContactsPhoneAdapter(Context context, List<ContactsPhoneBean> list, ArrayList<BlackListBean> blackListBean, ArrayList<WhiteListBean> whiteListBean) {
		mContext = context;
		mList = list;
		
		mBlackListBean = blackListBean;
		mWhiteListBean = whiteListBean;
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
		
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.contacts_item, null);
			
			viewHolder = new ViewHolder();
			viewHolder.header = convertView.findViewById(R.id.id_header);
			viewHolder.headerIndex = (TextView) convertView.findViewById(R.id.id_headerindex);
			viewHolder.image = (ImageView) convertView.findViewById(R.id.id_photo);
			viewHolder.name = (TextView) convertView.findViewById(R.id.id_name);
			viewHolder.number = (TextView) convertView.findViewById(R.id.id_phone);
			viewHolder.type = (TextView) convertView.findViewById(R.id.id_type);
			viewHolder.addedList = (TextView) convertView.findViewById(R.id.id_added_list);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ContactsPhoneBean bean = (ContactsPhoneBean) getItem(position);
		
		// 上一条数据的首字母
		String previewLetter = (position - 1) >= 0 ? mList.get(position-1).getFirstLetter() : " ";
		String firstLetter = bean.getFirstLetter();
		if (!firstLetter.equals(previewLetter)) {
			viewHolder.header.setVisibility(View.VISIBLE);
			viewHolder.headerIndex.setText(firstLetter);
		} else {
			viewHolder.header.setVisibility(View.GONE);
		}
		
		viewHolder.name.setText(Html.fromHtml(bean.getName()));
		viewHolder.number.setText(Html.fromHtml(bean.getNumber()));
		viewHolder.type.setText(ContactsContract.CommonDataKinds.Phone.getTypeLabel(mContext.getResources(), bean.getType(), null));
		
		//设置用户头像
		Uri uri = ContactsUtils.getContactPhotoUri(bean.getContactId());
		ContactsPhotoLoader.getInstance().loadImage(mContext, viewHolder.image, uri, bean.getPhotoId(), R.drawable.ic_contact_picture_holo_dark);
		
		//设置该号码是否已经添加到黑白名单
		int haveAddedList = haveAddedList(bean.getPhone());
		viewHolder.addedList.setBackgroundColor(mContext.getResources().getColor(R.color.theme_color));
		switch (haveAddedList) {
		case 0:
			viewHolder.addedList.setText(R.string.added_black_list);
			viewHolder.addedList.setTextColor(0xff000000);
			break;
		case 1:
			viewHolder.addedList.setText(R.string.added_white_list);
			viewHolder.addedList.setTextColor(0xffffffff);
			break;

		default:
			viewHolder.addedList.setText("");
			viewHolder.addedList.setBackgroundColor(0x00000000);
			break;
		}
		
		return convertView;
	}
	
	private static class ViewHolder {
		private View header;
		private TextView headerIndex;
		private ImageView image;
		private TextView name;
		private TextView number;
		private TextView type;
		private TextView addedList;
	}
	
	/**
	 * 判断电话号码是否已添加到黑名单或者白名单
	 * @param phone
	 * @return
	 */
	public int haveAddedList(String phone) {
		int size = mBlackListBean.size();
		for (int i = 0; i < size; i++) {
			BlackListBean bean = mBlackListBean.get(i);
			String listphone = bean.getPhone();
			if (phone.equals(listphone)) {
				return 0;
			}
		}
		
		size = mWhiteListBean.size();
		for (int i = 0; i < size; i++) {
			WhiteListBean bean = mWhiteListBean.get(i);
			String listphone = bean.getPhone();
			if (phone.equals(listphone)) {
				return 1;
			}
		}
		
		return -1;
	}
}
