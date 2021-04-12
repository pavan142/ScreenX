package com.frankenstein.screenx.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ScreenShotDao {

    @Query("SELECT * FROM ScreenShotEntity")
    List<ScreenShotEntity> getAll();

    @Query ("SELECT * FROM ScreenShotEntity INNER JOIN fts ON ScreenShotEntity.`rowid` = fts.`rowid` WHERE fts.text_content MATCH :query")
    List<ScreenShotEntity> findByContent(String query);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertScreenshots(List<ScreenShotEntity> screenShotEntityList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertSingleScreenshot(ScreenShotEntity entity);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void updateScreenshots(List<ScreenShotEntity> screenShotEntityList);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public void updateSingleScreenshot(ScreenShotEntity entity);

    @Query("SELECT * FROM ScreenShotEntity WHERE filename LIKE :filename")
    ScreenShotEntity getScreenShotByName(String filename);

    @Query("DELETE FROM ScreenShotEntity WHERE filename LIKE :filename")
    void deleteScreenShot(String filename);

    @Query("DELETE FROM ScreenShotEntity WHERE filename in (:filenames)")
    void deleteMultipleScreenShots(String[] filenames);

    @Query("DELETE FROM ScreenShotEntity")
    void deleteAll();
}
