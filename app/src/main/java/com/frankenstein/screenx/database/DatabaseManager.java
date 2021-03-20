package com.frankenstein.screenx.database;

import android.content.Context;

import androidx.room.Room;

import static com.frankenstein.screenx.Constants.DB_NAME;

public class DatabaseManager {
    private static ScreenShotDatabase _mInstance;

    public static ScreenShotDatabase getInstance(Context context) {
        if (_mInstance == null) {
            _mInstance = Room.databaseBuilder(context, ScreenShotDatabase.class, DB_NAME).build();
        }
        return _mInstance;
    }
}
