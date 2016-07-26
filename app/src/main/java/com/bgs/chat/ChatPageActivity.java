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
import com.bgs.chat.viewmodel.ChatHistory;
import com.bgs.chat.widgets.Emoji;
import com.bgs.chat.widgets.EmojiView;
import com.bgs.chat.widgets.SizeNotifierRelativeLayout;
import com.bgs.common.Constants;
import com.bgs.common.DisplayUtils;
import com.bgs.common.ExtraParamConstants;
import com.bgs.common.UUIDUtils;
import com.bgs.dheket.App;
import com.bgs.dheket.general.CircleTransform;
import com.bgs.dheket.merchant.R;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.MessageReadStatus;
import com.bgs.domain.chat.model.MessageSendStatus;
import com.bgs.domain.chat.model.MessageType;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;
import com.squareup.picasso.Picasso;

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

    private TextView contactNameTextView;
    private ListView chatListView;
    private EditText chatEditText1;
    private ArrayList<ChatMessage> chatMessages;

    private ImageView enterChatView1, emojiButton, contactPicture;
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

    Picasso picasso;
    //private App app;
    private Activity getActivity() {
        return ChatPageActivity.this;
    }


    private static final String ACTION_CHAT_FROM_CONTACT = "com.bgs.chat.action.CHAT_FROM_CONTACT";
    private static final String ACTION_CHAT_FROM_HISTORY = "com.bgs.chat.action.CHAT_FROM_HISTORY";

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
        contactPicture = (ImageView) findViewById(R.id.contact_picture);
        if ( chatContact != null && chatContact.getPicture() != null ) {
            picasso.with(this).load(chatContact.getPicture()).transform(new CircleTransform()).into(contactPicture);
        } else {
            picasso.with(this).load(R.drawable.com_facebook_profile_picture_blank_portrait).transform(new CircleTransform()).into(contactPicture);
        }
        contactNameTextView = (TextView) findViewById(R.id.contact_name);
        contactNameTextView.setText(chatContact != null ? chatContact.getName() : "");

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

        Log.d(Constants.TAG_CHAT, "ID => " + chatContact.getId());

        loadMessage();

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

    //SOCKET METHOD
    public Map<String, BroadcastReceiver> makeReceivers(){
        Map<String, BroadcastReceiver> map = new HashMap<String, BroadcastReceiver>();
        map.put(ChatClientService.ActivityEvent.NEW_MESSAGE, newMessageReceiver);
        map.put(ChatClientService.ActivityEvent.DELIVERY_STATUS, deliveryStatusReceiver);
        return map;
    }

    private void attemptSend() {
        //message always created if offline will be send simultanously until delevered
        //if (!App.getChatEngine().isConnected()) return;

        String message = chatEditText1.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            chatEditText1.requestFocus();
            return;
        }

        chatEditText1.setText("");
        final ChatMessage msg = ChatHelper.createMessageOut(UUIDUtils.uuidAsBase64(), chatContact.getId(), message);
        //save and add to list
        boolean success = addMessageOut(msg);
        if ( success ) {
            if(listAdapter!=null) {
                listAdapter.notifyDataSetChanged();
                scrollToBottom();
            }
        }
        else return;


        try {
            final UserApp userApp = App.getUserApp();
            JSONObject from = new JSONObject() {{
                put("name",userApp.getName());
                put("email",userApp.getEmail());
                put("phone",userApp.getPhone());
                put("picture",userApp.getPicture());
                put("type",userApp.getType());
            }};

            JSONObject to = new JSONObject() {
                {
                    put("name", chatContact.getName());
                    put("email", chatContact.getEmail());
                    put("phone", chatContact.getPhone());
                    put("picture", chatContact.getPicture());
                    put("type", chatContact.getUserType());
                }};

            JSONObject joMsg = new JSONObject() {{
                put("msgid", msg.getMsgid());
                put("text", msg.getMessageText());
            }};

            JSONObject joMessage = new JSONObject();
            joMessage.put("from", from);
            joMessage.put("to", to);
            joMessage.put("msg", joMsg);

            Log.d(Constants.TAG_CHAT, getClass().getName() + " => before send = " + joMessage);
            // perform the sending message attempt.
            App.getChatEngine().emitNewMessage(joMessage);
        } catch (JSONException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }

    }

    //call on create / resume : list source from db
    private void loadMessage() {
        List<ChatMessage> messages ;/*messageRepository.getListMessageByContact(chatContact.getId());
        for(ChatMessage msg : messages) {
            Log.d(Constants.TAG_CHAT, String.format("MSG => %s, TIME => %s  ", msg.getMessageText(), new Date(msg.getCreateTime())));
        }
        */
        messages = messageRepository.getListMessageByContactAndDate(chatContact.getId(), new Date(System.currentTimeMillis()));
        //for(ChatMessage msg : messages) {
        //Log.d(Constants.TAG_CHAT, String.format("MSG => %s, READ-STATUS => %s, TYPE=%s  ", msg.getMessageText(), msg.getMessageReadStatus(), msg.getMessageType()));
        //}
        messageRepository.updateReadStatus(messages, MessageReadStatus.READ);
        chatMessages.addAll(messages);
        if(listAdapter!=null) {
            listAdapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }

    //call when user send message or receive new message
    private boolean addMessageOut(ChatMessage message) {
        try {
            if (message.getMessageType() != MessageType.OUT) return false;

            messageRepository.createOrUpdate(message);
            chatMessages.add(message);
            return true;
        } catch ( Exception e ) {
            Log.e(Constants.TAG_CHAT, e.getMessage(),e);
        }

        return false;
    }

    private boolean addMessageIn(ChatMessage message) {
        try {
            if (message.getMessageType() != MessageType.IN) return false;

            message.setMessageReadStatus(MessageReadStatus.READ);
            message.setReceiveTime(System.currentTimeMillis());
            messageRepository.createOrUpdate(message);
            chatMessages.add(message);
            return true;
        } catch ( Exception e ) {
            Log.e(Constants.TAG_CHAT, e.getMessage(),e);
        }

        return false;
    }

    private void loginToChatServer() {
        App.getChatEngine().emitDoLogin( App.getUserApp());
    }

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatContact contact = intent.getParcelableExtra("contact");
                    ChatMessage msg = intent.getParcelableExtra("msg");
                    ChatMessage repliedMsg = intent.getParcelableExtra("repliedMsg");
                    Log.d(Constants.TAG_CHAT, getClass().getName() + String.format(" => new message = %s from %s ", msg.getMessageText(), contact.getEmail()));
                    //add in messasge
                    boolean success = addMessageIn(msg);
                    //update latest out message as replied
                    Log.d(Constants.TAG_CHAT, getClass().getName() + String.format(" => reply status = %s from %s ", msg.getMsgid(), contact.getEmail()));
                    updateMessageSendStatus(contact, repliedMsg);

                    if (success) {
                        if (listAdapter != null) {
                            listAdapter.notifyDataSetChanged();
                            scrollToBottom();
                        }
                    }
                }
            });
        }
    };

    private BroadcastReceiver deliveryStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatContact contact = intent.getParcelableExtra("contact");
                    ChatMessage msg = intent.getParcelableExtra("msg");
                    Log.d(Constants.TAG_CHAT, getClass().getName() + String.format(" => delivery status = %s from %s ", msg.getMsgid(), contact.getEmail()));
                    //update delivery status
                    updateMessageSendStatus(contact, msg);
                    listAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    //update delivered / reply
    public void updateMessageSendStatus(final ChatContact contact, final ChatMessage chatMessage) {
        if ( contact == null || chatMessage == null) return;
        //update only current active contact
        //Log.d(Constants.TAG_CHAT, String.format("UPDATED CONTACT => eq1=%s, eq2=%s", contact.getEmail().equalsIgnoreCase(chatContact.getEmail()), contact.getUserType().equals(chatContact.getUserType())));
        if ( contact.getEmail().equalsIgnoreCase(chatContact.getEmail()) && contact.getUserType().equals(chatContact.getUserType()) ) {
            ChatMessage _chatMessage = null;
            for(int i = chatMessages.size() - 1; i>=0; i-- ) {
                _chatMessage = chatMessages.get(i);
                if ( _chatMessage.getMessageType() != MessageType.OUT ) continue;

                //Log.d(Constants.TAG_CHAT, String.format("MSG => eq1=%s, eq2=%s", _chatMessage.getMsgid().equalsIgnoreCase(chatMessage.getMsgid()), _chatMessage.getMessageType().equals(chatMessage.getMessageType())));
                Log.d(Constants.TAG_CHAT, String.format("msgid=%s, type=%s, newsendstatus=%s",  _chatMessage.getMsgid(), _chatMessage.getMessageType(), chatMessage.getMessageSendStatus() ));
                if(_chatMessage.getMsgid().equalsIgnoreCase(chatMessage.getMsgid()) && _chatMessage.getMessageType().equals(chatMessage.getMessageType())) {
                    //hack for self chat jika sudah status replied tidak update
                    if ( MessageSendStatus.REPLIED.equals(_chatMessage.getMessageSendStatus()) == false )
                        _chatMessage.setMessageSendStatus(chatMessage.getMessageSendStatus());

                    break;
                }
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON PAUSE");
        hideEmojiPopup();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON RESUME");
        ChatClientService.registerReceivers(makeReceivers());
        loginToChatServer();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON STOP");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
    }

    private void scrollToBottom() {
        chatListView.post(new Runnable() {
            @Override
            public void run() {
                chatListView.setSelection(listAdapter.getCount() - 1);
            }
        });
    }
}
