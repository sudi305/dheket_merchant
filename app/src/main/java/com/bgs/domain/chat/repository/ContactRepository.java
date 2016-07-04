package com.bgs.domain.chat.repository;

import android.content.Context;
import android.util.Log;

import com.bgs.common.Constants;
import com.bgs.domain.chat.model.ChatContact;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhufre on 6/28/2016.
 */
public class ContactRepository extends BaseRepository<ChatContact> implements  IContactRepository {

    public ContactRepository(Context context) {
        super(context, ChatContact.class);
    }

    @Override
    public List<ChatContact> getListContact() {
        List<ChatContact> chatContacs = new ArrayList<ChatContact>();
        try {
            chatContacs = getDao().queryForAll();
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatContacs;
    }

    @Override
    public ChatContact getContactById(int id) {
        ChatContact chatContac = null;
        try {
            chatContac = getDao().queryForId(id);
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatContac;
    }

    @Override
    public ChatContact getContactByEmail(String email) {
        ChatContact chatContac = null;
        try {
            QueryBuilder<ChatContact, Integer> builder = getDao().queryBuilder();
            builder.where().eq(ChatContact.FIELD_NAME_EMAIL, email);
            chatContac = getDao().queryForFirst(builder.prepare());
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
        return chatContac;
    }

    public void createOrUpdate(ChatContact contact) {
        try {
            getDao().createOrUpdate(contact);
        } catch (SQLException e) {
            Log.e(Constants.TAG_CHAT, e.getMessage(), e);
        }
    }
}
