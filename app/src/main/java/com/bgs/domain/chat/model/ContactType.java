package com.bgs.domain.chat.model;

/**
 * Created by madhur on 17/01/15.
 */
public enum ContactType {
    PRIVATE, GROUP;

    public static ContactType parse(int value) {
        for(ContactType item : values()) {
            if ( item.ordinal() == value )
                return item;
        }
        return null;
    }
};
