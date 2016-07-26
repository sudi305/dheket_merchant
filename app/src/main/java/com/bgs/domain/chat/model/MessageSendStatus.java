package com.bgs.domain.chat.model;

/**
 * Created by madhur on 17/01/15.
 */
public enum MessageSendStatus {
    NEW, DELIVERED, REPLIED;

    public static MessageSendStatus parse(int value) {
        for(MessageSendStatus item : values()) {
            if ( item.ordinal() == value )
                return item;
        }
        return null;
    }
}
