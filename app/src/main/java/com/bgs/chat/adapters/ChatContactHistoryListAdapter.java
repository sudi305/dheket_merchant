package com.bgs.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bgs.chat.viewmodel.ChatHistory;
import com.bgs.dheket.general.CircleTransform;
import com.bgs.dheket.merchant.R;
import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.MessageType;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatContactHistoryListAdapter extends BaseAdapter {

    private ArrayList<ChatHistory> chatContactHistories;
    private Context context;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");
    Picasso picasso;

    public ChatContactHistoryListAdapter(ArrayList<ChatHistory> chatContactHistories, Context context) {
        this.chatContactHistories = chatContactHistories;
        this.context = context;

    }

    @Override
    public int getCount() {
        return chatContactHistories.size();
    }

    @Override
    public Object getItem(int position) {
        return chatContactHistories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatHistory history = chatContactHistories.get(position);
        ViewHolder holder;

        if (convertView == null) {
            v = LayoutInflater.from(context).inflate(R.layout.chat_contact_history_item, null, false);
            holder = new ViewHolder();
            holder.picture = (ImageView) v.findViewById(R.id.chat_contact_picture);
            holder.nameTextView = (TextView) v.findViewById(R.id.chat_contact_name);
            //holder.emailTextView = (TextView) v.findViewById(R.id.chat_contact_email);
            holder.timeTextView = (TextView) v.findViewById(R.id.chat_contact_time);
            holder.messageTextView = (TextView) v.findViewById(R.id.chat_contact_msg);
            holder.msgCountTextView = (TextView) v.findViewById(R.id.chat_contact_msg_count);

            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }

        holder.timeTextView.setText("");
        if ( history != null ) {
            ChatContact contact = history.getContact();
            if ( contact != null && !"".equalsIgnoreCase(contact.getPicture()) ) {
                // set profile image to imageview using Picasso or Native methods
                picasso.with(context).load(contact.getPicture()).transform(new CircleTransform()).into(holder.picture);
            }

            holder.nameTextView.setText(history.getContact().getName());
            holder.messageTextView.setText("");
            ChatMessage msg = history.getLastChatMessage();
            if (  msg != null ) {
                String msgText = history.getLastChatMessage().getMessageText();
                if ( msgText.length() > 40 ) msgText = msgText.substring(0, 40) + "...";
                holder.messageTextView.setText( msgText);
                long time = msg.getMessageType() == MessageType.IN ? msg.getReceiveTime() : msg.getSendTime();
                holder.timeTextView.setText(SIMPLE_DATE_FORMAT.format(new Date(time)));
            }

            if ( history.getNewMessageCount() > 0 ) {
                holder.msgCountTextView.setVisibility(TextView.VISIBLE);
                holder.msgCountTextView.setText(String.valueOf(history.getNewMessageCount()));
            }

        }

        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatHistory contact = chatContactHistories.get(position);
        return contact.getContact().getContactType().ordinal();
    }


    private class ViewHolder {
        public ImageView picture;
        public TextView nameTextView;
        //public TextView emailTextView;
        public TextView messageTextView;
        public TextView timeTextView;
        public TextView msgCountTextView;

    }
}
