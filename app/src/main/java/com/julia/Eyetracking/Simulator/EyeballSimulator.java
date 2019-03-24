package com.julia.Eyetracking.Simulator;

import android.content.res.Resources;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.Timestamp;
import com.julia.Eyetracking.HelperMethods;

import java.util.Random;


/**
 * Simulator that (slightly) more realistically simulates eye data
 */
public class EyeballSimulator implements ISimulator {

    Random random = new Random();
    private final static long MaximumFixationTime = 3000;
    private final static float SaccadeSpeed = 15; //15 radians per second
    private final static float MaximumPupilDiameter = 60;
    private final static double TargetDistanceThreshold = .05;
    //simulator internals
    private PointF fixationTarget = new PointF();
    private float fixationTime;
    private int widthBounds;
    private int heightBounds;

    private PointF currentLeftEyePosition;
    private PointF currentRightEyePosition;
    private float ipd = .03f; // normalized interpupillary distance
    private boolean leftEye = true;

    private float distanceFromScreen = 1; //in meters


    /**
     * Initialize some of the metrics
     */
    public EyeballSimulator()
    {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        this.heightBounds = dm.heightPixels;
        this.widthBounds = dm.widthPixels;
        //this.xDpm = dm.xdpi * 0.0254;
        //this.yDpm = dm.ydpi * 0.0254;

        this.currentRightEyePosition = new PointF(0.5f+this.ipd, 0.5f);
        this.currentLeftEyePosition = new PointF(0.5f-this.ipd, 0.5f);
    }

    @Override
    public EyetrackingData update(float deltaTime) {

        //Countdown timer to "hold" the target at its current position
        if (fixationTime  <= 0)
        {
            newFixationTarget();
        }
        else
        {
            fixationTime -= deltaTime;
        }

        //update eye positions based on deltaTime
        doEyeMovement(deltaTime);

        //toggle between eye updates
        PointF currentEyePosition;
        if (leftEye)
        {
            currentEyePosition = currentLeftEyePosition;
        }
        else
        {
            currentEyePosition = currentRightEyePosition;
        }


        EyetrackingData data = new EyetrackingData();
        data.setTimestamp(Timestamp.now());
        data.setPupilDiameter(Math.round(MaximumPupilDiameter - random.nextFloat()*3));
        data.setNormalizedPosX(currentEyePosition.x);
        data.setNormalizedPosY(currentEyePosition.y);
        data.setId(leftEye);
        data.setConfidence(random.nextFloat());

        //toggle back between eyes
        leftEye = !leftEye;

        return data;
    }


    /**
     * Create a new fixation target to move the eyeballs to
     */
    private void newFixationTarget()
    {
        this.fixationTarget = new PointF((random.nextFloat() * widthBounds)/widthBounds, (random.nextFloat() * heightBounds)/heightBounds );
        this.fixationTime = Math.round(random.nextFloat() * MaximumFixationTime);

        Log.d(this.getClass().toString(), String.format("New fixation target at x %f, y %f", this.fixationTarget.x, this.fixationTarget.y));
    }

    private void doEyeMovement(float deltaTime)
    {
        //deltatime in milliseconds
        //movements arc, delta time to seconds
        float arc = SaccadeSpeed * deltaTime/1000 * this.distanceFromScreen;
        double chordLength = Math.sin(SaccadeSpeed/2*deltaTime/1000) * this.distanceFromScreen * 2;
        PointF leftEyeToTarget = HelperMethods.vectorDifference(this.fixationTarget, currentLeftEyePosition);

        if (HelperMethods.vectorDistance(this.fixationTarget, currentLeftEyePosition) < TargetDistanceThreshold)
        {
            //add some random noise
            currentLeftEyePosition.x += random.nextFloat()*.001;
            currentLeftEyePosition.y += random.nextFloat()*.001;
        }
        else
        {
            currentLeftEyePosition.x += -leftEyeToTarget.x*chordLength;
            currentLeftEyePosition.y += -leftEyeToTarget.y*chordLength;
        }

        PointF rightEyeToTarget = HelperMethods.vectorDifference(this.fixationTarget, currentRightEyePosition);

        if (HelperMethods.vectorDistance(this.fixationTarget, currentRightEyePosition) < TargetDistanceThreshold)
        {
            //add some random noise
            currentRightEyePosition.x += random.nextFloat()*.001;
            currentRightEyePosition.y += random.nextFloat()*.001;
        }
        else {
            currentRightEyePosition.x += -rightEyeToTarget.x * chordLength;
            currentRightEyePosition.y += -rightEyeToTarget.y * chordLength;
        }

    }

}
