package com.example.hodophile.ui.home;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * TimeParcel class encapsulates the time
 * and will be stored in the database.
 */
public class TimeParcel implements Parcelable, Comparable<TimeParcel> {

    private int hr, min;

    public TimeParcel() {
    }

    public TimeParcel(int hr, int min) {
        this.hr = hr;
        this.min = min;
    }

    protected TimeParcel(Parcel in) {
        hr = in.readInt();
        min = in.readInt();
    }

    public static final Creator<TimeParcel> CREATOR = new Creator<TimeParcel>() {
        @Override
        public TimeParcel createFromParcel(Parcel in) {
            return new TimeParcel(in);
        }

        @Override
        public TimeParcel[] newArray(int size) {
            return new TimeParcel[size];
        }
    };

    public int getHr() { return hr; }

    public int getMin() { return min; }

    public void setHr(int hr) { this.hr = hr; }

    public void setMin(int min) { this.min = min; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.hr);
        dest.writeInt(this.min);
    }

    @Override
    public String toString() {
        String amPm = "AM";
        int hour = this.hr;
        if (this.hr >= 12) {
            amPm = "PM";
        }
        if (this.hr > 12) {
            hour = hour - 12;
        }
        return String.format("%02d:%02d", hour, this.min) + amPm;
    }

    @Override
    public int compareTo(TimeParcel o) {
        if (this.hr == o.getHr()) {
            return this.min - o.getMin();
        }
        return this.hr - o.getHr();
    }
}
