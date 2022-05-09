package com.example.cyclosens.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Activity  implements Serializable {

    private String key;
    private String nameActivity;
    private String dateActivity;
    private long duration;
    private int bpmAv;
    private int strengthAv;
    private int speedAv;
    private int distance;
    private ArrayList<Position> positionList;

    public String getNameActivity() { return nameActivity; }
    public String getDateActivity() { return dateActivity; }
    public long getDuration() { return duration; }
    public String getKey() { return key; }
    public int getBpmAv() { return bpmAv; }
    public int getStrengthAv() { return strengthAv; }
    public int getSpeedAv() { return speedAv; }
    public int getDistance() { return distance; }
    public ArrayList<Position> getPositionList() { return positionList; }

    public void setNameActivity(String nameActivity) { this.nameActivity = nameActivity; }
    public void setDateActivity(String dateActivity) { this.dateActivity = dateActivity; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setKey(String key) { this.key = key; }
    public void setBpmAv(int bpmAv) { this.bpmAv = bpmAv; }
    public void setStrengthAv(int strengthAv) { this.strengthAv = strengthAv; }
    public void setSpeedAv(int speedAv) { this.speedAv = speedAv; }
    public void setDistance(int distance) { this.distance = distance; }
    public void setPositionList(ArrayList<Position> positionList) { this.positionList = positionList; }

    public Activity(String key, String nameActivity, String dateActivity, long duration, int bpmAv, int strengthAv, int speedAv, int distance, ArrayList<Position> positionList) {
        setKey(key);
        setNameActivity(nameActivity);
        setDateActivity(dateActivity);
        setDuration(duration);
        setBpmAv(bpmAv);
        setStrengthAv(strengthAv);
        setSpeedAv(speedAv);
        setDistance(distance);
        setPositionList(positionList);
    }
}
