/**  

 * Copyright © 2016公司名字. All rights reserved.

 * @Title: ListActivity.java

 * @Prject: DockPhoneSafe

 * @Package: com.auratech.dockphonesafe

 * @Description: TODO

 * @author: Administrator  

 * @date: Oct 9, 2016 6:00:32 PM

 */
package com.auratech.dockphonesafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.auratech.dockphonesafe.fragment.CallsLogListFragment;
import com.auratech.dockphonesafe.fragment.ContactsPhoneListFragment;

/**
 * @ClassName: ListActivity
 * @Description: TODO
 * @author: steven zhang
 * @date: Oct 9, 2016 6:00:32 PM
 */
public class ListActivity extends FragmentActivity implements OnClickListener {
	
	public static final String SHOW_TYPE = "type";
	public static final int SHOW_CONTACTS = 0;
	public static final int SHOW_CALLLOG  = 1;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.contacts_main);
		
		findViewById(R.id.id_back).setOnClickListener(this);
		TextView title = (TextView) findViewById(R.id.id_title);
		
		Intent intent = getIntent();
		int type = intent.getIntExtra(SHOW_TYPE, SHOW_CONTACTS);
		if (type == SHOW_CONTACTS) {
			title.setText(R.string.contacts);
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new ContactsPhoneListFragment()).commit();  
		} else if (type == SHOW_CALLLOG) {
			title.setText(R.string.call_log);
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new CallsLogListFragment()).commit();  
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_back:
			finish();
			break;

		default:
			break;
		}
	}
}
