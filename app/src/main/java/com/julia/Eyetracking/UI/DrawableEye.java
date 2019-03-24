package com.julia.Eyetracking.UI;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Data class to hold some parameters inside DrawView
 */
public class DrawableEye {

    private PointF position;
    float pupilDiameter;
    boolean isVisible;

    public DrawableEye() {
        this.isVisible = false;
    }

    public DrawableEye(PointF position, float pupilDiameter) {
        this.position = position;
        this.pupilDiameter = pupilDiameter;
        this.isVisible = true;
    }

    /**
     * Getter/setters
     */

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public float getPupilDiameter() {
        return pupilDiameter;
    }

    public void setPupilDiameter(float pupilDiameter) {
        this.pupilDiameter = pupilDiameter;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
