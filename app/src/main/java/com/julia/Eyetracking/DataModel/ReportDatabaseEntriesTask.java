package com.julia.Eyetracking.DataModel;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Async task to do a get query on all the entries
 */
public class ReportDatabaseEntriesTask extends AsyncTask<Void, Void, Void> {

    private EyetrackingDatabase database;

    public ReportDatabaseEntriesTask(EyetrackingDatabase database)
    {
        this.database = database;
    }
    @Override
    protected Void doInBackground(Void...params) {
        Log.d(this.getClass().toString(), String.format("%d data entries added to the database", this.database.dbOperations().getAll().size()));
        return null;
    }
}
