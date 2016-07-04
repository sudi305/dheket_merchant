package com.bgs.domain.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by madhur on 17/01/15.
 */
@DatabaseTable(tableName = ChatMessage.TABLE_NAME_MESSAGES)
public class ChatMessage implements Parcelable {
    public static final String TABLE_NAME_MESSAGES = "messages";
    public static final String FIELD_NAME_ID     = "id";
    public static final String FIELD_NAME_CONTACT_ID   = "contact_id";
    public static final String FIELD_NAME_MESSAGE_TEXT   = "message_text";
    public static final String FIELD_NAME_TYPE   = "type";
    public static final String FIELD_NAME_SEND_STATUS   = "send_status";
    public static final String FIELD_NAME_READ_STATUS   = "read_status";
    public static final String FIELD_NAME_SEND_TIME   = "send_time";
    public static final String FIELD_NAME_DELIVERED_TIME   = "delivered_time";
    public static final String FIELD_NAME_RECEIVED_TIME   = "received_time";
    public static final String FIELD_NAME_CREATED_TIME   = "created_time";


    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int id;
    @DatabaseField(columnName = FIELD_NAME_CONTACT_ID)
    private int contactId;
    @DatabaseField(columnName = FIELD_NAME_MESSAGE_TEXT)
    private String messageText;
    @DatabaseField(columnName = FIELD_NAME_TYPE, dataType = DataType.ENUM_INTEGER)
    private MessageType messageType;
    @DatabaseField(columnName = FIELD_NAME_SEND_STATUS, dataType = DataType.ENUM_INTEGER)
    private MessageSendStatus messageSendStatus;
    @DatabaseField(columnName = FIELD_NAME_READ_STATUS, dataType = DataType.ENUM_INTEGER)
    private MessageReadStatus messageReadStatus;
    @DatabaseField(columnName = FIELD_NAME_SEND_TIME)
    private long sendTime;
    @DatabaseField(columnName = FIELD_NAME_DELIVERED_TIME)
    private long deliveredTime;
    @DatabaseField(columnName = FIELD_NAME_RECEIVED_TIME)
    private long receiveTime;
    @DatabaseField(columnName = FIELD_NAME_CREATED_TIME)
    private long createTime;

    public ChatMessage() {}

    public ChatMessage(int contactId, String messageText, MessageType messageType, MessageSendStatus messageSendStatus, MessageReadStatus messageReadStatus, long sendTime, long createTime) {
        this(0, contactId, messageText, messageType, messageSendStatus, messageReadStatus, sendTime, 0, 0, createTime);
    }

    public ChatMessage(int contactId, String messageText, MessageType messageType, MessageSendStatus messageSendStatus, MessageReadStatus messageReadStatus, long sendTime, long deliveredTime, long receiveTime, long createTime) {
        this(0, contactId, messageText, messageType, messageSendStatus, messageReadStatus, sendTime, deliveredTime, receiveTime, createTime);
    }

    /**
     * @param id
     * @param contactId
     * @param messageText
     * @param messageType
     * @param messageSendStatus
     * @param messageReadStatus
     * @param sendTime
     * @param deliveredTime
     * @param receiveTime
     * @param createTime
     */
    public ChatMessage(int id, int contactId, String messageText, MessageType messageType, MessageSendStatus messageSendStatus, MessageReadStatus messageReadStatus, long sendTime, long deliveredTime, long receiveTime, long createTime) {
        this.id = id;
        this.contactId = contactId;
        this.messageText = messageText;
        this.messageType = messageType;
        this.messageSendStatus = messageSendStatus;
        this.messageReadStatus = messageReadStatus;
        this.sendTime = sendTime;
        this.deliveredTime = deliveredTime;
        this.receiveTime = receiveTime;
        this.createTime = createTime;
    }

    public ChatMessage(Parcel in) {
        this.id = in.readInt();
        this.contactId = in.readInt();
        this.messageText = in.readString();
        this.messageType = MessageType.parse(in.readInt());
        this.messageSendStatus = MessageSendStatus.parse(in.readInt());
        this.messageReadStatus = MessageReadStatus.parse(in.readInt());
        this.sendTime = in.readLong();
        this.deliveredTime = in.readLong();
        this.receiveTime = in.readLong();
        this.createTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.contactId);
        dest.writeString(this.messageText);
        dest.writeInt(this.messageType.ordinal());
        dest.writeInt(this.messageSendStatus.ordinal());
        dest.writeInt(this.messageReadStatus.ordinal());
        dest.writeLong(this.sendTime);
        dest.writeLong(this.deliveredTime);
        dest.writeLong(this.receiveTime);
        dest.writeLong(this.createTime);
    }


    public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {

        @Override
        public ChatMessage createFromParcel(Parcel source) {
            return new ChatMessage(source);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public void setMessageSendStatus(MessageSendStatus messageSendStatus) { this.messageSendStatus = messageSendStatus; }
    public void setMessageReadStatus(MessageReadStatus messageReadStatus) { this.messageReadStatus = messageReadStatus; }
    public String getMessageText() { return messageText; }
    public MessageType getMessageType() { return messageType; }
    public MessageSendStatus getMessageSendStatus() { return messageSendStatus; }
    public MessageReadStatus getMessageReadStatus() { return messageReadStatus; }
    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId= contactId; }
    public long getSendTime() { return sendTime; }
    public void setSendTime(long sendTime) { this.sendTime = sendTime;}
    public long getDeliveredTime() { return deliveredTime;}
    public void setDeliveredTime(long deliveredTime) { this.deliveredTime = deliveredTime;}
    public long getReceiveTime() { return receiveTime; }
    public void setReceiveTime(long receiveTime) { this.receiveTime = receiveTime; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}
