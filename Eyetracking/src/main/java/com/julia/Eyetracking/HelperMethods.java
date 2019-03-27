package com.julia.Eyetracking;

import android.graphics.PointF;

public class HelperMethods {
    /**
     * Helper method to get the distance between the points represented as a PointF (but really the
     * vector components
     * @param a
     * @param b
     * @return
     */
    public static PointF vectorDifference(PointF a, PointF b)
    {
        return new PointF(b.x-a.x, b.y - a.y);
    }

    /**
     * Get the distance between two points
     * @return
     */
    public static double vectorDistance(PointF a, PointF b)
    {
        PointF diff = vectorDifference(a, b);
        return Math.sqrt(Math.pow(diff.x, 2) + Math.pow(diff.y, 2));
    }

    /**
     * Method to help calculate a smoothed average
     * @param currentAverage
     * @param newValue
     * @param smoothing
     * @return
     */
    public static double ExponentialMovingAverage(double currentAverage, double newValue, double smoothing)
    {
        return newValue*smoothing + currentAverage*(1-smoothing);
    }
}
