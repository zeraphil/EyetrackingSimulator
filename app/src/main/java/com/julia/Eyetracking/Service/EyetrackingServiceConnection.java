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

public class EyetrackingServiceConnection implements ServiceConnection {

    private Activity currentActivity;

    /**
     * Service connection parameters
     */
    private boolean isBound;
    private Messenger serviceMessenger;
    private Messenger replyToMessenger = new Messenger(new IncomingMessageHandler());

    private IEyetrackingDataListener listener;

    public EyetrackingServiceConnection(){}
    public EyetrackingServiceConnection(Activity activity)
    {
        this.currentActivity = activity;
    }

    public EyetrackingServiceConnection(Activity activity, IEyetrackingDataListener listener)
    {
        this.currentActivity = activity;
        this.listener = listener;
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

    public boolean isBound() {
        return isBound;
    }

    public IEyetrackingDataListener getListener() {
        return listener;
    }

    public void setListener(IEyetrackingDataListener listener) {
        this.listener = listener;
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(this.getClass().toString(), "Connected to service");
        this.isBound = true;
        this.serviceMessenger = new Messenger(service);
        registerToService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.isBound = false;
        this.serviceMessenger = null;
    }

    /**
     * Handler for incoming messages from service
     */
    protected class IncomingMessageHandler extends Handler {
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

    /**
     * Method to toggle between bound/unbound states
     * @param bound
     */
    public void connectToService(boolean bound)
    {
        if(bound)
        {         // Bind to the service
            try {
                Log.d(this.getClass().toString(), MessengerEyetrackingService.class.toString());
                currentActivity.bindService(new Intent(currentActivity, MessengerEyetrackingService.class), this, Context.BIND_AUTO_CREATE);
                isBound = bound;
            }
            catch (Exception e) {

                Log.e(this.getClass().toString(),Log.getStackTraceString(e));
            }
        }
        else
        {
            unregisterFromService();
            currentActivity.unbindService(this);
            isBound = bound;
        }
    }

    /**
     * Handle a message containing data, and pass it to the listener
     * @param message
     */
    private void onNewDataMessage(Message message) {
        //Get the data from the message parcel
        EyetrackingData data = message.getData().getParcelable(Constants.EyetrackingDataParcel);
        if(listener != null) {
            this.listener.onEyetrackingDataMessage(data);
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
            Log.d(this.getClass().toString(), "Registering to service");
            serviceMessenger.send(msg);
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
        } catch (RemoteException e) {
            Log.e(this.getClass().toString(),Log.getStackTraceString(e));
        }

    }
}
