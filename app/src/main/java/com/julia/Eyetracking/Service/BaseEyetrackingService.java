package com.julia.Eyetracking.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.Simulator.EyeballSimulator;
import com.julia.Eyetracking.Simulator.ISimulator;
import com.julia.Eyetracking.Simulator.RandomSimulator;
import com.julia.Eyetracking.Simulator.SimulatorType;


/**
 * Class that defines the simulation paradigm, variables, and methods of the service,
 * but only puts messages into log
 */
public class BaseEyetrackingService extends Service {

    /**
     * Target we publish for clients to send messages to IncomingMessageHandler.
     */
    protected Messenger incomingMessenger;

    /**
     * Simulation fields
     */
    protected boolean simulating = false;
    protected final int updateInterval = 16; // 1/60 seconds, in milliseconds
    protected SimulatorType simulatorType;
    protected ISimulator simulator;

    //this thread will do simulation and message handling
    protected HandlerThread handlerThread = new HandlerThread("EyetrackingThread");

    /**
     * The Service thread that will run our simulation and send messages
     */
    private Handler handler;
    protected Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                //get the data on every simulation update
                EyetrackingData data = simulator.update(updateInterval);
                //then send it through the overriden interface
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
        this.simulatorType = SimulatorType.EYEBALL;
        setSimulatorType(simulatorType);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.getClass().toString(), "Service is bound.");
        //run the simulation and messaging on the service's own thread
        this.handlerThread.start();
        this.handler = new Handler(handlerThread.getLooper());
        this.incomingMessenger = new Messenger(new IncomingMessageHandler(handlerThread.getLooper()));
        return this.incomingMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        //quit the thread when no one's looking
        this.handlerThread.quitSafely();
        return false;
    }

    /**
     * Override this method to change the behavior of the data sending in the service
     * @param data
     */
    public void sendData(EyetrackingData data)
    {
        Log.d(this.getClass().toString(), data.toString());
    }

    /**
     * Method to change the simulation type
     * @param type
     */
    public void setSimulatorType(SimulatorType type)
    {
        switch(type)
        {
            case RANDOM:
                this.simulator = new RandomSimulator();
                break;
            case EYEBALL:
                this.simulator = new EyeballSimulator();
                break;
        }
    }

    /**
     * Method to register a client and begin the simulation
     * @param msg
     */
    protected void onRegisterMessage(Message msg)
    {
        Log.d(this.getClass().toString(), "Registering client");
    }

    /**
     * Method to unregister the client and stop the simulation if necessary
     * @param msg
     */
    protected void onUnregisterMessage(Message msg)
    {
        Log.d(this.getClass().toString(), "Unregistering client");
    }

    /**
     * Start the simulation in the handler
     */
    protected void startSimulation()
    {
        if (this.simulator == null)
        {
            setSimulatorType(simulatorType);
        }
        this.handler.post(runnable);
        this.simulating = true;
    }

    /**
     * Stop the simulation in the handler and set simulating to false
     */
    protected void stopSimulation()
    {
        this.handler.removeCallbacks(runnable);
        this.simulating = false;
    }

    /**
     * Handle the register/unregister messages
     */
    protected class IncomingMessageHandler extends Handler {

        public IncomingMessageHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(this.getClass().toString(), "Handle message called.");

            switch (msg.what) {
                case EyetrackingServiceMessages.REGISTER:
                    onRegisterMessage(msg);
                    break;
                case EyetrackingServiceMessages.UNREGISTER:
                    onUnregisterMessage(msg);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
