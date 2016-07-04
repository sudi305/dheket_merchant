package com.bgs.domain.dao;

import com.j256.ormlite.dao.BaseDaoImpl;

import java.sql.SQLException;

/**
 * Created by zhufre on 7/2/2016.
 */
public class CustomDao<T, ID> extends BaseDaoImpl<T, ID> {
    protected CustomDao(final Class<T> dataClass) throws SQLException {
        super(dataClass);
    }

    @Override
    public int create(final T data) throws SQLException {
        int result = super.create(data);
        // Send an event with EventBus or Otto
        return result;
    }
}
