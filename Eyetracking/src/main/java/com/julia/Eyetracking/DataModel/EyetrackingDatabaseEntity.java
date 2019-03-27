package com.julia.Eyetracking.DataModel;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Class that decomposes the message to parameters that the Android Room implementation can understand,
 * the special Timestamp class cannot be data converted...
 */
@Entity
public class EyetrackingDatabaseEntity implements Serializable {
    @PrimaryKey
    @NonNull
    private String uniqueID;
    private long seconds;
    private int nanoseconds;
    private boolean id;
    private float confidence;
    private float normalizedPosX;
    private float normalizedPosY;
    private int pupilDiameter;

    @NonNull
    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(@NonNull String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public int getNanoseconds() {
        return nanoseconds;
    }

    public void setNanoseconds(int nanoseconds) {
        this.nanoseconds = nanoseconds;
    }

    public boolean isId() {
        return id;
    }

    public void setId(boolean id) {
        this.id = id;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public float getNormalizedPosX() {
        return normalizedPosX;
    }

    public void setNormalizedPosX(float normalizedPosX) {
        this.normalizedPosX = normalizedPosX;
    }

    public float getNormalizedPosY() {
        return normalizedPosY;
    }

    public void setNormalizedPosY(float normalizedPosY) {
        this.normalizedPosY = normalizedPosY;
    }

    public int getPupilDiameter() {
        return pupilDiameter;
    }

    public void setPupilDiameter(int pupilDiameter) {
        this.pupilDiameter = pupilDiameter;
    }
}
