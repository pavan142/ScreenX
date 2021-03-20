package com.frankenstein.screenx.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ScreenShotEntity {
    @PrimaryKey @NonNull
    public String filename;

    @ColumnInfo(name="text_content")
    public String textContent;

    @ColumnInfo(name="meta_data")
    public String metaData;
}
