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
        data.confidence = rand.nextFloat();
        data.id = rand.nextBoolean();
        data.normalizedPosX = rand.nextFloat() * widthBounds;
        data.normalizedPosY = rand.nextFloat() * heightBounds;
        data.pupilDiameter = rand.nextInt(this.maxPupilDiameter);
        data.timestamp = Timestamp.now();

        return data;
    }
}
