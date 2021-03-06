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
import com.julia.Eyetracking.DataModel.EyetrackingDatabaseEntity;
import com.julia.Eyetracking.Tasks.InsertEyetrackingToDatabaseTask;

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
    private LinkedBlockingQueue<EyetrackingDatabaseEntity> serializableDataQueue = new LinkedBlockingQueue<>();

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
                    EyetrackingData data = message.getData().getParcelable(Constants.EYETRACKING_DATA_PARCEL);
                    if (data != null) {
                        this.serializableDataQueue.put(data.toDatabaseEntity());
                    }
                    break;
                case EyetrackingServiceMessages.FLATBUFFER_DATA:
                    ByteBuffer buffer = ByteBuffer.wrap(message.getData().getByteArray(Constants.EYETRACKING_DATA_BYTES));
                    buffer.position(message.getData().getInt(Constants.BYTE_BUFFER_POSITION));
                    data = EyetrackingData.fromFlatBuffer(buffer);
                    if (data != null) {
                        this.serializableDataQueue.put(data.toDatabaseEntity());
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

        if(serializableDataQueue.size() > serializationItemThreshold)
        {
            serializeQueueToDatabase();
        }
    }

    /**
     * Method that creates an asynchronous task to batch process messages to the queue.
     * If the app crashes, we probably lose this data
     */
    private void serializeQueueToDatabase()
    {
        Log.d(this.getClass().toString(), "Dumping Queue To database");

        ArrayList<EyetrackingDatabaseEntity> dataList = new ArrayList<>();
        //drain the queue to a collection that will go into the database (freeing up the queue)
        this.serializableDataQueue.drainTo(dataList);
        this.database.dbOperations().insertBatchEyetrackingData(dataList);
    }

    /**
     * Run this method to put in the last batch of messages when an unbind operation happens on the main thread
     */
    private void cleanupQueue()
    {
        Log.d(this.getClass().toString(), "Cleaning Queue into database on unbind");
        if(serializableDataQueue.size() > 0)
        {
            ArrayList<EyetrackingDatabaseEntity> dataList = new ArrayList<>();
            this.serializableDataQueue.drainTo(dataList);
            //need the async task to carry this operation out from an unbind call
            InsertEyetrackingToDatabaseTask task = new InsertEyetrackingToDatabaseTask(this.database);
            EyetrackingDatabaseEntity[] array = new EyetrackingDatabaseEntity[dataList.size()];
            array = dataList.toArray(array);
            task.execute(array);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        this.database = Room.databaseBuilder(this.getApplicationContext(), EyetrackingDatabase.class, Constants.EYETRACKING_DATABASE).fallbackToDestructiveMigration().build();
        this.handlerThread.start();
        this.incomingMessenger = new Messenger(new DatabaseRoomService.IncomingMessageHandler(this.handlerThread.getLooper()));
        return this.incomingMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent){
        this.handlerThread.quitSafely();
        cleanupQueue();
        return false;
    }
}
