package com.bgs.chat.viewmodel;

import com.bgs.domain.chat.model.ChatMessage;
import com.bgs.domain.chat.model.MessageReadStatus;
import com.bgs.domain.chat.model.MessageSendStatus;
import com.bgs.domain.chat.model.MessageType;

/**
 * Created by zhufre on 6/14/2016.
 */
public class ChatHelper {

    public static ChatMessage createMessageIn(final String msgid, final int contactId, final String messageText) {
        return createMessage(msgid, contactId, messageText, MessageType.IN);
    }

    public static ChatMessage createMessageOut(final String msgid, final int contactId, final String messageText) {
        return createMessage(msgid, contactId, messageText, MessageType.OUT);
    }

    private static ChatMessage createMessage(final String msgid, final int contactId, final String messageText, final MessageType messageType) {
        if(messageText.trim().length()==0)
            return null ;

        final ChatMessage message = new ChatMessage();

        message.setMsgid(msgid);

        if ( contactId > 0 )
            message.setContactId(contactId);

        if ( messageType == MessageType.OUT) {
            message.setMessageSendStatus(MessageSendStatus.NEW);
            message.setSendTime(System.currentTimeMillis());
        } else if ( messageType == MessageType.IN) {
            message.setMessageReadStatus(MessageReadStatus.NEW);
        }
        message.setMessageText(messageText);
        message.setMessageType(messageType);
        message.setCreateTime(System.currentTimeMillis());

        return message;
    }
}
