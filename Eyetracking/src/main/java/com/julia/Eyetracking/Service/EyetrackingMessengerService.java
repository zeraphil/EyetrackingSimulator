package com.julia.Eyetracking.Service;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.julia.Eyetracking.Constants;
import com.julia.Eyetracking.DataModel.EyetrackingData;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Service that uses the binder framework for IPC
 * Override the sendData/register/unregister methods from the base service model
 */
public class EyetrackingMessengerService extends EyetrackingSimulationService {

    /**
     * Target we publish for clients to send messages to IncomingMessageHandler.
     */
    protected ArrayList<Messenger> clientMessengers;

    public EyetrackingMessengerService()
    {
        super();
        this.clientMessengers = new ArrayList<>();
    }

    /**
     * Method sends data to all registered clients implementing the replyTo Messenger
     * @param data
     */
    @Override
    public void sendData(EyetrackingData data)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.EYETRACKING_DATA_PARCEL, data);
        ArrayList<Messenger> deadClients = new ArrayList<>();

        for(Messenger m : this.clientMessengers)
        {
            Message message = Message.obtain(null, EyetrackingServiceMessages.PARCEL_DATA, 0 , 0);
            bundle.putLong(Constants.IPC_TIMESTAMP, Instant.now().toEpochMilli());

            message.setData(bundle);

            try {
                m.send(message);
            }
            catch (Exception e)
            {
                Log.e(this.getClass().toString(), Log.getStackTraceString(e));
                //this client is probably dead, mark to remove
                deadClients.add(m);
            }
        }

        removeDeadClients(deadClients);
    }

    /**
     * Method to register a client and begin the simulation
     * @param msg
     */
    @Override
    protected void onRegisterMessage(Message msg)
    {
        super.onRegisterMessage(msg);
        this.clientMessengers.add(msg.replyTo);
        if(this.clientMessengers.size() > 0 && !this.simulating)
        {
            Log.d(this.getClass().toString(), "Have a client, start simulation");
            startSimulation();
        }
    }

    /**
     * Method to unregister the client and stop the simulation if necessary
     * @param msg
     */
    @Override
    protected void onUnregisterMessage(Message msg)
    {
        super.onUnregisterMessage(msg);
        this.clientMessengers.remove(msg.replyTo);
        if(this.clientMessengers.size() <= 0)
        {
            Log.d(this.getClass().toString(), "No clients, stopping simulation");
            stopSimulation();
        }
    }

    /**
     * If any client has raised a RemoteException, remove them from the list
     * @param deadClients
     */
    private void removeDeadClients(ArrayList<Messenger> deadClients)
    {
        if(deadClients.size() > 0)
        {
            for (Messenger m : deadClients)
            {
                this.clientMessengers.remove(m);
            }
        }
    }

}
