package com.bgs.chat.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bgs.dheket.App;
import com.bgs.common.Constants;

public class ChatTaskService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_UPDATE_CONTACT = "com.bgs.chat.services.action.UPDATE_CONTACT";
    private static final String ACTION_GET_CONTACTS = "com.bgs.chat.services.action.GET_CONTACTS";

    //private static final String EXTRA_PARAM1 = "in.co.madhur.chatbubblesdemo.extra.PARAM1";
    //private static final String EXTRA_PARAM2 = "in.co.madhur.chatbubblesdemo.extra.PARAM2";

    private ChatClientService chatClientService;

    public ChatTaskService() {
        super("ChatTaskService");
    }

    public static void startActionUpdateContact(Context context) {
        Intent intent = new Intent(context, ChatTaskService.class);
        intent.setAction(ACTION_UPDATE_CONTACT);
        context.startService(intent);
    }

    public static void startActionGetContacts(Context context) {
        Intent intent = new Intent(context, ChatTaskService.class);
        intent.setAction(ACTION_GET_CONTACTS);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Log.d("DHEKET", "handle intent...");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_CONTACT.equals(action)) {
                //final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                //final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdateContact();
            } else if (ACTION_GET_CONTACTS.equals(action)) {
                handleActionGetContacts();
            }
        }
    }

    /**
     * update ui contact
     * parameters.
     */
    private void handleActionUpdateContact() {
        try {
            chatClientService = App.getChatClientService();
            //Log.d("DHEKET", "IS LOGIN "+ ((MainChatActivity)getApplicationContext()).isLogin());
            //Log.d("DHEKET", "run background services get contacts...");


        }catch (Exception e) {
            Log.e(Constants.TAG_CHAT,e.getMessage(), e);
        }
    }

    /***
     * send message to server for GET CONTACTS
     */
    private void handleActionGetContacts() {
        try {
            chatClientService = App.getChatClientService();
            //Log.d("DHEKET", "IS LOGIN "+ ((MainChatActivity)getApplicationContext()).isLogin());
            Log.d(Constants.TAG_CHAT, "run background get contacts...");
            if ( chatClientService.isConnected() ) chatClientService.emit(ChatClientService.SocketEmit.GET_CONTACTS);

        }catch (Exception e) {
            Log.e(Constants.TAG_CHAT,e.getMessage(), e);
        }
    }
}
