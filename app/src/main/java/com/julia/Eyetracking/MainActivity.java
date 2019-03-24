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
import com.julia.Eyetracking.DataModel.SerializableEyetrackingData;
import com.julia.Eyetracking.DataModel.EyetrackingDatabase;
import com.julia.Eyetracking.DataModel.InsertEyetrackingToDatabaseTask;
import com.julia.Eyetracking.DataModel.ReportDatabaseEntriesTask;
import com.julia.Eyetracking.Service.EyetrackingServiceConnection;
import com.julia.Eyetracking.Service.IEyetrackingDataListener;
import com.julia.Eyetracking.Service.MessengerEyetrackingService;
import com.julia.Eyetracking.Service.EyetrackingServiceMessages;
import com.julia.Eyetracking.UI.DrawView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private long lastTimestamp = 0;
    private double averageLatency = 0.0;

    /**
     * Connection module
     */
    EyetrackingServiceConnection serviceConnection;
    IEyetrackingDataListener eyetrackingDataListener;

    /**
     * Serialization parameters
     */
    private EyetrackingDatabase database;
    private static final int serializationItemThreshold = 100;
    private LinkedBlockingQueue<SerializableEyetrackingData> serializableDataQueue = new LinkedBlockingQueue<>();

    DrawView drawView;
    TextView textView;
    //Incoming message handler, mainly to handle data messages

    /**
     * Method called from the button to Toggle processing
     * @param view
     */
    public void bindServiceToggle(View view)
    {
        Log.d(this.getClass().toString(), "Toggle button called" + serviceConnection.isBound());

        Button toggle = (Button)findViewById(R.id.button);
        if (serviceConnection.isBound())
        {
            serviceConnection.connectToService(false);
            toggle.setText("Start");
            if (serializableDataQueue.size() > 0 )
            {
                serializeQueueToDatabase();
            }
            drawView.clearDrawView();

        }
        else
        {
            serviceConnection.connectToService(true);
            toggle.setText("Stop");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = Room.databaseBuilder(this.getApplicationContext(), EyetrackingDatabase.class, Constants.EyetrackingDatabase).fallbackToDestructiveMigration().build();
        drawView = findViewById(R.id.view);
        textView = findViewById(R.id.textView);

        this.eyetrackingDataListener = new IEyetrackingDataListener() {
            @Override
            public void onEyetrackingDataMessage(EyetrackingData data) {
                onNewDataMessage(data);
            }
        };

        serviceConnection = new EyetrackingServiceConnection(this, this.eyetrackingDataListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (serviceConnection.isBound()) {
            serviceConnection.connectToService(false);
            if (serializableDataQueue.size() > 0 )
            {
                serializeQueueToDatabase();
            }
        }
    }


    /**
     * Handle a message containing data, including serializing it and updating the visualization
     * @param data
     */
    private void onNewDataMessage(EyetrackingData data)
    {
        averageLatency = HelperMethods.ExponentialMovingAverage(averageLatency, Instant.now().toEpochMilli() - lastTimestamp, 0.8);

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
            textView.setText(String.format("Latency: %2f ms", averageLatency));
        }

        //Log.d(this.getClass().toString(), data.getTimestamp().toString());
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

        ArrayList<SerializableEyetrackingData> dataList = new ArrayList<>();
        serializableDataQueue.drainTo(dataList);
        InsertEyetrackingToDatabaseTask task = new InsertEyetrackingToDatabaseTask(this.database);
        SerializableEyetrackingData[] array = new SerializableEyetrackingData[dataList.size()];
        array = dataList.toArray(array);
        task.execute(array);
    }



}
