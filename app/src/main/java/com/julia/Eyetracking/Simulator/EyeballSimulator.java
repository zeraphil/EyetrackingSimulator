package com.julia.Eyetracking.Simulator;

import android.content.res.Resources;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;

import com.julia.Eyetracking.DataModel.EyetrackingData;
import com.julia.Eyetracking.DataModel.EyetrackingDatabase_Impl;
import com.julia.Eyetracking.DataModel.Timestamp;

import java.util.Random;


/**
 * Simulator that more realistically simulates eye data
 */
public class EyeballSimulator implements ISimulator {

    Random random = new Random();
    private final static long MaximumFixationTime = 5000;
    private final static float SaccadeSpeed = 15; //15 radians per second
    private final static float MaximumPupilDiameter = 6;
    //simulator internals
    private PointF fixationTarget = new PointF();
    private float fixationTime;
    private int widthBounds;
    private int heightBounds;

    private double xDpm;//width density per meters
    private double yDpm;//height density per meters

    private PointF currentLeftEyePosition;
    private PointF currentRightEyePosition;
    private float interpupillaryDistance = .03f; // in m
    private boolean leftEye = true;

    private float distanceFromScreen = 1; //in meters


    public EyeballSimulator()
    {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        this.heightBounds = dm.heightPixels;
        this.widthBounds = dm.widthPixels;
        this.xDpm = dm.xdpi * 0.0254;
        this.yDpm = dm.ydpi * 0.0254;

        this.currentRightEyePosition = new PointF(widthBounds/2 - interpupillaryDistance, heightBounds/2);
        this.currentLeftEyePosition = new PointF(widthBounds/2 + interpupillaryDistance, heightBounds/2);

    }

    @Override
    public EyetrackingData update(float deltaTime) {

        if (fixationTime  <= 0)
        {
            newFixationTarget();
        }
        else
        {
            fixationTime -= deltaTime;
        }

        doEyeMovement(deltaTime);

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
        data.setPupilDiameter(3 + Math.round(random.nextFloat()*MaximumPupilDiameter-3));
        data.setNormalizedPosX(currentEyePosition.x);
        data.setNormalizedPosY(currentEyePosition.y);
        data.setId(leftEye);
        data.setConfidence(random.nextFloat());

        //toggle between eyes
        leftEye = !leftEye;

        return data;
    }

    private void newFixationTarget()
    {
        this.fixationTarget = new PointF(random.nextFloat() * widthBounds, random.nextFloat() * heightBounds );
        this.fixationTime = Math.round(random.nextFloat() * MaximumFixationTime);

        Log.d(this.getClass().toString(), String.format("New fixation target at x %f, y %f", this.fixationTarget.x, this.fixationTarget.y));
    }

    private void doEyeMovement(float deltaTime)
    {
        //deltatime in milliseconds
        //movements arc
        float arc = SaccadeSpeed * deltaTime/1000 * this.distanceFromScreen;
        PointF leftEyeToTarget = vectorBetweenTwoPoints(this.fixationTarget, currentLeftEyePosition);

        if (distanceOfVectorDifference(leftEyeToTarget) < 10)
        {
            currentLeftEyePosition.x += random.nextFloat()*2;
            currentLeftEyePosition.y += random.nextFloat()*2;
        }
        else
        {
            currentLeftEyePosition.x += -leftEyeToTarget.x*arc;
            currentLeftEyePosition.y += -leftEyeToTarget.y*arc;
        }

        PointF rightEyeToTarget = vectorBetweenTwoPoints(this.fixationTarget, currentLeftEyePosition);

        if (distanceOfVectorDifference(rightEyeToTarget) < 10)
        {
            currentRightEyePosition.x += random.nextFloat()*2;
            currentRightEyePosition.y += random.nextFloat()*2;
        }
        else {
            currentRightEyePosition.x += -rightEyeToTarget.x * arc;
            currentRightEyePosition.y += -rightEyeToTarget.y * arc;
        }

    }

    private PointF vectorBetweenTwoPoints(PointF a, PointF b)
    {
        return new PointF(b.x-a.x, b.y - a.y);
    }

    private double distanceOfVectorDifference(PointF v)
    {
        return Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2));
    }

}
