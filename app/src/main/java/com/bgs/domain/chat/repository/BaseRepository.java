package com.bgs.domain.chat.repository;

import android.content.Context;

import com.bgs.domain.dao.DBHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * Created by zhufre on 7/2/2016.
 */
public class BaseRepository<T> {
    private static DBHelper dbHelper;
    private Class<T> clazz;
    protected Dao<T, Integer> dao = null;

    protected BaseRepository(Context context, Class<T> clazz) {
        if ( dbHelper == null)
            dbHelper = new DBHelper(context);

        this.clazz = clazz;
    }

    protected Dao<T, Integer> getDao() throws SQLException {
        try {
            return dbHelper.getDao(clazz);
        } catch (SQLException e) {
            throw e;
        }
    }


}
