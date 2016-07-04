package com.bgs.chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bgs.chat.ChatPageActivity;
import com.bgs.dheket.merchant.R;
import com.bgs.chat.adapters.ChatContactHistoryListAdapter;
import com.bgs.chat.viewmodel.ChatHistory;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;
import com.bgs.domain.chat.repository.IMessageRepository;
import com.bgs.domain.chat.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;

public class ChatContactHistoryFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "num";

    private int mNum;
    private ListView contactHistoryListView;
    private ChatContactHistoryListAdapter listAdapter;
    private ArrayList<ChatHistory> chatContactHistories;
    private IContactRepository contactRepository;
    private IMessageRepository messageRepository;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ChatContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatContactHistoryFragment newInstance(int param1) {
        ChatContactHistoryFragment fragment = new ChatContactHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNum = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chatcontact_history, container, false);

        chatContactHistories = new ArrayList<ChatHistory>();

        contactHistoryListView = (ListView) rootView.findViewById(R.id.chat_contact_history_list_view);
        contactHistoryListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        contactHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatContact contact = (chatContactHistories.get((int)id)).getContact();
                //Toast.makeText(getActivity(), contact.getName(), Toast.LENGTH_LONG).show();
                contact.setActive(1);
                ChatPageActivity.startChatFromHistory(getActivity(), contact);
            }
        });

        listAdapter = new ChatContactHistoryListAdapter(chatContactHistories, getActivity());
        contactHistoryListView.setAdapter(listAdapter);

        /*
        ChatHistory history = new ChatHistory();
        history.setContact(new Contact() {{
            setName("User2");
            setEmail("usertwo@gmail.com");
            setContactType(ContactType.PRIVATE);
        }});
        history.setNewMessageCount(13);
        history.setLastChatMessage(new ChatMessage() {{
            setMessageText("test");
            setSenderName("");
            setMessageType(MessageType.SEND);
            setMessageSendStatus(MessageSendStatus.SENT);
        }});
        chatContactHistories.add(history);
        */
        contactRepository = new ContactRepository(getActivity());
        messageRepository = new MessageRepository(getActivity());
        List<ChatContact> contactList = contactRepository.getListContact();
        fillHistoryList(contactList, false);
        return rootView;
    }

    private void fillHistoryList(List<ChatContact> contactList, boolean update) {
        for (ChatContact contact : contactList) {
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

            listAdapter.notifyDataSetChanged();
            showEmptyMessage();
        }

    }
    private void showEmptyMessage() {
        if ( chatContactHistories.size() == 0 ) {
            TextView emptyView = (TextView) getView().findViewById(R.id.chat_contact_history_empty);
            if ( emptyView != null ) emptyView.setVisibility(TextView.VISIBLE);
        }
    }

    public void updateHistory(final ArrayList<ChatHistory> contactHistoriesList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatContactHistories.clear();
                for(ChatHistory contactHistory : contactHistoriesList ) {
                    chatContactHistories.add(contactHistory);
                    listAdapter.notifyDataSetChanged();
                    //Log.d(getResources().getString(R.string.app_name), "c:" + contact.getName());
                }
                showEmptyMessage();
            }
        });
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        List<ChatContact> contactList = contactRepository.getListContact();
        fillHistoryList(contactList, true);
        for(ChatHistory item : chatContactHistories) {
            item.getContact().setActive(0);
        }

    }
}
