package com.julia.Eyetracking.DataModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.flatbuffers.FlatBufferBuilder;
import com.julia.Eyetracking.DataModel.FlatBufferSchema.Message;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Class to hold the Eyetracking data Message with a Parcelable interface for passing to Binder framework
 * Includes adapter methods to convert to a database entry and flat buffer serialization
 */
public class EyetrackingData implements Parcelable {

    private String uniqueID;
    private Timestamp timestamp;
    private boolean id;
    private float confidence;
    private float normalizedPosX;
    private float normalizedPosY;
    private int pupilDiameter;


    public EyetrackingData() {
        this.uniqueID = UUID.randomUUID().toString();
    }

    public EyetrackingDatabaseEntity toDatabaseEntity()
    {
        EyetrackingDatabaseEntity entity = new EyetrackingDatabaseEntity();
        entity.setUniqueID(this.uniqueID);
        entity.setConfidence(this.confidence);
        entity.setId(this.id);
        entity.setNanoseconds(this.timestamp.getNanoseconds());
        entity.setSeconds(this.timestamp.getSeconds());
        entity.setNormalizedPosX(this.normalizedPosX);
        entity.setNormalizedPosY(this.normalizedPosY);
        entity.setPupilDiameter(this.pupilDiameter);

        return entity;
    }

    /**
     * To flat buffer, for more modularity
     * @return
     */
    public ByteBuffer toFlatBuffer()
    {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        int uuid = builder.createString(this.uniqueID);
        int ts = com.julia.Eyetracking.DataModel.FlatBufferSchema.Timestamp.createTimestamp
                (builder, this.timestamp.getSeconds(), this.timestamp.getNanoseconds());

        Message.startMessage(builder);
        Message.addUniqueId(builder, uuid);
        Message.addConfidence(builder, this.confidence);
        Message.addID(builder, this.id);
        Message.addNormalizedPosX(builder, this.normalizedPosX);
        Message.addNormalizedPosY(builder, this.normalizedPosY);
        Message.addPupilDiameter(builder, this.pupilDiameter);

        Message.addTimestamp(builder, com.julia.Eyetracking.DataModel.FlatBufferSchema.Timestamp.createTimestamp
                (builder, this.timestamp.getSeconds(), this.timestamp.getNanoseconds()));

        int msg = Message.endMessage(builder);
        Message.finishMessageBuffer(builder, msg);
        return builder.dataBuffer();
    }

    /**
     * Builder method to get data parcel from flat buffer
     * @param flatBuffer
     * @return
     */
    public static EyetrackingData fromFlatBuffer(ByteBuffer flatBuffer)
    {
        try {
            Message in = Message.getRootAsMessage(flatBuffer);

            EyetrackingData data = new EyetrackingData();
            data.uniqueID = in.UniqueId();
            data.timestamp = new Timestamp(in.Timestamp().seconds(), in.Timestamp().nanoseconds());
            data.id = in.ID();
            data.confidence = in.Confidence();
            data.normalizedPosX = in.NormalizedPosX();
            data.normalizedPosY = in.NormalizedPosY();
            data.pupilDiameter = in.PupilDiameter();

            return data;
        }
        catch (Exception e)
        {
            //problem in the deserialization, most likely.
            Log.e(EyetrackingData.class.toString(), Log.getStackTraceString(e));
            return null;
        }

    }


    /**
     * Getter/setter
     */

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public boolean getId() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uniqueID);
        dest.writeParcelable(this.timestamp, flags);
        dest.writeByte(this.id ? (byte) 1 : (byte) 0);
        dest.writeFloat(this.confidence);
        dest.writeFloat(this.normalizedPosX);
        dest.writeFloat(this.normalizedPosY);
        dest.writeInt(this.pupilDiameter);
    }

    protected EyetrackingData(Parcel in) {
        this.uniqueID = in.readString();
        this.timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        this.id = in.readByte() != 0;
        this.confidence = in.readFloat();
        this.normalizedPosX = in.readFloat();
        this.normalizedPosY = in.readFloat();
        this.pupilDiameter = in.readInt();
    }

    public static final Creator<EyetrackingData> CREATOR = new Creator<EyetrackingData>() {
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