package com.bgs.domain.chat.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by zhufre on 6/7/2016.
 */
@DatabaseTable(tableName = ChatContact.TABLE_NAME_CONTACTS)
public class ChatContact implements Parcelable {
    public static final String TABLE_NAME_CONTACTS = "contacs";
    public static final String FIELD_NAME_ID     = "id";
    public static final String FIELD_NAME_NAME   = "name";
    public static final String FIELD_NAME_PICTURE   = "picture";
    public static final String FIELD_NAME_EMAIL   = "email";
    public static final String FIELD_NAME_PHONE   = "phone";
    public static final String FIELD_NAME_USER_TYPE = "user_type";
    public static final String FIELD_NAME_IS_GROUP   = "is_group";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int id;
    @DatabaseField(columnName = FIELD_NAME_NAME)
    private String name;
    @DatabaseField(columnName = FIELD_NAME_PICTURE)
    private String picture;
    @DatabaseField(columnName = FIELD_NAME_EMAIL)
    private String email;
    @DatabaseField(columnName = FIELD_NAME_PHONE)
    private String phone;
    @DatabaseField(columnName = FIELD_NAME_USER_TYPE)
    private String userType;
    @DatabaseField(columnName = FIELD_NAME_IS_GROUP)
    private int isGroup;

    //transient
    private int active;

    public ChatContact() {}

    public ChatContact(String name, String picture, String email, String phone, UserType userType) {
        this(0, name, picture, email, phone, userType);
    }
    /**
     * 2param id
     * @param name
     * @param picture
     * @param email
     * @param phone
     * @param userType
     */
    public ChatContact(int id, String name, String picture, String email, String phone, UserType userType) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.email = email;
        this.phone = phone;
        this.userType = userType.toString();
        this.isGroup = 0;
        this.active = 0;
    }

    public ChatContact(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.picture = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.userType = in.readString();
        this.isGroup = in.readInt();
        this.active = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.picture);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.userType.toString());
        dest.writeInt(this.isGroup);
        dest.writeInt(this.active);
    }

    public static final Creator<ChatContact> CREATOR = new Creator<ChatContact>() {

        @Override
        public ChatContact createFromParcel(Parcel source) {
            return new ChatContact(source);
        }

        @Override
        public ChatContact[] newArray(int size) {
            return new ChatContact[size];
        }
    };

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public UserType getUserType() {
        return userType != null ? UserType.parse(userType) : null;
    }
    public void setUserType(UserType userType) {
        this.userType = userType.toString();
    }
    public int getIsGroup() { return isGroup; }
    public void setIsGroup(int isGroup) { this.isGroup = isGroup; }
    public int getActive() {return active;}
    public void setActive(int active) {this.active = active;}
}
