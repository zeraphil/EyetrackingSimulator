package com.julia.Eyetracking.DataModel;

import android.os.AsyncTask;

public class InsertEyetrackingToDatabaseTask extends AsyncTask<EyetrackingDataSerializable, Void, Void> {

    private EyetrackingDatabase database;

    public InsertEyetrackingToDatabaseTask(EyetrackingDatabase database)
    {
        this.database = database;
    }

    @Override
    protected Void doInBackground(EyetrackingDataSerializable... eyetrackingData) {
        this.database.dbOperations().insertBatchEyetrackingData(eyetrackingData);
        return null;
    }
}
