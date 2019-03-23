package com.julia.Eyetracking.DataModel;

import android.os.Parcel;
import android.os.Parcelable;

public class EyetrackingData implements Parcelable {

    public Timestamp timestamp;
    public boolean id;
    public float confidence;
    public float normalizedPosX;
    public float normalizedPosY;
    public int pupilDiameter;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.timestamp, flags);
        dest.writeByte(this.id ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.confidence);
        dest.writeFloat(this.normalizedPosX);
        dest.writeFloat(this.normalizedPosY);
        dest.writeInt(this.pupilDiameter);
    }

    public EyetrackingData() {
    }

    protected EyetrackingData(Parcel in) {
        this.timestamp = in.readParcelable(com.julia.Eyetracking.DataModel.Timestamp.class.getClassLoader());
        this.id = in.readByte() != 0;
        this.confidence = in.readFloat();
        this.normalizedPosX = in.readFloat();
        this.normalizedPosY = in.readFloat();
        this.pupilDiameter = in.readInt();
    }

    public static final Parcelable.Creator<EyetrackingData> CREATOR = new Parcelable.Creator<EyetrackingData>() {
        @Override
        public EyetrackingData createFromParcel(Parcel source) {
            return new EyetrackingData(source);
        }

        @Override
        public EyetrackingData[] newArray(int size) {
            return new EyetrackingData[size];
        }
    };
}