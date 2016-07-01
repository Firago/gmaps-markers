package com.dfirago.maps.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dfirago.maps.model.MarkerEntity;


/**
 * Created by dmfi on 28/06/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "MapsDatabase";

    private static final String DATABASE_CREATE = "create table " + MarkerEntity.KEY_TABLE_NAME + "("
            + MarkerEntity.KEY_ID + " integer primary key ,"
            + MarkerEntity.KEY_POSITION_LAT + " double ,"
            + MarkerEntity.KEY_POSITION_LNG + " double ,"
            + MarkerEntity.KEY_IMAGE_URI + " text "
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop tale if exists " + MarkerEntity.KEY_TABLE_NAME);
        onCreate(db);
    }
}