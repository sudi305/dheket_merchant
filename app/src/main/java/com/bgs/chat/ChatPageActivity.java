package com.bgs.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bgs.chat.adapters.ChatListAdapter;
import com.bgs.chat.services.ChatClientService;
import com.bgs.chat.viewmodel.ChatHelper;
import com.bgs.chat.widgets.Emoji;
import com.bgs.chat.widgets.EmojiView;
import com.bgs.chat.widgets.SizeNotifierRelativeLayout;
import com.bgs.common.Constants;
import com.bgs.common.DisplayUtils;
import com.bgs.common.ExtraParamConstants;
import com.bgs.dheket.App;
import com.bgs.dheket.merchant.R;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.ContactType;
import com.bgs.domain.chat.model.MessageReadStatus;
import com.bgs.domain.chat.model.MessageType;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ChatPageActivity extends AppCompatActivity implements SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate, NotificationCenter.NotificationCenterDelegate {

    private TextView userContactTextView;
    private ListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;

    private ImageView enterChatView1, emojiButton;
    private ImageButton goBackButton;
    private ChatListAdapter listAdapter;
    private EmojiView emojiView;
    private SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
    private boolean showingEmoji;
    private int keyboardHeight;
    private boolean keyboardVisible;
    private WindowManager.LayoutParams windowLayoutParams;

    private IContactRepository contactRepository;
    private IMessageRepository messageRepository;
    private ChatContact chatContact;
    private ChatClientService chatClientService;

    //private App app;
    private Activity getActivity() {
        return ChatPageActivity.this;
    }

    private static final String ACTION_CHAT_FROM_CONTACT = "com.bgs.chat.action.CHAT_FROM_CONTACT";
    private static final String ACTION_CHAT_FROM_HISTORY = "com.bgs.chat.action.CHAT_FROM_HISTORY";
    private static final String ACTION_CHAT_FROM_LOCATION = "com.bgs.chat.action.FROM_LOCATION";

    public static void startChatFromContact(Context context, ChatContact contact) {
        startChatActivity(context, ACTION_CHAT_FROM_CONTACT, contact);
        Log.d(Constants.TAG_CHAT, "START CHAT FROM CONTACT");
    }

    public static void startChatFromHistory(Context context, ChatContact contact) {
        startChatActivity(context, ACTION_CHAT_FROM_HISTORY, contact);
        Log.d(Constants.TAG_CHAT, "START CHAT FROM HISTORY");
    }


    private static void startChatActivity(Context context, String action, ChatContact contact) {
        Intent intent = new Intent(context, ChatPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(action);
        intent.putExtra(ExtraParamConstants.CHAT_CONTACT, contact);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON CREATE");
        setContentView(R.layout.activity_chatpage);

        //get object intent
        chatContact =  (ChatContact)getIntent().getParcelableExtra(ExtraParamConstants.CHAT_CONTACT);

        contactRepository = new ContactRepository(getApplicationContext());
        messageRepository = new MessageRepository(getApplicationContext());

        DisplayUtils.statusBarHeight = getStatusBarHeight();
        userContactTextView = (TextView) findViewById(R.id.user_contact);
        userContactTextView.setText(chatContact != null ? chatContact.getName() : "");

        chatMessages = new ArrayList<>();
        chatListView = (ListView) findViewById(R.id.chat_list_view);
        chatListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        chatEditText1 = (EditText) findViewById(R.id.chat_edit_text1);
        enterChatView1 = (ImageView) findViewById(R.id.enter_chat1);
        // Hide the emoji on click of edit text
        chatEditText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showingEmoji) hideEmojiPopup();
            }
        });

        goBackButton = (ImageButton) findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackActivity();
            }
        });

        emojiButton = (ImageView) findViewById(R.id.emojiButton);
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmojiPopup(!showingEmoji);
            }
        });

        listAdapter = new ChatListAdapter(chatMessages, getActivity());
        chatListView.setAdapter(listAdapter);

        chatEditText1.setOnKeyListener(keyListener);
        enterChatView1.setOnClickListener(clickListener);
        chatEditText1.addTextChangedListener(watcher1);

        sizeNotifierRelativeLayout = (SizeNotifierRelativeLayout) findViewById(R.id.chat_layout);
        sizeNotifierRelativeLayout.delegate = this;

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);

        chatEditText1.clearFocus();
        chatListView.requestFocus();

        chatClientService = App.getChatClientService();

        Log.d(Constants.TAG_CHAT, "ID => " + chatContact.getId());

        List<ChatMessage> messages ;/*messageRepository.getListMessageByContact(chatContact.getId());
        for(ChatMessage msg : messages) {
            Log.d(Constants.TAG_CHAT, String.format("MSG => %s, TIME => %s  ", msg.getMessageText(), new Date(msg.getCreateTime())));
        }
        */
        messages = messageRepository.getListMessageByContactAndDate(chatContact.getId(), new Date(System.currentTimeMillis()));
        //for(ChatMessage msg : messages) {
            //Log.d(Constants.TAG_CHAT, String.format("MSG => %s, READ-STATUS => %s, TYPE=%s  ", msg.getMessageText(), msg.getMessageReadStatus(), msg.getMessageType()));
        //}
        addMessage(messages);

    }

    @Override
    public void onBackPressed() {
        goBackActivity();
    }

    private void goBackActivity() {
        Intent intent = null;
       if ( getIntent().getAction().equalsIgnoreCase(ACTION_CHAT_FROM_CONTACT)
                || getIntent().getAction().equalsIgnoreCase(ACTION_CHAT_FROM_HISTORY)) {
            intent = new Intent(getActivity(), ChatHistoryActivity.class);
        }
        if ( intent != null ) {
            startActivity(intent);
            finish();
        }
    }

    public Map<String, BroadcastReceiver> makeReceivers(){
        Map<String, BroadcastReceiver> map = new HashMap<String, BroadcastReceiver>();
        map.put(ChatClientService.SocketEvent.CONNECT, connectReceiver);
        map.put(ChatClientService.SocketEvent.NEW_MESSAGE, newMessageReceiver);
        return map;
    }

    //SOCKET METHOD
    private void attemptSend() {
        //if (null == userContact.getName()) return;
        if (!chatClientService.isConnected()) return;

        String message = chatEditText1.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            chatEditText1.requestFocus();
            return;
        }

        chatEditText1.setText("");
        ChatMessage msg = ChatHelper.createMessage(chatContact.getId(), message, MessageType.OUT);
        addMessage(msg);

        Log.d(getResources().getString(R.string.app_name), "before send = " + message);
        JSONObject joMessage = new JSONObject();
        JSONObject user = new JSONObject();
        try {
            UserApp userApp = App.getUserApp();
            //user.put("id", String.valueOf(System.currentTimeMillis()));
            user.put("name", userApp.getName());
            user.put("email", userApp.getEmail());
            user.put("phone", userApp.getPhone());
            user.put("picture", userApp.getPicture());

            joMessage.put("from", user);
            joMessage.put("to", chatContact.getEmail());
            joMessage.put("msg", message);
        } catch (JSONException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        //message = String.format("{to:'%s',msg:'%s'}",userContact.getName(), message);
        // perform the sending message attempt.
        chatClientService.emitNewMessage(joMessage);

    }

    //call on create / resume : list source from db
    private void addMessage(List<ChatMessage> messages) {
        messageRepository.updateReadStatus(messages, MessageReadStatus.READ);
        chatMessages.addAll(messages);
        if(listAdapter!=null) {
            listAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }

    //call when user send message or receive new message
    private void addMessage(ChatMessage message) {
        if ( message.getMessageType() == MessageType.IN) {
            message.setMessageReadStatus(MessageReadStatus.READ);
            message.setReceiveTime(System.currentTimeMillis());
        }
        messageRepository.createOrUpdate(message);
        chatMessages.add(message);
        if(listAdapter!=null) {
            listAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }

    private void loginToChatServer() {
        if ( !chatClientService.isLogin() ) {
            JSONObject user = new JSONObject();
            try {
                UserApp userApp = App.getUserApp();
                user.put("name", userApp.getName());
                user.put("email", userApp.getEmail());
                user.put("phone", userApp.getPhone());
                user.put("picture", userApp.getPicture());
                chatClientService.emitDoLogin( user);
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
            }
        }
    }

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loginToChatServer();
        }
    };

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = intent.getStringExtra("data");
                    JSONObject from;
                    String message, email, name, phone, picture;
                    try {
                        JSONObject joData = new JSONObject(data);
                        from = joData.getJSONObject("from");
                        name = from.getString("name");
                        email = from.getString("email");
                        phone = from.getString("phone");
                        picture = from.getString("picture");
                        message = joData.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT );
                    Log.d(Constants.TAG_CHAT, String.format("from=%s\r\nmessage=%s ", from, message));
                    ChatContact contact = contactRepository.getContactByEmail(email);
                    if ( contact == null) {
                        contact = new ChatContact(name, picture, email, phone, ContactType.PRIVATE);
                    } else {
                        contact.setName(name);
                        contact.setPicture(picture);
                        contact.setPhone(phone);
                    }


                    contactRepository.createOrUpdate(contact);

                    //removeTyping(username);
                    ChatMessage msg = ChatHelper.createMessage(contact.getId(), message, MessageType.IN);
                    addMessage(msg);
                    messageRepository.createOrUpdate(msg);
                }
            });
        }
    };

    private EditText.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press

                EditText editText = (EditText) v;

                if(v==chatEditText1)
                {
                    editText.append("\n");
                }

                return true;
            }

            return false;

        }
    };

    private ImageView.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(v==enterChatView1)
            {
                //sendMessage(chatEditText1.getText().toString(), MessageType.SEND);
                attemptSend();
            }

            chatEditText1.setText("");

        }
    };

    private final TextWatcher watcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (chatEditText1.getText().toString().equals("")) {

            } else {
                enterChatView1.setImageResource(R.drawable.ic_chat_send);

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(editable.length()==0){
                enterChatView1.setImageResource(R.drawable.ic_chat_send);
            }else{
                enterChatView1.setImageResource(R.drawable.ic_chat_send_active);
            }
        }
    };


    /**
     * Show or hide the emoji popup
     *
     * @param show
     */
    private void showEmojiPopup(boolean show) {
        showingEmoji = show;

        if (show) {
            if (emojiView == null) {
                if (getActivity() == null) {
                    return;
                }
                emojiView = new EmojiView(getActivity());

                emojiView.setListener(new EmojiView.Listener() {
                    public void onBackspace() {
                        chatEditText1.dispatchKeyEvent(new KeyEvent(0, 67));
                    }

                    public void onEmojiSelected(String symbol) {
                        int i = chatEditText1.getSelectionEnd();
                        if (i < 0) {
                            i = 0;
                        }
                        try {
                            CharSequence localCharSequence = Emoji.replaceEmoji(symbol, chatEditText1.getPaint().getFontMetricsInt(), DisplayUtils.dp(20));
                            chatEditText1.setText(chatEditText1.getText().insert(i, localCharSequence));
                            int j = i + localCharSequence.length();
                            chatEditText1.setSelection(j, j);
                        } catch (Exception e) {
                            Log.e(Constants.TAG_CHAT, "Error showing emoji");
                        }
                    }
                });


                windowLayoutParams = new WindowManager.LayoutParams();
                windowLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                if (Build.VERSION.SDK_INT >= 21) {
                    windowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                } else {
                    windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
                    windowLayoutParams.token = getActivity().getWindow().getDecorView().getWindowToken();
                }
                windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            }

            final int currentHeight;

            if (keyboardHeight <= 0)
                keyboardHeight = App.getInstance().getSharedPreferences("emoji", 0).getInt("kbd_height", DisplayUtils.dp(200));

            currentHeight = keyboardHeight;

            WindowManager wm = (WindowManager) App.getInstance().getSystemService(Activity.WINDOW_SERVICE);

            windowLayoutParams.height = currentHeight;
            windowLayoutParams.width = DisplayUtils.displaySize.x;

            try {
                if (emojiView.getParent() != null) {
                    wm.removeViewImmediate(emojiView);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG_CHAT, e.getMessage());
            }

            try {
                wm.addView(emojiView, windowLayoutParams);
            } catch (Exception e) {
                Log.e(Constants.TAG_CHAT, e.getMessage());
                return;
            }

            if (!keyboardVisible) {
                if (sizeNotifierRelativeLayout != null) {
                    sizeNotifierRelativeLayout.setPadding(0, 0, 0, currentHeight);
                }

                return;
            }

        }
        else {
            removeEmojiWindow();
            if (sizeNotifierRelativeLayout != null) {
                sizeNotifierRelativeLayout.post(new Runnable() {
                    public void run() {
                        if (sizeNotifierRelativeLayout != null) {
                            sizeNotifierRelativeLayout.setPadding(0, 0, 0, 0);
                        }
                    }
                });
            }
        }


    }



    /**
     * Remove emoji window
     */
    private void removeEmojiWindow() {
        if (emojiView == null) {
            return;
        }
        try {
            if (emojiView.getParent() != null) {
                WindowManager wm = (WindowManager) App.getInstance().getSystemService(Context.WINDOW_SERVICE);
                wm.removeViewImmediate(emojiView);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG_CHAT, e.getMessage());
        }
    }



    /**
     * Hides the emoji popup
     */
    public void hideEmojiPopup() {
        if (showingEmoji) {
            showEmojiPopup(false);
        }
    }

    /**
     * Check if the emoji popup is showing
     *
     * @return
     */
    public boolean isEmojiPopupShowing() {
        return showingEmoji;
    }



    /**
     * Updates emoji views when they are complete loading
     *
     * @param id
     * @param args
     */
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (emojiView != null) {
                emojiView.invalidateViews();
            }

            if (chatListView != null) {
                chatListView.invalidateViews();
            }
        }
    }

    @Override
    public void onSizeChanged(int height) {

        Rect localRect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

        WindowManager wm = (WindowManager) App.getInstance().getSystemService(Activity.WINDOW_SERVICE);
        if (wm == null || wm.getDefaultDisplay() == null) {
            return;
        }


        if (height > DisplayUtils.dp(50) && keyboardVisible) {
            keyboardHeight = height;
            App.getInstance().getSharedPreferences("emoji", 0).edit().putInt("kbd_height", keyboardHeight).commit();
        }


        if (showingEmoji) {
            int newHeight = 0;

            newHeight = keyboardHeight;

            if (windowLayoutParams.width != DisplayUtils.displaySize.x || windowLayoutParams.height != newHeight) {
                windowLayoutParams.width = DisplayUtils.displaySize.x;
                windowLayoutParams.height = newHeight;

                wm.updateViewLayout(emojiView, windowLayoutParams);
                if (!keyboardVisible) {
                    sizeNotifierRelativeLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (sizeNotifierRelativeLayout != null) {
                                sizeNotifierRelativeLayout.setPadding(0, 0, 0, windowLayoutParams.height);
                                sizeNotifierRelativeLayout.requestLayout();
                            }
                        }
                    });
                }
            }
        }


        boolean oldValue = keyboardVisible;
        keyboardVisible = height > 0;
        if (keyboardVisible && sizeNotifierRelativeLayout.getPaddingBottom() > 0) {
            showEmojiPopup(false);
        } else if (!keyboardVisible && keyboardVisible != oldValue && showingEmoji) {
            showEmojiPopup(false);
        }

    }

    /**
     * Get the system status bar height
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //TODO
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON PAUSE");
        hideEmojiPopup();
        chatClientService.unregisterReceivers();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON RESUME");
        chatClientService.registerReceivers(makeReceivers());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON STOP");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //socket.off("new message", onNewMessage);
        //socket.off("user joined", onUserJoined);
        //socket.off("user left", onUserLeft);
        //socket.off("typing", onTyping);
        //socket.off("stop typing", onStopTyping);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);

    }


    private void scrollToBottom() {
        chatListView.smoothScrollToPosition(listAdapter.getCount() - 1);
    }


}
