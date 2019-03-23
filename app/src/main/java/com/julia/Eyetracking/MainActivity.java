package com.julia.Eyetracking;

import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.EyetrackingDatabase;
import com.julia.Eyetracking.DataModel.InsertEyetrackingToDatabaseTask;
import com.julia.Eyetracking.DataModel.ReportDatabaseEntriesTask;
import com.julia.Eyetracking.Service.BaseEyetrackingService;
import com.julia.Eyetracking.Service.EyetrackingServiceMessages;

public class MainActivity extends AppCompatActivity {

    private boolean isBound;
    private Messenger serviceMessenger;
    private Messenger replyToMessenger = new Messenger(new IncomingMessageHandler());
    private EyetrackingDatabase database;
    //setting reply messenger and handler
    private class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            //Log.d(this.getClass().toString(), "Handle message called.");
            switch (msg.what) {
                case EyetrackingServiceMessages.DATA:
                    onNewDataMessage(msg);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MainActivity", "Connected to service");
            isBound = true;
            serviceMessenger = new Messenger(service);
            registerToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            serviceMessenger = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = Room.databaseBuilder(this.getApplicationContext(), EyetrackingDatabase.class, Constants.EyetrackingDatabase).fallbackToDestructiveMigration().build();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (isBound) {
            bindService(false);
            isBound = false;
        }
    }

    private void onNewDataMessage(Message message)
    {
        EyetrackingData data = message.getData().getParcelable(Constants.EyetrackingDataParcel);

        InsertEyetrackingToDatabaseTask task = new InsertEyetrackingToDatabaseTask(this.database);
        task.execute(data.toSerializable());

        Log.d(this.getClass().toString(), data.getTimestamp().toString());
    }

    private void bindService(boolean bind)
    {
        if(bind)
        {         // Bind to the service
            try {
                Log.d("MainActivity",BaseEyetrackingService.class.toString());

                bindService(new Intent(this, BaseEyetrackingService.class), this.connection, Context.BIND_AUTO_CREATE);
            }
            catch (Exception e) {

                Log.e("MainActivity",Log.getStackTraceString(e));
            }
        }
        else
        {
            unregisterFromService();
            unbindService(this.connection);
        }
    }

    private void registerToService()
    {
        if (!isBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, EyetrackingServiceMessages.REGISTER, 0, 0);
        msg.replyTo = replyToMessenger;
        try {
            Log.d("MainActivity", "Registering to service");
            serviceMessenger.send(msg);
        } catch (RemoteException e) {

            Log.e("MainActivity",Log.getStackTraceString(e));
        }
    }

    private void unregisterFromService()
    {
        if (!isBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, EyetrackingServiceMessages.UNREGISTER, 0, 0);
        msg.replyTo = replyToMessenger;
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e("MainActivity",Log.getStackTraceString(e));
        }

        ReportDatabaseEntriesTask task = new ReportDatabaseEntriesTask(this.database);
        task.execute();
    }

    public void bindServiceToggle(View view)
    {
        Log.d("MainActivity", "Toggle button called");
        bindService(!isBound);
    }

}
