package com.bgs.chat.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bgs.common.Constants;
import com.bgs.dheket.App;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.UserType;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by zhufre on 7/19/2016.
 */
public class ChatEngine {
    private boolean mLogin = false;
    private Socket mSocket;
    private Map<String, BroadcastReceiver> mReceivers = new HashMap<String, BroadcastReceiver>();
    private Context mContext;

    public ChatEngine(Context context) {
        mContext = context;
        initSocket();
    }

    private void initSocket()
    {
        try {
            IO.Options options = new IO.Options();
            //options.reconnectionAttempts = 1;
            //options.forceNew = true;
            options.reconnectionDelay = 1000;
            options.reconnectionDelayMax = 2000;
            options.timeout = 2000;
            mSocket = IO.socket(Constants.CHAT_SERVER_URL, options);
            mSocket.on(SocketEvent.CONNECT, onConnect);
            mSocket.on(SocketEvent.DISCONNECT, onDisconnect);
            mSocket.on(SocketEvent.CONNECT_ERROR, onConnectError);
            mSocket.on(SocketEvent.CONNECT_TIMEOUT, onConnectError);
            mSocket.on(SocketEvent.LOGIN, onLogin);
            mSocket.on(SocketEvent.USER_JOIN, onUserJoin);
            mSocket.on(SocketEvent.USER_LEFT, onUserLeft);
            mSocket.on(SocketEvent.TYPING, onTyping);
            mSocket.on(SocketEvent.STOP_TYPING, onStopTyping);
            mSocket.on(SocketEvent.LIST_CONTACT, onListContact);
            mSocket.on(SocketEvent.UPDATE_CONTACT, onUpdateContact);
            mSocket.on(SocketEvent.NEW_MESSAGE, onNewMessage);
            mSocket.on(SocketEvent.DELIVERY_STATUS, onDeliveryStatus);
            mSocket.connect();
        } catch (URISyntaxException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
    }

    public void emitDoLogin(final UserApp userApp) {
        if ( isLogin() || userApp == null) return;
        try {
            JSONObject user = new JSONObject();
            user.put("name", userApp.getName());
            user.put("email", userApp.getEmail());
            user.put("phone", userApp.getPhone());
            user.put("picture", userApp.getPicture());
            user.put("type", userApp.getType());
            emitDoLogin(user);
        } catch (JSONException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
    }

    public void emitDoLogin(final Object... args) {
        emit(SocketEmit.DO_LOGIN, args);
    }

    public void emitGetContacts(final Object... args) {
        emit(SocketEmit.GET_CONTACTS, args);
    }

    public void emitNewMessage(final Object... args) {
        emit(SocketEmit.NEW_MESSAGE, args);
    }

    public void emitDeliveryStatus(final Object... args) { emit(SocketEmit.DELIVERY_STATUS, args); }

    private void emit(final String event, final Object... args) {
        if ( mSocket.connected() )
            mSocket.emit(event, args);
    }

    private Emitter.Listener onDeliveryStatus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            if ( mReceivers.containsKey(SocketEvent.DELIVERY_STATUS))
                sendChatServiceBroadcast(SocketEvent.DELIVERY_STATUS, data.toString());
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            if ( mReceivers.containsKey(SocketEvent.NEW_MESSAGE))
                sendChatServiceBroadcast(SocketEvent.NEW_MESSAGE, data.toString());
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //JSONObject data = (JSONObject) args[0];
            Log.d(Constants.TAG_CHAT, getClass().getName() + " => CONNECTED");
            if ( mReceivers.containsKey(SocketEvent.CONNECT))
                sendChatServiceBroadcast(SocketEvent.CONNECT);
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mLogin = false;
            //JSONObject data = (JSONObject) args[0];
            Log.d(Constants.TAG_CHAT , getClass().getName() + " => DISCONNECTED");
            if ( mReceivers.containsKey(SocketEvent.DISCONNECT))
                sendChatServiceBroadcast(SocketEvent.DISCONNECT);
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mLogin = false;
            //JSONObject data = (JSONObject) args[0];
            Log.d(Constants.TAG_CHAT , getClass().getName() + " => CONNECTION ERROR");
            if ( mReceivers.containsKey(SocketEvent.CONNECT_ERROR))
                sendChatServiceBroadcast(SocketEvent.CONNECT_ERROR);
        }
    };

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            try {
                mLogin = data.getBoolean("success");
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT , e.getMessage(), e);
                return;
            }
            Log.d(Constants.TAG_CHAT , getClass().getName() + " => Login " + mLogin);
            if ( mReceivers.containsKey(SocketEvent.LOGIN))
                sendChatServiceBroadcast(SocketEvent.LOGIN, data.toString());
        }
    };

    private Emitter.Listener onUserJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            if ( mReceivers.containsKey(SocketEvent.USER_JOIN))
                sendChatServiceBroadcast(SocketEvent.USER_JOIN, data.toString());

        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(Constants.TAG_CHAT, getClass().getName() + " => UserLeft " + data.toString());
            if ( mReceivers.containsKey(SocketEvent.USER_LEFT))
                sendChatServiceBroadcast(SocketEvent.USER_LEFT, data.toString());
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(Constants.TAG_CHAT, getClass().getName() + " => Typing " + data.toString());
            if ( mReceivers.containsKey(SocketEvent.TYPING))
                sendChatServiceBroadcast(SocketEvent.TYPING, data.toString());

        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d(Constants.TAG_CHAT, getClass().getName() + " => StopTyping " + data.toString());
            if ( mReceivers.containsKey(SocketEvent.STOP_TYPING))
                sendChatServiceBroadcast(SocketEvent.STOP_TYPING, data.toString());

        }
    };


    private Emitter.Listener onListContact = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            //Log.d(Constants.TAG_CHAT, "onListContact = " + data.toString());
            if ( mReceivers.containsKey(SocketEvent.LIST_CONTACT))
                sendChatServiceBroadcast(SocketEvent.LIST_CONTACT, data.toString());
        }
    };

    private Emitter.Listener onUpdateContact = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            if ( mReceivers.containsKey(SocketEvent.UPDATE_CONTACT))
                sendChatServiceBroadcast(SocketEvent.UPDATE_CONTACT, data.toString());
        }

    };

    private void sendChatServiceBroadcast(String event){
        sendChatServiceBroadcast(event, null);
    }
    private void sendChatServiceBroadcast(String event, String data){
        Intent intent = new Intent(event);
        if ( data != null ) intent.putExtra("data", data);

        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public boolean isLogin() {return mLogin;}
    public Socket getSocket() { return mSocket; }
    public boolean isConnected() {return mSocket.connected();}

    public void registerReceivers(Map<String, BroadcastReceiver> receivers) {
        synchronized (mReceivers) {
            unregisterReceivers();
            mReceivers = receivers;
            //re-register
            for (String event : mReceivers.keySet()) {
                LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceivers.get(event), new IntentFilter(event));
            }
        }
    }

    private void unregisterReceivers() {
        //Log.d(Constants.TAG_CHAT, "unregisterReceivers()=>mRegistered=" + mRegistered);
        for (String event : mReceivers.keySet()) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceivers.get(event));
        }
    }


    public static class SocketEmit {
        //EVENT OUT
        public final static String DO_LOGIN = "do login";
        public final static String NEW_MESSAGE = "new message";
        public final static String DELIVERY_STATUS = "delivery status";
        public final static String GET_CONTACTS = "get contacts";
    }


    public static class SocketEvent
    {
        public final static String CONNECT = Socket.EVENT_CONNECT;
        public final static String DISCONNECT = Socket.EVENT_DISCONNECT;
        public final static String CONNECT_ERROR = Socket.EVENT_CONNECT_ERROR;
        public final static String CONNECT_TIMEOUT = Socket.EVENT_CONNECT_TIMEOUT;

        public final static String LOGIN = "login";
        public final static String USER_JOIN = "user join";
        public final static String USER_LEFT = "user left";
        public final static String NEW_MESSAGE = "new message";
        public final static String DELIVERY_STATUS = "delivery status";
        public final static String TYPING = "typing";
        public final static String STOP_TYPING = "stop typing";
        public final static String LIST_CONTACT = "list contact";
        public final static String UPDATE_CONTACT = "update contact";


        private static String[] EVENTS = {
                CONNECT, DISCONNECT,
                CONNECT_ERROR, CONNECT_TIMEOUT,
                LOGIN, USER_JOIN,
                USER_LEFT, NEW_MESSAGE, DELIVERY_STATUS,
                TYPING, STOP_TYPING,
                LIST_CONTACT, UPDATE_CONTACT
        };
    }

}
