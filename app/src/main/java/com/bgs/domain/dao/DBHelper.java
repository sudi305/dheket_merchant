package com.bgs.domain.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bgs.domain.chat.model.ChatContact;
import com.bgs.domain.chat.model.ChatMessage;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by zhufre on 7/2/2016.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME    = "dheket-chat.db";
    private static final int DATABASE_VERSION = 5;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, ChatContact.class);
            TableUtils.createTable(connectionSource, ChatMessage.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, ChatContact.class, true);
            TableUtils.dropTable(connectionSource, ChatMessage.class, true);
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}