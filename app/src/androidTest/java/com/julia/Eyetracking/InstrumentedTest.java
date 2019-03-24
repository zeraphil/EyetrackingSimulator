package com.julia.Eyetracking;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.SerializableEyetrackingData;
import com.julia.Eyetracking.DataModel.EyetrackingDatabase;
import com.julia.Eyetracking.Simulator.RandomSimulator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    public static final int iterations = 1000;

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.julia.Eyetracking", appContext.getPackageName());
    }

    @Test
    public void roomPersistenceInsertAndDelete() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.julia.Eyetracking", appContext.getPackageName());
        EyetrackingDatabase database = Room.databaseBuilder(appContext, EyetrackingDatabase.class, Constants.EyetrackingDatabase).fallbackToDestructiveMigration().build();
        RandomSimulator simulator = new RandomSimulator();
        long timePrior = Instant.now().toEpochMilli();
        database.dbOperations().deleteAll();

        for(int i = 0; i < iterations; i++)
        {
            EyetrackingData data = simulator.update(1);
            database.dbOperations().insertEyetrackingData(data.toSerializable());
        }
        Log.d(this.getClass().toString(), "Time completed: " + (Instant.now().toEpochMilli() - timePrior));

        List<SerializableEyetrackingData> all = database.dbOperations().getAll();
        Log.d(this.getClass().toString(), "Count of all entries " + all.size());

        //did we put in the expected amount of objects, succesfully
        Assert.assertEquals(all.size(), iterations);

        database.dbOperations().deleteAll();
        //Assert.assertEquals(all.size(), 0);
    }

    /**
     * Test to ensure flat buffer serial/deserial works
     *
     */

    @Test public void flatBufferTest()
    {
        RandomSimulator simulator = new RandomSimulator();
        long timePrior = Instant.now().toEpochMilli();

        for(int i = 0; i < iterations; i++)
        {
            EyetrackingData data = simulator.update(1);
            ByteBuffer buf = data.toFlatBuffer();
            byte[] arr = buf.array();

            ByteBuffer bff = ByteBuffer.wrap(arr);
            //test it immediately
            EyetrackingData fbData = new EyetrackingData(bff);
            //did we deserialize correctly
           // Log.d("test", data.getUniqueID());
           // Log.d("test", fbData.getUniqueID());
            Assert.assertEquals(data.getUniqueID(), fbData.getUniqueID());

        }
        Log.d(this.getClass().toString(), "Time completed: " + (Instant.now().toEpochMilli() - timePrior));
    }

    /**
     * Stub, service test rule doesn't seem to work
     */
   // @Rule
    //public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testWithBoundService() {
       /* IBinder binder = mServiceRule.bindService(
                new Intent(InstrumentationRegistry.getTargetContext(), EyetrackingFlatBufferService.class));
        MyService service = ((MyService.LocalBinder) binder).getService();
        assertTrue("True wasn't returned", service.doSomethingToReturnTrue());*/
    }

}
