package com.bgs.chat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bgs.chat.services.ChatClientService;

/**
 * Created by zhufre on 7/8/2016.
 */
public class BootAndUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "BootAndUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent.getAction().equals("android.intent.action.MY_PACKAGE_REPLACED")) {
            Intent startServiceIntent = new Intent(context, ChatClientService.class);
            context.startService(startServiceIntent);
        }
    }
}