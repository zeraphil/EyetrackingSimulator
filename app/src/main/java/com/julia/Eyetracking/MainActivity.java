package com.julia.Eyetracking;

import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.Service.ServiceConnectionManager;
import com.julia.Eyetracking.Service.IEyetrackingDataListener;
import com.julia.Eyetracking.UI.DrawView;

import java.time.Instant;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /**
     * Parameters to measure latency
     */
    private long lastTimestamp = 0;
    private double averageLatency = 0.0;

    /**
     * Service connection parameters
     */
    ServiceConnectionManager serviceConnection;

    /**
     * Listener that will handler our eyetracking data parcel callback
     */
    IEyetrackingDataListener eyetrackingDataListener;


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
        Log.d(this.getClass().toString(), "Toggle button called" + this.serviceConnection.areServicesBound());

        Button toggle = findViewById(R.id.button);
        if (this.serviceConnection.areServicesBound())
        {
            //once we're done, disconnect, and dump the queue
            this.serviceConnection.connectToServices(false);
            toggle.setText("Bind");
            //also clear the visualization
            this.drawView.clearDrawView();
        }
        else
        {
            this.serviceConnection.connectToServices(true);
            toggle.setText("Unbind");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.drawView = findViewById(R.id.view);
        this.textView = findViewById(R.id.textView);

        this.eyetrackingDataListener = new IEyetrackingDataListener() {
            @Override
            public void onEyetrackingDataMessage(EyetrackingData data) {
                onNewEyetrackingData(data);
            }
        };

        this.serviceConnection = new ServiceConnectionManager(this, this.eyetrackingDataListener, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (this.serviceConnection.areServicesBound()) {
            //stop the service, and dump the message queue
            this.serviceConnection.connectToServices(false);
        }
    }


    /**
     * Handle a message containing data, including serializing it and updating the visualization
     * @param data
     */
    private void onNewEyetrackingData(EyetrackingData data)
    {
        this.averageLatency = HelperMethods.ExponentialMovingAverage(this.averageLatency, Instant.now().toEpochMilli() - this.lastTimestamp, 0.8);

        //update our visualization
        this.drawView.updateEye(data.getId(), new PointF(data.getNormalizedPosX(), data.getNormalizedPosY()), data.getPupilDiameter());

        this.textView.setText(String.format(Locale.US,"Latency: %2f ms", averageLatency));

        //Log.d(this.getClass().toString(), data.getTimestamp().toString());
        //set the current time to last
        this.lastTimestamp = Instant.now().toEpochMilli();
    }



}
