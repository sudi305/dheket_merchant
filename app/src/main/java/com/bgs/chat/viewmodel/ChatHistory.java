package com.bgs.chat.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;

/**
 * Created by zhufre on 6/8/2016.
 */
public class ChatHistory implements Parcelable {

    private int id;
    private ChatContact contact;
    private int newMessageCount;
    private ChatMessage lastChatMessage;

    public ChatHistory() {}

    /**
     *
     * @param contact
     * @param newMessageCount
     * @param lastChatMessage
     */
    public ChatHistory(ChatContact contact, int newMessageCount, ChatMessage lastChatMessage) {
        this(0, contact, newMessageCount, lastChatMessage);
    }
    /**
     * @param id
     * @param contact
     * @param newMessageCount
     * @param lastChatMessage
     */
    public ChatHistory(int id, ChatContact contact, int newMessageCount, ChatMessage lastChatMessage) {
        this.id = id;
        this.contact = contact;
        this.newMessageCount = newMessageCount;
        this.lastChatMessage = lastChatMessage;
    }

    public ChatHistory(Parcel in) {
        this.id = in.readInt();
        this.contact = in.readParcelable(ChatContact.class.getClassLoader());
        this.newMessageCount = in.readInt();
        this.lastChatMessage = in.readParcelable(ChatMessage.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeParcelable(this.contact, flags);
        dest.writeInt(this.newMessageCount);
        dest.writeParcelable(this.lastChatMessage, flags);
    }

    public static final Creator<ChatHistory> CREATOR = new Creator<ChatHistory>() {
        @Override
        public ChatHistory createFromParcel(Parcel in) {
            return new ChatHistory(in);
        }

        @Override
        public ChatHistory[] newArray(int size) {
            return new ChatHistory[size];
        }
    };

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public ChatMessage getLastChatMessage() {
        return lastChatMessage;
    }
    public void setLastChatMessage(ChatMessage lastChatMessage) { this.lastChatMessage = lastChatMessage;}
    public ChatContact getContact() {
        return contact;
    }
    public void setContact(ChatContact contact) {
        this.contact = contact;
    }
    public int getNewMessageCount() {
        return newMessageCount;
    }
    public void setNewMessageCount(int newMessageCount) {
        this.newMessageCount = newMessageCount;
    }


}
