package com.bgs.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bgs.chat.adapters.ChatTabPagerAdapter;
import com.bgs.chat.fragments.ChatContactFragment;
import com.bgs.chat.fragments.ChatContactHistoryFragment;
import com.bgs.chat.services.ChatClientService;
import com.bgs.chat.viewmodel.ChatHelper;
import com.bgs.dheket.App;
import com.bgs.common.Constants;
import com.bgs.dheket.general.Utility;
import com.bgs.dheket.merchant.MainMenuActivity;
import com.bgs.dheket.merchant.R;
import com.bgs.dheket.viewmodel.UserApp;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.ContactType;
import com.bgs.domain.chat.model.MessageType;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainChatActivity extends AppCompatActivity {
    ViewPager viewPager;

    private ImageButton goBackButton;
    private ChatTabPagerAdapter pagerAdapter;

    private ChatClientService chatClientService;
    private IContactRepository contactRepository;
    private IMessageRepository messageRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON CREATE");
        setContentView(R.layout.activity_mainchat);
        contactRepository = new ContactRepository(getApplicationContext());
        messageRepository = new MessageRepository(getApplicationContext());

        goBackButton = (ImageButton) findViewById(R.id.go_back_button);
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toMainMenu();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagerAdapter = new ChatTabPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        // Attach the page change listener to tab strip and **not** the view pager inside the activity
        tabsStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(MainChatActivity.this, "Selected page position: " + position, Toast.LENGTH_SHORT).show();

            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });


        //Log.d(getResources().getString(R.string.app_name), "count=" + tabsStrip.getChildCount());

        UserApp userApp = App.getUserApp();
        if ( userApp == null ) {
            String id = Utility.getDeviceUniqueID(getContentResolver());
            userApp = new UserApp(id, id, "", id + "@zmail.com", "");
            App.updateUserApp(userApp);
        }
        TextView title = (TextView) findViewById(R.id.user_app);
        title.setText(title.getText() + " - " + userApp.getName());
        chatClientService = App.getChatClientService();
        Log.d(Constants.TAG_CHAT,"chatClientService = " + chatClientService);
        attemptLoginToChatServer();


    }

    public void toMainMenu(){
        Intent mainMenu = new Intent(this, MainMenuActivity.class);
        startActivity(mainMenu);
        finish();
    }

    private Activity getActivity() {
        return this;
    }

    private void attemptLoginToChatServer() {
        if ( !chatClientService.isLogin() ) {
            JSONObject user = new JSONObject();
            try {
                String name = Utility.getDeviceUniqueID(getContentResolver());
                UserApp userApp = App.getUserApp();
                user.put("name", userApp.getName());
                user.put("email", userApp.getEmail());
                user.put("phone", userApp.getPhone());
                chatClientService.emit(ChatClientService.SocketEmit.DO_LOGIN, user);
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
            }
        }
    }

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            attemptLoginToChatServer();
        }
    };

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            String data = intent.getStringExtra("data");
            JSONObject from;
            String message;
            try {

                JSONObject joData = new JSONObject(data);
                from = joData.getJSONObject("from");
                message = joData.getString("message");

                //String id = from.getString("id");
                String name = from.getString("name");
                String email = from.getString("email");
                String phone = from.getString("phone");

                //Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT );
                Log.d(Constants.TAG_CHAT, "message2 = " + message);
                //removeTyping(username);
                //final ChatContactFragment fragment0 = (ChatContactFragment) pagerAdapter.getItem(1);
                ChatContact contact = contactRepository.getContactByEmail(email);
                if ( contact == null) {
                    contact = new ChatContact(name, "", email, phone, ContactType.PRIVATE);
                    contactRepository.createOrUpdate(contact);
                }
                if ( contact.getActive() != 1) {
                    final ChatMessage lastMessage = ChatHelper.createMessage(contact.getId(), message, MessageType.IN);
                    messageRepository.createOrUpdate(lastMessage);
                    final ChatContactHistoryFragment fragment1 = (ChatContactHistoryFragment) pagerAdapter.getItem(0);
                    final long newMessages = messageRepository.getNewMessageCountByContact(contact.getId());
                    fragment1.updateContactHistory(contact, (int)newMessages, lastMessage);
                }

            } catch (JSONException e) {
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
            Log.d(Constants.TAG_CHAT, "list contacts = " + contacts);
            //Toast.makeText(getApplicationContext(), "Login2 " + isLogin, Toast.LENGTH_SHORT).show();


            final ArrayList<ChatContact> contactList = new ArrayList<ChatContact>();
            for(int i = 0; i < contacts.length(); i++) {
                try {
                    JSONObject joContact = contacts.getJSONObject(i);
                    String id = joContact.getString("id");
                    String name = joContact.getString("name");
                    String email = joContact.getString("email");
                    String phone = joContact.getString("phone");
                    //skip contact for current app user

                    //if ( email.equalsIgnoreCase(app.getUserApp().getEmail())) continue;
                    ChatContact contact = contactRepository.getContactByEmail(email);
                    if ( contact == null ) {
                        contact = new ChatContact(name, "", email, phone, ContactType.PRIVATE);
                        contactRepository.createOrUpdate(contact);
                        contactList.add(contact);
                    }

                } catch (JSONException e) {
                    Log.d(Constants.TAG_CHAT,e.getMessage(), e);
                }
            }

            updateContactList(contactList);

        }
    };

    private void updateContactList(ArrayList<ChatContact> contactList) {
        if ( contactList.size() > 0) {
            ChatContactFragment fragment = (ChatContactFragment) pagerAdapter.getItem(1);
            fragment.updateContact(contactList);
        }
    }
    private BroadcastReceiver updateContactReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            String data = intent.getStringExtra("data");
            boolean remove = false;
            try {
                JSONObject joData = new JSONObject(data);
                JSONObject joContact = null;
                remove = joData.getBoolean("remove");
                joContact = joData.getJSONObject("contact");
                //Toast.makeText(getApplicationContext(), "Login1 " + isLogin, Toast.LENGTH_SHORT).show();

                Log.d(Constants.TAG_CHAT, "update contact = " + joContact);
                //Toast.makeText(getApplicationContext(), "Login2 " + isLogin, Toast.LENGTH_SHORT).show();
                if ( joContact != null ) {

                    String id = joContact.getString("id");
                    String name = joContact.getString("name");
                    String email = joContact.getString("email");
                    String phone = joContact.getString("phone");

                    ChatContactFragment fragment = (ChatContactFragment) pagerAdapter.getItem(1);
                    if (remove) {
                        //fragment.removeContact(email);
                    } else {
                        ChatContact contact = contactRepository.getContactByEmail(email);
                        if ( contact == null ) {
                            contact = new ChatContact(name, "", email, phone, ContactType.PRIVATE);
                            contactRepository.createOrUpdate(contact);
                            fragment.updateContact(contact);
                        }
                    }
                }

            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
                return;
            }


        }
    };

    private BroadcastReceiver userJoinReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            JSONObject user;
            try {
                JSONObject joData = new JSONObject(data);
                user = joData.getJSONObject("user");
                Log.d(Constants.TAG_CHAT, "User Join " + user.getString("email"));
            } catch (JSONException e) {
                Log.e(Constants.TAG_CHAT, e.getMessage(), e);
                return;
            }
            //retrive contact
            //ChatTaskService.startActionGetContacts(getActivity());
        }
    };

    public Map<String, BroadcastReceiver> makeReceivers(){
        Map<String, BroadcastReceiver> map = new HashMap<String, BroadcastReceiver>();
        map.put(ChatClientService.SocketEvent.CONNECT, connectReceiver);
        map.put(ChatClientService.SocketEvent.USER_JOIN, userJoinReceiver);
        map.put(ChatClientService.SocketEvent.NEW_MESSAGE, newMessageReceiver);
        map.put(ChatClientService.SocketEvent.LIST_CONTACT, listContactReceiver);
        map.put(ChatClientService.SocketEvent.UPDATE_CONTACT, updateContactReceiver);
        return map;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON PAUSE");
        chatClientService.unregisterReceivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON STOP");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Constants.TAG_CHAT,getLocalClassName() + " => ON RESUME");
        Log.d(Constants.TAG_CHAT, "chatClientService=" + chatClientService);
        chatClientService.registerReceivers(makeReceivers());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        socket.off("login", onLogin);
        socket.off("user join", onUserJoin);
        socket.off("list contact", onListContact);
        socket.off("update contact", onUpdateContact);
        socket.off("new message", onNewMessage);
        */
    }


}
