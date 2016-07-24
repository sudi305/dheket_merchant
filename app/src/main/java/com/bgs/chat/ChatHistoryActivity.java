package com.bgs.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bgs.chat.adapters.ChatContactHistoryListAdapter;
import com.bgs.chat.services.ChatClientService;
import com.bgs.chat.viewmodel.ChatHistory;
import com.bgs.common.Constants;
import com.bgs.dheket.App;
import com.bgs.dheket.merchant.MainMenuActivity;
import com.bgs.dheket.merchant.R;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by SND on 6/13/2016.
 */
public class ChatHistoryActivity extends AppCompatActivity {
    android.support.v7.app.ActionBar actionBar;
    String urls = "";
    Picasso picasso;

    private ListView contactHistoryListView;
    private ChatContactHistoryListAdapter listAdapter;
    private ArrayList<ChatHistory> chatContactHistories;
    private IContactRepository contactRepository;
    private IMessageRepository messageRepository;

    private Activity getActivity() {
        return ChatHistoryActivity.this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.d_ic_back));
        //actionBar.setHomeAsUpIndicator(R.drawable.logo);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Chats");
        //actionBar.setSubtitle(Html.fromHtml("<font color='#FFBF00'>Location in Radius " + formatter.format(radius) + " Km</font>"));

        //urls = String.format(getResources().getString(R.string.lin));//"http://dheket.esy.es/getLocationByCategory.php"

        chatContactHistories = new ArrayList<ChatHistory>();

        contactHistoryListView = (ListView) findViewById(R.id.chat_contact_history_list_view);
        contactHistoryListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        contactHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatContact contact = (chatContactHistories.get((int)id)).getContact();
                //Toast.makeText(getActivity(), contact.getName(), Toast.LENGTH_LONG).show();
                contact.setActive(1);
                ChatPageActivity.startChatFromHistory(getActivity(), contact);
                finish();
            }
        });

        listAdapter = new ChatContactHistoryListAdapter(chatContactHistories, getActivity());
        contactHistoryListView.setAdapter(listAdapter);

        contactRepository = new ContactRepository(getActivity());
        messageRepository = new MessageRepository(getActivity());
        List<ChatContact> contactList = contactRepository.getListContact();
        fillContactHistory(contactList, false);
    }


    public Map<String, BroadcastReceiver> makeReceivers(){
        Map<String, BroadcastReceiver> map = new HashMap<String, BroadcastReceiver>();
        map.put(ChatClientService.ActivityEvent.NEW_MESSAGE, newMessageReceiver);
        map.put(ChatClientService.ActivityEvent.LIST_CONTACT, listContactReceiver);
        return map;
    }

    private void loginToChatServer() {
        App.getChatEngine().emitDoLogin( App.getUserApp());
    }

    private BroadcastReceiver newMessageReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            ChatContact contact = intent.getParcelableExtra("contact");
            ChatMessage msg = intent.getParcelableExtra("msg");
            Log.d(Constants.TAG_CHAT, getClass().getName() + String.format(" => new message = %s from %s ", msg.getMessageText(), contact.getEmail()));
            //new message & contact data sdh di save pada chatclientservice
            if ( contact != null && contact.getActive() != 1) {
                final ChatMessage lastMessage = msg;
                final long newMessages = messageRepository.getNewMessageCountByContact(contact.getId());
                updateContactHistory(contact, (int)newMessages, lastMessage);
            }


        }
    };

    private BroadcastReceiver listContactReceiver = new BroadcastReceiver()  {
        @Override
        public void onReceive(Context context, Intent intent){
            ArrayList<ChatContact> contactList = intent.getParcelableArrayListExtra("contactList");
            Log.d(Constants.TAG_CHAT, getClass().getName() + " => list contacts = " + contactList.size());
            reloadContactHistory();

        }
    };

    private void showEmptyMessage() {
        if ( chatContactHistories.size() == 0 ) {
            TextView emptyView = (TextView) findViewById(R.id.chat_contact_history_empty);
            if ( emptyView != null ) emptyView.setVisibility(TextView.VISIBLE);
        }
    }

    private void fillContactHistory(List<ChatContact> contactList, boolean update) {
        for (ChatContact contact : contactList) {
            //Log.d(Constants.TAG_CHAT, "CONTACT-PICTURE = "+ contact.getPicture());
            long newMessageCount = messageRepository.getNewMessageCountByContact(contact.getId());
            //Log.d(Constants.TAG_CHAT, "newMessageCount=" + newMessageCount);
            ChatMessage lastMessage = messageRepository.getLastMessageByContact(contact.getId());
            ChatHistory history = new ChatHistory(contact, (int)newMessageCount, lastMessage);
            if ( update ) {
                for(ChatHistory _history : chatContactHistories) {
                    if ( _history.getContact().getId() == contact.getId()) {
                        _history.setLastChatMessage(lastMessage);
                        break;
                    }
                }
            } else {
                chatContactHistories.add(history);
            }
        }
        listAdapter.notifyDataSetChanged();
        showEmptyMessage();
    }

    public void updateContactHistory(final ChatContact contact, final int newMessageCount, final ChatMessage lastChatMessage) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean exist = false;
                ChatHistory _contactHistory = null;
                for(int i = chatContactHistories.size() - 1; i>=0; i-- ) {
                    _contactHistory = chatContactHistories.get(i);
                    if(_contactHistory.getContact().getEmail().equalsIgnoreCase(contact.getEmail())) {
                        exist = true;
                        //update other member
                        _contactHistory.setNewMessageCount(newMessageCount);
                        _contactHistory.setLastChatMessage(lastChatMessage);
                        break;
                    }
                }
                //add if not exist
                if ( !exist) {
                    //create new
                    ChatHistory history = new ChatHistory();
                    history.setContact(contact);
                    history.setNewMessageCount(newMessageCount);
                    history.setLastChatMessage(lastChatMessage);
                    chatContactHistories.add(history);
                }

                listAdapter.notifyDataSetChanged();
                //Log.d(getResources().getString(R.string.app_name), "c:" + contact.getName());
                showEmptyMessage();
            }
        });

    }

    public void onBackPressed() {
        toMainMenu();
    }

    public void toMainMenu(){
        Intent toMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(toMainMenu);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //this.menu = menu;
        //getMenuInflater().inflate(R.menu.menu_main_slider, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            toMainMenu();
            return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }


    private void reloadContactHistory() {
        //reset selected contact status
        List<ChatContact> contactList = contactRepository.getListContact();
        fillContactHistory(contactList, true);
        /*
        for(ChatHistory item : chatContactHistories) {
            item.getContact().setActive(0);
        }
        */

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Constants.TAG_CHAT, getLocalClassName() + " => ON PAUSE");
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
        ChatClientService.registerReceivers(makeReceivers());
        loginToChatServer();
        reloadContactHistory();
    }
}
