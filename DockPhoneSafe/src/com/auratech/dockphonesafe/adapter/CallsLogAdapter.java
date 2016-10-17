package com.auratech.dockphonesafe.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;
import com.auratech.dockphonesafe.bean.BlackListBean;
import com.auratech.dockphonesafe.bean.CallsLogBean;
import com.auratech.dockphonesafe.bean.WhiteListBean;
import com.auratech.dockphonesafe.utils.ContactsPhotoLoader;


/**
 * @ClassName: CallsLogAdapter
 * @Description: TODO
 * @author: steven zhang
 * @date: Sep 27, 2016 2:31:16 PM
 */
public class CallsLogAdapter extends BaseAdapter {
	
	private List<CallsLogBean> mList;
	private Context mContext;
	private ArrayList<BlackListBean> mBlackListBean;
	private ArrayList<WhiteListBean> mWhiteListBean;
	
	public CallsLogAdapter(Context context, List<CallsLogBean> list, ArrayList<BlackListBean> blackListBean, ArrayList<WhiteListBean> whiteListBean) {
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
			convertView = View.inflate(mContext, R.layout.callslog_item, null);
			
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) convertView.findViewById(R.id.id_image);
			viewHolder.name = (TextView) convertView.findViewById(R.id.name);
			viewHolder.number = (TextView) convertView.findViewById(R.id.number);
			viewHolder.label = (TextView) convertView.findViewById(R.id.label);
			viewHolder.typeicon = convertView.findViewById(R.id.call_type_icons);
			viewHolder.typedate = (TextView) convertView.findViewById(R.id.call_count_and_date);
//			viewHolder.duration = (TextView) convertView.findViewById(R.id.call_duration);
			viewHolder.addedList = (TextView) convertView.findViewById(R.id.id_added_list);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		CallsLogBean bean = (CallsLogBean) getItem(position);
		
		
		// The date of this call, relative to the current time.
		//设置通话距离现在的时间
        CharSequence dateText = DateUtils.getRelativeTimeSpanString(bean.getDate(),
        			System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE);
        viewHolder.typedate.setText(dateText);
        
        
        //设置通话类型，来电、去电、未接
        int type = bean.getType();
        switch (type) {
		case CallLog.Calls.INCOMING_TYPE:
			viewHolder.typeicon.setBackgroundResource(R.drawable.ic_call_incoming_holo_dark);
			break;
		case CallLog.Calls.OUTGOING_TYPE:
			viewHolder.typeicon.setBackgroundResource(R.drawable.ic_call_outgoing_holo_dark);
			break;
		case CallLog.Calls.MISSED_TYPE:
			viewHolder.typeicon.setBackgroundResource(R.drawable.ic_call_missed_holo_dark);
			break;

		default:
			break;
		}
        
        //------------------------------------
        CharSequence numberFormattedLabel = Phone.getTypeLabel(mContext.getResources(), bean.getType(), bean.getNumbertype());
        
        final CharSequence nameText;
        final CharSequence numberText;
        final CharSequence labelText;
        final CharSequence displayNumber = getDisplayNumber(bean.getNumber(), bean.getFormattednumber());
        if (TextUtils.isEmpty(bean.getName())) {
            nameText = displayNumber;
            if (TextUtils.isEmpty(bean.getGeocodedlocation())) {
                numberText = mContext.getResources().getString(R.string.call_log_empty_gecode);
            } else {
                numberText = bean.getGeocodedlocation();
            }
            labelText = null;
        } else {
            nameText = bean.getName();
            numberText = displayNumber;
            labelText = numberFormattedLabel;
        }
        //设置用户名字
        viewHolder.name.setText(nameText);
        //设置用户电话号码
        viewHolder.number.setText(numberText);
        //设置用户电话类型
        viewHolder.label.setText(labelText);
        //------------------------------------
		
        //设置用户头像
		String lookupuri = bean.getLookupuri();
		if (lookupuri != null) {
			ContactsPhotoLoader.getInstance().loadImage(mContext, viewHolder.image, Uri.parse(bean.getLookupuri()), bean.getPhotoid(), R.drawable.ic_contact_picture_holo_dark);
		} else {
			viewHolder.image.setImageResource(R.drawable.ic_contact_picture_holo_dark);
		}
	
//		//设置通话时长
//		String duration = getCallDuration(bean.getDuration());
//		viewHolder.duration.setText(duration);
		
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
		private ImageView image;
		private TextView name;
		private TextView number;
		private TextView label;
		private TextView typedate;
//		private TextView duration;
		private View typeicon;
		private TextView addedList;
	}

	
    private static final String UNKNOWN_NUMBER = "-1";
    private static final String PRIVATE_NUMBER = "-2";
    private static final String PAYPHONE_NUMBER = "-3";
    
    /**
     * 获取要显示的电话号码
     * @param number
     * @param formattedNumber
     * @return
     */
    private CharSequence getDisplayNumber(CharSequence number, CharSequence formattedNumber) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }
        if (number.equals(UNKNOWN_NUMBER)) {
            return mContext.getResources().getString(R.string.unknown);
        }
        if (number.equals(PRIVATE_NUMBER)) {
            return mContext.getResources().getString(R.string.private_num);
        }
        if (number.equals(PAYPHONE_NUMBER)) {
            return mContext.getResources().getString(R.string.payphone);
        }
        if (TextUtils.isEmpty(formattedNumber)) {
            return number;
        } else {
            return formattedNumber;
        }
    }
    
    /**
     * 获取通话时长
     */
    @SuppressWarnings("unused")
	private String getCallDuration(long duration) {
    	String time = "00:00";
    	int hour = 0;
    	int minute = 0;
    	int second = 0;
    	
    	if (duration < 0) {
    		return time;
    	}
    	
    	if (duration >= 3600) { //大于3600s说明通话时长超过1个小时,即60分钟,所以其格式为00:00:00
    		hour = (int) (duration / 3600);
    		minute = (int) ((duration-hour*3600) / 60);
    		second = (int) (duration % 60);
    		time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
    	} else { //小于1个小时，其格式为00:00
    		minute = (int) (duration / 60);
    		second = (int) (duration % 60);
    		time = String.format(Locale.getDefault(), "%02d:%02d", minute, second);
    	}
    	
    	return time;
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
