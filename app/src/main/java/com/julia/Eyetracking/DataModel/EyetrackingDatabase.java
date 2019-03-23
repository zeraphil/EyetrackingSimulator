package com.julia.Eyetracking.DataModel;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {EyetrackingDataSerializable.class}, version = 1, exportSchema = false)
public abstract class EyetrackingDatabase extends RoomDatabase {
    public abstract DbOperations dbOperations();
}
