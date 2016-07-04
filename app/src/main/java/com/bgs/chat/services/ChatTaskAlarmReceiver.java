package com.bgs.chat.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by zhufre on 6/15/2016.
 */
public class ChatTaskAlarmReceiver extends BroadcastReceiver {
    public static int REQUEST_CODE = 89898;
    private static final String ACTION_UPDATE_CONTACT = "com.bgs.chat.services.action.UPDATE_CONTACT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent != null ) {
            String action = intent.getAction();
            if ( ACTION_UPDATE_CONTACT.equalsIgnoreCase(action)) {
                ChatTaskService.startActionUpdateContact(context);
            }
        }
    }

    /**
     *
     * @param context
     * @param interval
     */
    public static void startUpdateContactScheduler(Context context, long interval) {
        Intent intent = new Intent(context.getApplicationContext(), ChatTaskAlarmReceiver.class);
        intent.setAction(ACTION_UPDATE_CONTACT);
        schedule(context, intent, interval);
    }

    /**
     *
     * @param context
     * @param intent
     * @param interval
     */
    private static void schedule(Context context, Intent intent, long interval) {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }


}
