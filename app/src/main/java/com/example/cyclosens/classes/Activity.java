package com.example.cyclosens.classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Activity  implements Serializable {

    private String key;
    private String nameActivity;
    private String dateActivity;
    private long duration;
    private int bpmAv;
    private int strenghAv;
    private ArrayList<Position> positionList;

    public String getNameActivity() { return nameActivity; }
    public String getDateActivity() { return dateActivity; }
    public long getDuration() { return duration; }
    public String getKey() { return key; }
    public int getBpmAv() { return bpmAv; }
    public int getStrenghAv() { return strenghAv; }
    public ArrayList<Position> getPositionList() { return positionList; }

    public void setNameActivity(String nameActivity) { this.nameActivity = nameActivity; }
    public void setDateActivity(String dateActivity) { this.dateActivity = dateActivity; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setKey(String key) { this.key = key; }
    public void setBpmAv(int bpmAv) { this.bpmAv = bpmAv; }
    public void setStrenghAv(int strenghAv) { this.strenghAv = strenghAv; }
    public void setPositionList(ArrayList<Position> positionList) { this.positionList = positionList; }

    public Activity(String key, String nameActivity, String dateActivity, long duration, int bpmAv, int strenghAv, ArrayList<Position> positionList) {
        setKey(key);
        setNameActivity(nameActivity);
        setDateActivity(dateActivity);
        setDuration(duration);
        setBpmAv(bpmAv);
        setStrenghAv(strenghAv);
        setPositionList(positionList);
    }
}
