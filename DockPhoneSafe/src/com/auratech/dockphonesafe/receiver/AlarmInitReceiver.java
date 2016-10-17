/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.auratech.dockphonesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.auratech.dockphonesafe.service.DockService;
import com.auratech.dockphonesafe.utils.AlarmAlertWakeLock;
import com.auratech.dockphonesafe.utils.AlarmUtils;
import com.auratech.dockphonesafe.utils.AsyncHandler;
import com.auratech.dockphonesafe.utils.Utils;

public class AlarmInitReceiver extends BroadcastReceiver {

    ///M: Power off alarm for IPO boot@{
    private static final String IPO_BOOT_ACTION = "android.intent.action.ACTION_BOOT_IPO";
    private static Boolean sIPOBOOT = false;
    ///@}
    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        Utils.writeLogToSdcard("AlarmInitReceiver " + action);

        /* M: Power off alarm
         * Note: Never call updateNextAlarm when the device is boot from power off
         * alarm, since it would make the power off alarm dismiss the wrong alarm. @{
         */
        if (IPO_BOOT_ACTION.equals(action)) {
            Log.d("TAG","Receive android.intent.action.ACTION_BOOT_IPO intent.");
            setIPOBootValue(true);
            return;
        }
        //The TIME_SET broadcast is send by IPO, ACTION_TIME_CHANGED = TIME_SET
        if (getIPOBootValue() && action.equals(Intent.ACTION_TIME_CHANGED)) {
            Log.d("TAG","IPO boot time changed return");
            return;
        }
        ///@}

        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();

        // We need to increment the global id out of the async task to prevent
        // race conditions
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                // Remove the snooze alarm after a boot.
                if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                    setIPOBootValue(false);
                    
                    Intent intent = new Intent(context, DockService.class);
            		context.startService(intent);
                }

                // Update all the alarm instances on time change event
                AlarmUtils.fixAlarmInstances(context);

                result.finish();
                Utils.writeLogToSdcard("AlarmInitReceiver finished");
                wl.release();
            }
        });
    }

    ///M: get the value of IPO boot @{
    private boolean getIPOBootValue() {
        return sIPOBOOT;
    }
    ///@}

    ///M: set the value of IPO boot @{
    private void setIPOBootValue(boolean mValue) {
        sIPOBOOT = mValue;
    }
    ///@}
}
