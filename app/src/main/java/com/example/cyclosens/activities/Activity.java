package com.example.cyclosens.activities;

import android.os.Parcel;
import android.os.Parcelable;

public class Activity  implements Parcelable {

    private String nameActivity;
    private String dateActivity;

    public String getNameActivity() { return nameActivity; }
    public String getDateActivity() { return dateActivity; }

    public void setNameActivity(String nameActivity) { this.nameActivity = nameActivity; }
    public void setDateActivity(String dateActivity) { this.dateActivity = dateActivity; }

    public Activity(String nameActivity, String dateActivity) {
        setNameActivity(nameActivity);
        setDateActivity(dateActivity);
    }

    protected Activity(Parcel in) {
        this.dateActivity = in.readString();
        this.nameActivity = in.readString();
    }

    public static final Creator<Activity> CREATOR = new Creator<Activity>() {
        @Override
        public Activity createFromParcel(Parcel in) {
            return new Activity(in);
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.nameActivity);
        parcel.writeString(this.dateActivity);
    }
}
