package com.auratech.dockphonesafe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.auratech.dockphonesafe.service.DockService;
import com.auratech.dockphonesafe.service.DockService.OnDockInfoListener;

public class MainActivity extends Activity implements OnDockInfoListener {

	private ListView mListView;
	private List<HashMap<String, String>> mList;
	private SimpleAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		Intent intent = new Intent(this, DockService.class);
		startService(intent);
		
		mList = new ArrayList<HashMap<String,String>>();
		mListView = (ListView) findViewById(R.id.id_listview);
		mAdapter = new SimpleAdapter(this, mList, android.R.layout.simple_list_item_1, new String[]{"result"}, new int[]{android.R.id.text1});
		mListView.setAdapter(mAdapter);
		
		DockService.addOnDockInfoListener(this);
		
//		new AlphabetIndexer(cursor, sortedColumnIndex, alphabet);
		
		int [] a = new int[] {0, 5, 6, 8, 12, 19};
		
		for (int i = 0; i < 21; i++) {
			Log.d("TAG", "i:"+i+",value:"+Arrays.binarySearch(a, i));
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DockService.removeOnDockInfoListener(this);
	}


	@Override
	public void onUpdated(String result) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("result", result);
		mList.add(map);
		mAdapter.notifyDataSetChanged();
	}

}
