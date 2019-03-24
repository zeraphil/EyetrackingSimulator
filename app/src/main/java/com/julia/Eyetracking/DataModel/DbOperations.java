package com.julia.Eyetracking.DataModel;

import android.arch.persistence.room.Dao;
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
    void insertEyetrackingData(SerializableEyetrackingData data);
    @Insert
    void insertBatchEyetrackingData(List<SerializableEyetrackingData> dataList);
    @Insert
    void insertBatchEyetrackingData(SerializableEyetrackingData[] dataList);
    @Update
    void updataEyetrackingData(SerializableEyetrackingData data);
    @Query ("SELECT * From SerializableEyetrackingData WHERE seconds < :seconds")
    List<SerializableEyetrackingData> getDataBeforeTime(long seconds);
    @Query("SELECT * FROM SerializableEyetrackingData")
    List<SerializableEyetrackingData> getAll();
    @Query ("DELETE FROM SerializableEyetrackingData")
    void deleteAll();
}
