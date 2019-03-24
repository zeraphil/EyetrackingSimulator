package com.julia.Eyetracking;

import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.EyetrackingDataSerializable;
import com.julia.Eyetracking.DataModel.EyetrackingDatabase;
import com.julia.Eyetracking.DataModel.InsertEyetrackingToDatabaseTask;
import com.julia.Eyetracking.DataModel.ReportDatabaseEntriesTask;
import com.julia.Eyetracking.Service.MessengerEyetrackingService;
import com.julia.Eyetracking.Service.EyetrackingServiceMessages;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private long lastTimestamp = 0;
    private double averageLatency = 0.0;
    /**
     * Service connection parameters
     */
    private boolean isBound;
    private Messenger serviceMessenger;
    private Messenger replyToMessenger = new Messenger(new IncomingMessageHandler());

    /**
     * Serialization parameters
     */
    private EyetrackingDatabase database;
    private static final int serializationItemThreshold = 1000;
    private LinkedBlockingQueue<EyetrackingDataSerializable> serializableDataQueue = new LinkedBlockingQueue<>();

    DrawView drawView;
    TextView textView;
    //Incoming message handler, mainly to handle data messages
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

    //the connection that will manage the service
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

    /**
     * Method called from the button to Toggle processing
     * @param view
     */
    public void bindServiceToggle(View view)
    {
        Log.d("MainActivity", "Toggle button called" + isBound);
        bindService(!isBound);
        Button toggle = (Button)findViewById(R.id.button);
        if (isBound)
        {
            toggle.setText("Stop");
        }
        else
        {
            toggle.setText("Start");

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = Room.databaseBuilder(this.getApplicationContext(), EyetrackingDatabase.class, Constants.EyetrackingDatabase).fallbackToDestructiveMigration().build();
        drawView = findViewById(R.id.view);
        textView = findViewById(R.id.textView);

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

    /**
     * Handle a message containing data, including serializing it and updating the visualization
     * @param message
     */
    private void onNewDataMessage(Message message)
    {
        averageLatency = (Instant.now().toEpochMilli() - lastTimestamp);
        textView.setText(String.format("Latency: %f", averageLatency));
        //Get the data from the message parcel
        EyetrackingData data = message.getData().getParcelable(Constants.EyetrackingDataParcel);

        //update our visualization
        drawView.updateEye(data.getId(), new PointF(data.getNormalizedPosX(), data.getNormalizedPosY()), data.getPupilDiameter());

        //add to our queue, and dump/drain if necessary
        try {
            serializableDataQueue.put(data.toSerializable());
        }
        catch (InterruptedException e)
        {
            Log.e(this.getClass().toString(), "interrupted thread");
        }

        if (serializableDataQueue.size() > serializationItemThreshold )
        {
            serializeQueueToDatabase();
        }

        Log.d(this.getClass().toString(), data.getTimestamp().toString());
        //set the current time to last
        lastTimestamp = Instant.now().toEpochMilli();
    }

    /**
     * Method that creates an asynchronous task to batch process messages to the queue.
     * If the app crashes, we probably lose this data
     */
    private void serializeQueueToDatabase()
    {
        Log.d(this.getClass().toString(), "Dumping Queue To database");

        ArrayList<EyetrackingDataSerializable> dataList = new ArrayList<>();
        serializableDataQueue.drainTo(dataList);
        InsertEyetrackingToDatabaseTask task = new InsertEyetrackingToDatabaseTask(this.database);
        EyetrackingDataSerializable[] array = new EyetrackingDataSerializable[dataList.size()];
        array = dataList.toArray(array);
        task.execute(array);
    }

    /**
     * Method to toggle between bound/unbound states
     * @param bind
     */
    private void bindService(boolean bind)
    {
        if(bind)
        {         // Bind to the service
            try {
                Log.d("MainActivity", MessengerEyetrackingService.class.toString());

                bindService(new Intent(this, MessengerEyetrackingService.class), this.connection, Context.BIND_AUTO_CREATE);
                isBound = true;
            }
            catch (Exception e) {

                Log.e("MainActivity",Log.getStackTraceString(e));
            }
        }
        else
        {
            unregisterFromService();
            unbindService(this.connection);
            isBound = false;
        }
    }

    /**
     * Creates a message to register the activity to the service
     */
    private void registerToService()
    {
        if (!isBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, EyetrackingServiceMessages.REGISTER, 0, 0);
        msg.replyTo = replyToMessenger;
        try {
            Log.d("MainActivity", "Registering to service");
            serviceMessenger.send(msg);
            lastTimestamp = Instant.now().toEpochMilli();
        } catch (RemoteException e) {

            Log.e("MainActivity",Log.getStackTraceString(e));
        }
    }

    /**
     * Creates a message to unregister the activity to the service
     */
    private void unregisterFromService()
    {
        if (!isBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, EyetrackingServiceMessages.UNREGISTER, 0, 0);
        msg.replyTo = replyToMessenger;
        try {
            serviceMessenger.send(msg);
            lastTimestamp = Instant.now().toEpochMilli();
        } catch (RemoteException e) {
            Log.e("MainActivity",Log.getStackTraceString(e));
        }

        if (serializableDataQueue.size() > 0 )
        {
            serializeQueueToDatabase();
        }

        ReportDatabaseEntriesTask task = new ReportDatabaseEntriesTask(this.database);
        task.execute();

    }

}
