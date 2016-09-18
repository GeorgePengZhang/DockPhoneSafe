package com.auratech.dockphonesafe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.auratech.dockphonesafe.service.DockService;

public class Main extends Activity implements OnClickListener {

	private ProgressDialog waitingDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Intent intent = new Intent(this, DockService.class);
		startService(intent);
		
		findViewById(R.id.id_whitelist).setOnClickListener(this);
		findViewById(R.id.id_balcklist).setOnClickListener(this);
		findViewById(R.id.id_disturb).setOnClickListener(this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(DockService.ACTION_SHOW_DIALOG);
		filter.addAction(DockService.ACTION_HIDE_DIALOG);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}
	
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null) {
				Log.d("TAG", "action:"+action);
				if (action.equals(DockService.ACTION_SHOW_DIALOG)) {
					showWaitingDialog();
				} else if (action.equals(DockService.ACTION_HIDE_DIALOG)) {
					hideWaitingDialog();
				}
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_whitelist:
			openActivityByClass(WhiteListActivity.class);
			break;
		case R.id.id_balcklist:
			openActivityByClass(BlackListActivity.class);
			break;
		case R.id.id_disturb:
			openActivityByClass(DisturbActivity.class);
			break;

		default:
			break;
		}
	}

	public void openActivityByClass(Class<?> cls) {
		Intent intent = new Intent(Main.this, cls);
		startActivity(intent);
	}
	
	private void showWaitingDialog() {
		if (waitingDialog == null) {
			waitingDialog = new ProgressDialog(Main.this);
			waitingDialog.setCancelable(false);
			waitingDialog.setMessage("Í¬²½ÖÐ...");
		}
		
		waitingDialog.show();
	}	
	
	private void hideWaitingDialog() {
		if (waitingDialog != null && waitingDialog.isShowing()) {
			waitingDialog.cancel();
		}
	}
}
