package com.julia.Eyetracking.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.julia.Eyetracking.DataModel.EyetrackingDatabase;
import com.julia.Eyetracking.DataModel.EyetrackingDatabaseEntity;

/**
 * Async class to batch process a queue of messages.
 */
public class InsertEyetrackingToDatabaseTask extends AsyncTask<EyetrackingDatabaseEntity, Void, Void> {

    private EyetrackingDatabase database;

    public InsertEyetrackingToDatabaseTask(EyetrackingDatabase database)
    {
        this.database = database;
    }

    @Override
    protected Void doInBackground(EyetrackingDatabaseEntity... eyetrackingData) {
        this.database.dbOperations().insertBatchEyetrackingData(eyetrackingData);
        Log.d(this.getClass().toString(), String.format("Database size = %d data entries", this.database.dbOperations().getAll().size()));

        return null;
    }
}
