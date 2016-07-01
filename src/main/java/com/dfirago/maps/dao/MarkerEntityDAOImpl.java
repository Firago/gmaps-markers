package com.dfirago.maps.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dfirago.maps.model.MarkerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmfi on 28/06/2016.
 */
public class MarkerEntityDAOImpl implements MarkerEntityDAO {

    private static final String TABLE_NAME = "markers";

    private final DatabaseHelper helper;

    public MarkerEntityDAOImpl(Context context) {
        helper = new DatabaseHelper(context);
    }

    @Override
    public List<MarkerEntity> list() {
        List<MarkerEntity> result = new ArrayList<>();
        Cursor cursor = helper.getReadableDatabase().rawQuery("select * from " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                MarkerEntity entity = new MarkerEntity();
                entity.setId(cursor.getLong(0));
                entity.setLat(cursor.getDouble(1));
                entity.setLng(cursor.getDouble(2));
                entity.setUri(cursor.getString(3));
                result.add(entity);
                cursor.moveToNext();
            }
        }
        return result;
    }

    @Override
    public MarkerEntity save(MarkerEntity marker) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarkerEntity.KEY_POSITION_LAT, marker.getLat());
        insertValues.put(MarkerEntity.KEY_POSITION_LNG, marker.getLng());
        insertValues.put(MarkerEntity.KEY_IMAGE_URI, marker.getUri());
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(MarkerEntity.KEY_TABLE_NAME, null, insertValues);
        marker.setId(id);
        return marker;
    }

    @Override
    public void delete(MarkerEntity marker) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String whereClause = MarkerEntity.KEY_ID + "=" + marker.getId();
        db.delete(MarkerEntity.KEY_TABLE_NAME, whereClause, null);
    }
}
