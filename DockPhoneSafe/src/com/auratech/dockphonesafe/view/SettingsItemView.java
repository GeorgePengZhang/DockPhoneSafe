/**  

 * Copyright © 2016公司名字. All rights reserved.

 * @Title: SettingsItemView.java

 * @Prject: DockPhoneSafe

 * @Package: com.auratech.dockphonesafe.view

 * @Description: TODO

 * @author: Administrator  

 * @date: Oct 14, 2016 11:12:36 AM

 */
package com.auratech.dockphonesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;

/**
 * @ClassName: SettingsItemView
 * @Description: TODO
 * @author: steven zhang
 * @date: Oct 14, 2016 11:12:36 AM
 */
public class SettingsItemView extends LinearLayout implements OnClickListener {
	
	private View header;
	private TextView headerText;
	private ViewGroup item;
	private TextView itemText;
	private SwitchButton itemSwitch;
	private OnItemClickListener mOnItemClickListener;
	private TextView itemContent;

	public SettingsItemView(Context context) {
		super(context);
		init();
	}

	public SettingsItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		
	}

	private void init() {
		inflate(getContext(), R.layout.settings_item, this);
		
		setOrientation(VERTICAL);
		
		header = findViewById(R.id.id_header);
		headerText = (TextView) findViewById(R.id.id_headertext);
		item = (ViewGroup) findViewById(R.id.id_item);
		itemText = (TextView) findViewById(R.id.id_itemtext);
		itemContent = (TextView) findViewById(R.id.id_itemcontent);
		itemSwitch = (SwitchButton) findViewById(R.id.id_itemswitch);
		
		item.setOnClickListener(this);
		
		showHeader(false); //默认隐藏header
	}

	public void showHeader(boolean show) {
		if (show) {
			header.setVisibility(VISIBLE);
		} else {
			header.setVisibility(GONE);
		}
	}
	
	public void showSwitch(boolean show) {
		if (show) {
			itemSwitch.setVisibility(VISIBLE);
		} else {
			itemSwitch.setVisibility(GONE);
		}
	}
	
	public void setHeaderText(String text) {
		headerText.setText(text);
	}
	
	public void setItemText(String text) {
		itemText.setText(text);
	}
	
	public void setItemContent(String text) {
		itemContent.setVisibility(View.VISIBLE);
		itemContent.setText(text);
	}
	
	public CharSequence getItemContent() {
		return itemContent.getText();
	}
	
	/**
	 * 设置view以及其子view的点击事件是否生效
	 * @param v
	 * @param enabled
	 */
	public void setItemEnabled(boolean enabled) {
		itemText.setEnabled(enabled);
		itemContent.setEnabled(enabled);
		itemSwitch.setEnabled(enabled);
		item.setEnabled(enabled);
	}
	
	
	/**
	 * 设置view是否选中
	 * @param checked
	 */
	public void setChecked(boolean checked) {
		itemSwitch.setChecked(checked);
	}
	
	/**
	 * 判断item是否选中
	 * @return
	 */
	public boolean isChecked() {
		return itemSwitch.isChecked();
	}

	@Override
	public void onClick(View v) {
		if (R.id.id_item == v.getId()) {
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(v);
			}
		}
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		mOnItemClickListener = onItemClickListener;
	}
	
	public interface OnItemClickListener {
		public void onItemClick(View v);
	}
}
