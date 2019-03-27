package com.julia.Eyetracking.Service;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.julia.Eyetracking.Constants;
import com.julia.Eyetracking.DataModel.EyetrackingData;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;

public class EyetrackingFlatBufferService extends EyetrackingSimulationService {

    /**
     * Target we publish for clients to send messages to IncomingMessageHandler.
     */
    protected ArrayList<Messenger> clientMessengers;

    public EyetrackingFlatBufferService()
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
        ByteBuffer buf = data.toFlatBuffer();
        bundle.putLong(Constants.IPC_TIMESTAMP, Instant.now().toEpochMilli());
        bundle.putInt(Constants.BYTE_BUFFER_POSITION, buf.position());
        bundle.putByteArray(Constants.EYETRACKING_DATA_BYTES, buf.array());

        ArrayList<Messenger> deadClients = new ArrayList<>();

        for(Messenger m : this.clientMessengers)
        {
            Message message = Message.obtain(null, EyetrackingServiceMessages.FLATBUFFER_DATA, 0 , 0);
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
