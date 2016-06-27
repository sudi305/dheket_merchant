package com.bgs.dheket.sqlite;
import android.graphics.Bitmap;

/**
 * Created by SND on 6/13/2016.
 */
public class ModelMerchant {
    long id;
    long id_merchant;
    String merchant_name;
    String email;
    String facebook_photo;
    Bitmap photo;
    int isLogin;

    public ModelMerchant(){

    }

    public ModelMerchant(long id_merchant, String merchant_name, String email, String facebook_photo, int isLogin){
        this.id_merchant = id_merchant;
        this.merchant_name = merchant_name;
        this.email = email;
        this.facebook_photo = facebook_photo;
        this.isLogin = isLogin;
    }

    public ModelMerchant(long id, String merchant_name, String email, String facebook_photo, Bitmap photo, int isLogin){
        this.id = id;
        this.merchant_name = merchant_name;
        this.email = email;
        this.facebook_photo = facebook_photo;
        this.photo = photo;
        this.isLogin = isLogin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMerchant_name() {
        return merchant_name;
    }

    public void setMerchant_name(String merchant_name) {
        this.merchant_name = merchant_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebook_photo() {
        return facebook_photo;
    }

    public void setFacebook_photo(String facebook_photo) {
        this.facebook_photo = facebook_photo;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public int isLogin() {
        return isLogin;
    }

    public void setIsLogin(int isLogin) {
        this.isLogin = isLogin;
    }

    public long getId_merchant() {
        return id_merchant;
    }

    public void setId_merchant(long id_merchant) {
        this.id_merchant = id_merchant;
    }
}
