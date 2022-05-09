package com.example.cyclosens.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Itinerary implements Serializable {

    private int duration;
    private int distance;
    private ArrayList<Position> positionList;

    public int getDuration() { return duration; }
    public int getDistance() { return distance; }
    public ArrayList<Position> getPositionList() { return positionList; }

    public void setDuration(int duration) { this.duration = duration; }
    public void setDistance(int distance) { this.distance = distance; }
    public void setPositionList(ArrayList<Position> positionList) { this.positionList = positionList; }

    public Itinerary(int duration, int distance,ArrayList<Position> positionList) {
        setDuration(duration);
        setDistance(distance);
        setPositionList(positionList);
    }
}