package com.julia.Eyetracking.Service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.julia.Eyetracking.Constants;
import com.julia.Eyetracking.DataModel.EyetrackingData;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;


/**
 * Accessory class to handle the binding unbinding outside of the main activtity, as well as
 * handle inter-service communication, namely eyetracking datastream to eyetracking database
 * messages.
 */
public class ServiceConnectionManager {

    private Activity currentActivity;

    /**
     * Bool that returns whether one or more services are bound
     */
    private boolean servicesAreBound;

    /**
     * Flag to invoke the database service connection when binding
     */
    private boolean storeToDatabase = false;

    /**
     * Answer to the "what to use for networked connection"
     */
    private boolean doFlatBufferService = true;

    /**
     * Messenger/service connection pair for communicating with the eyetracking data stream service
     */
    protected Messenger eyetrackingServiceMessenger;
    private ServiceConnection eyetrackingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(this.getClass().toString(), "Bound the eyetracking service.");
            eyetrackingServiceMessenger = new Messenger(service);
            //send a message to the eyetracking service, passing the local incoming handler as a reply to
            sendRegisterMessage(eyetrackingServiceMessenger, incomingMessenger);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            eyetrackingServiceMessenger = null;
        }
    };

    /**
     * Messenger/service connection pair for communicating with the eyetracking database service
     */
    protected Messenger databaseServiceMessenger;
    private ServiceConnection databaseServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(this.getClass().toString(), "Bound the database service.");
            databaseServiceMessenger = new Messenger(service);
            //send a register message to the database service, passing the local incoming handler as a reply to
            sendRegisterMessage(databaseServiceMessenger, incomingMessenger);
            if(eyetrackingServiceMessenger!= null)
            {
                sendRegisterMessage(eyetrackingServiceMessenger, databaseServiceMessenger);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            databaseServiceMessenger = null;
        }
    };


    //handles incoming messenges from services
    private Messenger incomingMessenger = new Messenger(new IncomingMessageHandler(new WeakReference<>(this)));

    private IEyetrackingDataListener listener;

    public ServiceConnectionManager(){}
    public ServiceConnectionManager(Activity activity)
    {
        this.currentActivity = activity;
    }

    public ServiceConnectionManager(Activity activity, IEyetrackingDataListener listener, boolean doStorage)
    {
        this.currentActivity = activity;
        this.listener = listener;
        this.storeToDatabase = doStorage;
    }

    /**
     * Handler for incoming messages from service
     */
    protected static class IncomingMessageHandler extends Handler {

        WeakReference<ServiceConnectionManager> manager;
        public IncomingMessageHandler(WeakReference<ServiceConnectionManager> manager)
        {
            this.manager = manager;
        }

        @Override
        public void handleMessage(Message msg) {
            //Log.d(this.getClass().toString(), "Handle message called.");
            switch (msg.what) {
                case EyetrackingServiceMessages.FLATBUFFER_DATA:
                case EyetrackingServiceMessages.PARCEL_DATA:
                    this.manager.get().onNewDataMessage(msg);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Method to toggle between bound/unbound states
     * @param
     */
    public void connectToServices(boolean doBinding)
    {
        if(doBinding)
        {         // Bind to the service
            try {
                Log.d(this.getClass().toString(), EyetrackingMessengerService.class.toString());
                if (!doFlatBufferService)
                {
                    currentActivity.bindService(new Intent(currentActivity, EyetrackingMessengerService.class), this.eyetrackingServiceConnection, Context.BIND_AUTO_CREATE);
                }
                else
                {
                    currentActivity.bindService(new Intent(currentActivity, EyetrackingFlatBufferService.class), this.eyetrackingServiceConnection, Context.BIND_AUTO_CREATE);
                }

                if(this.storeToDatabase)
                {
                    this.currentActivity.bindService(new Intent(currentActivity, DatabaseRoomService.class), this.databaseServiceConnection, Context.BIND_AUTO_CREATE);
                    this.storeToDatabase = true;
                }

                servicesAreBound = true;

            }
            catch (Exception e) {

                Log.e(this.getClass().toString(),Log.getStackTraceString(e));
            }
        }
        else
        {
            //do all the unregister messages before we unbind
            sendUnregisterMessage(this.eyetrackingServiceMessenger, this.incomingMessenger);

            if(this.storeToDatabase)
            {
                //unregister from both me and the eyetracking messenger
                sendUnregisterMessage(this.databaseServiceMessenger, this.incomingMessenger);
                sendUnregisterMessage(this.eyetrackingServiceMessenger, this.databaseServiceMessenger);
                this.currentActivity.unbindService(this.databaseServiceConnection);
            }
            this.currentActivity.unbindService(this.eyetrackingServiceConnection);
            servicesAreBound = false;
        }
    }

    /**
     * Handle a message containing data, and pass it to the listener
     * Handle the data types depending on the dytpe of data message
     * @param message
     */
    public void onNewDataMessage(Message message) {
        //Get the data from the message parcel

        switch (message.what) {
            case EyetrackingServiceMessages.PARCEL_DATA:
            EyetrackingData data = message.getData().getParcelable(Constants.EyetrackingDataParcel);
            if(data != null) {
                if (listener != null) {
                    this.listener.onEyetrackingDataMessage(data);
                }
            }
            break;
            case EyetrackingServiceMessages.FLATBUFFER_DATA:
                ByteBuffer buffer = ByteBuffer.wrap(message.getData().getByteArray(Constants.EyetrackingDataBytes));
                buffer.position(message.getData().getInt(Constants.ByteBufferPosition));
                try {
                    data = new EyetrackingData(buffer);
                    if (data != null) {
                        if (listener != null) {
                            this.listener.onEyetrackingDataMessage(data);
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.e(this.getClass().toString(), Log.getStackTraceString(e));
                }
                break;
        }
    }

    /**
     * Creates a message to register the activity to the service
     */
    private void sendRegisterMessage(Messenger messenger, Messenger replyTo)
    {
        if (!this.servicesAreBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, EyetrackingServiceMessages.REGISTER, 0, 0);
        msg.replyTo = replyTo;
        try {
            Log.d(this.getClass().toString(), "Registering to service");
            messenger.send(msg);
        } catch (RemoteException e) {

            Log.e("MainActivity",Log.getStackTraceString(e));
        }
    }

    /**
     * Creates a message to unregister the activity to the service
     */
    private void sendUnregisterMessage(Messenger messenger, Messenger replyTo)
    {
        if (!this.servicesAreBound) return;
        // Create and send a message to the service, using a supported 'what' value
        Message msg = Message.obtain(null, EyetrackingServiceMessages.UNREGISTER, 0, 0);
        msg.replyTo = replyTo;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.e(this.getClass().toString(),Log.getStackTraceString(e));
        }
    }

    /**
     * getter/setters
     */
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public boolean areServicesBound() {
        return servicesAreBound;
    }

    public IEyetrackingDataListener getListener() {
        return listener;
    }

    public void setListener(IEyetrackingDataListener listener) {
        this.listener = listener;
    }
}
