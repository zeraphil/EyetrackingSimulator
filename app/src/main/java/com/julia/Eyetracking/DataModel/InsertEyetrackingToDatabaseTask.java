package com.julia.Eyetracking.DataModel;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Async class to batch process a queue of messages.
 */
public class InsertEyetrackingToDatabaseTask extends AsyncTask<SerializableEyetrackingData, Void, Void> {

    private EyetrackingDatabase database;

    public InsertEyetrackingToDatabaseTask(EyetrackingDatabase database)
    {
        this.database = database;
    }

    @Override
    protected Void doInBackground(SerializableEyetrackingData... eyetrackingData) {
        this.database.dbOperations().insertBatchEyetrackingData(eyetrackingData);
        Log.d(this.getClass().toString(), String.format("Database size = %d data entries", this.database.dbOperations().getAll().size()));

        return null;
    }
}
