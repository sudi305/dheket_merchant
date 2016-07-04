package com.bgs.domain.chat.model;

/**
 * Created by madhur on 17/01/15.
 */
public enum MessageType {
    IN, OUT;

    public static MessageType parse(int value) {
        for(MessageType item : values()) {
            if ( item.ordinal() == value )
                return item;
        }
        return null;
    }
};
