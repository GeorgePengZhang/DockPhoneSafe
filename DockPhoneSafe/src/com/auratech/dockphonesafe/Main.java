package com.auratech.dockphonesafe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.auratech.dockphonesafe.fragment.BlackListFragment;
import com.auratech.dockphonesafe.fragment.SettingsFragment;
import com.auratech.dockphonesafe.fragment.WhiteListFragment;
import com.auratech.dockphonesafe.service.DockService;
import com.auratech.dockphonesafe.view.PagerSlidingTabStrip;

public class Main extends FragmentActivity {

	private PagerSlidingTabStrip mTabs;
	private ViewPager mPager;
	
	private final String[] TITLE = {
		"Black List",
		"White List",
		"Settings"
	};
	private ArrayList<Fragment> mFragList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.main);
		
		Intent intent = new Intent(this, DockService.class);
		startService(intent);
		
		mFragList = new ArrayList<Fragment>();
		mFragList.add(new BlackListFragment());
		mFragList.add(new WhiteListFragment());
		mFragList.add(new SettingsFragment());
		
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mPagerAdapter);
		
		mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mTabs.setViewPager(mPager);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	PagerAdapter mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
		
		@Override
		public int getCount() {
			return TITLE.length;
		}
		
		@Override
		public Fragment getItem(int position) {
			return mFragList.get(position);
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return TITLE[position];
		}
	};
}
