package com.bgs.dheket.sqlite;

/**
 * Created by SND on 6/25/2016.
 */
public class ModelLocation {
    long id;
    long id_location;
    String location_name;
    String location_address;
    double latitude;
    double longitude;
    int category_id;
    String category_name;
    String phone;
    int isPromo;
    long merchant_id;
    String description;
    String location_tag;
    String user_email;

    public ModelLocation(){

    }

    public ModelLocation(long id, long id_location, String location_name, String location_address,
                         double latitude, double longitude, int category_id, String category_name, String phone, int isPromo,
                         long merchant_id, String description, String location_tag, String user_email) {
        this.id = id;
        this.id_location = id_location;
        this.location_name = location_name;
        this.location_address = location_address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
        this.category_name = category_name;
        this.phone = phone;
        this.isPromo = isPromo;
        this.merchant_id = merchant_id;
        this.description = description;
        this.location_tag = location_tag;
        this.user_email = user_email;
    }

    public ModelLocation(long id_location, String location_name, String location_address,
                         double latitude, double longitude, int category_id, String category_name, String phone,
                         int isPromo, long merchant_id, String description, String location_tag,
                         String user_email) {
        this.id_location = id_location;
        this.location_name = location_name;
        this.location_address = location_address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
        this.category_name = category_name;
        this.phone = phone;
        this.isPromo = isPromo;
        this.merchant_id = merchant_id;
        this.description = description;
        this.location_tag = location_tag;
        this.user_email = user_email;
    }

    public ModelLocation(long id, String location_name, String location_address,
                         double latitude, double longitude, int category_id, String category_name, String phone,
                         int isPromo, String description, String location_tag, String user_email) {
        this.id = id;
        this.location_name = location_name;
        this.location_address = location_address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category_id = category_id;
        this.category_name = category_name;
        this.phone = phone;
        this.isPromo = isPromo;
        this.description = description;
        this.location_tag = location_tag;
        this.user_email = user_email;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId_location() {
        return id_location;
    }

    public void setId_location(long id_location) {
        this.id_location = id_location;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getLocation_address() {
        return location_address;
    }

    public void setLocation_address(String location_address) {
        this.location_address = location_address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getIsPromo() {
        return isPromo;
    }

    public void setIsPromo(int isPromo) {
        this.isPromo = isPromo;
    }

    public long getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(long merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation_tag() {
        return location_tag;
    }

    public void setLocation_tag(String location_tag) {
        this.location_tag = location_tag;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }
}
