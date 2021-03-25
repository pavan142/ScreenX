package com.frankenstein.screenx.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ScreenShotEntity.class, FtsEntity.class}, version = 1)
public abstract class ScreenShotDatabase extends RoomDatabase {
    public abstract ScreenShotDao screenShotDao();
}
