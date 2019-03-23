package com.julia.Eyetracking.Simulator;

import android.content.res.Resources;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.Timestamp;

import java.util.Random;

public class RandomSimulator implements ISimulator {

    //Simulator Internals
    int widthBounds;
    int heightBounds;
    int maxPupilDiameter = 6;

    public RandomSimulator()
    {
        this.heightBounds = Resources.getSystem().getDisplayMetrics().heightPixels;
        this.widthBounds = Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    @Override
    public EyetrackingData update(float deltaTime)
    {
        Random rand  = new Random();

        EyetrackingData data = new EyetrackingData();
        data.setConfidence(rand.nextFloat());
        data.setId(rand.nextBoolean());
        data.setNormalizedPosX(rand.nextFloat() * widthBounds);
        data.setNormalizedPosY(rand.nextFloat() * heightBounds);
        data.setPupilDiameter(this.maxPupilDiameter);
        data.setTimestamp(Timestamp.now());

        return data;
    }
}
