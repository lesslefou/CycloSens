package com.example.cyclosens.classes;
import java.io.Serializable;

public class Position implements Serializable {

    private Double lat;
    private Double lng;

    public Double getLat() { return lat; }
    public Double getLng() { return lng; }

    public void setLat(Double lat) { this.lat = lat; }
    public void setLng(Double lng) { this.lng = lng; }

    public Position(Double lat, Double lng) {
        setLat(lat);
        setLng(lng);
    }

    public Position(){}
}
