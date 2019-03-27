package com.julia.Eyetracking.DataModel;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Interface to handle the Android Room CRUD operations. Currently just insert/query
 */
@Dao
public interface DbOperations {
    @Insert
    void insertEyetrackingData(EyetrackingDatabaseEntity data);
    @Insert
    void insertBatchEyetrackingData(List<EyetrackingDatabaseEntity> dataList);
    @Insert
    void insertBatchEyetrackingData(EyetrackingDatabaseEntity[] dataList);
    @Update
    void updataEyetrackingData(EyetrackingDatabaseEntity data);
    @Query ("SELECT * From EyetrackingDatabaseEntity WHERE seconds < :seconds")
    List<EyetrackingDatabaseEntity> getDataBeforeTime(long seconds);
    @Query("SELECT * FROM EyetrackingDatabaseEntity")
    List<EyetrackingDatabaseEntity> getAll();
    @Query ("DELETE FROM EyetrackingDatabaseEntity")
    void deleteAll();
}
