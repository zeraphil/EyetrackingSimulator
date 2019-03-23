package com.julia.Eyetracking.DataModel;

import android.os.AsyncTask;
import android.util.Log;

public class InsertEyetrackingToDatabaseTask extends AsyncTask<EyetrackingDataSerializable, Void, Void> {

    private EyetrackingDatabase database;

    public InsertEyetrackingToDatabaseTask(EyetrackingDatabase database)
    {
        this.database = database;
    }

    @Override
    protected Void doInBackground(EyetrackingDataSerializable... eyetrackingData) {
        this.database.dbOperations().insertBatchEyetrackingData(eyetrackingData);
        Log.d(this.getClass().toString(), String.format("Database size = %d data entries", this.database.dbOperations().getAll().size()));

        return null;
    }
}
