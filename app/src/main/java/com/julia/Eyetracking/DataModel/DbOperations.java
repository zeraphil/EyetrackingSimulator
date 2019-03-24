package com.julia.Eyetracking.DataModel;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Interface to handle the Android Room CRUD operations. Currently just insert/get
 */
@Dao
public interface DbOperations {
    @Insert
    void insertEyetrackingData(EyetrackingDataSerializable data);
    @Insert
    void insertBatchEyetrackingData(List<EyetrackingDataSerializable> dataList);
    @Insert
    void insertBatchEyetrackingData(EyetrackingDataSerializable[] dataList);
    @Update
    void updataEyetrackingData(EyetrackingDataSerializable data);
    @Query ("SELECT * From EyetrackingDataSerializable WHERE seconds < :seconds")
    List<EyetrackingDataSerializable> getDataBeforeTime(long seconds);
    @Query("SELECT * FROM EyetrackingDataSerializable")
    List<EyetrackingDataSerializable> getAll();
    @Query ("DELETE FROM EyetrackingDataSerializable")
    void deleteAll();
}
