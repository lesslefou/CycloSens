package com.example.cyclosens.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Itinerary implements Serializable {

    private int duration;
    private Float distance;
    private ArrayList<Position> positionList;

    public int getDuration() { return duration; }
    public Float getDistance() { return distance; }
    public ArrayList<Position> getPositionList() { return positionList; }

    public void setDuration(int duration) { this.duration = duration; }
    public void setDistance(Float distance) { this.distance = distance; }
    public void setPositionList(ArrayList<Position> positionList) { this.positionList = positionList; }

    public Itinerary(int duration, Float distance,ArrayList<Position> positionList) {
        setDuration(duration);
        setDistance(distance);
        setPositionList(positionList);
    }
}