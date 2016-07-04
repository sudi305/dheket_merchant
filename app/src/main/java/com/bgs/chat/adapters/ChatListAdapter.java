package com.bgs.chat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bgs.chat.widgets.Emoji;
import com.bgs.dheket.general.Utility;
import com.bgs.dheket.merchant.R;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.MessageSendStatus;
import com.bgs.domain.chat.model.MessageType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatListAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> chatMessages;
    private Context context;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm");

    public ChatListAdapter(ArrayList<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;

    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        ChatMessage message = chatMessages.get(position);
        ViewHolderSend sendHolder;
        ViewHolderReply replyHolder;
        if (message.getMessageType() == MessageType.OUT) {
            if (convertView == null) {
                v = LayoutInflater.from(context).inflate(R.layout.chat_user_send_item, null, false);

                sendHolder = new ViewHolderSend();
                sendHolder.messageTextView = (TextView) v.findViewById(R.id.message_text);
                sendHolder.timeTextView = (TextView) v.findViewById(R.id.time_text);
                sendHolder.messageStatus = (ImageView) v.findViewById(R.id.user_reply_status);
                v.setTag(sendHolder);

            } else {
                v = convertView;
                sendHolder = (ViewHolderSend) v.getTag();

            }

            sendHolder.messageTextView.setText(Emoji.replaceEmoji(message.getMessageText(), sendHolder.messageTextView.getPaint().getFontMetricsInt(), Utility.dp(16) ));
            //holder2.messageTextView.setText(message.getMessageText());
            sendHolder.timeTextView.setText(SIMPLE_DATE_FORMAT.format(new Date(message.getCreateTime())));

            if (message.getMessageSendStatus() == MessageSendStatus.DELIVERED) {
                sendHolder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_double_tick));
            } else if (message.getMessageSendStatus() == MessageSendStatus.NEW) {
                sendHolder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_single_tick));
            }

        } else if (message.getMessageType() == MessageType.IN) {
            boolean grupMessage = false;//(message.getContactId() > 0 ) ? false : true;
            if (convertView == null) {
                if ( grupMessage)
                    v = LayoutInflater.from(context).inflate(R.layout.chat_user_reply_withsender_item, null, false);
                else
                    v = LayoutInflater.from(context).inflate(R.layout.chat_user_reply_item, null, false);

                replyHolder = new ViewHolderReply();

                if ( grupMessage)
                    replyHolder.senderTextView = (TextView) v.findViewById(R.id.chat_company_reply_author);

                replyHolder.messageTextView = (TextView) v.findViewById(R.id.message_text);
                replyHolder.timeTextView = (TextView) v.findViewById(R.id.time_text);

                v.setTag(replyHolder);
            } else {
                v = convertView;
                replyHolder = (ViewHolderReply) v.getTag();

            }
            //if ( grupMessage)
            //    replyHolder.senderTextView.setText(message.getSenderName());

            replyHolder.messageTextView.setText(Emoji.replaceEmoji(message.getMessageText(), replyHolder.messageTextView.getPaint().getFontMetricsInt(), Utility.dp(16)));
            replyHolder.timeTextView.setText(SIMPLE_DATE_FORMAT.format(new Date(message.getCreateTime())));

        }


        return v;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.getMessageType().ordinal();
    }

    private class ViewHolderReply {
        public TextView senderTextView;
        public TextView messageTextView;
        public TextView timeTextView;


    }

    private class ViewHolderReplyWithSender {
        public TextView senderTextView;
        public TextView messageTextView;
        public TextView timeTextView;
    }

    private class ViewHolderSend {
        public ImageView messageStatus;
        public TextView messageTextView;
        public TextView timeTextView;

    }
}
