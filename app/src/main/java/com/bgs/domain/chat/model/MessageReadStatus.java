package com.bgs.domain.chat.model;

/**
 * Created by madhur on 17/01/15.
 */
public enum MessageReadStatus {
    NEW, READ;

    public static MessageReadStatus parse(int value) {
        for(MessageReadStatus item : values()) {
            if ( item.ordinal() == value )
                return item;
        }
        return null;
    }
}
