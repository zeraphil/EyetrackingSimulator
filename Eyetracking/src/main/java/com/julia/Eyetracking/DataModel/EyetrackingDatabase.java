package com.julia.Eyetracking.DataModel;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Implementation of the Android room database, per documentation
 */
@Database(entities = {SerializableEyetrackingData.class}, version = 2, exportSchema = false)
public abstract class EyetrackingDatabase extends RoomDatabase {
    public abstract DbOperations dbOperations();
}
