package org.hcilab.circog_watch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoStart extends BroadcastReceiver {

    private static final String TAG	= AutoStart.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (CircogPrefs.RESTART_SERVICE_AFTER_REBOOT) {
            Log.i(TAG, "restarting Circog NotificationScheduler");

            // Start the notification trigger service
            if(Util.getBool(context, CircogPrefs.PREF_CONSENT_GIVEN, false)) {
                Intent intentNotificationTriggerService = new Intent(context, NotificationTriggerService.class);
                context.startForegroundService(intentNotificationTriggerService);
            }
        } else {
            Log.i(TAG, "restarting of Circog NotificationScheduler aborted -- no consent yet");
        }

    }
}
