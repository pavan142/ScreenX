package com.frankenstein.screenx.database;

import android.content.Context;

import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;

import static com.frankenstein.screenx.Constants.DB_NAME;

public class DatabaseManager {
    private static ScreenShotDatabase _mInstance;

    public static ScreenShotDatabase getInstance(Context context) {
        if (_mInstance == null) {
            // TODO: Remove the fallbackToDestructiveMigration once the database design iterations are finalized, and proper migrations could be defined later on
            _mInstance = Room.databaseBuilder(context, ScreenShotDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
        }
        return _mInstance;
    }
}
