package com.example.cyclosens.classes;

import java.util.ArrayList;

public class Legs {
    public Duration duration = new Duration();
    public String end_address = "";
    public String start_address = "";
    public Location end_location = new Location();
    public Location start_location = new Location();
    public ArrayList<Steps> steps = new ArrayList<Steps>();
}
