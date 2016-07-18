package com.bgs.domain.chat.model;

/**
 * Created by madhur on 17/01/15.
 */
public enum UserType {
    USER("U"), MERCHANT("M");

    private String VALUE;
    UserType(String value) {
        VALUE = value;
    }
    public static UserType parse(String value) {

        for(UserType item : values()) {
            if ( item.VALUE.equalsIgnoreCase(value) )
                return item;
        }
        return null;
    }


    @Override
    public String toString() {
        return VALUE;
    }
};
