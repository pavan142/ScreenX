package com.frankenstein.screenx.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;

@Fts4(contentEntity = ScreenShotEntity.class)
@Entity(tableName = "fts")
public class FtsEntity {
    @ColumnInfo(name = "text_content")
    public String textContent;
}

