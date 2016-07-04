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
import com.bgs.chat.adapters.ChatContactListAdapter;
import com.bgs.chat.services.ChatTaskService;
import com.bgs.dheket.merchant.R;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.repository.ContactRepository;
import com.bgs.domain.chat.repository.IContactRepository;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

public class ChatContactFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "pos";
    private static final String ARG_CONTACTS = "contacts";

    private int mNum;
    private Socket socket;

    private ListView contactListView;
    private ChatContactListAdapter listAdapter;
    private ArrayList<ChatContact> chatContacts;
    private IContactRepository contactRepository;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param pos Parameter 1.
     * @return A new instance of fragment ChatContactFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatContactFragment newInstance(int pos) {
        ChatContactFragment fragment = new ChatContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNum = getArguments().getInt(ARG_POSITION);
            //chatContacts = getArguments().getParcelableArrayList(ARG_CONTACTS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chatcontact, container, false);

        chatContacts = new ArrayList<ChatContact>();

        contactListView = (ListView) rootView.findViewById(R.id.chat_contact_list_view);
        contactListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatContact contact = chatContacts.get((int)id);
                contact.setActive(1);
                ChatPageActivity.startChatFromContact(getActivity(), contact);
            }
        });

        listAdapter = new ChatContactListAdapter(chatContacts, getActivity());
        contactListView.setAdapter(listAdapter);

        //Contact contact = new Contact("User1", "", "userone@gmail.com", "",  ContactType.PRIVATE);
        //chatContacts.add(contact);

        //contact = new Contact("User2", "", "usertwo@gmail.com", "",  ContactType.PRIVATE);
        //chatContacts.add(contact);

        contactRepository = new ContactRepository(getActivity());
        List<ChatContact> contactList = contactRepository.getListContact();
        if ( contactList.size() == 0) {
            ChatTaskService.startActionGetContacts(getActivity());
        } else {
            chatContacts.addAll(contactList);
        }
        return rootView;

    }

    private void showEmptyMessage() {
        if ( chatContacts.size() == 0 ) {
            TextView emptyView = (TextView) getView().findViewById(R.id.chat_contact_empty);
            if ( emptyView != null ) emptyView.setVisibility(TextView.VISIBLE);
        }
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
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        for(ChatContact item : chatContacts) {
            item.setActive(0);
        }
    }
    /**
     *
     * @param contactList
     */
    public void updateContact(final ArrayList<ChatContact> contactList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //chatContacts.clear();
                for(ChatContact newContact : contactList ) {
                    boolean exist = false;
                    for(ChatContact oldContact : chatContacts) {
                        if ( newContact.getEmail().equalsIgnoreCase(oldContact.getEmail())) {
                            //update existing member except email
                            oldContact.setName(newContact.getName());
                            oldContact.setId(newContact.getId());
                            oldContact.setPhone(newContact.getPhone());
                            oldContact.setPicture(newContact.getPicture());
                            exist = true;
                            break;
                        }
                    }
                    if ( !exist ) chatContacts.add(newContact);
                    //Log.d(getResources().getString(R.string.app_name), "c:" + contact.getName());
                }

                listAdapter.notifyDataSetChanged();
                showEmptyMessage();
            }
        });
    }

    /**
     *
     * @param contact
     */
    public void updateContact(final ChatContact contact) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean exist = false;
                ChatContact _contact = null;
                for(int i = chatContacts.size() - 1; i>=0; i-- ) {
                    _contact = chatContacts.get(i);
                    if(_contact.getEmail().equalsIgnoreCase(contact.getEmail())) {
                        exist = true;
                        //update other member
                        _contact.setId(contact.getId());
                        break;
                    }
                }
                //add if not exist
                if ( !exist) chatContacts.add(contact);

                listAdapter.notifyDataSetChanged();
                showEmptyMessage();
            }
        });

    }

    /**
     *
     * @param contactEmail
     */
    public void removeContact(final String contactEmail) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ChatContact _contact;
                for(int i = chatContacts.size() - 1; i>=0; i-- ) {
                    _contact = chatContacts.get(i);
                    if(_contact.getEmail().equalsIgnoreCase(contactEmail)) {
                        chatContacts.remove(i);
                        break;
                    }
                }

                listAdapter.notifyDataSetChanged();
                //Log.d(getResources().getString(R.string.app_name), "c:" + contact.getName());
                showEmptyMessage();
                                        }
        });
    }


    /**
     *
     * @param contactEmail
     * @return
     */
    public ChatContact getContact(final String contactEmail) {
        for(ChatContact contact : chatContacts) {
            if(contact.getEmail().equalsIgnoreCase(contactEmail)) {
                return contact;
            }
        }
        return null;
    }
}
