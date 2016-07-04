package com.bgs.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bgs.dheket.merchant.R;
import com.bgs.domain.chat.model.ChatContact;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatContactListAdapter extends BaseAdapter {

    private ArrayList<ChatContact> chatContacts;
    private Context context;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");

    public ChatContactListAdapter(ArrayList<ChatContact> chatContacts, Context context) {
        this.chatContacts = chatContacts;
        this.context = context;

    }

    @Override
    public int getCount() {
        return chatContacts.size();
    }

    @Override
    public Object getItem(int position) {
        return chatContacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatContact contact = chatContacts.get(position);
        ViewHolder1 holder1;

            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_contact_item, null, false);
                holder1 = new ViewHolder1();
                holder1.picture = (ImageView) v.findViewById(R.id.chat_contact_picture);
                holder1.nameTextView = (TextView) v.findViewById(R.id.chat_contact_name);
                holder1.emailTextView = (TextView) v.findViewById(R.id.chat_contact_email);

                v.setTag(holder1);
            } else {
                v = convertView;
                holder1 = (ViewHolder1) v.getTag();
            }

        if ( contact != null ) {
            //holder1.picture.setBackground();
            holder1.nameTextView.setText(contact.getName());
            holder1.emailTextView.setText(contact.getEmail());
        }

        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatContact contact = chatContacts.get(position);
        return contact.getContactType().ordinal();
    }


    private class ViewHolder1 {
        public ImageView picture;
        public TextView nameTextView;
        public TextView emailTextView;


    }
}
