package com.julia.Eyetracking.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.julia.Eyetracking.Constants;
import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.Simulator.ISimulator;
import com.julia.Eyetracking.Simulator.RandomSimulator;
import com.julia.Eyetracking.Simulator.SimulatorType;

import java.util.ArrayList;

public class BaseEyetrackingService extends Service {

    /**
     * Target we publish for clients to send messages to IncomingMessageHandler.
     */
    private Messenger incomingMessenger;
    private ArrayList<Messenger> clientMessengers;

    /**
     * Simulation fields
     */
    private boolean simulating = false;
    private final int updateInterval = 16; // 1/60 seconds, in milliseconds
    private SimulatorType simulatorType;
    private ISimulator simulator;

    /**
     * The Service thread that will run our simulation and send messages
     */
    private final Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                EyetrackingData data = simulator.update(updateInterval);
                sendData(data);
                //Log.d(this.getClass().toString(), data.timestamp.toString());
            }
            catch (Exception e) {
                Log.e(this.getClass().toString(), Log.getStackTraceString(e));
            }
            finally{
                //task to be done
                handler.postDelayed(this, updateInterval);
            }
        }
    };

    public BaseEyetrackingService()
    {
        clientMessengers = new ArrayList<>();
        simulatorType = SimulatorType.RANDOM;
        setSimulatorType(simulatorType);
    }

    public void setSimulatorType(SimulatorType type)
    {
        switch(type)
        {
            case RANDOM:
                simulator = new RandomSimulator();
                break;
        }
    }

    private void sendData(EyetrackingData data)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.EyetrackingDataParcel, data);

        Message message = Message.obtain(null, EyetrackingServiceMessages.DATA, 0 , 0);
        message.setData(bundle);

        for(Messenger m : clientMessengers)
        {
            try {
                m.send(message);
            }
            catch (RemoteException e)
            {
                Log.e(this.getClass().toString(), Log.getStackTraceString(e));
            }
        }
    }

    public void registerClient(Messenger m)
    {
        clientMessengers.add(m);
        if(clientMessengers.size() > 0 && !this.simulating)
        {
            Log.d(this.getClass().toString(), "Have a client, start simulation");
            startSimulation();
        }
    }

    public void unregisterClient(Messenger m)
    {
        clientMessengers.remove(m);
        if(clientMessengers.size() <= 0)
        {
            Log.d(this.getClass().toString(), "No clients, stopping simulation");
            stopSimulation();
        }
    }

    private void startSimulation()
    {
        if (this.simulator == null)
        {
            setSimulatorType(simulatorType);
        }
        this.handler.post(runnable);
        this.simulating = true;
    }

    private void stopSimulation()
    {
       this.handler.removeCallbacks(runnable);
       this.simulating = false;
    }

    public class IncomingMessageHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Log.d(this.getClass().toString(), "Handle message called.");

            switch (msg.what) {
                case EyetrackingServiceMessages.REGISTER:
                    registerClient(msg.replyTo);
                    Log.d(this.getClass().toString(), "Registering client");
                    break;
                case EyetrackingServiceMessages.UNREGISTER:
                    unregisterClient(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.getClass().toString(), "Service is bound.");
        incomingMessenger = new Messenger(new IncomingMessageHandler());
        return incomingMessenger.getBinder();
    }
}
