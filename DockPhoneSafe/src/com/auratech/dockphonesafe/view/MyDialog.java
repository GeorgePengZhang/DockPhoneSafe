/**  

 * Copyright © 2016公司名字. All rights reserved.

 * @Title: MyDialog.java

 * @Prject: DockPhoneSafe

 * @Package: com.auratech.dockphonesafe.view

 * @Description: TODO

 * @author: Administrator  

 * @date: Oct 9, 2016 3:24:39 PM

 */
package com.auratech.dockphonesafe.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.auratech.dockphonesafe.R;

/**
 * @ClassName: MyDialog
 * @Description: TODO
 * @author: steven zhang
 * @date: Oct 9, 2016 3:24:39 PM
 */
public class MyDialog extends Dialog implements android.view.View.OnClickListener {

	float screenDensity;
	int screenWidth;
	int screenHeight;
	private TextView mTitleTV; //标题栏
	private ViewGroup mContent; //内容栏
	private ViewGroup mButtonGroup; //按钮栏
	private Button mCancel;
	private Button mOK;
	private IOnCancelAndOkClickListener mCancelAndOkListener;

	public MyDialog(Context context) {
		super(context, R.style.MyDialog);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		setContentView(R.layout.custom_dialog_layout);

		getDisplay(context);
		init(context, (int) (screenWidth * 0.8), 0, Gravity.CENTER);

		mTitleTV = (TextView) findViewById(R.id.id_title);
		mContent = (ViewGroup) findViewById(R.id.id_content);
		mButtonGroup = (ViewGroup) findViewById(R.id.id_button);
		mCancel = (Button) findViewById(R.id.id_cancel);
		mOK = (Button) findViewById(R.id.id_ok);
		
		mCancel.setOnClickListener(this);
		mOK.setOnClickListener(this);
	}

	public MyDialog(Context context, int width, int height, int gravity) {
		super(context, R.style.MyDialog);
		setContentView(R.layout.custom_dialog_layout);

		init(context, width, height, gravity);
	}

	public MyDialog(Context context, int layoutResId) {
		super(context, R.style.MyDialog);
		setContentView(layoutResId);

		getDisplay(context);
		init(context, (int) (screenWidth * 0.8), 0, Gravity.CENTER);
	}

	public MyDialog(Context context, int width, int height, int layoutResId,
			int gravity) {
		super(context, R.style.MyDialog);
		setContentView(layoutResId);

		init(context, width, height, gravity);
	}

	private void init(Context context, int width, int height, int gravity) {
		Window window = getWindow();
		LayoutParams params = window.getAttributes();
		if (width != 0) {
			params.width = width;
		}
		if (height != 0) {
			params.height = height;
		}
		params.gravity = gravity;
		window.setAttributes(params);
	}

	private void getDisplay(Context context) {
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		screenDensity = displayMetrics.density;
		screenWidth = displayMetrics.widthPixels;
		screenHeight = displayMetrics.heightPixels;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_cancel:
			cancel();
			mCancelAndOkListener.onCancel();
			break;
		case R.id.id_ok:
			cancel();
			mCancelAndOkListener.onOK();
			break;

		default:
			break;
		}
	}
	
	/**
	 * 设置标题栏是否显示
	 * @param isShow
	 */
	public void showCustomTitle(boolean isShow) {
		if (isShow) {
			mTitleTV.setVisibility(View.VISIBLE);
		} else {
			mTitleTV.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 设置标题栏的要显示的字符串
	 * @param title
	 */
	public void setCustomTitle(String title) {
		mTitleTV.setText(title);
	}
	
	/**
	 * 设置标题栏的要显示的字符串
	 * @param id
	 */
	public void setCustomTitle(int id) {
		mTitleTV.setText(id);
	}
	
	/**
	 * 设置需要显示的内容view
	 * @param view
	 */
	public void setCustomContentView(View view) {
		mContent.removeAllViews();
		mContent.addView(view);
	}
	
	/**
	 * 设置需要显示的内容view
	 * @param layoutId
	 */
	public void setCustomContentView(int layoutId) {
		mContent.removeAllViews();
		View view = View.inflate(getContext(), layoutId, null);
		mContent.addView(view);
	}
	
	/**
	 * 设置按钮栏是否显示
	 * @param isShow
	 */
	public void showCustomButtonGroup(boolean isShow) {
		if (isShow) {
			mButtonGroup.setVisibility(View.VISIBLE);
		} else {
			mButtonGroup.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 获得取消按钮的引用
	 * @return
	 */
	public Button getCancelButton() {
		return mCancel;
	}
	
	/**
	 * 获得确定按钮的引用
	 * @return
	 */
	public Button getOkButton() {
		return mOK;
	}
	
	
	/**
	 * 设置确定和取消按钮的监听事件
	 * @param listener
	 */
	public void setOnCancelAndOkClickListener(IOnCancelAndOkClickListener listener) {
		mCancelAndOkListener = listener;
	}

	public interface IOnCancelAndOkClickListener {
		public void onCancel();
		public void onOK();
	}
}
