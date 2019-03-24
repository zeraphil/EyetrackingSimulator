package com.julia.Eyetracking.DataModel;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.util.Locale;

/**
 * Parcelable Timestamp class
 */
public class Timestamp implements Parcelable {

    private long seconds;
    private int nanoseconds;

    public Timestamp(long seconds, int nanoseconds)
    {
        this.seconds = seconds;
        this.nanoseconds = nanoseconds;
    }

    public static Timestamp now()
    {
        return new Timestamp(Instant.now().getEpochSecond(), Instant.now().getNano());
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Seconds %d", this.seconds);
    }

    public long getSeconds()
    {
        return this.seconds;
    }

    public int getNanoseconds()
    {
        return this.nanoseconds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.seconds);
        dest.writeInt(this.nanoseconds);
    }

    protected Timestamp(Parcel in) {
        this.seconds = in.readLong();
        this.nanoseconds = in.readInt();
    }



    public static final Parcelable.Creator<Timestamp> CREATOR = new Parcelable.Creator<Timestamp>() {
        @Override
        public Timestamp createFromParcel(Parcel source) {
            return new Timestamp(source);
        }

        @Override
        public Timestamp[] newArray(int size) {
            return new Timestamp[size];
        }
    };
}

