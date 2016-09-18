package com.auratech.dockphonesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.auratech.dockphonesafe.utils.AlarmAlertWakeLock;
import com.auratech.dockphonesafe.utils.AlarmUtils;
import com.auratech.dockphonesafe.utils.AsyncHandler;

public class AlarmStateManager extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, final Intent intent) {

		final PendingResult result = goAsync();
		final PowerManager.WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
		wl.acquire();
		Log.d("TAG", "AlarmStateManager"+intent.getAction());
		AsyncHandler.post(new Runnable() {
			@Override
			public void run() {
				AlarmUtils.updateNextAlarmInstance(context, intent);
				result.finish();
				wl.release();
			}
		});
	}
}
