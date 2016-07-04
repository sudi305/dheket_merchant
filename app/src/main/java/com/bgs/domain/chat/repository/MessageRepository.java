package com.bgs.domain.chat.repository;

import android.content.Context;
import android.util.Log;

import com.bgs.common.Constants;
import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.MessageReadStatus;
import com.bgs.domain.chat.model.MessageSendStatus;
import com.bgs.domain.chat.model.MessageType;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by zhufre on 6/28/2016.
 */
public class MessageRepository extends BaseRepository<ChatMessage> implements  IMessageRepository {

    public MessageRepository(Context context) {
        super(context, ChatMessage.class);
    }

    @Override
    public ChatMessage getMessageById(int id) {
        ChatMessage chatMessage = null;
        try {
            chatMessage = getDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatMessage;
    }

    @Override
    public ChatMessage getLastMessageByContact(int contactId) {
        ChatMessage chatMessage = null;
        try {
            QueryBuilder<ChatMessage, Integer> builder = getDao().queryBuilder();
            builder.orderBy(ChatMessage.FIELD_NAME_ID, false)
                    .where().eq(ChatMessage.FIELD_NAME_CONTACT_ID, contactId);
            chatMessage = getDao().queryForFirst(builder.prepare());
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatMessage;
    }

    @Override
    public List<ChatMessage> getListMessageByContactAndDate(int contactId, Date date) {
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        try {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date);
            cal1.set(Calendar.HOUR_OF_DAY, 0); cal1.set(Calendar.MINUTE, 0); cal1.set(Calendar.SECOND, 0); cal1.set(Calendar.MILLISECOND, 0);

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            cal2.set(Calendar.HOUR_OF_DAY, 23); cal2.set(Calendar.MINUTE, 59); cal2.set(Calendar.SECOND, 59); cal2.set(Calendar.MILLISECOND, 0);

            QueryBuilder<ChatMessage, Integer> builder = getDao().queryBuilder();
            builder.where()
                    .eq(ChatMessage.FIELD_NAME_CONTACT_ID, contactId)
                    .and().between(ChatMessage.FIELD_NAME_CREATED_TIME,  cal1.getTimeInMillis(), cal2.getTimeInMillis());
            chatMessages = getDao().query(builder.prepare());
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatMessages;
    }

    @Override
    public List<ChatMessage> getListMessageByContact(int contactId) {
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        try {
            chatMessages = getDao().queryForEq(ChatMessage.FIELD_NAME_CONTACT_ID, contactId);
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatMessages;
    }

    @Override
    public List<ChatMessage> getListNewMessageByContact(int contactId) {
        List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();
        try {
            QueryBuilder<ChatMessage, Integer> builder = getDao().queryBuilder();
            builder.where()
                    .eq(ChatMessage.FIELD_NAME_CONTACT_ID, contactId)
                    .and().eq(ChatMessage.FIELD_NAME_READ_STATUS, MessageReadStatus.NEW);
            chatMessages = getDao().query(builder.prepare());
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatMessages;
    }

    @Override
    public long getNewMessageCount() {
        long result = 0;
        try {
            QueryBuilder<ChatMessage, Integer> builder = getDao().queryBuilder();
            builder.setCountOf(true)
                    .where()
                    .eq(ChatMessage.FIELD_NAME_READ_STATUS, MessageReadStatus.NEW);
            result = getDao().countOf(builder.prepare());
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }

        return result;
    }

    @Override
    public long getNewMessageCountByContact(int contactId) {
        long result = 0;
        try {
            QueryBuilder<ChatMessage, Integer> builder = getDao().queryBuilder();
            builder.setCountOf(true)
                    .where()
                    .eq(ChatMessage.FIELD_NAME_CONTACT_ID, contactId)
                    .and().eq(ChatMessage.FIELD_NAME_READ_STATUS, MessageReadStatus.NEW);
            result = getDao().countOf(builder.prepare());
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }

        return result;
    }


    public void createOrUpdate(ChatMessage message) {
        try {
            getDao().createOrUpdate(message);
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
    }

    @Override
    public void updateReadStatus(List<ChatMessage> messages, MessageReadStatus status) {
        for(ChatMessage msg : messages) {
            updateReadStatus(msg, status);
        }
    }

    @Override
    public void updateReadStatus(ChatMessage message, MessageReadStatus status) {
        //update hanya status yg belum sama dengan param
        if ( message.getMessageType() == MessageType.IN && message.getMessageReadStatus() != status) {
            message.setMessageReadStatus(status);
            createOrUpdate(message);
        }
    }

    @Override
    public void updateSendStatus(List<ChatMessage> messages, MessageSendStatus status) {
        for(ChatMessage msg : messages) {
            updateSendStatus(msg, status);
        }
    }

    @Override
    public void updateSendStatus(ChatMessage message, MessageSendStatus status) {
        //update hanya status yg belum sama dengan param
        if ( message.getMessageType() == MessageType.OUT && message.getMessageSendStatus() != status) {
            message.setMessageSendStatus(status);
            createOrUpdate(message);
        }
    }


}
