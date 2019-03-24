package com.julia.Eyetracking.Simulator;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.Timestamp;

import java.util.Random;

public class RandomSimulator implements ISimulator {

    //Simulator Internals
    int maxPupilDiameter = 60;

    public RandomSimulator()
    {

    }

    @Override
    public EyetrackingData update(float deltaTime)
    {
        Random rand  = new Random();

        EyetrackingData data = new EyetrackingData();
        data.setConfidence(rand.nextFloat());
        data.setId(rand.nextBoolean());
        data.setNormalizedPosX(rand.nextFloat());
        data.setNormalizedPosY(rand.nextFloat());
        data.setPupilDiameter(this.maxPupilDiameter);
        data.setTimestamp(Timestamp.now());

        return data;
    }
}
