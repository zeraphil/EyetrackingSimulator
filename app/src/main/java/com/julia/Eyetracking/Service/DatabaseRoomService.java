package com.julia.Eyetracking.Service;


import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.julia.Eyetracking.Constants;
import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.EyetrackingDatabase;
import com.julia.Eyetracking.DataModel.SerializableEyetrackingData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Service for taking eyetracking data messages and storing them into the Android Room database
 */
public class DatabaseRoomService extends Service {

    private HandlerThread handlerThread = new HandlerThread("DatabaseRoomServiceThread");

    /**
     * Serialization parameters
     */
    private EyetrackingDatabase database;
    private static final int serializationItemThreshold = 60; //dump about once per second
    private LinkedBlockingQueue<SerializableEyetrackingData> serializableDataQueue = new LinkedBlockingQueue<>();

    /**
     * Target we publish for clients to send messages to IncomingMessageHandler.
     */
    protected Messenger incomingMessenger;

    /**
     * Handler for incoming messages, which include data messages from the eyetracking service,
     * and register/unregister messages (not currently doing anything with them)
     */
    protected class IncomingMessageHandler extends Handler {

        public IncomingMessageHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //Log.d(this.getClass().toString(), "Handle message called.");
            switch (msg.what) {
                case EyetrackingServiceMessages.REGISTER:
                    Log.d(this.getClass().toString(), "Register message called.");
                    break;
                case EyetrackingServiceMessages.UNREGISTER:
                    Log.d(this.getClass().toString(), "Unregister message called.");
                    break;
                case EyetrackingServiceMessages.PARCEL_DATA:
                    onNewDataMessage(msg);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Handle a message containing data, including serializing it to the database
     * Handle the data types depending on the dtype of data message
     * @param message
     */
    public void onNewDataMessage(Message message) {
        //Get the data from the message parcel

        try {
            switch (message.what) {
                case EyetrackingServiceMessages.PARCEL_DATA:
                    EyetrackingData data = message.getData().getParcelable(Constants.EyetrackingDataParcel);
                    if (data != null) {
                        this.serializableDataQueue.put(data.toSerializable());
                    }
                    break;
                case EyetrackingServiceMessages.FLATBUFFER_DATA:
                    ByteBuffer buffer = ByteBuffer.wrap(message.getData().getByteArray(Constants.EyetrackingDataBytes));
                    buffer.position(message.getData().getInt(Constants.ByteBufferPosition));

                    data = new EyetrackingData(buffer);
                    if (data != null) {
                        this.serializableDataQueue.put(data.toSerializable());
                    }

                    break;
            }
        }
        catch (InterruptedException e)
        {
            Log.e(this.getClass().toString(), Log.getStackTraceString(e));
        }
        catch (Exception e)
        {
            Log.e(this.getClass().toString(), Log.getStackTraceString(e));
        }
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
        this.database.dbOperations().insertBatchEyetrackingData(dataList);
        //Don't need an async task anymore as I'm running this service on its own thread
        /*InsertEyetrackingToDatabaseTask task = new InsertEyetrackingToDatabaseTask(this.database);
        //SerializableEyetrackingData[] array = new SerializableEyetrackingData[dataList.size()];
        //array = dataList.toArray(array);
        //task.execute(array);*/
    }


    @Override
    public IBinder onBind(Intent intent) {
        this.database = Room.databaseBuilder(this.getApplicationContext(), EyetrackingDatabase.class, Constants.EyetrackingDatabase).fallbackToDestructiveMigration().build();
        this.handlerThread.start();
        this.incomingMessenger = new Messenger(new DatabaseRoomService.IncomingMessageHandler(this.handlerThread.getLooper()));
        return this.incomingMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        this.handlerThread.quitSafely();
        System.out.println("unbound");
        return false;
    }
}
