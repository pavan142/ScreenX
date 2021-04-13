package com.frankenstein.screenx.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ScreenShotDao {

      // As this query returns the entire row, this would contain the text_content column too
      // which makes this a very costly operation. So disabling it, as there is no need in current flows
      // to get text_content of all rows in a single query.
//    @Query("SELECT * FROM ScreenShotEntity")
//    List<ScreenShotEntity> getAllWithTextContent();

    @Query("SELECT filename, appname FROM ScreenShotEntity")
    List<ScreenShotEntity> getAll();

    @Query("SELECT filename, appname FROM ScreenShotEntity")
    LiveData<List<ScreenShotEntity>> getLiveAll();

    @Query("SELECT filename, appname FROM ScreenShotEntity WHERE text_content IS NOT NULL")
    List<ScreenShotEntity> getAllParsed();

    @Query ("SELECT filename FROM ScreenShotEntity INNER JOIN fts ON ScreenShotEntity.`rowid` = fts.`rowid` WHERE fts.text_content MATCH :query")
    LiveData<List<String>> findByContent(String query);

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
