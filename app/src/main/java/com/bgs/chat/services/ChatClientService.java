package com.bgs.chat.services;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bgs.chat.viewmodel.ChatHelper;
import com.bgs.common.Constants;
import com.bgs.dheket.App;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.MessageSendStatus;
import com.bgs.domain.chat.model.MessageType;
import com.bgs.domain.chat.model.UserType;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhufre on 6/26/2016.
 */
public class ChatClientService extends Service {
    private ChatEngine chatEngine;
    private IMessageRepository messageRepository;
    private IContactRepository contactRepository;
    private static Map<String, BroadcastReceiver> mReceivers = new HashMap<String, BroadcastReceiver>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.TAG_CHAT, getClass().getName() + " => onStartCommand");
        messageRepository = new MessageRepository(getApplicationContext());
        contactRepository = new ContactRepository(getApplicationContext());

        chatEngine = App.getChatEngine();
        chatEngine.registerReceivers( makeReceivers());

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Map<String, BroadcastReceiver> makeReceivers(){
        Map<String, BroadcastReceiver> map = new HashMap<String, BroadcastReceiver>();
        map.put(ChatEngine.SocketEvent.CONNECT, onConnectReceiver);
        map.put(ChatEngine.SocketEvent.LOGIN, onLoginReceiver);
        map.put(ChatEngine.SocketEvent.USER_JOIN, userJoinReceiver);
        map.put(ChatEngine.SocketEvent.NEW_MESSAGE, newMessageReceiver);
        map.put(ChatEngine.SocketEvent.DELIVERY_STATUS, deliveryStatusReceiver);
        map.put(ChatEngine.SocketEvent.LIST_CONTACT, listContactReceiver);
        return map;
    }

    public void loginWithFbAccount() {
        if ( AccessToken.getCurrentAccessToken() == null ) return;
        try {
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {

                    JSONObject json = response.getJSONObject();
                    try {
                        if (json != null) {
                            Log.d(Constants.TAG, "json fb = " + json.toString());
                            String id = json.getString("id");
                            String name = json.getString("name");
                            String gender = json.getString("gender");
                            String email = json.getString("email");
                            String imageUsr = json.getString("picture");

                            String profilePicUrl = "";
                            if (json.has("picture")) {
                                profilePicUrl = json.getJSONObject("picture").getJSONObject("data").getString("url");
                            }

                            //update user app
                            //add by supri 2016/6/16
                            UserApp userApp = App.getUserApp();
                            if (userApp == null) userApp = new UserApp();
                            userApp.setName(name);
                            userApp.setEmail(email);
                            userApp.setId(id);
                            userApp.setPicture(profilePicUrl);
                            userApp.setType(Constants.USER_TYPE);
                            App.updateUserApp(userApp);
                            Log.d(Constants.TAG, "App.getInstance().getUserApp()=" + App.getUserApp());
                            //DO LOGIN
                            chatEngine.emitDoLogin(App.getUserApp());
                        }

                    } catch (JSONException e) {
                        Log.e(Constants.TAG, e.getMessage(), e);
                    }
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,email,gender,picture.type(large)");
            request.setParameters(parameters);
            request.executeAsync();
        } catch (NetworkOnMainThreadException ne) {
            Log.e(Constants.TAG_CHAT, ne.getMessage(), ne);
        }
    }

    public void emitDoLogin() {
        loginWithFbAccount();
    }

    private BroadcastReceiver onConnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG_CHAT , getClass().getName() + " => EMIT DO LOGIN ");
            emitDoLogin();
        }
    };

    private BroadcastReceiver onLoginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG_CHAT , getClass().getName() + " => EMIT GET CONTACTS ");
            chatEngine.emitGetContacts();
        }
    };

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            String data = intent.getStringExtra("data");
            String msgid, text, email, name, phone, picture, type;
            try {
                JSONObject joData = new JSONObject(data);
                Log.d(Constants.TAG_CHAT, "::::" + getClass().getName() + " => new message = " + joData);
                JSONObject joFrom = joData.getJSONObject("from");
                name = joFrom.getString("name");
                email = joFrom.getString("email");
                phone = joFrom.getString("phone");
                picture = joFrom.getString("picture");
                type = joFrom.getString("type");

                JSONObject joMsg = joData.getJSONObject("msg");
                msgid = joMsg.getString("msgid");
                text = joMsg.getString("text");

            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
                return;
            }

            ChatContact contact = contactRepository.getContactByEmail(email, UserType.parse(type));
            if ( contact == null) {
                contact = new ChatContact(name, picture, email, phone, UserType.parse(type));
            } else {
                contact.setName(name);
                contact.setPicture(picture);
                contact.setPhone(phone);
                contact.setUserType(UserType.parse(type));
            }
            contactRepository.createOrUpdate(contact);

            //check existing before save
            ChatMessage msg = messageRepository.getMessageInByContactAndMsgid(contact.getId(), msgid);
            if ( msg == null ) {
                msg = ChatHelper.createMessageIn(msgid, contact.getId(), text);
                messageRepository.createOrUpdate(msg);
            }
            //update reply status latest out message
            final ChatMessage repliedMsg = messageRepository.getLastMessageOutByContact(contact.getId());
            //if exist
            if ( repliedMsg != null ) {
                repliedMsg.setMessageSendStatus(MessageSendStatus.REPLIED);
                messageRepository.createOrUpdate(repliedMsg);
            }
            sendNewMessageEventBroadcast(contact, repliedMsg, msg);
            //send delivery status to sender
            try {
                final JSONObject joTo = new JSONObject();
                joTo.put("email", contact.getEmail());
                joTo.put("type", contact.getUserType());
                final JSONObject joMsg = new JSONObject();
                joMsg.put("msgid", msgid);
                joMsg.put("status", MessageSendStatus.DELIVERED.ordinal());
                JSONObject joData = new JSONObject() {{
                    put("to", joTo);
                    put("msg", joMsg);
                }};
                chatEngine.emitDeliveryStatus(joData);
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
            }
        }
    };

    private BroadcastReceiver deliveryStatusReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            String data = intent.getStringExtra("data");
            String msgid, email, type;
            int status = 0;
            try {
                JSONObject joData = new JSONObject(data);
                Log.d(Constants.TAG_CHAT, "::::" + getClass().getName() + " <= delivery status = " + joData);
                JSONObject joTo = joData.getJSONObject("to");
                email = joTo.getString("email");
                type = joTo.getString("type");

                JSONObject joMsg = joData.getJSONObject("msg");
                msgid = joMsg.getString("msgid");
                status = joMsg.getInt("status");

            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
                return;
            }

            ChatContact contact = contactRepository.getContactByEmail(email, UserType.parse(type));
            if ( contact != null ) {

                ChatMessage msg = messageRepository.getMessageOutByContactAndMsgid(contact.getId(), msgid);
                Log.d(Constants.TAG_CHAT, getClass().getName() +  String.format(" => DELIVERED STATUS MSGID=%s, MSG=%s, FROM=%s-%s", msgid, msg, contact.getId(), contact.getUserType() ));
                if ( msg != null ) {
                    //jika sudah replied tidak update
                    if ( MessageSendStatus.REPLIED.equals(msg.getMessageSendStatus()) == false ) {
                        msg.setMessageSendStatus(MessageSendStatus.parse(status));
                        messageRepository.createOrUpdate(msg);
                        //broadcast delivery status to activity
                        sendDeliveryStatusEventBroadcast(contact, msg);
                    }
                }
            }
        }
    };

    private BroadcastReceiver listContactReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            String data = intent.getStringExtra("data");
            //Log.d(getResources().getString(R.string.app_name), "list contact ");
            JSONArray contacts = new JSONArray();
            try {
                JSONObject joData = new JSONObject(data);
                contacts = joData.getJSONArray("contacts");
                //Toast.makeText(getApplicationContext(), "Login1 " + isLogin, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
                return;
            }
            Log.d(Constants.TAG_CHAT, "::::" + getClass().getName() + " => list contacts = " + contacts);
            //Toast.makeText(getApplicationContext(), "Login2 " + isLogin, Toast.LENGTH_SHORT).show();

            final ArrayList<ChatContact> contactList = new ArrayList<ChatContact>();
            for(int i = 0; i < contacts.length(); i++) {
                try {
                    JSONObject joContact = contacts.getJSONObject(i);
                    String id = joContact.getString("id");
                    String name = joContact.getString("name");
                    String email = joContact.getString("email");
                    String phone = joContact.getString("phone");
                    String picture = joContact.getString("picture");
                    String type = joContact.getString("type");
                    //skip contact for current app user

                    //if ( email.equalsIgnoreCase(app.getUserApp().getEmail())) continue;
                    ChatContact contact = contactRepository.getContactByEmail(email, UserType.parse(type));
                    if ( contact == null ) {
                        contact = new ChatContact(name, picture, email, phone, UserType.parse(type));
                    } else {
                        contact.setName(name);
                        contact.setPicture(picture);
                        contact.setPhone(phone);
                    }
                    //save new or update
                    contactRepository.createOrUpdate(contact);
                    contactList.add(contact);

                } catch (JSONException e) {
                    Log.d(Constants.TAG_CHAT,e.getMessage(), e);
                }
            }

            sendListContactEventBroadcast(contactList);

        }
    };

    private BroadcastReceiver userJoinReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            try {
                JSONObject joData = new JSONObject(data);
                JSONObject user = joData.getJSONObject("user");
                Log.d(Constants.TAG_CHAT, "::::" + getClass().getCanonicalName() + " => User Join = " + user.getString("email"));
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
                return;
            }
            //retrive contact
            chatEngine.emitGetContacts();
            //ChatTaskService.startActionGetContacts(getActivity());
        }
    };

    private void sendListContactEventBroadcast(ArrayList<ChatContact> contactList){
        Intent intent = new Intent(ActivityEvent.LIST_CONTACT);
        if ( contactList != null ) intent.putExtra("contactList", contactList);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
    }

    private void sendNewMessageEventBroadcast(ChatContact contact, ChatMessage repliedMsg, ChatMessage msg){
        Intent intent = new Intent(ActivityEvent.NEW_MESSAGE);
        if ( contact != null ) intent.putExtra("contact", contact);
        if ( msg != null ) intent.putExtra("msg", msg);
        if ( repliedMsg != null) intent.putExtra("repliedMsg", repliedMsg);

        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
    }

    private void sendDeliveryStatusEventBroadcast(ChatContact contact, ChatMessage msg){
        Intent intent = new Intent(ActivityEvent.DELIVERY_STATUS);
        if ( contact != null ) intent.putExtra("contact", contact);
        if ( msg != null ) intent.putExtra("msg", msg);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
    }

    public static void registerReceivers(Map<String, BroadcastReceiver> receivers) {
        synchronized (mReceivers) {
            unregisterReceivers();
            mReceivers = receivers;
            //re-register
            for (String event : mReceivers.keySet()) {
                LocalBroadcastManager.getInstance(App.getInstance()).registerReceiver(mReceivers.get(event), new IntentFilter(event));
            }
        }
    }

    private static void unregisterReceivers() {
        //Log.d(Constants.TAG_CHAT, "unregisterReceivers()=>mRegistered=" + mRegistered);
        for (String event : mReceivers.keySet()) {
            LocalBroadcastManager.getInstance(App.getInstance()).unregisterReceiver(mReceivers.get(event));
        }
    }

    public static class ActivityEvent
    {
        public final static String LOGIN = "activity login";
        public final static String USER_JOIN = "activity user join";
        public final static String USER_LEFT = "activity user left";
        public final static String NEW_MESSAGE = "activity new message";
        public final static String DELIVERY_STATUS = "activity delivery status";
        public final static String TYPING = "activity typing";
        public final static String STOP_TYPING = "activity stop typing";
        public final static String LIST_CONTACT = "activity list contact";
        public final static String UPDATE_CONTACT = "activity update contact";


        private static String[] EVENTS = {
                LOGIN, USER_JOIN,
                USER_LEFT, NEW_MESSAGE, DELIVERY_STATUS,
                TYPING, STOP_TYPING,
                LIST_CONTACT, UPDATE_CONTACT
        };
    }
}

