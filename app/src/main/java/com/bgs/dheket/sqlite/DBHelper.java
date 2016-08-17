package com.bgs.dheket.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bgs.dheket.general.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SND on 6/13/2016.
 */
public class DBHelper extends SQLiteOpenHelper{
    private static final String LOG = DBHelper.class.getName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "dheket_merchant";

    private static final String TABLE_MERCHANT = "merchant";
    private static final String KEY_ID = "id";
    private static final String KEY_ID_MERCHANT = "id_merchant";
    private static final String KEY_MERCHANT_NAME = "merchant_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FACEBOOK_PHOTO = "facebook_photo";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_ISLOGIN = "isLogin";

    private static final String CREATE_TABLE_MERCHANT =
            "CREATE TABLE " + TABLE_MERCHANT +
                    "(" +
                        KEY_ID + " BIGINT PRIMARY KEY," +
                        KEY_ID_MERCHANT + " BIGINT," +
                        KEY_MERCHANT_NAME + " TEXT," +
                        KEY_EMAIL + " TEXT," +
                        KEY_FACEBOOK_PHOTO + " TEXT," +
                        KEY_PHOTO + " BLOB," +
                        KEY_ISLOGIN + " INTEGER"+
                    ")";

    private static final String TABLE_LOCATION = "location";
    private static final String KEY_ID_LOC = "id";
    private static final String KEY_LOCATION_ID = "id_location";
    private static final String KEY_LOCATION_NAME = "location_name";
    private static final String KEY_LOCATION_ADDRESS = "location_address";
    private static final String KEY_LOCATION_LATITUDE = "latitude";
    private static final String KEY_LOCATION_LONGITUDE = "longitude";
    private static final String KEY_LOCATION_CAT_ID = "category_id";
    private static final String KEY_LOCATION_CAT_NAME = "category_name";
    private static final String KEY_LOCATION_PHONE = "phone";
    private static final String KEY_LOCATION_ISPROMO = "isPromo";
    private static final String KEY_LOCATION_MERCHANT_ID = "merchant_id";
    private static final String KEY_LOCATION_CREATE_BY = "create_by";
    private static final String KEY_LOCATION_DESCRIPTION = "description";
    private static final String KEY_LOCATION_LOCATION_TAG = "location_tag";
    private static final String KEY_LOCATION_USER_EMAIL = "user_email";

    private static final String CREATE_TABLE_LOCATION =
            "CREATE TABLE " + TABLE_LOCATION +
                    "(" +
                        KEY_ID_LOC + " BIGINT PRIMARY KEY," +
                        KEY_LOCATION_ID + " BIGINT," +
                        KEY_LOCATION_NAME + " TEXT," +
                        KEY_LOCATION_ADDRESS + " TEXT," +
                        KEY_LOCATION_LATITUDE + " DOUBLE," +
                        KEY_LOCATION_LONGITUDE + " DOUBLE," +
                        KEY_LOCATION_CAT_ID + " INTEGER," +
                        KEY_LOCATION_CAT_NAME + " TEXT," +
                        KEY_LOCATION_PHONE + " TEXT," +
                        KEY_LOCATION_ISPROMO + " INTEGER," +
                        KEY_LOCATION_MERCHANT_ID + " BIGINT," +
                        KEY_LOCATION_CREATE_BY + " BIGINT," +
                        KEY_LOCATION_DESCRIPTION + " TEXT," +
                        KEY_LOCATION_LOCATION_TAG + " TEXT," +
                        KEY_LOCATION_USER_EMAIL + " TEXT" +
                    ")";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MERCHANT);
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MERCHANT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        onCreate(db);
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    // --------------- TABLE MERCHANT --------------- //
    public long createMerchant(ModelMerchant merchant){
        SQLiteDatabase db = this.getWritableDatabase();
        Log.e(LOG, "merchant " + merchant.toString());
        ContentValues values = new ContentValues();
        values.put(KEY_ID_MERCHANT, merchant.getId_merchant());
        values.put(KEY_MERCHANT_NAME, merchant.getMerchant_name());
        values.put(KEY_EMAIL, merchant.getEmail());
        values.put(KEY_FACEBOOK_PHOTO, merchant.getFacebook_photo());
        if (merchant.getPhoto()!=null)values.put(KEY_PHOTO, Utility.getBytes(merchant.getPhoto()));
        values.put(KEY_ISLOGIN, merchant.isLogin());

        long merchant_id = db.insert(TABLE_MERCHANT, null, values);
        Log.e(LOG, "merchant id " + merchant_id);
        return merchant_id;
    }

    public String getMerchantTopId(){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_MERCHANT + " ORDER BY " + KEY_ID + " ASC LIMIT 1";
        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        //Log.e("datanya ",""+c.);

        if (c != null) c.moveToFirst();
        Log.e("cursor", "" + c.getCount());

        ModelMerchant m = new ModelMerchant();

        if (c.moveToFirst()) {
            Log.e("move to first",""+c.moveToFirst());
            m.setId(c.getLong(c.getColumnIndex(KEY_ID)));
            m.setId_merchant(c.getLong(c.getColumnIndex(KEY_ID_MERCHANT)));
            m.setMerchant_name(c.getString(c.getColumnIndex(KEY_MERCHANT_NAME)));
            m.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
            m.setFacebook_photo(c.getString(c.getColumnIndex(KEY_FACEBOOK_PHOTO)));
//        c.getBlob(c.getColumnIndex(KEY_PHOTO)).length
            m.setPhoto(Utility.getPhoto(c.getBlob(c.getColumnIndex(KEY_PHOTO))));
//        if (!c.getString(c.getColumnIndex(KEY_PHOTO)).isEmpty())m.setPhoto(Utility.getPhoto(c.getBlob(c.getColumnIndex(KEY_PHOTO))));
            m.setIsLogin(c.getInt(c.getColumnIndex(KEY_ISLOGIN)));
        }
        Log.e("merchant id",""+m.getId()+" "+ m.getId_merchant()+" "+m.getMerchant_name());
        return m.getEmail();
    }

    public List<ModelMerchant> getAllMerchant(){
        List<ModelMerchant> merchants = new ArrayList<ModelMerchant>();
        String selectQuery = "SELECT id, id_merchant, merchant_name, email FROM " + TABLE_MERCHANT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);

        if (c.moveToFirst()){
            do {
                ModelMerchant m = new ModelMerchant();
                m.setId(c.getLong(c.getColumnIndex(KEY_ID)));
                m.setId_merchant(c.getLong(c.getColumnIndex(KEY_ID_MERCHANT)));
                m.setMerchant_name(c.getString(c.getColumnIndex(KEY_MERCHANT_NAME)));
                m.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));

                merchants.add(m);
            } while (c.moveToNext());
        }
         return merchants;
    }

    //public ModelMerchant getMerchant(long merchant_id){
    public ModelMerchant getMerchant(long merchant_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MERCHANT + " WHERE " +
                                KEY_ID + " = " + merchant_id;
        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) c.moveToFirst();
        Log.e("get merchant ", ""+c.moveToFirst());
        ModelMerchant m = new ModelMerchant();

        if (c.moveToFirst()) {
            Log.e("get merchant ", "dapat data");
            m.setId(c.getLong(c.getColumnIndex(KEY_ID)));
            m.setId_merchant(c.getLong(c.getColumnIndex(KEY_ID_MERCHANT)));
            m.setMerchant_name(c.getString(c.getColumnIndex(KEY_MERCHANT_NAME)));
            m.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
            m.setFacebook_photo(c.getString(c.getColumnIndex(KEY_FACEBOOK_PHOTO)));
//        c.getBlob(c.getColumnIndex(KEY_PHOTO)).length
            m.setPhoto(Utility.getPhoto(c.getBlob(c.getColumnIndex(KEY_PHOTO))));
//        if (!c.getString(c.getColumnIndex(KEY_PHOTO)).isEmpty())m.setPhoto(Utility.getPhoto(c.getBlob(c.getColumnIndex(KEY_PHOTO))));
            m.setIsLogin(c.getInt(c.getColumnIndex(KEY_ISLOGIN)));
        }

        return m;
    }

    public ModelMerchant getMerchantByEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MERCHANT + " WHERE " +
                KEY_EMAIL + " = '" + email + "'";
        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) c.moveToFirst();
        Log.e("get merchant ", ""+c.moveToFirst());
        ModelMerchant m = new ModelMerchant();

        if (c.moveToFirst()) {
            m.setId(c.getLong(c.getColumnIndex(KEY_ID)));
            m.setId_merchant(c.getLong(c.getColumnIndex(KEY_ID_MERCHANT)));
            m.setMerchant_name(c.getString(c.getColumnIndex(KEY_MERCHANT_NAME)));
            m.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
            m.setFacebook_photo(c.getString(c.getColumnIndex(KEY_FACEBOOK_PHOTO)));
//        c.getBlob(c.getColumnIndex(KEY_PHOTO)).length
            m.setPhoto(Utility.getPhoto(c.getBlob(c.getColumnIndex(KEY_PHOTO))));
//        if (!c.getString(c.getColumnIndex(KEY_PHOTO)).isEmpty())m.setPhoto(Utility.getPhoto(c.getBlob(c.getColumnIndex(KEY_PHOTO))));
            m.setIsLogin(c.getInt(c.getColumnIndex(KEY_ISLOGIN)));
        }

        return m;
    }

    public boolean findMerchantByEmail(String email){
        boolean found = false;

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_MERCHANT + " WHERE " +
                KEY_EMAIL + " = '" + email + "'";

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.getCount()>0) found = true;
        Log.e(LOG, selectQuery + " " + c.getCount());
        return found;
    }

    public boolean merchantIsEmpty(){
        boolean empty = true;
        List<ModelMerchant> merchants = new ArrayList<ModelMerchant>();
        String selectQuery = "SELECT * FROM " + TABLE_MERCHANT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);

        if (c!=null) empty = false;

        return empty;
    }

    public long updateMerchant(ModelMerchant merchant){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID_MERCHANT, merchant.getId_merchant());
        values.put(KEY_MERCHANT_NAME, merchant.getMerchant_name());
        values.put(KEY_EMAIL, merchant.getEmail());
        values.put(KEY_FACEBOOK_PHOTO, merchant.getFacebook_photo());
        values.put(KEY_PHOTO, Utility.getBytes(merchant.getPhoto()));
        values.put(KEY_ISLOGIN, merchant.isLogin());

        return db.update(TABLE_MERCHANT, values, KEY_EMAIL + " = ?",
                new String[]{String.valueOf(merchant.getEmail())});
    }

    public long updateLoginMerchant(int isLogin, String email){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ISLOGIN, isLogin);

        return db.update(TABLE_MERCHANT, values, KEY_EMAIL + " = ?",
                new String[]{String.valueOf(email)});
    }

    public void deleteMerchant(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MERCHANT,null,null);
    }

    // --------------- END TABLE MERCHANT --------------- //

    // --------------- TABLE LOCATION --------------- //
    public long createLocation(ModelLocation location){
        SQLiteDatabase db = this.getWritableDatabase();
        Log.e(LOG, "lokasi " + location.toString());
        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION_ID, location.getId_location());
        values.put(KEY_LOCATION_NAME, location.getLocation_name());
        values.put(KEY_LOCATION_ADDRESS, location.getLocation_address());
        values.put(KEY_LOCATION_LATITUDE, location.getLatitude());
        values.put(KEY_LOCATION_LONGITUDE, location.getLongitude());
        values.put(KEY_LOCATION_CAT_ID, location.getCategory_id());
        values.put(KEY_LOCATION_CAT_NAME, location.getCategory_name());
        values.put(KEY_LOCATION_PHONE, location.getPhone());
        values.put(KEY_LOCATION_ISPROMO, location.getIsPromo());
        values.put(KEY_LOCATION_MERCHANT_ID, location.getMerchant_id());
        values.put(KEY_LOCATION_CREATE_BY, location.getCreate_by());
        values.put(KEY_LOCATION_DESCRIPTION, location.getDescription());
        values.put(KEY_LOCATION_LOCATION_TAG, location.getLocation_tag());
        values.put(KEY_LOCATION_USER_EMAIL, location.getUser_email());

        long location_id = db.insert(TABLE_LOCATION, null, values);
        Log.e(LOG, "lokasi id " + location_id);
        return location_id;
    }

    public ModelLocation getLocationByEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " WHERE " +
                KEY_LOCATION_USER_EMAIL + " = '" + email +"'";
        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null) c.moveToFirst();

        ModelLocation m = new ModelLocation();

        if (c.moveToFirst()) {
            m.setId(c.getLong(c.getColumnIndex(KEY_ID_LOC)));
            m.setId_location(c.getLong(c.getColumnIndex(KEY_LOCATION_ID)));
            m.setLocation_name(c.getString(c.getColumnIndex(KEY_LOCATION_NAME)));
            m.setLocation_address(c.getString(c.getColumnIndex(KEY_LOCATION_ADDRESS)));
            m.setLatitude(c.getDouble(c.getColumnIndex(KEY_LOCATION_LATITUDE)));
            m.setLongitude(c.getDouble(c.getColumnIndex(KEY_LOCATION_LONGITUDE)));
            m.setCategory_id(c.getInt(c.getColumnIndex(KEY_LOCATION_CAT_ID)));
            m.setCategory_name(c.getString(c.getColumnIndex(KEY_LOCATION_CAT_NAME)));
            m.setPhone(c.getString(c.getColumnIndex(KEY_LOCATION_PHONE)));
            m.setIsPromo(c.getInt(c.getColumnIndex(KEY_LOCATION_ISPROMO)));
            m.setMerchant_id(c.getLong(c.getColumnIndex(KEY_LOCATION_MERCHANT_ID)));
            m.setCreate_by(c.getLong(c.getColumnIndex(KEY_LOCATION_CREATE_BY)));
            m.setDescription(c.getString(c.getColumnIndex(KEY_LOCATION_DESCRIPTION)));
            m.setLocation_tag(c.getString(c.getColumnIndex(KEY_LOCATION_LOCATION_TAG)));
            m.setUser_email(c.getString(c.getColumnIndex(KEY_LOCATION_USER_EMAIL)));
        }

        return m;
    }

    public boolean findLocationByEmail(String email){
        boolean found = false;

        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " WHERE " +
                KEY_LOCATION_USER_EMAIL + " = '" + email + "'";

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.getCount()>0) found = true;
        Log.e(LOG, selectQuery + " " + c.getCount());
        return found;
    }

    public boolean locationIsEmpty(){
        boolean empty = true;
        List<ModelLocation> locations = new ArrayList<ModelLocation>();
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery,null);

        if (c!=null) empty = false;

        return empty;
    }

    public long updateLocation(ModelLocation location){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOCATION_ID, location.getId_location());
        values.put(KEY_LOCATION_NAME, location.getLocation_name());
        values.put(KEY_LOCATION_ADDRESS, location.getLocation_address());
        values.put(KEY_LOCATION_LATITUDE, location.getLatitude());
        values.put(KEY_LOCATION_LONGITUDE, location.getLongitude());
        values.put(KEY_LOCATION_CAT_ID, location.getCategory_id());
        values.put(KEY_LOCATION_CAT_NAME, location.getCategory_name());
        values.put(KEY_LOCATION_PHONE, location.getPhone());
        values.put(KEY_LOCATION_ISPROMO, location.getIsPromo());
        values.put(KEY_LOCATION_MERCHANT_ID, location.getMerchant_id());
        values.put(KEY_LOCATION_CREATE_BY, location.getCreate_by());
        values.put(KEY_LOCATION_DESCRIPTION, location.getDescription());
        values.put(KEY_LOCATION_LOCATION_TAG, location.getLocation_tag());
        values.put(KEY_LOCATION_USER_EMAIL, location.getUser_email());

        long update = db.update(TABLE_LOCATION, values, KEY_LOCATION_USER_EMAIL + " = ?",
                new String[]{String.valueOf(location.getUser_email())});
        Log.e("update ",""+update);
        return update;
    }

    public void deleteLocation(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION,null,null);
    }
    // --------------- END TABLE MERCHANT --------------- //
}
