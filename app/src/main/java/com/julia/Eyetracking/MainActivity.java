package com.julia.Eyetracking;

import android.arch.persistence.room.Room;
import android.graphics.PointF;
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
import com.julia.Eyetracking.Service.EyetrackingServiceConnection;
import com.julia.Eyetracking.Service.IEyetrackingDataListener;
import com.julia.Eyetracking.UI.DrawView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    /**
     * Parameters to measure latency
     */
    private long lastTimestamp = 0;
    private double averageLatency = 0.0;

    /**
     * Service connection parameters
     */
    EyetrackingServiceConnection serviceConnection;

    /**
     * Listener that will handler our eyetracking data parcel callback
     */
    IEyetrackingDataListener eyetrackingDataListener;

    /**
     * Serialization parameters
     */
    private EyetrackingDatabase database;
    private static final int serializationItemThreshold = 100;
    private LinkedBlockingQueue<SerializableEyetrackingData> serializableDataQueue = new LinkedBlockingQueue<>();

    /**
     * UI components, to update
     */
    private DrawView drawView;
    private TextView textView;
    //Incoming message handler, mainly to handle data messages

    /**
     * Method called from the button to Toggle processing
     * @param view
     */
    public void bindServiceToggle(View view)
    {
        Log.d(this.getClass().toString(), "Toggle button called" + this.serviceConnection.isBound());

        Button toggle = (Button)findViewById(R.id.button);
        if (this.serviceConnection.isBound())
        {
            //once we're done, disconnect, and dump the queue
            this.serviceConnection.connectToService(false);
            toggle.setText("Start");
            if (this.serializableDataQueue.size() > 0 )
            {
                serializeQueueToDatabase();
            }
            //also clear the visualization
            this.drawView.clearDrawView();
        }
        else
        {
            this.serviceConnection.connectToService(true);
            toggle.setText("Stop");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.database = Room.databaseBuilder(this.getApplicationContext(), EyetrackingDatabase.class, Constants.EyetrackingDatabase).fallbackToDestructiveMigration().build();
        this.drawView = findViewById(R.id.view);
        this.textView = findViewById(R.id.textView);

        this.eyetrackingDataListener = new IEyetrackingDataListener() {
            @Override
            public void onEyetrackingDataMessage(EyetrackingData data) {
                onNewDataMessage(data);
            }
        };

        this.serviceConnection = new EyetrackingServiceConnection(this, this.eyetrackingDataListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (this.serviceConnection.isBound()) {
            //stop the service, and dump the message queue
            this.serviceConnection.connectToService(false);
            if (this.serializableDataQueue.size() > 0 )
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
        this.averageLatency = HelperMethods.ExponentialMovingAverage(this.averageLatency, Instant.now().toEpochMilli() - this.lastTimestamp, 0.8);

        //update our visualization
        this.drawView.updateEye(data.getId(), new PointF(data.getNormalizedPosX(), data.getNormalizedPosY()), data.getPupilDiameter());

        //add to our queue, and dump/drain if necessary
        try {
            this.serializableDataQueue.put(data.toSerializable());
        }
        catch (InterruptedException e)
        {
            Log.e(this.getClass().toString(), "interrupted thread");
        }

        if (this.serializableDataQueue.size() > serializationItemThreshold )
        {
            serializeQueueToDatabase();
            this.textView.setText(String.format("Latency: %2f ms", averageLatency));
        }

        //Log.d(this.getClass().toString(), data.getTimestamp().toString());
        //set the current time to last
        this.lastTimestamp = Instant.now().toEpochMilli();
    }

    /**
     * Method that creates an asynchronous task to batch process messages to the queue.
     * If the app crashes, we probably lose this data
     */
    private void serializeQueueToDatabase()
    {
        Log.d(this.getClass().toString(), "Dumping Queue To database");

        ArrayList<SerializableEyetrackingData> dataList = new ArrayList<>();
        this.serializableDataQueue.drainTo(dataList);
        InsertEyetrackingToDatabaseTask task = new InsertEyetrackingToDatabaseTask(this.database);
        SerializableEyetrackingData[] array = new SerializableEyetrackingData[dataList.size()];
        array = dataList.toArray(array);
        task.execute(array);
    }



}
