package com.bgs.dheket.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.bgs.domain.chat.model.UserType;

/**
 * Created by zhufre on 6/7/2016.
 */
public class UserApp implements Parcelable {
    private String id;
    private String name;
    private String picture;
    private String email;
    private String phone;
    private UserType type;

    public UserApp() {
        this.phone = "";
        this.picture = "";
    }
    /**
     * 2param id
     * @param name
     * @param picture
     * @param email
     * @param phone
     */
    public UserApp(String id, String name, String picture, String email, String phone, UserType type) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.email = email;
        this.phone = phone;
        this.type = type;
    }

    public UserApp(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.picture = in.readString();
        this.email = in.readString();
        this.phone = in.readString();
        this.type = UserType.parse(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.picture);
        dest.writeString(this.email);
        dest.writeString(this.phone);
        dest.writeString(this.type.toString());
    }

    public static final Creator<UserApp> CREATOR = new Creator<UserApp>() {

        @Override
        public UserApp createFromParcel(Parcel source) {
            return new UserApp(source);
        }

        @Override
        public UserApp[] newArray(int size) {
            return new UserApp[size];
        }
    };

    public String getId() {
        return id;
    }
    public void setId(String id) {
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
    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }
}
